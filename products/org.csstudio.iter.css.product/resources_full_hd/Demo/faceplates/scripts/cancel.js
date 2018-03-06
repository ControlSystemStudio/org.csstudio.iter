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

var Submitted        = 2;
var Canceled         = 3;
var flag	 		 = 0;

//First PV is the trigger PV - Submit button has been clicked on
i = 1;

//For all input PVs send the new value
while (i < pvs.length) {
	
	flag = PVUtil.getLong(pvs[i+3]);
	
	//Only if a new value was submitted
	if (flag == Submitted) {
			
		//Write the previous value
		pvs[i+1].setValue(PVUtil.getDouble(pvs[i+2]));
		
		//Backup the canceled value
		pvs[i+2].setValue(PVUtil.getDouble(pvs[i]));
		
		//Update the status to Canceled
		pvs[i+3].setValue(Canceled);
	
	}
	
	i+=4;
}
