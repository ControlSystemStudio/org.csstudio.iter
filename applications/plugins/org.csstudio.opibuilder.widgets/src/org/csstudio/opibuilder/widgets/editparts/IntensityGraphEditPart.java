package org.csstudio.opibuilder.widgets.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.datadefinition.ColorMap;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.util.UIBundlingThread;
import org.csstudio.opibuilder.visualparts.BorderFactory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.opibuilder.widgets.figures.IntensityGraphFigure;
import org.csstudio.opibuilder.widgets.figures.IntensityGraphFigure.IProfileDataChangeLisenter;
import org.csstudio.opibuilder.widgets.model.IntensityGraphModel;
import org.csstudio.opibuilder.widgets.model.IntensityGraphModel.AxisProperty;
import org.csstudio.platform.data.IValue;
import org.csstudio.platform.data.ValueUtil;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;

/**The widget editpart of intensity graph widget.
 * @author Xihui Chen
 *
 */
public class IntensityGraphEditPart extends AbstractPVWidgetEditPart {

	
	private boolean innerTrig;
	
	@Override
	protected IFigure doCreateFigure() {
		IntensityGraphModel model = getWidgetModel();
		IntensityGraphFigure graph = new IntensityGraphFigure(getExecutionMode());
		graph.setMin(model.getMinimum());
		graph.setMax(model.getMaximum());
		graph.setDataWidth(model.getDataWidth());
		graph.setDataHeight(model.getDataHeight());
		graph.setColorMap(model.getColorMap());
		graph.setShowRamp(model.isShowRamp());
		graph.setCropLeft(model.getCropLeft());
		graph.setCropRigth(model.getCropRight());
		graph.setCropTop(model.getCropTOP());
		graph.setCropBottom(model.getCropBottom());
		//init X-Axis
		for(AxisProperty axisProperty : AxisProperty.values()){
			String propID = IntensityGraphModel.makeAxisPropID(
					IntensityGraphModel.X_AXIS_ID, axisProperty.propIDPre);
			setAxisProperty(graph.getXAxis(), axisProperty, model.getPropertyValue(propID));
		}
		//init Y-Axis
		for(AxisProperty axisProperty : AxisProperty.values()){
			String propID = IntensityGraphModel.makeAxisPropID(
					IntensityGraphModel.Y_AXIS_ID, axisProperty.propIDPre);
			setAxisProperty(graph.getYAxis(), axisProperty, model.getPropertyValue(propID));
		}
		//add profile data listener
		if(getExecutionMode() == ExecutionMode.RUN_MODE && 
				(model.getHorizonProfileYPV().trim().length() >0 || 
						model.getVerticalProfileYPV().trim().length() > 0)){
			graph.addProfileDataListener(new IProfileDataChangeLisenter(){

				public void profileDataChanged(double[] xProfileData,
						double[] yProfileData, Range xAxisRange, Range yAxisRange) {
					//horizontal
					setPVValue(IntensityGraphModel.PROP_HORIZON_PROFILE_Y_PV_NAME, xProfileData);
					double[] horizonXData = new double[xProfileData.length];
					double d = (xAxisRange.getUpper() - xAxisRange.getLower())/(xProfileData.length-1);
					for(int i=0; i<xProfileData.length; i++){
						horizonXData[i] = xAxisRange.getLower() + d *i;
					}
					setPVValue(IntensityGraphModel.PROP_HORIZON_PROFILE_X_PV_NAME, horizonXData);
					//vertical
					setPVValue(IntensityGraphModel.PROP_VERTICAL_PROFILE_Y_PV_NAME, yProfileData);
					double[] verticalXData = new double[yProfileData.length];
					d = (yAxisRange.getUpper() - yAxisRange.getLower())/(yProfileData.length-1);
					for(int i=0; i<yProfileData.length; i++){
						verticalXData[i] = yAxisRange.getUpper() - d*i;
					}
					setPVValue(IntensityGraphModel.PROP_VERTICAL_PROFILE_X_PV_NAME, verticalXData);					
				}	
				
			});
		}
		
		return graph;
	}
	
	@Override
	public IntensityGraphModel getWidgetModel() {
		return (IntensityGraphModel)getModel();
	}

	@Override
	protected void registerPropertyChangeHandlers() {
		innerUpdateGraphAreaSizeProperty();
		registerAxisPropertyChangeHandler();
		IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
		
			public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
				if(newValue == null || !(newValue instanceof IValue))
					return false;
				IValue value = (IValue)newValue;
				//if(ValueUtil.getSize(value) > 1){
					((IntensityGraphFigure)figure).setDataArray(ValueUtil.getDoubleArray(value));
				//}else
				//	((IntensityGraphFigure)figure).setDataArray(ValueUtil.getDouble(value));			
				return false;
			}
		};
		
		setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, handler);
		
				
		getWidgetModel().getProperty(IntensityGraphModel.PROP_MIN).addPropertyChangeListener(
				new PropertyChangeListener() {				
					public void propertyChange(PropertyChangeEvent evt) {
						((IntensityGraphFigure)figure).setMin((Double)evt.getNewValue());
						figure.repaint();
						innerUpdateGraphAreaSizeProperty();
					}
				});
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_MAX).addPropertyChangeListener(
				new PropertyChangeListener() {				
					public void propertyChange(PropertyChangeEvent evt) {
						((IntensityGraphFigure)figure).setMax((Double)evt.getNewValue());
						figure.repaint();
						innerUpdateGraphAreaSizeProperty();
					}
				});
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_BORDER_STYLE).removeAllPropertyChangeListeners();
		getWidgetModel().getProperty(IntensityGraphModel.PROP_BORDER_STYLE).addPropertyChangeListener(
				new PropertyChangeListener() {
				
					public void propertyChange(PropertyChangeEvent evt) {
						figure.setBorder(
								BorderFactory.createBorder(BorderStyle.values()[(Integer)evt.getNewValue()],
								getWidgetModel().getBorderWidth(), getWidgetModel().getBorderColor(),
								getWidgetModel().getName()));
						innerUpdateGraphAreaSizeProperty();
					}
				});
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_BORDER_WIDTH).removeAllPropertyChangeListeners();
		getWidgetModel().getProperty(IntensityGraphModel.PROP_BORDER_WIDTH).addPropertyChangeListener(
				new PropertyChangeListener() {
				
					public void propertyChange(PropertyChangeEvent evt) {
						figure.setBorder(
								BorderFactory.createBorder(getWidgetModel().getBorderStyle(),
								(Integer)evt.getNewValue(), getWidgetModel().getBorderColor(),
								getWidgetModel().getName()));
						innerUpdateGraphAreaSizeProperty();
					}
				});
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((IntensityGraphFigure)figure).setDataWidth((Integer)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(IntensityGraphModel.PROP_DATA_WIDTH, handler);
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((IntensityGraphFigure)figure).setDataHeight((Integer)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(IntensityGraphModel.PROP_DATA_HEIGHT, handler);
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((IntensityGraphFigure)figure).setColorMap((ColorMap)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(IntensityGraphModel.PROP_COLOR_MAP, handler);	
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((IntensityGraphFigure)figure).setCropLeft((Integer)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(IntensityGraphModel.PROP_CROP_LEFT, handler);	
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((IntensityGraphFigure)figure).setCropRigth((Integer)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(IntensityGraphModel.PROP_CROP_RIGHT, handler);	
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((IntensityGraphFigure)figure).setCropTop((Integer)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(IntensityGraphModel.PROP_CROP_TOP, handler);	
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((IntensityGraphFigure)figure).setCropBottom((Integer)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(IntensityGraphModel.PROP_CROP_BOTTOM, handler);	
		
		
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_SHOW_RAMP).addPropertyChangeListener(
				new PropertyChangeListener() {				
					public void propertyChange(PropertyChangeEvent evt) {
						((IntensityGraphFigure)getFigure()).setShowRamp((Boolean)evt.getNewValue());
						Dimension d = ((IntensityGraphFigure)getFigure()).getGraphAreaInsets();	
						innerTrig = true;
						getWidgetModel().setPropertyValue(IntensityGraphModel.PROP_GRAPH_AREA_WIDTH, 
								getWidgetModel().getWidth() - d.width);
						innerTrig = false;
					}
		});
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_WIDTH).addPropertyChangeListener(
				new PropertyChangeListener() {				
					public void propertyChange(PropertyChangeEvent evt) {
						if(!innerTrig){ // if it is not triggered from inner
							innerTrig = true;
							Dimension d = ((IntensityGraphFigure)getFigure()).getGraphAreaInsets();	
							getWidgetModel().setPropertyValue(IntensityGraphModel.PROP_GRAPH_AREA_WIDTH, 
								((Integer)evt.getNewValue()) - d.width);			
							innerTrig = false; // reset innerTrig to false after each inner triggering
						}else //if it is triggered from inner, do nothing
							innerTrig = false;
					}
		});				
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_GRAPH_AREA_WIDTH).addPropertyChangeListener(
				new PropertyChangeListener() {				
					public void propertyChange(PropertyChangeEvent evt) {
						if(!innerTrig){
							innerTrig = true;
							Dimension d = ((IntensityGraphFigure)getFigure()).getGraphAreaInsets();				
							getWidgetModel().setPropertyValue(IntensityGraphModel.PROP_WIDTH, 
									((Integer)evt.getNewValue()) + d.width);	
							innerTrig = false; // reset innerTrig to false after each inner triggering
						}else
							innerTrig = false;
					}
		});	
				
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_HEIGHT).addPropertyChangeListener(
				new PropertyChangeListener() {				
					public void propertyChange(PropertyChangeEvent evt) {
						if(!innerTrig){
							innerTrig = true;
							Dimension d = ((IntensityGraphFigure)getFigure()).getGraphAreaInsets();				
							getWidgetModel().setPropertyValue(IntensityGraphModel.PROP_GRAPH_AREA_HEIGHT, 
								((Integer)evt.getNewValue()) - d.height);
							innerTrig = false; // reset innerTrig to false after each inner triggering
						}else
							innerTrig = false;
					}
		});				
		
		getWidgetModel().getProperty(IntensityGraphModel.PROP_GRAPH_AREA_HEIGHT).addPropertyChangeListener(
				new PropertyChangeListener() {				
					public void propertyChange(PropertyChangeEvent evt) {
						if(!innerTrig){
							innerTrig = true;	
							Dimension d = ((IntensityGraphFigure)getFigure()).getGraphAreaInsets();				
							getWidgetModel().setPropertyValue(IntensityGraphModel.PROP_HEIGHT, 
								((Integer)evt.getNewValue()) + d.height);
							innerTrig = false; // reset innerTrig to false after each inner triggering
						}else
							innerTrig = false;
					}
		});	
	}
	
	private synchronized void innerUpdateGraphAreaSizeProperty(){
		Dimension d = ((IntensityGraphFigure)figure).getGraphAreaInsets();				
		innerTrig = true;
		getWidgetModel().setPropertyValue(IntensityGraphModel.PROP_GRAPH_AREA_WIDTH, 
				getWidgetModel().getSize().width - d.width);
		innerTrig = true; // recover innerTrig
		getWidgetModel().setPropertyValue(IntensityGraphModel.PROP_GRAPH_AREA_HEIGHT, 
				getWidgetModel().getSize().height - d.height);
		innerTrig = false; // reset innerTrig to false after each inner triggering
	}

	private void registerAxisPropertyChangeHandler(){
		for(String axisID : new String[]{IntensityGraphModel.X_AXIS_ID, IntensityGraphModel.Y_AXIS_ID}){
			for(AxisProperty axisProperty : AxisProperty.values()){
				final IWidgetPropertyChangeHandler handler = 
					new AxisPropertyChangeHandler(
							axisID.equals(IntensityGraphModel.X_AXIS_ID)? 
									((IntensityGraphFigure)getFigure()).getXAxis():
									((IntensityGraphFigure)getFigure()).getYAxis(), 
									axisProperty);
				//must use listener instead of handler because the prop sheet need to be 
				//refreshed immediately.
				getWidgetModel().getProperty(IntensityGraphModel.makeAxisPropID(
						axisID, axisProperty.propIDPre)).
							addPropertyChangeListener(new PropertyChangeListener() {					
								public void propertyChange(PropertyChangeEvent evt) {
									handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
									UIBundlingThread.getInstance().addRunnable(new Runnable(){
										public void run() {
											getFigure().repaint();
										}
									});
							}
						});
								
			}
		}
		
	}
	
	private void setAxisProperty(Axis axis, AxisProperty axisProperty, Object newValue){
		switch (axisProperty) {
		case TITLE:
			axis.setTitle((String)newValue);		
			break;
		case AXIS_COLOR:
			axis.setForegroundColor(CustomMediaFactory.getInstance().getColor(((OPIColor)newValue).getRGBValue()));
			break;
		case MAX:
			axis.setRange(axis.getRange().getLower(), (Double)newValue);
			break;
		case MIN:
			axis.setRange((Double)newValue, axis.getRange().getUpper());
			break;
		case TITLE_FONT:
			axis.setTitleFont(CustomMediaFactory.getInstance().getFont(
					((OPIFont)newValue).getFontData()));
			break;
		case MAJOR_TICK_STEP_HINT:
			axis.setMajorTickMarkStepHint((Integer)newValue);
			break;
		case SHOW_MINOR_TICKS:
			axis.setMinorTicksVisible((Boolean)newValue);
			break;
		case VISIBLE:
			axis.setVisible((Boolean)newValue);
			break;
		default:
			break;
		}		
}
	
	@Override
	public double[] getValue() {
		return ((IntensityGraphFigure)getFigure()).getDataArray();
	}

	@Override
	public void setValue(Object value) {
		if(value instanceof double[] || value instanceof Double[]){
			((IntensityGraphFigure)getFigure()).setDataArray((double[]) value);
		}
	}
	
	@Override
	public void deactivate() {
		((IntensityGraphFigure)getFigure()).dispose();
		super.deactivate();
	}

	class AxisPropertyChangeHandler implements IWidgetPropertyChangeHandler {
		private AxisProperty axisProperty;
		private Axis axis;
		public AxisPropertyChangeHandler(Axis axis, AxisProperty axisProperty) {
			this.axis = axis;
			this.axisProperty = axisProperty;
		}
		public boolean handleChange(Object oldValue, Object newValue,
				IFigure refreshableFigure) {
			setAxisProperty(axis, axisProperty, newValue);		
			innerTrig = true;
			innerUpdateGraphAreaSizeProperty();
			axis.revalidate();		
			return true;
		}
	}
	
}