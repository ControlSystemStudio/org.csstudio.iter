package org.csstudio.opibuilder.converter.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Specific class representing EdmFont property.
 *
 * @author Matevz
 *
 */
public class EdmFont extends EdmAttribute {

	private static Logger log = Logger.getLogger("org.csstudio.opibuilder.converter.parser.EdmFont");

	private String name;
	private boolean bold;
	private boolean italic;
	private double size;

	/**
	 * Constructor which parses EdmFont from general EdmAttribute value.
	 *
	 * @param genericAttribute EdmAttribute containing general EdmFont data.
	 * @param required false if this attribute is optional, else true
	 * @throws EdmException if EdmAttribute contains invalid data.
	 */
	public EdmFont(EdmAttribute genericAttribute, boolean required) throws EdmException {
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

		Pattern p = Pattern.compile("(\\w*?)-(\\w*?)-([ri])-(\\d.*?\\.\\d)");
		Matcher m = p.matcher(getValue(0));

		String nameStr = "";
		String weightStr = "";
		String styleStr = "";
		String sizeStr = "";
		try {
			m.find();
			nameStr = m.group(1);
			weightStr = m.group(2);
			styleStr = m.group(3);
			sizeStr = m.group(4);
		}
		catch (Exception e) {
			throw new EdmException(EdmException.FONT_FORMAT_ERROR, "Invalid font format.");
		}

		name = nameStr;

		if (weightStr.equals("bold")) {
			bold = true;
		} else if (weightStr.equals("medium")) {
			bold = false;
		} else {
			throw new EdmException(EdmException.SPECIFIC_PARSING_ERROR,
			"Error parsing font weight (bold) value.");
		}

		if (styleStr.equals("i")) {
			italic = true;
		} else if (styleStr.equals("r")) {
			italic = false;
		} else {
			throw new EdmException(EdmException.SPECIFIC_PARSING_ERROR,
			"Error parsing font style (italic) value.");
		}

		try {
			size = Double.valueOf(sizeStr).doubleValue();
		} catch (Exception e) {
			throw new EdmException(EdmException.COLOR_FORMAT_ERROR, "Invalid RGB color format.");
		}
		setInitialized(true);
	}

	public String getName() {
		return name;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public double getSize() {
		return size;
	}
}