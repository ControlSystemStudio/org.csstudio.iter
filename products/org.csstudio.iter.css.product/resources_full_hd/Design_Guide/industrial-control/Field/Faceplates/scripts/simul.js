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
importPackage(Packages.java.lang);

var request    = PVUtil.getDouble(pvs[0]);
var feedback   = pvs[1];
var init_value = PVUtil.getDouble(pvs[1]);
var status	   = PVUtil.getLong(pvs[2]);
var Canceled   = 3;


runnable = {
	run:function() {	
		var i = (request < init_value) ? -1 : 1;
		var position = init_value;
		
		//Cancel operation may change the request
		//In this case, the submitted action is terminated
		while (position != request) {
			if(!display.isActive())
				return;			
			Thread.sleep(1000);
			
			//If request has been canceled, the simulation has to be undone
			if (status == Canceled) {
				i = (request < init_value) ? -1 : 1;
			}
			position +=i;
			feedback.setValue(position);
		}
	}	
};		

//The script can be triggered by a Cancel action
//Requested value should be different from the initial one, otherwise nothing to do
if (status != Canceled && request != init_value) {
	new Thread(new Runnable(runnable)).start();
}
