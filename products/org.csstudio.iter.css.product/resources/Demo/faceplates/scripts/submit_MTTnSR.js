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
importPackage(Packages.org.csstudio.platform.data);
importPackage(Packages.org.eclipse.jface.dialogs);

var submit     = PVUtil.getLong(pvs[0]);
var OK         = 1;
var NOK        = 0;
var confirm    = false;
var flag	   = OK;

//first PV is the trigger Submit PV
i = 1;
//check if all entry fields have been validated
while (i < pvs.length) {
//	flag = flag && PVUtil.getLong(pvs[i+2]);
	if (PVUtil.getLong(pvs[i+2]) == NOK) {
		flag = NOK;
	}
	i+=3;
}

// If validation was OK
if (flag == OK) {
	confirm = GUIUtil.openConfirmDialog("Are you sure you want to change the alarm setpoints?");
	if (confirm) {
		i = 1;
		//check if all entry fields have been validated
		while (i < pvs.length) {
			pvs[i+1].setValue(PVUtil.getDouble(pvs[i]));
			i+=3;
		}
	}
} else {
	MessageDialog.openError(
		null, "Input Submission", "Some entries are invalid. Submission canceled");

}