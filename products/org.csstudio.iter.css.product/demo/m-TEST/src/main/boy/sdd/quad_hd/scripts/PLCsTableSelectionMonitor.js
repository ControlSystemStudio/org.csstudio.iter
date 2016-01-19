importClass(Packages.org.csstudio.opibuilder.scriptUtil.DataUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.PVUtil);
importClass(Packages.org.csstudio.opibuilder.scriptUtil.ScriptUtil);

var table = widget.getTable();
var fct_name=display.getPropertyValue("name");

var selectionListener  = new Packages.org.csstudio.swt.widgets.natives.SpreadSheetTable.ITableSelectionChangedListener() {
    selectionChanged: function(selection) {
    	var selectedrow=  table.getSelection();
    	var cuName=selectedrow[0][0];
		var phyName=selectedrow[0][1];

		var macroInput = DataUtil.createMacrosInput(true);
		macroInput.put("CU", cuName);
		macroInput.put("PHY_NAME", phyName);
		macroInput.put("FCT_NAME", fct_name);
        	// open OPI
        	// see https://svnpub.iter.org/codac/iter/codac/dev/units/m-css-boy/trunk/org.csstudio.opibuilder/src/org/csstudio/opibuilder/scriptUtil/ScriptUtil.java
    	if (cuName.indexOf("P") == 0) {
		    ScriptUtil.openOPI(display.getWidget("Table"), fct_name+"-PLCDetails.opi", 1, macroInput);
		}
		else {
		    ScriptUtil.openOPI(display.getWidget("Table"), fct_name+"-CubiclePLCDetails.opi", 0, macroInput);
		}
	}
};
table.addSelectionChangedListener(selectionListener);

