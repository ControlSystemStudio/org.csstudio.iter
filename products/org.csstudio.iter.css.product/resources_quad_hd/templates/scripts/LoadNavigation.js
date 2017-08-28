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

	// getting the current cbs level for this screen ITER-CBS1-CBS2-CBS3
	var current_level = (widget.getMacroValue("LEVEL") == null ? "" : widget.getMacroValue("LEVEL").toUpperCase());
	var depth = getNavigationDepth(current_level);
	var path = "";

	// getting if a canvas (frame) OPI is used to load mimics OPI
	var canvas_opi = widget.getMacroValue("CANVAS_OPI");
	var opi_macro = "MIMIC_FILE";
	if (canvas_opi == null || canvas_opi.toUpperCase() != "TRUE") {
		canvas_opi = "FALSE";
		opi_macro = "OPI_FILE";
	}

	// getting the type of OPI: USER or ALARMS LIST
	var opi_type = widget.getMacroValue("OPI_TYPE");
	if (opi_type == null || opi_type.toUpperCase() != "ALARM") {
		opi_type = "USER";
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
		// browsing the CBS tree structure starting from 0 to max depth
		listCBS(root, 0, depth);
	}

// ---

// recursive list function on CBS tree
function listCBS(current, iLevel, depth){
	System.out.println(" >>> listCBS(current, iLevel, depth, path): "+ current + " " + iLevel + " " + depth + " " + path);
	var cbs = current.getChildren();
	if (cbs) {
		var itr = cbs.iterator();
		while (itr.hasNext() && iLevel <= depth) {
		    var elt = itr.next();
		    if (currentCBS(elt)) {
			    updateGlobalNavigationButtons(elt, iLevel);
			    // continue the parsing of the CBS navigation tree
				System.out.println(" >>> listCBS(elt, iLevel + 1, depth, path): "+ elt + " " + (iLevel+1) + " " + depth + " " + path);
				listCBS(elt, iLevel + 1, depth);			    	
			    if (lastNavigationLevel(iLevel, depth)) {
			    	updateMimicNavigationButtons(current, elt);
			    }
		    }
		}
	}
}

function getNavigationDepth(current_level) {
	if (current_level == "" || !current_level) {
		// CBS 0 is not specified or empty name
		return 0;
	}
	// getting the number of levels
	// for instance UTIL-S15-AG07 has 3 CBS levels - ITER is CBS0
	var words = current_level.split("-");
	return words.length - 1;
}

function currentCBS(elt) {
	if (getNavigationDepth(current_level) == 0) {
		return true;
	}
	
	var cbs = elt.getAttributeValue("name").toUpperCase();
	var cbs_path = (path == "" ? cbs : path + "-" + cbs);
	System.out.println(" >>> currentCBS(elt, path): "+ elt + " " + path + "/" + cbs + "/" + cbs_path + "/" + current_level);
	if (cbs_path && current_level.startsWith(cbs_path)) {
		path = cbs_path;
		System.out.println(" >>> currentCBS(elt, path): "+ elt + " " + path);
	  	return true;
	 }
	 System.out.println(" >>> currentCBS(elt, path): "+ elt + " " + path);
	 return false;
}

function lastNavigationLevel(currentLevel, lastLevel){
	return (currentLevel == lastLevel)
}

function updateGlobalNavigationButtons(thisCBS, level){
	if (level == 0){
	    // adding the Home button for level 0 - ITER overview
		addHomeButton(thisCBS);
	} else {
		// adding an Up button for intermediate levels
		addUpButton(thisCBS);
	}
}

function updateMimicNavigationButtons(parentCBS, thisCBS){
	if (thisCBS.getChildren().size()) {
		// adding Mimic buttons
	    addMimicButtons(thisCBS);
	} else {
		// repeat parent Mimic buttons
		addMimicButtons(parentCBS);
		disableMimicButton(thisCBS.getAttributeValue("name"));
	}
}

function getGeneralNavigationContainer() {
	return display.getWidget("GENERAL NAVIGATION BAR");
}

function getGeneralNavigationContainerHeight() {
	return getGeneralNavigationContainer().getPropertyValue("height");
}

function getGeneralNavigationContainerWidth() {
	return getGeneralNavigationContainer().getPropertyValue("width");
}

function getMimicNavigationContainer() {
	return display.getWidget("MIMIC SPECIFIC - NAVIGATION AREA");
}

function getMimicNavigationContainerHeight() {
	return getMimicNavigationContainer().getPropertyValue("height");
}

function getMimicNavigationContainerWidth() {
	return getMimicNavigationContainer().getPropertyValue("width");
}

function getLineOneNavigationContainer() {
	return getMimicNavigationContainer().getWidget("OneLineNavigation");
}

function getLineTwoNavigationContainer() {
	return getMimicNavigationContainer().getWidget("TwoLineNavigation");
}

function addHomeButton(elt) {
	// if no name defined, ITER is the default cbs name
	var cbs_name = (!elt.getAttributeValue("name") ? "ITER" : elt.getAttributeValue("name")).toUpperCase();
	
	// creating the linking container that display the HOME button
	var linkingContainer = createHomeButtonContainer();
	if (linkingContainer) {
	    // reading attribute from element using JDOM
	    // adding macros to the container
		linkingContainer.addMacro("CBS", cbs_name.toUpperCase());	
		linkingContainer.addMacro("CBS_PATH", path.toUpperCase());	
		linkingContainer.addMacro("TITLE", getDescription(elt).toUpperCase());	
		linkingContainer.addMacro(opi_macro, getOPI_FILE(elt));	
		linkingContainer.addMacro("ALARM_ROOT", getALARM_ROOT(elt));	
		addOPImacros(linkingContainer, elt);	
	
		// adding the linking container to the navigation widget
		getGeneralNavigationContainer().addChildToRight(linkingContainer);
		
		// setting the navigation button properties
	 	var button = getGeneralNavigationContainer().getWidget(cbs_name);	
	 	button.setPropertyValue("height", getGeneralNavigationContainerHeight());
	 	button.setPropertyValue("width", getGeneralNavigationContainerWidth()/3);
		setButton(button, elt);
	}
}

function createHomeButtonContainer() {
	var height = getGeneralNavigationContainerHeight();
	var width = getGeneralNavigationContainerWidth()/3;
	return createButtonContainer("NavigationHomeButton.opi", width, height);
}

function addUpButton(elt) {
	// creating the linking container that display the Up button
	var linkingContainer = createUpButtonContainer();
	if (linkingContainer) {			
	    // reading attribute from element using JDOM
	    // adding macros to the container
		linkingContainer.addMacro("CBS", elt.getAttributeValue("name").toUpperCase());	
		linkingContainer.addMacro("CBS_PATH", path.toUpperCase());	
		linkingContainer.addMacro("TITLE", getDescription(elt).toUpperCase());	
		linkingContainer.addMacro(opi_macro, getOPI_FILE(elt));	
		linkingContainer.addMacro("ALARM_ROOT", getALARM_ROOT(elt));	
		addOPImacros(linkingContainer, elt);	
	
		// adding the linking container to the navigation widget
		getGeneralNavigationContainer().addChildToRight(linkingContainer);
	
		// setting the navigation button properties
	 	var button = getGeneralNavigationContainer().getWidget(path.toUpperCase());	
	 	button.setPropertyValue("height", getGeneralNavigationContainerHeight());
	 	button.setPropertyValue("width", (getGeneralNavigationContainerWidth()*2/3)/5);
		setButton(button, elt);
	}
}

function createUpButtonContainer() {
	var height = getGeneralNavigationContainerHeight();
	var width = getGeneralNavigationContainerWidth();
	var home = getGeneralNavigationContainerWidth()/3;
	return createButtonContainer("NavigationUpButton.opi", (width - home)/5, height);
}

function addMimicButtons(root) {
	var cbs = root.getChildren();
	
	var container = getMimicNavigationContainer();
			
	var nbButtons = cbs.size();
	var nbButtonsPerLine = 10;
			
	// default button size for maximum 10 buttons on one line
	var height = getMimicNavigationContainerHeight();
	var width = (getMimicNavigationContainerWidth()/nbButtonsPerLine)-1;
	
	// if more than 10 buttons required, 2 lines of buttons are needed
	if (nbButtons > nbButtonsPerLine) {
		container = getLineOneNavigationContainer();
		height = height/2;
		// if more than 20 buttons, they have to fit on 2 lines
		if (nbButtons > (2 * nbButtonsPerLine)) {
			nbButtonsPerLine = Math.ceil(nbButtons/2);
			width = getMimicNavigationContainerWidth()/nbButtonsPerLine - 1;
		}
	} else {
		// cleaning the one and two line child containers are they are not
		// needed
		container.removeAllChildren();
	}

	// iterating over all children in XML
	var i = 0;
	var itr = cbs.iterator();
	while (itr.hasNext()) {
	    var elt = itr.next();
	    
	    if (elt.getAttributeValue("name")) {
			// creating the linking container that display the mimic specific
			// buttons
			var linkingContainer = createButtonContainer("NavigationMimicButton.opi", width, height);
			
			if (i == nbButtonsPerLine) {
				container = getLineTwoNavigationContainer();
			}
			
		    // reading attribute from element using JDOM
		    // adding macros to the container
			linkingContainer.addMacro("CBS", elt.getAttributeValue("name").toUpperCase());	
			linkingContainer.addMacro("CBS_PATH", path.toUpperCase());	
			linkingContainer.addMacro("TITLE", getDescription(elt).toUpperCase());	
			linkingContainer.addMacro(opi_macro, getOPI_FILE(elt));
			linkingContainer.addMacro("ALARM_ROOT", getALARM_ROOT(elt));	
			addOPImacros(linkingContainer, elt);	
		
			// adding the linking container to the navigation widget
			container.addChildToRight(linkingContainer);
		
		    // reading value of children in XML using JDOM
			// setting navigation button properties
		 	var button = container.getWidget(elt.getAttributeValue("name").toUpperCase());	
		 	button.setPropertyValue("height", height);
		 	button.setPropertyValue("width", width);
			setButton(button, elt);
	
		 	i+=1;
		 }
	}
}

function disableMimicButton(CBS) {
 	var button = getMimicNavigationContainer().getWidget(CBS);	
 	button.setPropertyValue("enabled", "false");	
}

function getOPI_FILE(elt) {
	var attribute = "";
	if (opi_type == "USER") {
		// getting the opi file from the navigation xml configuration file
		attribute = elt.getAttributeValue("opi_file");
	
		if (attribute && attribute.search(".opi") > 0) {
	    	// suppressing the extension .opi
	    	return attribute.substring(0, attribute.search(".opi"));
	    }
	    return attribute;
    } else {
    	// getting the opi file from a macro
		attribute = widget.getMacroValue("OPI_FILE");
	    return attribute;
    }
}

function getALARM_ROOT(elt) {
    return elt.getAttributeValue("alarm_root");
}

function getDescription(elt) {
    return elt.getAttributeValue("description");
}

function addOPImacros(container, elt) {
	// getting the opi file from the navigation xml configuration file (even for
	// alarms list)
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

function createButtonContainer(opi_container, width, height) {
		var linkingContainer = WidgetUtil.createWidgetModel("org.csstudio.opibuilder.widgets.linkingContainer");
			
		linkingContainer.setPropertyValue("opi_file", opi_container);
		linkingContainer.setPropertyValue("border_style", 0);
		linkingContainer.setPropertyValue("width", width);
		linkingContainer.setPropertyValue("height", height);
		linkingContainer.setPropertyValue("resize_behaviour", 2);

		return linkingContainer;
}

function setButton(button, elt) {
    // reading value of children in XML using JDOM
	// setting navigation button properties
 	button.setPropertyValue("tooltip", elt.getAttributeValue("description") + " ($(number_alarms) alarm(s))");
 	button.setPropertyValue("enabled", elt.getAttributeValue("enabled"));
 	if (elt.getAttributeValue("deprecated") && elt.getAttributeValue("deprecated").search("true") >= 0) {
 		button.setPropertyValue("border_style", 9);
	 	button.setPropertyValue("tooltip", elt.getAttributeValue("description") + " (deprecated)");
	}	
}
