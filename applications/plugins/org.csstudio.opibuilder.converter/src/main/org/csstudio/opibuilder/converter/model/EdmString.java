package org.csstudio.opibuilder.converter.model;

import org.apache.log4j.Logger;

/**
 * Specific class representing EdmString property.
 * 
 * @author Matevz
 *
 */
public class EdmString extends EdmAttribute {

	private static Logger log = Logger.getLogger("org.csstudio.opibuilder.converter.parser.EdmString");
	
	private String val; 
	
	/**
	 * Constructor, which parses string property from EdmAttribute general interface.
	 * 
	 * @param genericAttribute	EdmAttribute containing string format data.
	 * @param required false if this attribute is optional, else true
	 * @throws EdmException 
	 */
	public EdmString(EdmAttribute genericAttribute, boolean required) throws EdmException {
		super(genericAttribute);
		
		setRequired(required);
		
		if (genericAttribute == null || getValueCount() == 0) {
			if (isRequired())
				throw new EdmException(EdmException.REQUIRED_ATTRIBUTE_MISSING,
						"Trying to initialize a required attribute from null object.");
			else {
				log.warn("Missing optional property.");
				return;
			}
		}
		
		if (getValueCount() > 0)
			val = getValue(0);
		else
			val = "";
		
		log.debug("Parsed " + this.getClass().getName() + 
				" = " + val);
		setInitialized(true);
	}
	
	/**
	 * Returns the string value.
	 * @return	Value of EdmString instance.
	 */
	public String get() {
		return val;
	}
}