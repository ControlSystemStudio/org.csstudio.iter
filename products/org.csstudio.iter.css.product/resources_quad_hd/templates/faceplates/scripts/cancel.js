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
V1.0 Feb 2016 - cancel the submitted input
pvs[0]		: the first PV is the trigger PV - Cancel button has been clicked on
pvs[i+1]	: input field PV
pvs[i+2]	: PV to be written
pvs[i+3]	: PV value backup
pvs[i+4]	: input field flag

V2.0 June 2016 - ...
*/

importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
importPackage(Packages.org.csstudio.platform.data);
importPackage(Packages.org.eclipse.jface.dialogs);

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

var flag	 	= NOK;

//First PV is the trigger PV - Submit button has been clicked on
i = 1;

//For all input PVs send the new value
while (i < pvs.length) {
	
	flag_value = PVUtil.getLong(pvs[i+3]);
	
	//Only if a new value was submitted
	if (flag_value == SUBMITTED) {
			
		//Restore the previous value
		pvs[i+1].setValue(PVUtil.getDouble(pvs[i+2]));
		
		//Backup the canceled value
		pvs[i+2].setValue(PVUtil.getDouble(pvs[i]));
		
		//Update the status to CANCELED
		pvs[i+3].setValue(CANCELED);
	
	}
	
	i+=4;
}
