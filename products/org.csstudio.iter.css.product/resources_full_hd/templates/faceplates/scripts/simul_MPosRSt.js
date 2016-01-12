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

var request    = PVUtil.getDouble(pvs[0]);
var init_value = PVUtil.getDouble(pvs[1]);
var feedback   = pvs[1];
var flag       = PVUtil.getLong(pvs[2]);
var OK         = 1;

if (flag == OK && request != init_value) {
	feedback.setValue(request);
}
