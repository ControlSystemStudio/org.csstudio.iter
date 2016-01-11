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

var submit     = PVUtil.getDouble(pvs[0]);
var request    = PVUtil.getDouble(pvs[1]);
var init_value = PVUtil.getDouble(pvs[2]);
var flag       = PVUtil.getLong(pvs[3]);
var on         = PVUtil.getDouble(pvs[4]);
var off        = PVUtil.getDouble(pvs[5]);
var status     = pvs[6];
var request_pv = pvs[7];
var OK         = 1;
var NOK        = 0;
var position   = "unknown";
var confirm    = false;

// If validation was OK
if (flag == OK) {
	// If value has changed
	if (request != init_value) {
		if (request >= on) {
			position = "open";
			confirm = GUIUtil.openConfirmDialog("Are you sure you want to open the component?");
			if (confirm) {
				status.setValue(OK);
				request_pv.setValue(request);	
			} else {
				status.setValue(NOK);	
			}
		} else if (request <= off) {
			position = "close";
			confirm = GUIUtil.openConfirmDialog("Are you sure you want to close the component?");
			if (confirm) {
				status.setValue(OK);	
				request_pv.setValue(request);	
			} else {
				status.setValue(NOK);	
			}
		} else {
			MessageDialog.openInformation(
				null, "Input Submission Script", "Position request of " + request + " is nor ON nor OFF. Submission canceled.")
			status.setValue(NOK);	
		}
	} else {
		MessageDialog.openWarning(
			null, "Input Submission Script", "Position request of " + request + " is unchanged. Submission canceled.")
		status.setValue(NOK);	
	}
} else {
	MessageDialog.openError(
		null, "Input Submission Script", "Position request of " + request + " is not valid. Submission canceled");
	status.setValue(NOK);	
}
