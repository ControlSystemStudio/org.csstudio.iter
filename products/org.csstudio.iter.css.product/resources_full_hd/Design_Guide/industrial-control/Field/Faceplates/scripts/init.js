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

var request_value = PVUtil.getDouble(pvs[0]);
var request       = pvs[0];
var init_value    = PVUtil.getDouble(pvs[1]);
var null_value    = -1;

// If none, set an initial value
if (request_value == null_value) {
	request.setValue(init_value);
}
