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

var 
	request     = PVUtil.getDouble(pvs[0]),
	request_pv  = pvs[0],
	init_value  = PVUtil.getDouble(pvs[1]),
	high        = PVUtil.getDouble(pvs[2]),
	low         = PVUtil.getDouble(pvs[3]),
	flag        = pvs[4],
	OK          = 1,
	UNCHANGED	= 2,
	NOK         = 0,
	BADINPUT	= -1,
	RANGEOVER	= -2,
	RANGEUNDER	= -3,
	valid		= UNCHANGED;

// if value change
if (request != init_value) {

	// check if the new input is valid
	valid = validate();
}

//Fire a validation event
flag.setValue(valid);	

// Validation Checking of the Input Field
function validate() {
	var valid = OK;
	
	// Check bad input
	if (isNaN(request)) {
			valid = BADINPUT;	
	} else {
		// Check out of range input
		if (request > high || request < low) {
			valid = RANGEOVER;	
		}
	}
	return valid;			
}
