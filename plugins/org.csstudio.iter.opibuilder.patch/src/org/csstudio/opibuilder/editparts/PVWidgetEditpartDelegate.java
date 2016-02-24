package org.csstudio.opibuilder.editparts;

import static org.diirt.datasource.formula.ExpressionLanguage.formula;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.java.thread.ExecutionService;
import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.IPVWidgetModel;
import org.csstudio.opibuilder.preferences.BeastPreferencesHelper;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.properties.PVValueProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.util.AlarmRepresentationScheme;
import org.csstudio.opibuilder.util.BOYPVFactory;
import org.csstudio.opibuilder.util.BeastAlarmInfo;
import org.csstudio.opibuilder.util.BeastAlarmSeverityLevel;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPITimer;
import org.csstudio.opibuilder.util.WidgetBlinker;
import org.csstudio.opibuilder.visualparts.BorderFactory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.simplepv.IPV;
import org.csstudio.simplepv.IPVListener;
import org.csstudio.simplepv.VTypeHelper;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.diirt.datasource.PV;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.PVReaderListener;
import org.diirt.datasource.expression.DesiredRateReadWriteExpression;
import org.diirt.util.time.TimeDuration;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VTable;
import org.diirt.vtype.VType;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.EditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class PVWidgetEditpartDelegate implements IPVWidgetEditpart {
//    private interface AlarmSeverity extends ISeverity{
//        public void copy(ISeverity severity);
//    }
    private final class WidgetPVListener extends IPVListener.Stub{
        private String pvPropID;
        private boolean isControlPV;

        public WidgetPVListener(String pvPropID) {
            this.pvPropID = pvPropID;
            isControlPV = pvPropID.equals(controlPVPropId);
        }

        @Override
        public void connectionChanged(IPV pv) {
            if(!pv.isConnected())
                lastWriteAccess = null;
        }

        @Override
        public void valueChanged(IPV pv) {

            final AbstractWidgetModel widgetModel = editpart.getWidgetModel();

            //write access
//            if(isControlPV)
//                updateWritable(widgetModel, pv);

            if (pv.getValue() != null) {
                if (ignoreOldPVValue) {
                    widgetModel.getPVMap()
                            .get(widgetModel.getProperty(pvPropID))
                            .setPropertyValue_IgnoreOldValue(pv.getValue());
                } else
                    widgetModel.getPVMap()
                            .get(widgetModel.getProperty(pvPropID))
                            .setPropertyValue(pv.getValue());
            }

        }

        @Override
        public void writePermissionChanged(IPV pv) {
            if(isControlPV)
                updateWritable(editpart.getWidgetModel(), pvMap.get(pvPropID));
        }
    }
    //invisible border for no_alarm state, this can prevent the widget from resizing
    //when alarm turn back to no_alarm state/
    private static final AbstractBorder BORDER_NO_ALARM = new AbstractBorder() {

        public Insets getInsets(IFigure figure) {
            return new Insets(2);
        }

        public void paint(IFigure figure, Graphics graphics, Insets insets) {
        }
    };

    private int updateSuppressTime = 1000;
    private String controlPVPropId = null;

    private String controlPVValuePropId = null;
    /**
     * In most cases, old pv value in the valueChange() method of {@link IWidgetPropertyChangeHandler}
     * is not useful. Ignore the old pv value will help to reduce memory usage.
     */
    private boolean ignoreOldPVValue =true;

    private boolean isBackColorAlarmSensitive;
    private boolean isBorderAlarmSensitive;
    private boolean isForeColorAlarmSensitive;
    private AlarmSeverity alarmSeverity = AlarmSeverity.NONE;

    private Map<String, IPVListener> pvListenerMap = new HashMap<String, IPVListener>();

    private Map<String, IPV> pvMap = new HashMap<String, IPV>();
    private PropertyChangeListener[] pvValueListeners;
    private AbstractBaseEditPart editpart;
    private volatile AtomicBoolean lastWriteAccess;
    private Cursor savedCursor;

    private Color saveForeColor, saveBackColor;
    //the task which will be executed when the updateSuppressTimer due.
    protected Runnable timerTask;

    //The update from PV will be suppressed for a brief time when writing was performed
    protected OPITimer updateSuppressTimer;
    private IPVWidgetModel widgetModel;
    private boolean isAllValuesBuffered;

    private ListenerList setPVValueListeners;
    private ListenerList alarmSeverityListeners;
    private boolean isAlarmPulsing = false;
    private ScheduledFuture<?> scheduledFuture;

    private boolean pvsHaveBeenStarted = false;

    /**
     * @param editpart the editpart to be delegated.
     * It must implemented {@link IPVWidgetEditpart}
     */
    public PVWidgetEditpartDelegate(AbstractBaseEditPart editpart) {
        this.editpart = editpart;
    }

    public IPVWidgetModel getWidgetModel() {
        if(widgetModel == null)
            widgetModel = (IPVWidgetModel) editpart.getWidgetModel();
        return widgetModel;
    }

    public void doActivate(){
        saveFigureOKStatus(editpart.getFigure());
        if(editpart.getExecutionMode() == ExecutionMode.RUN_MODE){
                pvMap.clear();
                final Map<StringProperty, PVValueProperty> pvPropertyMap = editpart.getWidgetModel().getPVMap();

                for(final StringProperty sp : pvPropertyMap.keySet()){

                    if(sp.getPropertyValue() == null ||
                            ((String)sp.getPropertyValue()).trim().length() <=0)
                        continue;

	                /* BeastDataSource channels should not be configured as PVs.
	                 * If a Beast channel is set for PVName, it can only provide Alarm Sensitivity functionality, not values etc.
	                 * This is to prevent Alarm Tree Node BeastDS channels to be registered as PVs, because they will not be
	                 * found and the widget will (incorrectly) have the Disconnected state.
	                 *
	                 * To this end, we will ensure any PVs starting with "beast://" are not added to the pvMap.
	                 */
                    if (((String)sp.getPropertyValue()).toLowerCase().startsWith(BEAST_SCHEMA))
                        continue;

                    try {
                        IPV pv = BOYPVFactory.createPV((String) sp.getPropertyValue(),
                                isAllValuesBuffered);
                        pvMap.put(sp.getPropertyID(), pv);
                        editpart.addToConnectionHandler((String) sp.getPropertyValue(), pv);
                        WidgetPVListener pvListener = new WidgetPVListener(sp.getPropertyID());
                        pv.addListener(pvListener);
                        pvListenerMap.put(sp.getPropertyID(), pvListener);
                    } catch (Exception e) {
                        OPIBuilderPlugin.getLogger().log(Level.WARNING,
                                "Unable to connect to PV:" + (String)sp.getPropertyValue(), e); //$NON-NLS-1$
                    }
                }
            }
    }

    /**Start all PVs.
     * This should be called as the last step in editpart.activate().
     */
    public void startPVs() {
        pvsHaveBeenStarted = true;
        //the pv should be started at the last minute
        for(String pvPropId : pvMap.keySet()){
            IPV pv = pvMap.get(pvPropId);
            try {
                pv.start();
            } catch (Exception e) {
                OPIBuilderPlugin.getLogger().log(Level.WARNING,
                        "Unable to connect to PV:" + pv.getName(), e); //$NON-NLS-1$
            }
        }

        if (opiBeastAlarmsEnabled && editpart.getExecutionMode() == ExecutionMode.RUN_MODE) {
            createBeastAlarmListener(null);
        }
    }

    public void doDeActivate() {
        // disconnect the AlarmPV beast listener and attempt removal from widget blinking list
        if (alarmPV != null) {
            alarmPV.close();
            alarmPV = null;
            WidgetBlinker.INSTANCE.remove(this);
            beastInfo.reset();
        }

        if (pvsHaveBeenStarted) {
            for(IPV pv : pvMap.values())
                pv.stop();
            pvsHaveBeenStarted = false;
        }
            for(String pvPropID : pvListenerMap.keySet()){
                pvMap.get(pvPropID).removeListener(pvListenerMap.get(pvPropID));
            }

            pvMap.clear();
            pvListenerMap.clear();
            stopPulsing();
    }

    public IPV getControlPV(){
        if(controlPVPropId != null)
            return pvMap.get(controlPVPropId);
        return null;
    }


    /**Get the PV corresponding to the <code>PV Name</code> property.
     * It is same as calling <code>getPV("pv_name")</code>.
     * @return the PV corresponding to the <code>PV Name</code> property.
     * null if PV Name is not configured for this widget.
     */
    public IPV getPV(){
        return pvMap.get(IPVWidgetModel.PROP_PVNAME);
    }

    /**Get the pv by PV property id.
     * @param pvPropId the PV property id.
     * @return the corresponding pv for the pvPropId. null if the pv doesn't exist.
     */
    public IPV getPV(String pvPropId){
        return pvMap.get(pvPropId);
    }

    /**Get value from one of the attached PVs.
     * @param pvPropId the property id of the PV. It is "pv_name" for the main PV.
     * @return the {@link IValue} of the PV.
     */
    public VType getPVValue(String pvPropId){
        final IPV pv = pvMap.get(pvPropId);
        if(pv != null){
            return pv.getValue();
        }
        return null;
    }

    /**
     * @return the time needed to suppress reading back from PV after writing.
     * No need to suppress if returned value <=0
     */
    public int getUpdateSuppressTime(){
        return updateSuppressTime;
    }

    /**Set the time needed to suppress reading back from PV after writing.
     * No need to suppress if returned value <=0
     * @param updateSuppressTime
     */
    public void setUpdateSuppressTime(int updateSuppressTime) {
        this.updateSuppressTime = updateSuppressTime;
    }

    public void initFigure(IFigure figure){
        //initialize frequent used variables
        isBorderAlarmSensitive = getWidgetModel().isBorderAlarmSensitve();
        isBackColorAlarmSensitive = getWidgetModel().isBackColorAlarmSensitve();
        isForeColorAlarmSensitive = getWidgetModel().isForeColorAlarmSensitve();
        isAlarmPulsing = getWidgetModel().isAlarmPulsing();

        if(isBorderAlarmSensitive
                && editpart.getWidgetModel().getBorderStyle()== BorderStyle.NONE){
            editpart.setFigureBorder(BORDER_NO_ALARM);
        }
    }

    /**
     * Initialize the updateSuppressTimer
     */
    private synchronized void initUpdateSuppressTimer() {
        if(updateSuppressTimer == null)
            updateSuppressTimer = new OPITimer();
        if(timerTask == null)
            timerTask = new Runnable() {
                public void run() {
                    AbstractWidgetProperty pvValueProperty =
                            editpart.getWidgetModel().getProperty(controlPVValuePropId);
                    //recover update
                    if(pvValueListeners != null){
                        for(PropertyChangeListener listener: pvValueListeners){
                            pvValueProperty.addPropertyChangeListener(listener);
                        }
                    }
                    //forcefully set PV_Value property again
                    pvValueProperty.setPropertyValue(
                            pvValueProperty.getPropertyValue(), true);
                }
            };
    }

    /**For PV Control widgets, mark this PV as control PV.
     * @param pvPropId the propId of the PV.
     */
    public void markAsControlPV(String pvPropId, String pvValuePropId){
        controlPVPropId  = pvPropId;
        controlPVValuePropId = pvValuePropId;
        initUpdateSuppressTimer();
    }

    public boolean isPVControlWidget(){
        return controlPVPropId!=null;
    }

    public void registerBasePropertyChangeHandlers() {
        IWidgetPropertyChangeHandler borderHandler = new IWidgetPropertyChangeHandler(){
            public boolean handleChange(Object oldValue, Object newValue,
                    IFigure figure) {
                editpart.setFigureBorder(editpart.calculateBorder());
                return true;
            }
        };

        editpart.setPropertyChangeHandler(IPVWidgetModel.PROP_BORDER_ALARMSENSITIVE, borderHandler);


        // value
        IWidgetPropertyChangeHandler valueHandler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure figure) {
                // No valid value is given. Do nothing.
                if (newValue == null || !(newValue instanceof VType))
                    return false;

                AlarmSeverity newSeverity = VTypeHelper.getAlarmSeverity((VType) newValue);
                if(newSeverity == null)
                    return false;

                if (newSeverity != alarmSeverity) {
                    alarmSeverity = newSeverity;
                    fireAlarmSeverityChanged(newSeverity, figure);
                }
                return true;
            }
        };
        editpart.setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, valueHandler);

        // Border Alarm Sensitive
        addAlarmSeverityListener(new AlarmSeverityListener() {
            @Override
            public boolean severityChanged(AlarmSeverity severity, IFigure figure) {
                if (!isBorderAlarmSensitive)
                    return false;

                editpart.setFigureBorder(editpart.calculateBorder());
                return true;
            }
        });

        // BackColor Alarm Sensitive
        addAlarmSeverityListener(new AlarmSeverityListener() {
            @Override
            public boolean severityChanged(AlarmSeverity severity, IFigure figure) {
                if (!isBackColorAlarmSensitive)
                    return false;
                figure.setBackgroundColor(calculateBackColor());
                return true;
            }
        });

        // ForeColor Alarm Sensitive
        addAlarmSeverityListener(new AlarmSeverityListener() {
            @Override
            public boolean severityChanged(AlarmSeverity severity, IFigure figure) {
                if (!isForeColorAlarmSensitive)
                    return false;
                figure.setForegroundColor(calculateForeColor());
                return true;
            }
        });

        // Pulsing Alarm Sensitive
        addAlarmSeverityListener(new AlarmSeverityListener() {
            @Override
            public boolean severityChanged(AlarmSeverity severity, IFigure figure) {
                if (!isAlarmPulsing)
                    return false;
                if (severity == AlarmSeverity.MAJOR || severity == AlarmSeverity.MINOR) {
                    startPulsing();
                } else {
                    stopPulsing();
                }
                return true;
            }
        });

        class PVNamePropertyChangeHandler implements IWidgetPropertyChangeHandler{
            private String pvNamePropID;
            public PVNamePropertyChangeHandler(String pvNamePropID) {
                this.pvNamePropID = pvNamePropID;
            }
            public boolean handleChange(Object oldValue, Object newValue,
                    IFigure figure) {
                IPV oldPV = pvMap.get(pvNamePropID);
                editpart.removeFromConnectionHandler((String)oldValue);
                if(oldPV != null){
                    oldPV.stop();
                    oldPV.removeListener(pvListenerMap.get(pvNamePropID));
                }
                pvMap.remove(pvNamePropID);
                String newPVName = ((String)newValue).trim();
                if(newPVName.length() <= 0)
                    return false;

                /* Ensure any PVs starting with "beast://" are not added to the pvMap.
                 * See comment in doActivate().
                 */
                if (newPVName.toLowerCase().startsWith(BEAST_SCHEMA))
                    return false;

                try {
                    lastWriteAccess = null;
                    IPV newPV = BOYPVFactory.createPV(newPVName, isAllValuesBuffered);
                    WidgetPVListener pvListener = new WidgetPVListener(pvNamePropID);
                    newPV.addListener(pvListener);
                    pvMap.put(pvNamePropID, newPV);
                    editpart.addToConnectionHandler(newPVName, newPV);
                    pvListenerMap.put(pvNamePropID, pvListener);

                    newPV.start();
                }
                catch (Exception e) {
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, "Unable to connect to PV:" + //$NON-NLS-1$
                            newPVName, e);
                }

                return false;
            }
        }
        //PV name
        for(StringProperty pvNameProperty : editpart.getWidgetModel().getPVMap().keySet()){
            if(editpart.getExecutionMode() == ExecutionMode.RUN_MODE)
                editpart.setPropertyChangeHandler(pvNameProperty.getPropertyID(),
                    new PVNamePropertyChangeHandler(pvNameProperty.getPropertyID()));
        }

        if(editpart.getExecutionMode() == ExecutionMode.RUN_MODE)
            editpart.setPropertyChangeHandler(IPVWidgetModel.PROP_PVNAME, new BeastListenerPVNameChangeHandler());


        if(editpart.getExecutionMode() ==  ExecutionMode.EDIT_MODE)
            editpart.getWidgetModel().getProperty(IPVWidgetModel.PROP_PVNAME).addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    //reselect the widget to update feedback.
                    int selected = editpart.getSelected();
                    if(selected != EditPart.SELECTED_NONE){
                        editpart.setSelected(EditPart.SELECTED_NONE);
                        editpart.setSelected(selected);
                    }
                }
            });

        IWidgetPropertyChangeHandler backColorHandler = new IWidgetPropertyChangeHandler(){
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                saveBackColor = ((OPIColor)newValue).getSWTColor();
                return false;
            }
        };
        editpart.setPropertyChangeHandler(AbstractWidgetModel.PROP_COLOR_BACKGROUND, backColorHandler);

        IWidgetPropertyChangeHandler foreColorHandler = new IWidgetPropertyChangeHandler(){
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                saveForeColor = ((OPIColor)newValue).getSWTColor();
                return false;
            }
        };
        editpart.setPropertyChangeHandler(AbstractWidgetModel.PROP_COLOR_FOREGROUND, foreColorHandler);

        IWidgetPropertyChangeHandler backColorAlarmSensitiveHandler = new IWidgetPropertyChangeHandler() {

            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                isBackColorAlarmSensitive = (Boolean)newValue;
                figure.setBackgroundColor(calculateBackColor());
                return true;
            }
        };
        editpart.setPropertyChangeHandler(AbstractPVWidgetModel.PROP_BACKCOLOR_ALARMSENSITIVE, backColorAlarmSensitiveHandler);

        IWidgetPropertyChangeHandler foreColorAlarmSensitiveHandler = new IWidgetPropertyChangeHandler() {

            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                isForeColorAlarmSensitive = (Boolean)newValue;
                figure.setForegroundColor(calculateForeColor());
                return true;
            }
        };

        editpart.setPropertyChangeHandler(AbstractPVWidgetModel.PROP_FORECOLOR_ALARMSENSITIVE, foreColorAlarmSensitiveHandler);

        IWidgetPropertyChangeHandler alarmPulsingHandler = new IWidgetPropertyChangeHandler() {

            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                isAlarmPulsing = (Boolean)newValue;
                stopPulsing();
                fireAlarmSeverityChanged(alarmSeverity, figure);
                return true;
            }
        };

        editpart.setPropertyChangeHandler(AbstractPVWidgetModel.PROP_ALARM_PULSING, alarmPulsingHandler);


    }

    public synchronized void stopPulsing() {
        if (scheduledFuture != null) {
            // stop the pulsing runnable
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    public synchronized void startPulsing() {
        stopPulsing();
        Runnable pulsingTask = new Runnable() {
            public void run() {
                UIBundlingThread.getInstance().addRunnable(new Runnable() {

                    public void run() {
                        synchronized (PVWidgetEditpartDelegate.this) {
                            // Change the colours of all alarm sensitive components
                            if (isBackColorAlarmSensitive)
                                editpart.getFigure().setBackgroundColor(calculateBackColor());
                            if (isForeColorAlarmSensitive)
                                editpart.getFigure().setForegroundColor(calculateForeColor());
                        }
                    }
                });
            }
        };
        scheduledFuture = ExecutionService
                .getInstance()
                .getScheduledExecutorService()
                .scheduleAtFixedRate(pulsingTask, PreferencesHelper.getGUIRefreshCycle(), PreferencesHelper.getGUIRefreshCycle(),
                        TimeUnit.MILLISECONDS);
    }

    private void saveFigureOKStatus(IFigure figure) {
        saveForeColor = figure.getForegroundColor();
        saveBackColor = figure.getBackgroundColor();
    }

    /**
     * Start the updateSuppressTimer. All property change listeners of PV_Value property will
     * temporarily removed until timer is due.
     */
    protected synchronized void startUpdateSuppressTimer(){
        AbstractWidgetProperty pvValueProperty =
            editpart.getWidgetModel().getProperty(controlPVValuePropId);
        pvValueListeners = pvValueProperty.getAllPropertyChangeListeners();
        pvValueProperty.removeAllPropertyChangeListeners();
        updateSuppressTimer.start(timerTask, getUpdateSuppressTime());
    }

    protected Border calculateBorder(AlarmSeverity borderSeverity) {
        isBorderAlarmSensitive = getWidgetModel().isBorderAlarmSensitve();
        if(!isBorderAlarmSensitive)
            return null;

        Border alarmBorder;

        switch (borderSeverity) {
        case NONE:
            if(editpart.getWidgetModel().getBorderStyle() == BorderStyle.NONE)
                alarmBorder = BORDER_NO_ALARM;
            else
                alarmBorder = BorderFactory.createBorder(
                        editpart.getWidgetModel().getBorderStyle(),
                        editpart.getWidgetModel().getBorderWidth(),
                        editpart.getWidgetModel().getBorderColor(),
                        editpart.getWidgetModel().getName());
            break;
        case MAJOR:
            alarmBorder = AlarmRepresentationScheme.getMajorBorder(editpart.getWidgetModel().getBorderStyle());
            break;
        case MINOR:
            alarmBorder = AlarmRepresentationScheme.getMinorBorder(editpart.getWidgetModel().getBorderStyle());
            break;
        case INVALID:
        case UNDEFINED:
        default:
            alarmBorder = AlarmRepresentationScheme.getInvalidBorder(editpart.getWidgetModel().getBorderStyle());
            break;
        }

        return alarmBorder;
    }

    public Border calculateBorder() {
        if (!getWidgetModel().isBorderAlarmSensitve() || !isBeastAlarmAndConnected() || !isBeastAlarmActiveUnack())
            return calculateBorder(alarmSeverity);

        // this widget should Blink
        AlarmSeverity borderSeverity = alarmSeverity;
        synchronized (beastInfo) {
            // implied isBeastAlarmAndConnected() == true, isBeastAlarmActiveUnack() == true
            if (beastInfo.getBeastAlertBlinkState() == 0) {
                /* use default border for 'blink state 0';
                 * otherwise (no blinking or blink state 1) the current severity's border will be used
                 */
                borderSeverity = AlarmSeverity.NONE;
            }
        }
        return calculateBorder(borderSeverity);
    }

    public Color calculateBackColor() {
        return calculateAlarmColor(isBackColorAlarmSensitive, saveBackColor);
    }

    public Color calculateForeColor() {
        return calculateAlarmColor(isForeColorAlarmSensitive, saveForeColor);
    }

    public Color calculateAlarmColor(boolean isSensitive, Color saveColor) {
        if (!isSensitive) {
            return saveColor;
        }
        synchronized(beastInfo) {
            // if this widget should blink and the current blink color is the default one, return it early
            if (isBeastAlarmAndConnected() && isBeastAlarmActiveUnack() && beastInfo.getBeastAlertBlinkState() == 0)
                return saveColor;
            // otherwise normally calculate the severity color and return it
        }

        RGB alarmColor = AlarmRepresentationScheme.getAlarmColor(alarmSeverity);

        if (alarmColor != null) {
            // Alarm severity is either "Major", "Minor" or "Invalid".
            if (isAlarmPulsing &&
                    (alarmSeverity == AlarmSeverity.MINOR || alarmSeverity == AlarmSeverity.MAJOR)) {
                double alpha = 0.3;
                int period;
                if (alarmSeverity == AlarmSeverity.MINOR) {
                    period = PreferencesHelper.getPulsingAlarmMinorPeriod();
                } else {
                    period = PreferencesHelper.getPulsingAlarmMajorPeriod();
                }
                alpha += Math.abs(System.currentTimeMillis() % period - period / 2) / (double) period;
                alarmColor = new RGB(
                        (int) (saveColor.getRed() * alpha + alarmColor.red * (1-alpha)),
                        (int) (saveColor.getGreen() * alpha + alarmColor.green * (1-alpha)),
                        (int) (saveColor.getBlue() * alpha + alarmColor.blue * (1-alpha)));
            }
            return CustomMediaFactory.getInstance().getColor(alarmColor);
        } else {
            // Alarm severity is "OK".
            return saveColor;
        }
    }

    /**Set PV to given value. Should accept Double, Double[], Integer, String, maybe more.
     * @param pvPropId
     * @param value
     */
    public void setPVValue(String pvPropId, Object value){
        fireSetPVValue(pvPropId, value);
        final IPV pv = pvMap.get(pvPropId);
        if(pv != null){
            try {
                if(pvPropId.equals(controlPVPropId) && controlPVValuePropId != null && getUpdateSuppressTime() >0){ //activate suppress timer
                    synchronized (this) {
                        if(updateSuppressTimer == null || timerTask == null)
                            initUpdateSuppressTimer();
                        if(!updateSuppressTimer.isDue())
                            updateSuppressTimer.reset();
                        else
                            startUpdateSuppressTimer();
                    }

                }
                pv.setValue(value);
            } catch (final Exception e) {
                UIBundlingThread.getInstance().addRunnable(new Runnable(){
                    public void run() {
                        String message =
                            "Failed to write PV:" + pv.getName();
                        ErrorHandlerUtil.handleError(message, e);
                    }
                });
            }
        }
    }

    public void setIgnoreOldPVValue(boolean ignoreOldValue) {
        this.ignoreOldPVValue = ignoreOldValue;
    }


    @Override
    public String[] getAllPVNames() {
        if(editpart.getWidgetModel().getPVMap().isEmpty())
             return new String[]{""}; //$NON-NLS-1$
        Set<String> result = new HashSet<String>();

        for(StringProperty sp : editpart.getWidgetModel().getPVMap().keySet()){
            if(sp.isVisibleInPropSheet() && !((String)sp.getPropertyValue()).trim().isEmpty())
                result.add((String) sp.getPropertyValue());
        }
        return result.toArray(new String[result.size()]);
    }


    @Override
    public String getPVName() {
        if(getPV() != null)
            return getPV().getName();
        return getWidgetModel().getPVName();
    }

    @Override
    public void addSetPVValueListener(ISetPVValueListener listener) {
        if(setPVValueListeners == null){
            setPVValueListeners = new ListenerList();
        }
        setPVValueListeners.add(listener);
    }

    protected void fireSetPVValue(String pvPropId, Object value){
        if(setPVValueListeners == null)
            return;
        for(Object listener: setPVValueListeners.getListeners()){
            ((ISetPVValueListener)listener).beforeSetPVValue(pvPropId, value);
        }
    }

    public boolean isAllValuesBuffered() {
        return isAllValuesBuffered;
    }

    public void setAllValuesBuffered(boolean isAllValuesBuffered) {
        this.isAllValuesBuffered = isAllValuesBuffered;
    }

    private void updateWritable(final AbstractWidgetModel widgetModel, IPV pv) {
        if(lastWriteAccess == null || lastWriteAccess.get() != pv.isWriteAllowed()){
            if(lastWriteAccess == null)
                lastWriteAccess= new AtomicBoolean();
            lastWriteAccess.set(pv.isWriteAllowed());
            if(lastWriteAccess.get()){
                UIBundlingThread.getInstance().addRunnable(
                        editpart.getViewer().getControl().getDisplay(),new Runnable(){
                    public void run() {
                        setControlEnabled(true);
                    }
                });
            } else {
                UIBundlingThread.getInstance().addRunnable(
                        editpart.getViewer().getControl().getDisplay(),new Runnable(){
                    public void run() {
                        setControlEnabled(false);
                    }
                });
            }
        }
    }

    /**
     * Set whether the editpart is enabled for PV control.  Disabled
     * editparts have greyed-out figures, and the cursor is set to a cross.
     */
    @Override
    public void setControlEnabled(boolean enabled) {
        if (enabled) {
            IFigure figure = editpart.getFigure();
            if(figure.getCursor() == Cursors.NO)
                figure.setCursor(savedCursor);
            figure.setEnabled(editpart.getWidgetModel().isEnabled());
            figure.repaint();
        } else {
            IFigure figure = editpart.getFigure();
            if(figure.getCursor() != Cursors.NO)
                savedCursor = figure.getCursor();
            figure.setEnabled(false);
            figure.setCursor(Cursors.NO);
            figure.repaint();
        }
    }

    public void addAlarmSeverityListener(AlarmSeverityListener listener) {
        if(alarmSeverityListeners == null){
            alarmSeverityListeners = new ListenerList();
        }
        alarmSeverityListeners.add(listener);
    }

    private void fireAlarmSeverityChanged(AlarmSeverity severity, IFigure figure) {
        if(alarmSeverityListeners == null)
            return;
        for(Object listener: alarmSeverityListeners.getListeners()){
            ((AlarmSeverityListener)listener).severityChanged(severity, figure);
        }
    }

    /* ******************************************************************************************
     *
     * THE FOLLOWING ARE ADDITIONAL FIELDS AND METHODS WHICH DO NOT EXIST IN THE ORIGINAL CLASS
     *
     ********************************************************************************************/

    /* PVName change handler for BeastAlarmListener */
    private class BeastListenerPVNameChangeHandler implements IWidgetPropertyChangeHandler{
        @Override
        public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
            // recreate the BeastAlarmListener (PVName changed)
            String newPVName = ((String)newValue).trim();
            createBeastAlarmListener(newPVName);

            return false;
        }
    }

    private static final Logger log = Logger.getLogger(PVWidgetEditpartDelegate.class.getName());
    private static final String BEAST_SCHEMA = "beast://";
    // (secondary) PV on which we listen for BEAST events
    private final boolean opiBeastAlarmsEnabled = BeastPreferencesHelper.isOpiBeastAlarmsEnabled();
    private PV<?, Object> alarmPV;
    private boolean isBeastAlarm = false;
    private boolean isBeastAlarmNode = false; // is this an Alarm Node instead of a PV ?
    private final BeastAlarmInfo beastInfo = new BeastAlarmInfo();

    /**
     * Returns whether BEAST Alarm functionality is enabled and available (listener connected to the BeastDataSource).
     *
     * @return {@code true} when Beast enabled and listener connected, {@code false} otherwise
     */
    public boolean isBeastAlarmAndConnected() {
        synchronized (beastInfo) {
            return isBeastAlarm && beastInfo.isBeastChannelConnected();
        }
    }

    /**
     * Returns whether BEAST functionality is enabled, available and can be acted on ({@link #isBeastAlarmAndConnected} would return
     * {@code true} and Beast Latched Severity is not OK).
     *
     * @return {@code true} when BEAST is enabled & connected and the alarm can be acted upon, {@code false} otherwise
     */
    public boolean isBeastAlarmAndActionable() {
        synchronized (beastInfo) {
            return isBeastAlarm && beastInfo.isBeastChannelConnected() && beastInfo.getLatchedSeverity() != BeastAlarmSeverityLevel.OK;
        }
    }

    /**
     * Returns whether the Beast Alarm is active and unacknowledged.
     *
     * @return {@code true} if Beast Alarm is Active and UnAcknowledged (alarm-sensitive properties should blink), {@code false} otherwise.
     */
    public boolean isBeastAlarmActiveUnack() {
        synchronized (beastInfo) {
            return beastInfo.isCurrentAlarmActive() && !beastInfo.isAcknowledged();
        }
    }

    /**
     * Beast Alarm information for this widget's PV.
     *
     * @return Beast Alarm Info for this widget.
     */
    public BeastAlarmInfo getBeastAlarmInfo() {
        return beastInfo;
    }

    /**
     * Whether this widget's PVName links to a Beast Alarm Tree node or leaf.
     *
     * @return {@code true} if this widget's PVName is actually a BEAST Alarm node {@code false} if it's a PV (or not a BeastAlarm - also
     *         check {@link #isBeastAlarmAndConnected})
     */
    public boolean isBeastAlarmNode() {
        return isBeastAlarmNode;
    }

    /**
     * Acknowledges or UnAcknowledges (when possible) the Beast Alarm.
     *
     * @return {@code true} if the ACK or UNACK command was sent to the BeastDataSource PV, {@code false} if no attempt was made (not a
     *         Beast alarm PV, Beast channel not connected, Beast PV not in alarm).
     */
    public boolean toggleAlarmAcknowledgement() {
        synchronized (beastInfo) {
            if (!beastInfo.isBeastChannelConnected() || beastInfo.isLatchedAlarmOK() || alarmPV == null)
                return false;

            if (!beastInfo.isAcknowledged())
                alarmPV.write("ack");
            else
                alarmPV.write("unack");
        }

        return true;
    }

    /**
     * Triggers a color change on this widget's alarm-sensitive properties to make them blink (flash). Blinking is achieved by alternating
     * between the current severity and AlarmSeverity.NONE colors. Triggers {@link #fireAlarmSeverityChanged} to cause alarm-sensitive
     * properties to update their state.
     *
     * @param blinkState
     *            Next color that should be used. A value of 0 indicates the default (no alarm) color; 1 indicates that the current
     *            severity's color should be used.
     */
    public void performBeastBlink(final int blinkState) {
        if (!editpart.isActive())
            return;

        beastInfo.setBeastAlertBlinkState(blinkState);
        IFigure figure = editpart.getFigure();
        // all Alarm-sensitive properties have an AlarmSeverityListener;
        // fire this to ensure all props which are sensitive will be updated
        fireAlarmSeverityChanged(alarmSeverity, figure);
    }

    /**
     * Resets {@code beastInfo.beastAlertBlinkState} to 0 and triggers an update of alarm-sensitive properties, causing them to be reset to
     * their non-blinking state (color based on current severity).
     *
     * @param fireEvent If {@code true} {@link #fireAlarmSeverityChanged} will be called
     */
    public void resetBeastBlink(boolean fireEvent) {
        beastInfo.setBeastAlertBlinkState(0);
        if (!editpart.isActive() || !fireEvent)
            return;

        IFigure figure = editpart.getFigure();
        // all Alarm-sensitive properties have an AlarmSeverityListener;
        // fire this to ensure all props which are sensitive will be updated
        fireAlarmSeverityChanged(alarmSeverity, figure);
    }

    private String getBeastAlarmChannelName(String overridePvName) {
        String pvName = overridePvName;
        if (pvName == null || pvName.trim().isEmpty())
            pvName = getPVName();

        if (pvName == null || pvName.trim().isEmpty())
            return "";
        pvName = pvName.trim();
        if (pvName.indexOf("://") < 0) {
            return BEAST_SCHEMA + pvName;
        } else {
            return BEAST_SCHEMA + pvName.substring(pvName.indexOf("://") + 3);
        }
    }

    /**
     * If we have a reference to the parent EditPart ({@link #editpart}), return its Figure.
     *
     * @return {@code null} if {@code editpart} is null, {@code editpart.getFigure()} otherwise
     */
    IFigure getEditpartFigure() {
        return editpart != null ? editpart.getFigure() : null;
    }

    /**
     * If we have a reference to the parent EditPart, return its SWT Control's Display.
     *
     * @return {@code null} if {@code editpart} is null, {@code editpart.getViewer().getControl().getDisplay()} otherwise
     */
    public Display getEditpartDisplay() {
        return editpart.getViewer().getControl().getDisplay();
    }

    /**
     * Set whether this PVWidgetEditpartDelegate's PV is also connected to a BEAST Alarm
     * @param isBeast New value to be set for isBeastAlarm
     */
    public void setIsBeastAlarm(boolean isBeast) {
        isBeastAlarm = isBeast;
    }

    /**
     * Set this PVWidgetEditpartDelegate's AlarmSeverity.
     * @param newSeverity AlarmSeverity to be set
     */
    public void setAlarmSeverity(AlarmSeverity newSeverity) {
        alarmSeverity = newSeverity;
    }

    /**
     * Get the current AlarmSeverity.
     * @return the current AlarmSeverity
     */
    public AlarmSeverity getAlarmSeverity() {
        return alarmSeverity;
    }
    
    public void processBeastAlarmState() {
        boolean blinking = WidgetBlinker.INSTANCE.isBlinking(this);

        // for calling blinker.add() & fireAlarmSeverityChanged (from within resetBeastBlink)  on the UI thread,
        // otherwise SWT will error out
        Display display = getEditpartDisplay();
        if (display.isDisposed()) return;

        // The widget will Blink only when the PV is currently in alarm and has not yet been acknowledged
        if (isBeastAlarmAndConnected() && isBeastAlarmActiveUnack()) {
            if (!blinking) {
                // add() must be run on the display thread because it starts the blinking when the first widget is added
                display.syncExec(() -> WidgetBlinker.INSTANCE.add(this));
            }
        } else if (blinking) {
            // this branch also stops this widget's blinking in case of disconnection from the Alarm Server
            // (isBeastAlarmAndConnected() will be false after such a ConnectionChanged event is received)
            WidgetBlinker.INSTANCE.remove(this);
            display.asyncExec(() -> resetBeastBlink(true));
        }
    }

    private void createBeastAlarmListener(String overridePvName) {
        if (alarmPV != null)
            alarmPV.close();

        String alarmPVName = getBeastAlarmChannelName(overridePvName);
        if (alarmPVName.isEmpty()) {
            alarmPV = null;
            isBeastAlarm = false;
            isBeastAlarmNode = false;
            beastInfo.setBeastChannelConnected(false);
            return;
        }

        log.fine("Starting BeastAlarmListener for channel " + alarmPVName);
        beastInfo.setBeastChannelName(alarmPVName);
        // pre-agreed to have PVName start with the channel protocol ("beast://") if
        // it is for an Alarm Tree node instead of actual PV
        isBeastAlarmNode = getPVName().toLowerCase().startsWith(BEAST_SCHEMA);
        PVWidgetEditpartDelegate pvWidget = this;

        DesiredRateReadWriteExpression<?, Object> expr = formula(alarmPVName);

        try {
            alarmPV = PVManager
                    .readAndWrite(expr)
                    .timeout(TimeDuration.ofMillis(10000))
                    .readListener(new PVReaderListener<Object>() {
                        private boolean isFirstValueEvent = true;
                        private int latchedSeverityIdx = -1, currentSeverityIdx = -1;

                        @SuppressWarnings("unchecked")
                        @Override
                        public void pvChanged(PVReaderEvent<Object> event) {
                            String pvName = pvWidget.getWidgetModel().getPVName();
                            BeastAlarmInfo beast = pvWidget.getBeastAlarmInfo();
                            boolean wasChannelConnected, channelConnected;

                            synchronized (beast) {
                                wasChannelConnected = beast.isBeastChannelConnected();
                            }
                            channelConnected = event.getPvReader().isConnected();
                            
                            if (event.isExceptionChanged()) {
                                Exception e = event.getPvReader().lastException();
                                log.fine("BeastAlarmListener (" + pvName + ") received an EXCEPTION: " + e.toString());
                            }

                            if (event.isConnectionChanged()) {
                                synchronized (beast) {
                                    beast.setBeastChannelConnected(channelConnected);
                                }
                                // isBeastAlarm will only be true if we successfully connected to it at least once
                                if (channelConnected)
                                    pvWidget.setIsBeastAlarm(true);
                            }
                            
                            if (!event.isValueChanged() || event.getPvReader().getValue() == null) {
                                // even if we didn't receive a ValueChanged event, we might have to update UI
                                // on connection state changes
                                if (wasChannelConnected != channelConnected)
                                    pvWidget.processBeastAlarmState();
                                    
                                return;
                            }
                            
                            if (isFirstValueEvent) {
                                // We will only check 'format' of incoming message and find the columns we need
                                // the first time we receive a ValueChanged event

                                if (!(event.getPvReader().getValue() instanceof VTable)) {
                                    log.severe("BeastAlarmListener (" + pvName + "): data is not a VTable");
                                    return;
                                }
                            }

                            VTable table = (VTable) event.getPvReader().getValue();
                            if (isFirstValueEvent) {
                                if (table.getColumnCount() < 2) {
                                    log.severe("BeastAlarmListener (" + pvName + "): received VTable has fewer than 2 columns");
                                    return;
                                }

                                List<String> keys = (List<String>) table.getColumnData(0);
                                for (int i = 0; i < keys.size(); i++) {
                                    if ("AlarmStatus".equalsIgnoreCase(keys.get(i)))
                                        latchedSeverityIdx = i;
                                    if ("CurrentStatus".equalsIgnoreCase(keys.get(i)))
                                        currentSeverityIdx = i;
                                }
                                if (latchedSeverityIdx == -1 || currentSeverityIdx == -1) {
                                    log.severe("BeastAlarmListener (" + pvName + "): missing Latched or Current alarm status");
                                    return;
                                }

                                isFirstValueEvent = false;
                            }

                            List<String> data = (List<String>) table.getColumnData(1);

                            AlarmSeverity beastSeverity;
                            synchronized (beast) {
                                beast.setLatchedSeverity(BeastAlarmSeverityLevel.parse(data.get(latchedSeverityIdx)));
                                beast.setCurrentSeverity(BeastAlarmSeverityLevel.parse(data.get(currentSeverityIdx)));
                                beastSeverity = beast.getCurrentAlarmSeverity();
                            }

                            if (pvWidget.getAlarmSeverity() != beastSeverity) {
                                pvWidget.setAlarmSeverity(beastSeverity);
                            }

                            pvWidget.processBeastAlarmState();
                        }
                    }).asynchWriteAndMaxReadRate(TimeDuration.ofHertz(25));
        }
//        catch (org.diirt.datasource.TimeoutException e) {
//            // retry after a delay ?
//        }
        catch (Exception e) {
            log.warning("BeastAlarmListener instantiation failed for channel " + alarmPVName + " (" + e.toString() + ")");
        }
    }
}
