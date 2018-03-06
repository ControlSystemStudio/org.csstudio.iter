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

var flag       = pvs[2];
var Ended      = 4;

var request    = PVUtil.getDouble(pvs[0]);
var feedback   = PVUtil.getDouble(pvs[1]);

if (feedback == request) {
	flag.setValue(Ended);	
}