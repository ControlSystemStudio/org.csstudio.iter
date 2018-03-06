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

	//Getting the previous selected component
	try {
		var old_component = display.getWidget(PVUtil.getString(pvs[1]));
		if (old_component && display.getWidget(old_component)) {
			old_component.setPropertyValue("border_style", 0);
		}
	}
    catch (err) {
	}       

	//Getting the faceplate opi from the trigger PV
	var opiInput = PVUtil.getString(pvs[0]);
	if (opiInput.length() < 1) {
		//Getting the legend faceplate
		opiInput = widget.getMacroValue("LEGEND");
		
		if (opiInput == null) {
			//Setting the default faceplate to the legend provided in templates
			opiInput = "Legend.opi";
		}
	}

	//Splitting the faceplate variable to extract opi name and macros
	var words = opiInput.match(/(?:[^\s"]+|"[^"]*")+/g);
	
	//Creating a new Macro Input
	var macroInput = DataUtil.createMacrosInput(true);
	
	//Analysing each word
	var i=0;
	for (i in words) {
			
		if (words[i].search(".opi") > 0) {
			//OPI file
			opiInput = words[i];
			//Reloading the OPI file in the linking container again 
			//by setting the property value with forcing fire option in true.
			widgetController.setPropertyValue("opi_file", opiInput, true);
		} else {
	
			var macro = words[i].split("=", 2);
			if (macro.length > 1) {
	
				//Putting a new macro in Macro Input
				macroInput.put(macro[0], macro[1]);
				   
				//Highlighting and memorizing the selected component
				try {
				    var component = display.getWidget(macro[1]);	
					if (component) {
							component.setPropertyValue("border_style", 7);
							pvs[1].setValue(macro[1]);
					}
				}
			    catch (err) {
				}       
			}
		}
	}

	//Set the macro input of the linking container to this new macro input.
	widgetController.setPropertyValue("macros", macroInput);

	//Reload the OPI file in the linking container again 
	//by setting the property value with forcing fire option in true.
	widgetController.setPropertyValue("opi_file", opiInput, true);
