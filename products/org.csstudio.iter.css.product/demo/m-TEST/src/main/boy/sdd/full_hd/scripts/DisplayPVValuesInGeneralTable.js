importClass(Packages.org.csstudio.opibuilder.scriptUtil.PVUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.ColorFontUtil);


var table = widget.getTable();

//Fill PV Name only once
if (widget.getVar("firstTime") == null)
{
    widget.setVar("firstTime", true);
    
    for (var i=0;pv=pvs[i];i++) {
	// earlier when by default pv.getName() was giving name with 'epics://' prefix. Ripping it off before showing was done below
        table.setCellText(i, 0, pv.getName().trim())
        if (!pv.isConnected()) {
		table.setCellText(i, 1, "Disconnected");
	}
    }
    // Based on value of macro SHOW_PLC_IOC, enable visibility of PLCIOCDetailsTable 
    if (widget.getPropertyValue("name") == 'PLCIOCDetailsTable') {
	if (display.getMacroValue("SHOW_PLC_IOC") == "true") {
	    widget.setPropertyValue("visible", "true");
            display.getWidget("PLCIOCDetailsLabel").setPropertyValue("visible", "true");
	}
    }
}
    

//find index of the trigger PV
var i=0;
while (triggerPV != pvs[i]) {
    i+=1;
}

table.setCellText(i, 1, PVUtil.getString(triggerPV).trim());
table.setCellText(i, 2, PVUtil.getStatus(triggerPV).trim());
table.setCellText(i, 3, PVUtil.getSeverityString(triggerPV).trim());

var s = PVUtil.getSeverity(triggerPV);

var color = ColorFontUtil.WHITE
color = ColorFontUtil.WHITE;
if( s == 0) {
    color = ColorFontUtil.GREEN;
}
else if( s == 1) {
    color = ColorFontUtil.RED;
}
else if( s == 2) {
    color = ColorFontUtil.YELLOW;
}
else if( s == 3) {
    color = ColorFontUtil.PINK;
}   
table.setCellBackground(i, 3, color);
