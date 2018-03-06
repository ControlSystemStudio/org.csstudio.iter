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
V1.0 Feb 2016 - initialization of number when -1
pvs[0]		: input field PV
pvs[1]		: PV to be read to initialise the input field
pvs[2]		: input field flag

V2.0 June 2016 - support of number and text initialization via pvs[3] (default: number).
	Executed when flag == NOK (no more on -1 value)
*/

importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
importPackage(Packages.org.csstudio.platform.data);


var	flag		= pvs[2],
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
	

// If none, set an initial value
if (flag_value == NOK) {
	
	// Input type: number, text
	var	input_type	= "number";	
	try {
		input_type	= PVUtil.getString(pvs[3]);
	}
	catch(err) {
	// If input_type is not defined, a number is assumed
	}
		
	var input		= pvs[0],
		init		= pvs[1];
	
	// Depending of the input type
	if (input_type == "number") {
			var input_value	= PVUtil.getDouble(input);
			var init_value	= PVUtil.getDouble(init);
	} else if (input_type == "text") {
			var input_value	= PVUtil.getString(input);
			var init_value	= PVUtil.getString(init);
	}
	
	input.setValue(init_value);
	flag.setValue(OK);	
}