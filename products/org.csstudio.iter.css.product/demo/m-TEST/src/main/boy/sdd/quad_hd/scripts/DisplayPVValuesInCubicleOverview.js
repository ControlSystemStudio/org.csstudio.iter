importClass(Packages.org.csstudio.opibuilder.scriptUtil.PVUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.ColorFontUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.ConsoleUtil);

var table = widget.getTable();
var nbColPVs=2;
// find index of the trigger PV
var i=0;
while (i< pvs.length) {
if(pvs[i].isConnected()==true){


var s = PVUtil.getSeverity(pvs[i]);
}else{

var s =3;
}

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
else if( s == 3) {
    color = ColorFontUtil.PINK;
}
else if( s == 4) {
    color = ColorFontUtil.GREEN;
}


if (pvs[i].getName().indexOf("-CUBHLTS") != -1) {
                if(pvs[i].isConnected()==true){
                table.setCellText(i/nbColPVs, 3, PVUtil.getString(pvs[i]));
    }else{
                table.setCellText(i/nbColPVs, 3, "disconnected");
    }
    table.setCellBackground(i/nbColPVs, 3, color);
}
if (pvs[i].getName().indexOf("-PLCHLTS") != -1) {
if(pvs[i].isConnected()==true){
    table.setCellText(i/nbColPVs, 4, PVUtil.getString(pvs[i]));
    }else{
    table.setCellText(i/nbColPVs, 4, "disconnected");
    }
    table.setCellBackground(i/nbColPVs, 4, color);
}
i=i+1;
}
