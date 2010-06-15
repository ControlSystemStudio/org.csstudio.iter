package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.ScriptPropertyDescriptor;
import org.csstudio.opibuilder.script.PVTuple;
import org.csstudio.opibuilder.script.ScriptData;
import org.csstudio.opibuilder.script.ScriptsInput;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**The property for script.
 * @author Xihui Chen
 *
 */
public class ScriptProperty extends AbstractWidgetProperty {
	
	/**
	 * XML ELEMENT name <code>PATH</code>.
	 */
	public static final String XML_ELEMENT_PATH = "path"; //$NON-NLS-1$

	/**
	 * XML ATTRIBUTE name <code>PATHSTRING</code>.
	 */
	public static final String XML_ATTRIBUTE_PATHSTRING = "pathString"; //$NON-NLS-1$
	
	/**
	 * XML Element name <code>PV</code>.
	 */
	public static final String XML_ELEMENT_PV = "pv"; //$NON-NLS-1$

	public static final String XML_ATTRIBUTE_TRIGGER = "trig"; //$NON-NLS-1$

	
	public ScriptProperty(String prop_id, String description,
			WidgetPropertyCategory category) {
		super(prop_id, description, category, new ScriptsInput());
		
	}

	@Override
	public Object checkValue(Object value) {
		if(value == null)
			return null;
		ScriptsInput acceptableValue = null;
		if(value instanceof ScriptsInput){
			acceptableValue = (ScriptsInput)value;			
		}
		
		return acceptableValue;
	}

	
	@Override
	public Object getPropertyValue() {
		if(executionMode == ExecutionMode.RUN_MODE && widgetModel !=null){
			ScriptsInput value = (ScriptsInput) super.getPropertyValue();
			for(ScriptData sd : value.getScriptList()){
				for(Object pv : sd.getPVList().toArray()){
					PVTuple pvTuple = (PVTuple)pv;
					String newPV = OPIBuilderMacroUtil.replaceMacros(widgetModel, pvTuple.pvName);
					if(!newPV.equals(pvTuple.pvName)){
						int i= sd.getPVList().indexOf(pv);
						sd.getPVList().remove(pv);
						sd.getPVList().add(i, new PVTuple(newPV, pvTuple.trigger));
					}
				}
			}
			return value;
		}else
			return super.getPropertyValue();
	}
	
	
	@Override
	protected PropertyDescriptor createPropertyDescriptor() {
		return new ScriptPropertyDescriptor(prop_id, widgetModel, description);
	}

	@Override
	public ScriptsInput readValueFromXML(Element propElement) {
		ScriptsInput result = new ScriptsInput();
		for(Object oe : propElement.getChildren(XML_ELEMENT_PATH)){
			Element se = (Element)oe;
			ScriptData  sd = new ScriptData(new Path(se.getAttributeValue(XML_ATTRIBUTE_PATHSTRING)));
			for(Object o : se.getChildren(XML_ELEMENT_PV)){
				Element pve = (Element)o;
				boolean trig = true;
				if(pve.getAttribute(XML_ATTRIBUTE_TRIGGER) != null)
					trig = Boolean.parseBoolean(pve.getAttributeValue(XML_ATTRIBUTE_TRIGGER));
				sd.addPV(new PVTuple(pve.getText(), trig));
			}
			result.getScriptList().add(sd);			
		}		
		return result;
	}

	@Override
	public void writeToXML(Element propElement) {
		for(ScriptData scriptData : ((ScriptsInput)getPropertyValue()).getScriptList()){
				Element pathElement = new Element(XML_ELEMENT_PATH);
				pathElement.setAttribute(XML_ATTRIBUTE_PATHSTRING, 
						scriptData.getPath().toPortableString());				
				for(PVTuple pv : scriptData.getPVList()){
					Element pvElement = new Element(XML_ELEMENT_PV);
					pvElement.setText(pv.pvName);
					pvElement.setAttribute(XML_ATTRIBUTE_TRIGGER, Boolean.toString(pv.trigger));
					pathElement.addContent(pvElement);
				}
				propElement.addContent(pathElement);
		}		
	}

}