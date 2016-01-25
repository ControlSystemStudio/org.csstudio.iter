importClass(Packages.org.csstudio.opibuilder.scriptUtil.PVUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.ColorFontUtil);
// from org.csstudio.opibuilder.scriptUtil import ConsoleUtil

var func = display.getPropertyValue("name");
var type = widget.getPropertyValue("name");
var widgetType = "ellipse";
var varName = "XXXXXXX";

if (type.indexOf("PSH") != -1) {
    varName = "-SYSHLTS";
}
if (type.indexOf("PCF") != -1) {
    varName = "-SYSHLTS";
}
if (type.indexOf("SRV") != -1) {
    varName = "-SYSHLTS";
}
if (type.indexOf("PLC") != -1) {
    varName = "-PLCHLTS";
}
if (type.indexOf("COM") != -1) {
    varName = "-SYSHLTS";
}
if (type.indexOf("CHS") != -1) {
    varName = "-SYSHLTS";
}
// if ("IOM" in type.indexOf() != -1) {
//     varName = "-BS";
if (type.indexOf("CUB") != -1) {
    varName = "-CUBHLTS";
}
if (type.indexOf("Box") != -1) {
    widgetType = "rectangle";
}

if (triggerPV.getName().indexOf(varName) != -1) {
//             ConsoleUtil.writeInfo("Trigger PV found) { " +triggerPV.getName());
            
            var s = PVUtil.getSeverity(triggerPV);

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
            
            if ("ellipse" == widgetType) {
                widget.setPropertyValue("foreground_color", color);
	    }    
                
            var tooltip = PVUtil.getString(triggerPV);
            widget.setPropertyValue("tooltip", tooltip);
}

if (type.indexOf("IOM") != -1) {
	if (triggerPV.getName().indexOf(".SIMM") == -1) {
		var s = PVUtil.getSeverity(triggerPV);
		var color = ColorFontUtil.WHITE;
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
	    	else if( s == 4) {
		    color = ColorFontUtil.GREEN;
	    	} 
	    
		widget.setPropertyValue("foreground_color", color);
		
		var tooltip = PVUtil.getString(triggerPV);
		widget.setPropertyValue("tooltip", tooltip);
    	}
}
    
        

