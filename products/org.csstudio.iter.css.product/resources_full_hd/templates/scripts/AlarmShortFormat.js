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

var t = new Date(PVUtil.getTimeInMilliseconds(pvs[0])).toTimeString();
var st = t.split(" ");

widget.setPropertyValue("on_label", st[0]);
widget.setPropertyValue("off_label", st[0]);
