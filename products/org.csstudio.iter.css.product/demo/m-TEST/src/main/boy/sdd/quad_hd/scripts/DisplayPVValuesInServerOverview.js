importClass(Packages.org.csstudio.opibuilder.scriptUtil.PVUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.ColorFontUtil);


var table = widget.getTable();
var nbColPVs=3;
//find index of the trigger PV
var i=0;
while (triggerPV != pvs[i]) {
    i+=1;
}

table.setCellText(i/nbColPVs, i%nbColPVs +3, PVUtil.getString(triggerPV));

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
    
table.setCellBackground(i/nbColPVs, i%nbColPVs + 3, color);
