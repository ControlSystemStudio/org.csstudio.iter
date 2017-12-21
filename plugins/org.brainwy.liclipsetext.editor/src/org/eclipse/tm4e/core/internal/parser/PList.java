/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.core.internal.grammar.parser.PListGrammar;
import org.eclipse.tm4e.core.internal.theme.PListTheme;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PList<T> extends DefaultHandler {

	private final boolean theme;
	private final List<String> errors;
	private PListObject currObject;
	private T result;
	private StringBuilder text;

	public PList(boolean theme) {
		this.theme = theme;
		this.errors = new ArrayList<String>();
		this.currObject = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ("dict".equals(localName)) {
			this.currObject = create(currObject, false, theme);
		} else if ("array".equals(localName)) {
			this.currObject = create(currObject, true, theme);
		} else if ("key".equals(localName)) {
			if (currObject != null) {
				currObject.setLastKey(null);
			}
		}
		this.text = new StringBuilder("");
		super.startElement(uri, localName, qName, attributes);
	}

	private PListObject create(PListObject parent, boolean valueAsArray, boolean grammar) {
		if (theme) {
			return new PListTheme(parent, valueAsArray);
		}
		return new PListGrammar(parent, valueAsArray);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		endElement(localName);
		super.endElement(uri, localName, qName);
	}

	private void endElement(String tagName) {
		Object value = null;
		String text = this.text.toString();
		if ("key".equals(tagName)) {
			if (currObject == null || currObject.isValueAsArray()) {
				errors.add("key can only be used inside an open dict element");
				return;
			}
			currObject.setLastKey(text);
			return;
		} else if ("dict".equals(tagName) || "array".equals(tagName)) {
			if (currObject == null) {
				errors.add(tagName + " closing tag found, without opening tag");
				return;
			}
			value = currObject.getValue();
			currObject = currObject.getParent();
		} else if ("string".equals(tagName) || "data".equals(tagName)) {
			value = text;
		} else if ("date".equals(tagName)) {
			// TODO : parse date
		} else if ("integer".equals(tagName)) {
			try {
				value = Integer.parseInt(text);
			} catch (NumberFormatException e) {
				errors.add(text + " is not a integer");
				return;
			}
		} else if ("real".equals(tagName)) {
			try {
				value = Float.parseFloat(text);
			} catch (NumberFormatException e) {
				errors.add(text + " is not a float");
				return;
			}
		} else if ("true".equals(tagName)) {
			value = true;
		} else if ("false".equals(tagName)) {
			value = false;
		} else if ("plist".equals(tagName)) {
			return;
		} else {
			errors.add("Invalid tag name: " + tagName);
			return;
		}
		if (currObject == null) {
			result = (T) value;
		} else if (currObject.isValueAsArray()) {
			currObject.addValue(value);
		} else {
			if (currObject.getLastKey() != null) {
				currObject.addValue(value);
			} else {
				errors.add("Dictionary key missing for value " + value);
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.text.append(String.valueOf(ch, start, length));
		super.characters(ch, start, length);
	}

	public T getResult() {
		return result;
	}
}
