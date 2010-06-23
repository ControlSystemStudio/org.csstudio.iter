package org.csstudio.opibuilder.widgets.figures;


import org.csstudio.opibuilder.widgets.figureparts.RoundScale;
import org.csstudio.opibuilder.widgets.figureparts.RoundScaledRamp;
import org.csstudio.opibuilder.widgets.figureparts.RoundScaledRamp.Threshold;
import org.eclipse.swt.graphics.RGB;


/**
 * Abstract figure with a round ramp and a round scale. 
 * @author Xihui Chen
 *
 */
public class AbstractRoundRampedFigure extends AbstractMarkedWidgetFigure {	


	protected boolean gradient;
	protected RoundScaledRamp ramp;

	
	public AbstractRoundRampedFigure() {
		scale = new RoundScale();
		ramp = new RoundScaledRamp((RoundScale) scale);	
	}
	
	@Override
	public void setShowMarkers(boolean showMarkers) {		
		super.setShowMarkers(showMarkers);
		ramp.setVisible(showMarkers);	
	}
	
	@Override
	public void setLoloLevel(double loloLevel) {
		super.setLoloLevel(loloLevel);
		ramp.setThresholdValue(Threshold.LOLO, loloLevel);
	}
	
	@Override
	public void setLoLevel(double loLevel) {
		super.setLoLevel(loLevel);
		ramp.setThresholdValue(Threshold.LO, loLevel);
	}
	
	@Override
	public void setHiLevel(double hiLevel) {		
		super.setHiLevel(hiLevel);
		ramp.setThresholdValue(Threshold.HI, hiLevel);
	}
	
	@Override
	public void setHihiLevel(double hihiLevel) {
		super.setHihiLevel(hihiLevel);
		ramp.setThresholdValue(Threshold.HIHI, hihiLevel);
	}
	
	@Override
	public void setShowLolo(boolean showLolo) {
		super.setShowLolo(showLolo);	
		ramp.setThresholdVisibility(Threshold.LOLO, showLolo);
	}
	
	@Override
	public void setShowLo(boolean showLo) {
		super.setShowLo(showLo);		
		ramp.setThresholdVisibility(Threshold.LO, showLo);
	}
	
	@Override
	public void setShowHi(boolean showHi) {
		super.setShowHi(showHi);		
		ramp.setThresholdVisibility(Threshold.HI, showHi);
	}
	
	@Override
	public void setShowHihi(boolean showHihi) {
		super.setShowHihi(showHihi);		
		ramp.setThresholdVisibility(Threshold.HIHI, showHihi);
	}	
	
	@Override
	public void setLoloColor(RGB color) {
		super.setLoloColor(color);	
		ramp.setThresholdColor(Threshold.LOLO, color);
	}
	
	@Override
	public void setLoColor(RGB color) {
		super.setLoColor(color);	
		ramp.setThresholdColor(Threshold.LO, color);
	}
	
	@Override
	public void setHiColor(RGB color) {
		super.setHiColor(color);	
		ramp.setThresholdColor(Threshold.HI, color);
	}
	
	@Override
	public void setHihiColor(RGB color) {
		super.setHihiColor(color);	
		ramp.setThresholdColor(Threshold.HIHI, color);
	}

	/**
	 * @param gradient the gradient to set
	 */
	public void setGradient(boolean gradient) {
		this.gradient = gradient;
		ramp.setGradient(gradient);
	}
	
	@Override
	public void setRange(double min, double max) {
		super.setRange(min, max);
		ramp.setDirty(true);
	}
	
	@Override
	public void setLogScale(boolean logScale) {
		super.setLogScale(logScale);
		ramp.setDirty(true);
	}
}