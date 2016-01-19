importClass(Packages.org.csstudio.opibuilder.scriptUtil.PVUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.ColorFontUtil);
importPackage(Packages.org.csstudio.opibuilder.scriptUtil.*);

var table = widget.getTable();
var func = display.getPropertyValue("name");

var i = 0;
var row = 0;
var col = 3;
// ConsoleUtil.writeInfo("Trigger PV : " + triggerPV.getName());
while (triggerPV != pvs[i]) {
//     ConsoleUtil.writeInfo("pvs[i] : " + pvs[i].getName());
    if (col == 5) {
        if (pvs[i+1].getName().indexOf("PLC-IOCHLTS") != -1) {
            col = col+1;
        }
        else {
            col = 3
            row = row+1;
        }
    }
    else if (col == 3) {
        if ( (pvs[i+1].getName().indexOf("-SYSHLTS") != -1) ||  (pvs[i+1].getName().indexOf("-HLTS") != -1)) {
            col = 3;
            row = row+1;
        }
        else if (pvs[i+1].getName().indexOf("-IOCHLTS") != -1) {
            if (pvs[i+1].getName().indexOf("CORE-IOCHLTS") != -1) {
                col = 4;
            }
            else {
                col = 5;
            }
		}
	    else {
	       col += 1;
	       if (col > 5) {
	           row += 1;
	           col = 3;
	       }
		}
    }
    else {
        col += 1;
        if (col > 6) {
           row += 1;
           col = 3;
        }
    }
    i += 1;
}

table.setCellText(row, col, PVUtil.getString(triggerPV))
    
var s = PVUtil.getSeverity(triggerPV)
    
color = ColorFontUtil.WHITE
if( s == 0) {
    color = ColorFontUtil.GREEN
}
else if( s == 1) {
    color = ColorFontUtil.RED
}
else if( s == 2) {
    color = ColorFontUtil.YELLOW
}
else if( s == 3) {
    color = ColorFontUtil.PINK
}
else if( s == 3) {
    color = ColorFontUtil.PINK
}
table.setCellBackground(row, col, color)
