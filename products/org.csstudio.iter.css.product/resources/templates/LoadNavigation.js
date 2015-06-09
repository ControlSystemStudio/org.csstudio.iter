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
importPackage(Packages.java.lang)

	// getting the current cbs level for this screen
	var current_cbs = widget.getMacroValue("LEVEL");
	var depth = getLevelDepth(current_cbs);
	
	// getting the input xml file path
	var xml_input = widget.getMacroValue("INPUT");
	// getting the navigation xml file from the user navigation folder
	if (xml_input == null) {
		xml_input = "../navigation/Navigation.xml";
		// getting the default navigation xml file from the templates
		if (xml_input == null) {
			xml_input = "Navigation.xml";
		}
	}
	// loading XML document and getting the root element
	// the result is a JDOM Element
	var root = FileUtil.loadXMLFile(xml_input, widget);

	// browsing the CBS tree structure starting from 0 to max depth
	listCBS(root, 0, depth);

// ---

// recursive list function on CBS tree
function listCBS(current, depth, max_depth){
	var cbs = current.getChildren();	
	var itr = cbs.iterator();
	while (itr.hasNext() && depth <= max_depth) {
	    var elt = itr.next();
	    if (isEltCurrentCBS(elt, current_cbs)) {
		    if (depth == 0){
			    // adding the Home button for level 0 - ITER overview
		    	addHomeButton(elt);
		    } else {
		    	// adding an Up button for intermediate levels
		    	addUpButton(elt);
		    }
		    
	    	if (depth == max_depth) {
	    		// adding mimic specific levels for the last level
			    addMimicButtons(elt);
	    	}
	    	listCBS(elt, depth+1, max_depth);
	    	depth = max_depth + 1;
    	}
	}
}

function getLevelDepth(level) {
	if (level == "" || !level) {
		// CBS 0 is not specified or empty name
		return 0;
	}
	// getting the number of levels 
	// for instance UTIL-S15-AG07 has 3 CBS levels - ITER is CBS0
	var words = level.split("-");
	return words.length - 1;
}

function isEltCurrentCBS(elt, level) {
	if (getLevelDepth(level) == 0) {
		return true;
	}
	if (level == "" || !level || !elt.getAttributeValue("name")) {
		return false;
	}
	var currentLevel = elt.getAttributeValue("name");
	var words = level.split("-");
	var i = 0;
	for (i in words) {
	  if (words[i].search(currentLevel) >= 0) {
	  	return true;
	  }
	 }
	 return false;
}

function getGeneralNavigationContainer() {
	var container = display.getWidget("GENERAL NAVIGATION BAR");
	return container;
}

function getGeneralNavigationContainerHeight() {
	var container = getGeneralNavigationContainer();
	return container.getPropertyValue("height");
}

function getGeneralNavigationContainerWidth() {
	var container = getGeneralNavigationContainer();
	return container.getPropertyValue("width");
}

function getMimicNavigationContainer() {
	return widget;
}

function getMimicNavigationContainerHeight() {
	return widget.getPropertyValue("height");
}

function getMimicNavigationContainerWidth() {
	return widget.getPropertyValue("width");
}

function getLineOneNavigationContainer() {
	var container = getMimicNavigationContainer().getWidget("OneLineNavigation");
	return container;
}

function getLineTwoNavigationContainer() {
	var container = getMimicNavigationContainer().getWidget("TwoLineNavigation");
	return container;
}

function addHomeButton(elt) {
	var cbs_name = !elt.getAttributeValue("name") ? "ITER" : elt.getAttributeValue("name");
	
	//creating the linking container that display the HOME button
	var linkingContainer = createHomeButtonContainer();
	
    // reading attribute from element using JDOM
    // adding macros CBS and OPI_FILE to the container
	linkingContainer.addMacro("CBS", cbs_name);	
	linkingContainer.addMacro("OPI_FILE", getOPI_FILE(elt));	
	addOPImacros(linkingContainer, elt);	

	// adding the linking container to the navigation widget
	var container = getGeneralNavigationContainer();
	container.addChildToRight(linkingContainer);
	
	// setting the navigation button properties
 	var button = container.getWidget(cbs_name);	
	setButton(button, elt);
}

function createHomeButtonContainer() {
	height = getGeneralNavigationContainerHeight();
	width = getMimicNavigationContainerWidth();
	return createButtonContainer("NavigationHomeButton.opi", width/10, height);
}

function addUpButton(elt) {
	//creating the linking container that display the Up button
	var linkingContainer = createUpButtonContainer();
				
    // reading attribute from element using JDOM
    // adding macros CBS and OPI_FILE to the container
	linkingContainer.addMacro("CBS", elt.getAttributeValue("name"));	
	linkingContainer.addMacro("OPI_FILE", getOPI_FILE(elt));	
	addOPImacros(linkingContainer, elt);	

	// adding the linking container to the navigation widget
	var container = getGeneralNavigationContainer();
	container.addChildToRight(linkingContainer);

	// setting the navigation button properties
 	var button = container.getWidget(elt.getAttributeValue("name"));	
	setButton(button, elt);
}

function createUpButtonContainer() {
	height = getGeneralNavigationContainerHeight();
	width = getGeneralNavigationContainerWidth();
	home = getMimicNavigationContainerWidth()/10;
	return createButtonContainer("NavigationUpButton.opi", (width - home)/5, height);
}

function addMimicButtons(root) {
	var cbs = root.getChildren();

	// iterating over all children in XML
	var i = 0;
	var itr = cbs.iterator();
	while (itr.hasNext()) {
	    var elt = itr.next();
	    
	    if (elt.getAttributeValue("name")) {
			container = getMimicNavigationContainer();
			height = getMimicNavigationContainerHeight();
			width = getMimicNavigationContainerWidth();
			
			if (cbs.size() <= 10) {
				//creating the linking container that display the mimic specific buttons
				var linkingContainer = createButtonContainer("NavigationMimicButton.opi", width/10, height);
				// cleaning first the one and two line child containers
				if (i == 0) {
					container.removeAllChildren();
				} 
			} else {
				if (i < 10) {
					container = getLineOneNavigationContainer();
				} else {
					container = getLineTwoNavigationContainer();
				}
				var linkingContainer = createButtonContainer("NavigationMimicHalfButton.opi", width/10, height/2);
			}
			
		    // reading attribute from element using JDOM
		    // adding macros CBS and OPI_FILE to the container
			linkingContainer.addMacro("CBS", elt.getAttributeValue("name"));	
			linkingContainer.addMacro("OPI_FILE", getOPI_FILE(elt));
			addOPImacros(linkingContainer, elt);	
		
			// adding the linking container to the navigation widget
			container.addChildToRight(linkingContainer);
		
		    // reading value of children in XML using JDOM
			// setting navigation button properties 
		 	var button = container.getWidget(elt.getAttributeValue("name"));	
			setButton(button, elt);
	
		 	i+=1;
		 }
	}
}

function getOPI_FILE(elt) {
	var attribute = elt.getAttributeValue("opi_file");

	if (attribute && attribute.search(".opi") > 0) {
    	// suppressing the extension .opi
    	return attribute.substring(0, attribute.search(".opi"));
    }
    return attribute;
}

function addOPImacros(container, elt) {
	var attribute = elt.getAttributeValue("opi_file");
	if (attribute) {
		var words = attribute.split(" ");
	
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

		return linkingContainer;
}

function setButton(button, elt) {
    // reading value of children in XML using JDOM
	// setting navigation button properties 
 	button.setPropertyValue("tooltip", elt.getAttributeValue("description"));
 	button.setPropertyValue("enabled", elt.getAttributeValue("enabled"));
 	if (elt.getAttributeValue("deprecated") && elt.getAttributeValue("deprecated").search("true") >= 0) {
 		button.setPropertyValue("foreground_color", "Invalid");
	 	button.setPropertyValue("tooltip", elt.getAttributeValue("description") + " (deprecated)");
	}	
}
