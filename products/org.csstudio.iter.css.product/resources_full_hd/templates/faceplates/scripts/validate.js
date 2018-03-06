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

/*
V1.0 Feb 2016 - validation of number
pvs[0]		: input field PV
pvs[1]		: PV to be written once the input is validated
pvs[2]		: maximum
pvs[3]		: minimum
pvs[4]		: input field flag

V2.0 June 2016 - ...
*/

importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
importPackage(Packages.org.csstudio.platform.data);

var	flag		= pvs[4],
	flag_value	= PVUtil.getLong(flag);

// Input flag enumeration
var OK          = 1,
	UNCHANGED	= 2,
	SUBMITTED   = 3,
	CANCELED    = 4,
	ENDED       = 5,
	NOK         = 0,
	BADINPUT	= -1,
	RANGEOVER	= -2,
	RANGEUNDER	= -3;

var 
	input     	= pvs[0],
	input_value	= PVUtil.getDouble(input),
	init		= pvs[1],
	init_value  = PVUtil.getDouble(init),
	high        = pvs[2],
	high_value	= PVUtil.getDouble(high),
    low         = pvs[3], 
    low_value	= PVUtil.getDouble(low),
	valid		= UNCHANGED;

// Only if value has changed
if (input_value != init_value) {

	// check if the new input is valid
	valid = validate();
}

// Fire a validation event
flag.setValue(valid);	

// Validation Checking of the Input Field
function validate() {
	valid = OK;
	
	// Check bad input - Not a number (NaN)
	if (isNaN(input_value)) {
			valid = BADINPUT;	
	} else {
		// Check out of range input
		if (input_value > high_value) {
			valid = RANGEOVER;	
		} else if (input_value < low_value) {
			valid = RANGEUNDER;
		}
	}
	return valid;			
}
