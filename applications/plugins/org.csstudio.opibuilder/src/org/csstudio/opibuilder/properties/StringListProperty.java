package org.csstudio.opibuilder.properties;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.StringListPropertyDescriptor;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**The property for string list.
 * @author Xihui Chen
 *
 */
public class StringListProperty extends AbstractWidgetProperty {
	
	/**
	 * XML ELEMENT name <code>INCLUDE_PARENT_MACROS</code>.
	 */
	public static final String XML_ELEMENT_ITEM = "s"; //$NON-NLS-1$

	public StringListProperty(String prop_id, String description,
			WidgetPropertyCategory category, List<String> default_value) {
		super(prop_id, description, category, default_value);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object checkValue(Object value) {
		if(value == null)
			return null;
		List<String> acceptableValue = null;
		if(value instanceof List){	
			if(((List) value).size() == 0 || 
					(((List) value).size() > 0 && ((List) value).get(0) instanceof String))
			acceptableValue = (List<String>)value;			
		}		
		return acceptableValue;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Object getPropertyValue() {
		if(widgetModel !=null && widgetModel.getExecutionMode() == ExecutionMode.RUN_MODE){
			List<String> result= new ArrayList<String>();
			for(String item : (List<String>) super.getPropertyValue()){
				result.add(OPIBuilderMacroUtil.replaceMacros(
					widgetModel, item));
			}
			return result;
		}else
			return super.getPropertyValue();
	}
	
	
	@Override
	protected PropertyDescriptor createPropertyDescriptor() {
		return new StringListPropertyDescriptor(prop_id, description);
	}

	@Override
	public List<String> readValueFromXML(Element propElement) {
		List<String> result = new ArrayList<String>();		
		for(Object oe : propElement.getChildren()){
			Element se = (Element)oe;
			if(se.getName().equals(XML_ELEMENT_ITEM))
				result.add(se.getText());
		}		
		return result;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeToXML(Element propElement) {
		List<String> data = (List<String>)propertyValue;		
		for(String item : data){
			Element newElement = new Element(XML_ELEMENT_ITEM);
			newElement.setText(item);
			propElement.addContent(newElement);
		}
	}

}