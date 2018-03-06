/*
Copyright (c) : 2010-2018 ITER Organization,
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
	var HEIGHT = resolution_4k ? 36 : 18;

	// getting the current cbs level for this screen
	var current_level = (widget.getMacroValue("LEVEL") == null ? "" : widget.getMacroValue("LEVEL").toUpperCase());
	var path = "";

	// getting if a canvas (frame) OPI is used to load mimics OPI
	var canvas_opi = widget.getMacroValue("CANVAS_OPI");
	var opi_macro = "MIMIC_FILE";
	if (canvas_opi == null || canvas_opi.toUpperCase() != "TRUE") {
		canvas_opi = "FALSE";
		opi_macro = "OPI_FILE";
	}

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
	var WIDTH = resolution_4k ? 3200 - indent * INDENT_WIDTH : 1600 - indent * INDENT_WIDTH;
	var upLevel = "";
	var cbs = root.getChildren();
	if (cbs) {
		var itr = cbs.iterator();	
		while (itr.hasNext()) {
		    var elt = itr.next();
		        
		    // creating the container for the node 
			var linkingContainer = WidgetUtil.createWidgetModel("org.csstudio.opibuilder.widgets.linkingContainer");
				
			linkingContainer.setPropertyValue("opi_file", "CBSMapElt.opi");
			linkingContainer.setPropertyValue("border_style", 0);
	
			linkingContainer.setPropertyValue("height", HEIGHT);
			linkingContainer.setPropertyValue("width", WIDTH);    
			linkingContainer.setPropertyValue("x", indent * INDENT_WIDTH);
		    
		    if (currentCBS(elt)) {
				linkingContainer.setPropertyValue("background_color", "IO Grid");
		    }
			
			linkingContainer.setPropertyValue("tooltip", "Click on the OPIs map menu button and select which OPI to open");
			
		    // adding macros CBS and OPI_FILE to the container
			var cbs_name = elt.getAttributeValue("name").toUpperCase();
			linkingContainer.addMacro("CBS", cbs_name);
			
			upLevel = path;
			path = (path == "" ? cbs_name : path + "-" + cbs_name);
			linkingContainer.addMacro("CBS_PATH", path);	
	
			linkingContainer.addMacro(opi_macro, getOPI_FILE(elt));	
			linkingContainer.addMacro("ALARM_ROOT", getALARM_ROOT(elt));	
			addOPImacros(linkingContainer, elt);	
	
		    // adding the container to the parent widget 
		    widget.addChildToBottom(linkingContainer);
		
			// setting the CBS label properties
		 	var button = widget.getWidget(path);
		 	button.setPropertyValue("enabled", elt.getAttributeValue("enabled"));
		 	button.setPropertyValue("tooltip", elt.getAttributeValue("description") + " ($(number_alarms) alarm(s))");
	
			buildCBSMap(elt, indent+1);
			path = upLevel;
	 	}
	}
}

function currentCBS(elt) {
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
			    	return ~current_level.indexOf(macro_value);
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
	return elt.getAttributeValue("alarm_root");
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
