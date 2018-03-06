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
V1.0 Feb 2016 - ends when readback == input
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

// Is the required value reached?
if ((flag == SUBMITTED || flag == CANCELED) && readback_value == input_value) {
	flag.setValue(ENDED);	
}