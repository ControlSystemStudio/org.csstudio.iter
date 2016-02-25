from org.csstudio.opibuilder.scriptUtil import PVUtil
from org.csstudio.opibuilder.scriptUtil import ColorFontUtil
# from org.csstudio.opibuilder.scriptUtil import ConsoleUtil

func = display.getPropertyValue("name")
type = widget.getPropertyValue("name")
widgetType = "ellipse";
varName = "XXXXXXX";

if "PSH" in type:
    varName = "-SYSHLTS";
if "PCF" in type:
    varName = "-SYSHLTS";
if "SRV" in type:
    varName = "-SYSHLTS";
if "PLC" in type:
    varName = "-PLCHLTS";
if "COM" in type:
    varName = "-SYSHLTS";
if "CHS" in type:
    varName = "-SYSHLTS";
# if "IOM" in type:
#     varName = "-BS";
if "CUB" in type:
    varName = "-CUBHLTS";
if "Box" in type:
    widgetType = "rectangle";

if varName in triggerPV.getName():
#             ConsoleUtil.writeInfo("Trigger PV found: " +triggerPV.getName());
            
            s = PVUtil.getSeverity(triggerPV)

            color = ColorFontUtil.WHITE
            if s == 0:
                color = ColorFontUtil.GREEN
            elif s == 1:
                color = ColorFontUtil.RED
            elif s == 2:
                color = ColorFontUtil.YELLOW
            elif s == 3:
                color = ColorFontUtil.PINK
            elif s == 4:
                color = ColorFontUtil.GREEN   
            
            if "ellipse" == widgetType:
                widget.setPropertyValue("foreground_color", color)
                
            tooltip = PVUtil.getString(triggerPV)
            widget.setPropertyValue("tooltip", tooltip)

if "IOM" in type:
    if ".SIMM" not in triggerPV.getName():
        
        s = PVUtil.getSeverity(triggerPV)
        color = ColorFontUtil.WHITE
        if s == 0:
            color = ColorFontUtil.GREEN
        elif s == 1:
            color = ColorFontUtil.RED
        elif s == 2:
            color = ColorFontUtil.YELLOW
        elif s == 3:
            color = ColorFontUtil.PINK
        elif s == 4:
            color = ColorFontUtil.GREEN
            
        widget.setPropertyValue("foreground_color", color)
        
        tooltip = PVUtil.getString(triggerPV)
        widget.setPropertyValue("tooltip", tooltip)