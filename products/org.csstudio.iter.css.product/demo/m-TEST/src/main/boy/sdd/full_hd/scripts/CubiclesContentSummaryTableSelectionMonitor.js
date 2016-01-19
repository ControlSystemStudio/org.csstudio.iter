importClass(Packages.org.csstudio.swt.widgets.natives.SpreadSheetTable.ITableSelectionChangedListener);
importPackage(Packages.org.csstudio.opibuilder.scriptUtil);

var table = widget.getTable();
var fct_name=display.getPropertyValue("name");

var selectionChanged  = new Packages.org.csstudio.swt.widgets.natives.SpreadSheetTable.ITableSelectionChangedListener() {
    selectionChanged: function(selection) {
    	var selectedrow=  table.getSelection();
    	var cuIndex=selectedrow[0][0];
		var phyName=selectedrow[0][1];
		
        // change $(CU_INDEX) substitution
        var macroInput = DataUtil.createMacrosInput(true);
        macroInput.put("CUB", cuIndex);
        macroInput.put("PHY_NAME", phyName);
        macroInput.put("FCT_NAME", fct_name);
        // open OPI
        // see https://svnpub.iter.org/codac/iter/codac/dev/units/m-css-boy/trunk/org.csstudio.opibuilder/src/org/csstudio/opibuilder/scriptUtil/ScriptUtil.java
        ScriptUtil.openOPI(display.getWidget("Table"), fct_name+"-"+cuIndex+"-CubicleContents.opi", 1, macroInput);
    }
};
table.addSelectionChangedListener(selectionChanged);
