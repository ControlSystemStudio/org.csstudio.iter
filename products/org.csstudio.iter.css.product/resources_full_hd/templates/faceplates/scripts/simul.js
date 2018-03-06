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
V1.0 Feb 2016 - readback simulated when no I/O connected
pvs[0]		: input PV
pvs[1]		: readback PV
pvs[2]		: input field flag

V2.0 June 2016 - ...
*/

importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
importPackage(Packages.java.lang);

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

var	flag		= pvs[2],
	flag_value	= PVUtil.getLong(flag);

var input			= pvs[0],
	input_value		= PVUtil.getDouble(input),
	readback		= pvs[1], 
	readback_value	= PVUtil.getDouble(readback);

var init_value = PVUtil.getDouble(readback);

runnable = {
	run:function() {	
		var i = (input_value < init_value) ? -1 : 1;
		var position = init_value;
		
		//Cancel operation may change the input
		//In this case, the submitted action is terminated
		while (position != input_value) {
			if(!display.isActive())
				return;			
			Thread.sleep(1000);
			
			//If input has been canceled, the simulation has to be undone
			if (flag_value == CANCELED) {
				i = (input_value < init_value) ? -1 : 1;
			}
			position +=i;
			readback.setValue(position);
		}
	}	
};		

//The script can be triggered by a Cancel action
//Requested value should be different from the initial one, otherwise nothing to do
if (flag_value != CANCELED && input_value != init_value) {
	new Thread(new Runnable(runnable)).start();
}
