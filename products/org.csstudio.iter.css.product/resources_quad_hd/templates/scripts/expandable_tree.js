/*
Copyright (c) : 2010-2016 ITER Organization,
CS 90 046
13067 St. Paul-lez-Durance Cedex
France
 
This product is part of ITER CODAC software.
For the terms and conditions of redistribution or use of this software
refer to the file ITER-LICENSE.TXT located in the top level directory
of the distribution package.
*/

importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
importPackage(Packages.java.lang)

	var resolution_4k = widget.getPropertyValue("width") > 3000;
	var INDENT_WIDTH = resolution_4k ? 100 : 50;

	// getting the current cbs level for this screen
	var current_cbs = widget.getMacroValue("LEVEL");

	// getting the input xml file path
	var xml_input = widget.getMacroValue("INPUT");
	// getting the navigation xml file from the user navigation folder
	if (xml_input == null) {
		xml_input = "../navigation/Navigation.xml";
	}
	// loading XML document and getting the root element
	// the result is a JDOM Element
	var root = FileUtil.loadXMLFile(xml_input, widget);
	if (root) {
		// browsing the CBS tree structure starting from root
		buildCBSMap(root, 0);
	}

// ---

// recursive list function on CBS tree
function buildCBSMap(root, indent){
	
	var cbs = root.getChildren();	
	
	var itr = cbs.iterator();
	
	while (itr.hasNext()) {
	    var elt = itr.next();
	        
	    // creating the container for the node 
		var linkingContainer = WidgetUtil.createWidgetModel("org.csstudio.opibuilder.widgets.linkingContainer");
			
		linkingContainer.setPropertyValue("opi_file", "CBSMapElt.opi");
		linkingContainer.setPropertyValue("border_style", 0);

		linkingContainer.setPropertyValue("height", resolution_4k ? 60 : 35);
		linkingContainer.setPropertyValue("width", resolution_4k ? 3200 - indent * INDENT_WIDTH : 1600 - indent * INDENT_WIDTH);
	    
	    // no indent needed for root 
	    if (indent > 0) {
			linkingContainer.setPropertyValue("x", indent * INDENT_WIDTH);
	    }
	    
	    if (isEltCurrentCBS(elt, current_cbs)) {
			linkingContainer.setPropertyValue("background_color", "IO Grid");
	    } else {
			linkingContainer.setPropertyValue("background_color", "IO Background");
	    }
		
		linkingContainer.setPropertyValue("tooltip", "Click directly on the OPIs map buttons to open the related screen");
		
	    // adding macros CBS and OPI_FILE to the container
		linkingContainer.addMacro("CBS", elt.getAttributeValue("name"));

		linkingContainer.addMacro("OPI_FILE", getOPI_FILE(elt));	
		linkingContainer.addMacro("ALARM_ROOT", getALARM_ROOT(elt));	
		addOPImacros(linkingContainer, elt);	

	    // adding the container to the parent widget 
	    widget.addChildToBottom(linkingContainer);
	
		// setting the CBS label properties
	 	var button = widget.getWidget(elt.getAttributeValue("name"));	
 		button.setPropertyValue("enabled", elt.getAttributeValue("enabled"));
	 	button.setPropertyValue("tooltip", elt.getAttributeValue("description") + " ($(number_alarms) alarm(s))");
    
		buildCBSMap(elt, indent+1);
 	}
}

function isEltCurrentCBS(elt, level) {
	var attribute = elt.getAttributeValue("opi_file");
	if (attribute) {
		var words = attribute.split(" ");
	
		var i=0;
		for (i in words) {
			if (words[i].search("=") > 0) {
		    	var macros = words[i].split("=");
		    	// macro format: macro_name=macro_value
		    	var macro_name = macros[0];
		    	if (macro_name == "LEVEL") {
			    	var macro_value = (macros[1] == null) ? "" : macros[1];
			    	return ~level.indexOf(macro_value);
		       	}
		    }
		}
	}
	return false;
}

function getOPI_FILE(elt) {
	var attribute = elt.getAttributeValue("opi_file");

	if (attribute && attribute.search(".opi") > 0) {
    	// suppressing the extension .opi
    	return attribute.substring(0, attribute.search(".opi"));
    }
    return attribute;
}

function getALARM_ROOT(elt) {
	var attribute = elt.getAttributeValue("alarm_root");

    return attribute;
}

function addOPImacros(container, elt) {
	// getting the opi file from the navigation xml configuration file (even for alarms list)
	var attribute = elt.getAttributeValue("opi_file");
	
	if (attribute) {
		var words = attribute.match(/(?:[^\s']+|'[^']*')+/g);
	
		var i=0;
		for (i in words) {
			if (words[i].search("=") > 0) {
		    	var macros = words[i].split("=");
		    	// macro format: macro_name=macro_value
		    	var macro_name = macros[0];
		    	var macro_value = (macros[1] == null) ? "" : macros[1];
		       	container.addMacro(macro_name, macro_value);
		    }
		}
	}
}
