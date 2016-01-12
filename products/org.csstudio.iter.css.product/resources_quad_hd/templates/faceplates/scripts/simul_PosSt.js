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
importPackage(Packages.java.lang);

var request    = PVUtil.getDouble(pvs[0]);
var feedback   = pvs[1];
var init_value = PVUtil.getDouble(pvs[1]);
var flag       = PVUtil.getLong(pvs[2]);
var OK         = 1;


runnable = {
	run:function()
		{	
			if (flag == OK && request != init_value) {
				var i = (request < init_value) ? -1 : 1;
				var position = init_value;
				
				while (position != request) {
					if(!display.isActive())
						return;			
					Thread.sleep(1000);
					
					position +=i;
					feedback.setValue(position);	
				}
			}		
		}	
	};		

new Thread(new Runnable(runnable)).start();
