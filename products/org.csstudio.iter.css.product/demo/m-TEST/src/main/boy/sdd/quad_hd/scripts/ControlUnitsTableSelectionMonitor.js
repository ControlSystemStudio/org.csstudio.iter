
importClass(Packages.org.csstudio.swt.widgets.natives.SpreadSheetTable.ITableSelectionChangedListener);
importPackage(Packages.org.csstudio.opibuilder.scriptUtil);

var table = widget.getTable();
var fct_name=display.getPropertyValue("name");

var selectionChanged  = new Packages.org.csstudio.swt.widgets.natives.SpreadSheetTable.ITableSelectionChangedListener() {
    selectionChanged: function(selection) {
    	var selectedrow=  table.getSelection();
    	var cuName=selectedrow[0][0];
		var phyName=selectedrow[0][1];
    	var plcIocHlts =selectedrow[0][6];
        var cuType=selectedrow[0][7];
// change $(CU) substitution
        macroInput = DataUtil.createMacrosInput(true)
        macroInput.put("CU", cuName)
        macroInput.put("PHY_NAME", phyName)
        macroInput.put("FCT_NAME", fct_name)
        macroInput.put("CU_TYPE", cuType)
        if (plcIocHlts == "") {
        	macroInput.put("SHOW_PLC_IOC", "false")
        }
        else {
        	macroInput.put("SHOW_PLC_IOC", "true")
        }
        // open OPI
        // see https://svnpub.iter.org/codac/iter/codac/dev/units/m-css-boy/trunk/org.csstudio.opibuilder/src/org/csstudio/opibuilder/scriptUtil/ScriptUtil.java
        if (cuType == "POC with CA") {
            ScriptUtil.openOPI(display.getWidget("Table"), fct_name+"-POCWithCADetails.opi", 1, macroInput)
        }
        else if (cuType == "POC without CA") {
            ScriptUtil.openOPI(display.getWidget("Table"), fct_name+"-POCWithoutCADetails.opi", 1, macroInput)
        }
        else {
            ScriptUtil.openOPI(display.getWidget("Table"), fct_name+"-CtrlUnitDetails.opi", 1, macroInput)
        }
	
    }
};

table.addSelectionChangedListener(selectionChanged);
