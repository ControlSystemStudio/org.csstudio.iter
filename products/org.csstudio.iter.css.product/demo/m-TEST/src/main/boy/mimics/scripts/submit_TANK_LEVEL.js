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
importPackage(Packages.org.csstudio.platform.data);
importPackage(Packages.org.eclipse.jface.dialogs);

var 
	Submitted         	= 2,
	new_value,old_value	= 0,
	OK          = 1,
	NOK         = 0,
	UNCHANGED	= 2,
	BADINPUT	= -1,
	RANGEOVER	= -2,
	RANGEUNDER	= -3,
	valid		= NOK;

// Validate all input fields
valid = validateForm();

if (valid) {

	//First PV is the trigger PV - Submit button has been clicked on
	i = 1;
	
	//For all input PVs send the new value
	while (i < pvs.length) {
		
		new_value = PVUtil.getDouble(pvs[i]);
		old_value = PVUtil.getDouble(pvs[i+1]);
		flag	  = PVUtil.getLong(pvs[i+3]);
		
		//Only if the value has to be changed
		if (flag != UNCHANGED) {
	
			//Backup the old value
			pvs[i+2].setValue(old_value);
			
			//Write the new value
			pvs[i+1].setValue(new_value);
			
			//Update the status to Submitted
			pvs[i+3].setValue(Submitted);
		
		}
	
		i+=4;
	}
} else {
	displayErrorMsg();
}

function validateForm() {
	var 
		valid	= OK,
		i 		= 1;
	
	// For all input fields
	// Check the validity as no additional validation is required here
	while (i < pvs.length && valid) {
		valid = checkValidity(pvs[i+3]);
		i+=4;
	}
	return valid;
}

function reportValidity(pv) {
	return PVUtil.getDouble(pv);
}

function checkValidity(pv) {
	return (PVUtil.getDouble(pv) > 0 ? true : false);
}

function displayErrorMsg() {
	var 
		valid		= -1,
		errorMsg	= new Array(),
		popupMsg	= "",
		pv_name		= "",
		i 			= 1;
	
	// For all invalid input
	// Generate an error message
	while (i < pvs.length) {
		valid = reportValidity(pvs[i+3]);
		if (valid != OK && valid != UNCHANGED) {
			switch(valid) {
				case BADINPUT:
					errorMsg.push("Input for " + pvs[i+1].getName() + " is invalid and cannot be converted to a value" + "\n");
					break;
				case RANGEOVER:
					errorMsg.push("Input for " + pvs[i+1].getName() + " is invalid as greater than the trip setpoint" + "\n");
					break;
				default:
					errorMsg.push("Invalid input value for " + pvs[i+1].getName() + "\n");
			}
		}
		i+=4;
	}
	
	// in case of error, display an error popup window
	if (errorMsg.length > 0) {
		errorMsg.forEach(function(message) {
			popupMsg += message;
		});
		
		MessageDialog.openError(null, "Input Validation Script Error", popupMsg)
	}
}

