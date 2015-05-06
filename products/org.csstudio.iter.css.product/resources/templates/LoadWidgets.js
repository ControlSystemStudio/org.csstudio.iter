/*
Copyright (c) : 2010-2015 ITER Organization,
CS 90 046
13067 St. Paul-lez-Durance Cedex
France
 
This product is part of ITER CODAC software.
For the terms and conditions of redistribution or use of this software
refer to the file ITER-LICENSE.TXT located in the top level directory
of the distribution package.
*/

importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
importPackage(Packages.org.csstudio.opibuilder.persistence);
importPackage(Packages.java.lang)

// getting the input xml file path
var widgets_xml = widget.getMacroValue("INPUT");

// loading XML document and getting the root element
// the result is a JDOM Element
var root = FileUtil.loadXMLFile(widgets_xml, widget);

var symbolModel = widget.getWidget("SymbolModel");
var labelModel = widget.getWidget("LabelModel");
var symbolWidgetXML = XMLUtil.widgetToXMLString(symbolModel.getWidgetModel(), false);
var labelWidgetXML = XMLUtil.widgetToXMLString(labelModel.getWidgetModel(), false);

var widgetsList = root.getChildren();

// iterating over all children in XML
var itr = widgetsList.iterator();
var currentSymbol = null;
var currentLabel = null;
while (itr.hasNext()) 
{
	var elt = itr.next();
	currentSymbol = XMLUtil.fillWidgetsFromXMLString(symbolWidgetXML, null);
	currentLabel = XMLUtil.fillWidgetsFromXMLString(labelWidgetXML, null);

	setSymbol(currentSymbol, elt);
	setLabel(currentLabel, elt.getChildren().get(0));

	widget.addChild(currentSymbol);
	widget.addChild(currentLabel);
}

widget.setPropertyValue("show_scrollbar", "true");

function setSymbol(symbol, elt) {
	symbol.setPropertyValue("name", elt.getAttributeValue("name"));
	symbol.setPropertyValue("image_file", elt.getAttributeValue("image_file"));
	symbol.setPropertyValue("height", elt.getAttributeValue("height"));
	symbol.setPropertyValue("width", elt.getAttributeValue("width"));
	symbol.setPropertyValue("x", elt.getAttributeValue("x"));
	symbol.setPropertyValue("y", elt.getAttributeValue("y"));
}

function setLabel(label, elt) {
	label.setPropertyValue("name", elt.getAttributeValue("name"));
	label.setPropertyValue("text", elt.getAttributeValue("text"));
	label.setPropertyValue("height", elt.getAttributeValue("height"));
	label.setPropertyValue("width", elt.getAttributeValue("width"));
	label.setPropertyValue("x", elt.getAttributeValue("x"));
	label.setPropertyValue("y", elt.getAttributeValue("y"));
}