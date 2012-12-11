package org.csstudio.archive.reader.kblog;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.archive.vtype.ArchiveVStatistics;
import org.csstudio.archive.vtype.VTypeHelper;
import org.epics.pvmanager.data.AlarmSeverity;
import org.epics.pvmanager.data.VNumber;
import org.epics.pvmanager.data.VNumberArray;
import org.epics.pvmanager.data.VType;
import org.epics.pvmanager.data.ValueFactory;
import org.epics.util.time.TimeDuration;
import org.epics.util.time.Timestamp;

/**
 * Averaging sample iterator for KBLog.
 * 
 * This iterator reads values from "kblogrd" and calculate average/min/max values of each time step,
 * and return them as optimized values. Abnormal values ("connected", "disconnected", "N/A", etc.)
 * will be returned as original values.
 *
 * @author Takashi Nakamoto
 */
public class KBLogAveragedValueIterator implements KBLogValueIterator {
	private KBLogRawValueIterator base;
	private int stepSecond;
	private Timestamp currentTime;
	private Timestamp endTime;
	private VType nextBaseValue;
	private PriorityBlockingQueue<VType> processedValues;
	private boolean initialized;
	
	/**
	 * Constructor of KBLogValueIterator.
	 * 
	 * @param base Instance of KBLogValueIterator.
	 * @param startTime The beginning time of the time range.
	 * @param stepSecond Time step.
	 */
	public KBLogAveragedValueIterator(KBLogRawValueIterator base, Timestamp startTime, Timestamp endTime, int stepSecond) {
		this.base = base;
		this.currentTime = startTime;
		this.endTime = endTime;
		this.initialized = false;
		
		// make sure that the step second is equal to or greater than 1.
		if (stepSecond < 1)
			this.stepSecond = 1;
		else
			this.stepSecond = stepSecond;
		
		processedValues = new PriorityBlockingQueue<VType>(1, new ValueTimeComparator());
	}
	
	/**
	 * This method judges whether the given value represents normal value.
	 * Here, "normal value" is defined as a value which can be taken into account
	 * to calculate average value of a given time step. Thus, only IDoubleValue
	 * and ILongValue can be treated as normal values. Moreover, even if the given
	 * value is an instance of IDoubleValue, it can be treated as an abnormal value if 
	 * it represents NaN or infinity. Without condition, non-numeric values (e.g. array
	 * are considered as an abnormal value.
	 * 
	 * @param value value
	 * @return whether the given value represents normal value which can be processed while averaging. 
	 */
	private boolean isNormalValue(final VType value) {
	    if (! (value instanceof VNumber))
	        return false;
	    
	    final VNumber number = (VNumber) value;
	    final double dbl = number.getValue().doubleValue();
        if (Double.isNaN(dbl)  ||  Double.isInfinite(dbl))
	        return false;
        
        if (number.getAlarmSeverity() == AlarmSeverity.UNDEFINED)
            return false;
        
        return true;
	}
	
	/**
	 * This method judges whether the given value represents array or not.
	 * 
	 * @param value value
	 * @return whether the given value represents array or not
	 */
	private boolean isArray(final VType value) {
	    if (! (value instanceof VNumberArray))
	        return false;
	    final VNumberArray array = (VNumberArray) value;
	    return array.getData().size() >= 2;
	}

	/**
	 * Examine values in the current time step and push the averaged value, abnormal values
	 * to the processed value queue so that next() method can pop a value from the queue
	 * and return to any CSS application (e.g. DataBrowser). 
	 * 
	 * Note that only one array in each time step is added to the queue. It prevents DataBrowser
	 * and InspectSamples from showing too many arrays. Otherwise, CSS freezes when it tries to
	 * show all the arrays.
	 */
	private synchronized void examineCurrentTimeStep() {
		if (nextBaseValue == null)
			return;
		
		double avg = 0.0;
		double min = 0;
		double max = 0;
		long countOfNormalValue = 0;
		VType lastNormalValue = null;
		boolean addedArray = false;
		
		Timestamp nextTime = currentTime.plus(TimeDuration.ofSeconds(stepSecond));
		
		try{
			while (nextBaseValue != null) {
				Timestamp time = VTypeHelper.getTimestamp(nextBaseValue);
				
				if (time.compareTo(nextTime) >= 0) {
					// The obtained value is the data in the next step, which will be processed
					// when this method is called next time.
					break;
				}

				if (time.compareTo(currentTime) < 0) {
					// A value archived earlier than this time step is found.
					// Ignore this value and continue averaging.
					Logger.getLogger(Activator.ID).log(Level.WARNING,
							"The value transferred from " + base.getPathToKBLogRD() + " (" + base.getCommandID() + ") is not ordered in time.");

					if (!base.hasNext()) {
						nextBaseValue = base.next();
					} else {						
						nextBaseValue = null;
						break;
					}
					
					continue;
				}

				if (isNormalValue(nextBaseValue)) {
					final double val = VTypeHelper.toDouble(nextBaseValue);
				    if (Double.isNaN(val)) {
						// This part is not supposed to be reached.
						throw new Exception("This thread reached the part which must not be reached.");
					}
					
					if (countOfNormalValue == 0) {
						avg = val;
						max = val;
						min = val;
						countOfNormalValue = 1;
					} else {
						avg = (countOfNormalValue * avg + val) / (countOfNormalValue + 1.0);
						if (val > max)
							max = val;
						if (val < min)
							min = val;
						
						countOfNormalValue++;
					}
					lastNormalValue = nextBaseValue;
				} else {
					if (isArray(nextBaseValue)) {
						// If no array value in this time step is added to the queue,
						// add this array value to the queue.
						if (!addedArray) {
							processedValues.add(nextBaseValue);
							addedArray = true;
						}
					} else {
						// Add this abnormal value to the processed value queue.
						processedValues.add(nextBaseValue);
					}
				}
				
				if (base.hasNext())
					nextBaseValue = base.next();
				else {
					nextBaseValue = null;
					break;
				}
			}
		} catch (Exception ex) {
			Logger.getLogger(Activator.ID).log(Level.SEVERE,
					"Fatal error while calculating average values.", ex);				
			
			nextBaseValue = null;
			return;
		}
		
		if (countOfNormalValue == 1) {
			// Add the only original value in this time step to the processed value queue.
			processedValues.add(lastNormalValue);
		} else if (countOfNormalValue > 1) {
			// Middle time of this time step
			Timestamp midTime = currentTime.plus(TimeDuration.ofSeconds(stepSecond / 2.0));		
			
			// Add the averaged value in this time step to the processed value queue.
			VType averagedValue = new ArchiveVStatistics(midTime,
			        AlarmSeverity.NONE,
			        KBLogMessages.StatusNormal,
			        ValueFactory.displayNone(),
			        avg, min, max, 0.0, 1);
			processedValues.add(averagedValue);
		}
	}
	
	private synchronized void init() {
		try {
			if (base.hasNext())
				nextBaseValue = base.next();
			else
				nextBaseValue = null;
		} catch (Exception ex) {
			// If the base iterator returns no value, this iterator also returns no value. 
			nextBaseValue = null;
		}
		
		initialized = true;
	}
	
	/**
	 * Try to find values in the successive time steps until at least one value is found.
	 * nextBaseValue must be set before this method is called.
	 */
	private synchronized void findNextValues() {
		// If there is no processed value in the queue, examine successive time steps until
		// a valid value is found and pushed to the queue.
		while (processedValues.size() == 0) {
			if (nextBaseValue == null)
				return;	// no more value
			
			// Examine all values in the next time step.
			examineCurrentTimeStep();
		
			// Go to the next time step.
			currentTime = currentTime.plus(TimeDuration.ofSeconds(stepSecond));
			
			if (currentTime.compareTo(endTime) >= 0)
				break;
		}
	}

	@Override
	public synchronized boolean hasNext() {
		if (!initialized)
			init();

		// If there is a processed value in the queue, return true.
		if (processedValues.size() > 0)
			return true;

		// If there is no vale in the queue, try to find next values.
		findNextValues();
		
		// If at lest one value is found, this method returns true. Otherwise, false.
		return (processedValues.size() > 0);
	}

	@Override
	public synchronized VType next() throws Exception {
		if (!initialized)
			init();
		
		// If there is a processed value in the queue, return it.
		if (processedValues.size() > 0)
			return processedValues.poll();
		
		// If there is no value in the queue, try to find next values.
		findNextValues();
		
		// A processed value(s) is found as a result of examining successive time steps,
		// that value will be returned. Otherwise, this method returns null to indicate
		// that there's no more value in this iterator.
		if (processedValues.size() > 0)
			return processedValues.poll();
		else
			return null;
	}

	@Override
	public void close() {
		base.close();
	}
	
	@Override
	public boolean isClosed() {
		return base.isClosed();
	}
	
	/**
	 * A class to compare two values and judge which value has the earlier time stamp.
	 * 
	 * @author Takashi Nakamoto
	 */
	private class ValueTimeComparator implements Comparator<VType> {
		@Override
		public int compare(VType arg0, VType arg1) {
		    return VTypeHelper.getTimestamp(arg0).compareTo(VTypeHelper.getTimestamp(arg1));
		}
	}
}
