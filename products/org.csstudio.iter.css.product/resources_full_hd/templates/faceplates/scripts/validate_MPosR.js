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
importPackage(Packages.org.csstudio.platform.data);
importPackage(Packages.org.eclipse.jface.dialogs);

var request    = PVUtil.getDouble(pvs[0]);
var init_value = PVUtil.getDouble(pvs[1]);
var high       = PVUtil.getDouble(pvs[2]);
var low        = PVUtil.getDouble(pvs[3]);
var flag       = pvs[4];
var OK         = 1;
var NOK        = 0;


// value change
if (request != init_value) {
	// Check out of range input
	if (request > high) {
		MessageDialog.openError(
			null, "Input Validation Script", "Position request of " + request + " is more than Max Range of " + high);
			flag.setValue(NOK);	
	} else if (request == high) {
		MessageDialog.openWarning(
			null, "Input Validation Script", "Position request of " + request + " is equal to Max Range of " +high)
			flag.setValue(NOK);	
	} else if (request == low) {
		MessageDialog.openWarning(
			null, "Input Validation Script", "Position request of " + request + " is equal to Min Range of " +low)
			flag.setValue(NOK);	
	} else if (request < low) {
		MessageDialog.openError(
			null, "Input Validation Script", "Position request of " + request + " is less than Min Range of " + low)
			flag.setValue(NOK);	
	} else {
		MessageDialog.openInformation(
			null, "Validation against input limits", "Validation successful against Min " + low + " - Max " + high);
			flag.setValue(OK);	
	}			
}