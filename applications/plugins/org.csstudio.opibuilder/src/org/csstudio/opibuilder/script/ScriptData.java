package org.csstudio.opibuilder.script;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**The description data for a script.
 * @author Xihui Chen
 *
 */
public class ScriptData implements IAdaptable {
	
	public static String SCRIPT_EXTENSION = "js"; //$NON-NLS-1$
	
	/**
	 * The path of the script.
	 */
	private IPath path;
	
	/**
	 * The input PVs of the script. Which can be accessed in the script and trigger the script execution.
	 */
	private List<PVTuple> pvList;
	
	
	public ScriptData() {
		path = new Path("");
		pvList = new ArrayList<PVTuple>();
	}
	
	public ScriptData(IPath path) {
		this.path = path;
		pvList = new ArrayList<PVTuple>();
	}
	
	/**Set the script path.
	 * @param path the file path of the script.
	 * @return true if successful. false if the input is not a javascript file.
	 */
	public boolean setPath(IPath path){
		if(path.getFileExtension() != null && 
				path.getFileExtension().equals(SCRIPT_EXTENSION)){
			this.path = path; 
			return true;
		}
		return false;		
	}
	
	/**Get the path of the script.
	 * @return the file path.
	 */
	public IPath getPath() {
		return path;
	}
	
	/**Get the input PVs of the script 
	 * @return
	 */
	public List<PVTuple> getPVList() {
		return pvList;
	}
	
	public void addPV(PVTuple pvTuple){
		if(!pvList.contains(pvTuple)){
			pvList.add(pvTuple);
		}			
	}
	
	public void removePV(String pv){
		pvList.remove(pv);
	}	
	
	public ScriptData getCopy(){
		ScriptData copy = new ScriptData();
		copy.setPath(path);
		for(PVTuple pv : pvList){
			copy.addPV(new PVTuple(pv.pvName, pv.trigger));
		}
		return copy;
	}


	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if(adapter == IWorkbenchAdapter.class)
			return new IWorkbenchAdapter() {
				
				public Object getParent(Object o) {
					return null;
				}
				
				public String getLabel(Object o) {
					return path.toString();
				}
				
				public ImageDescriptor getImageDescriptor(Object object) {
					return CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(
							OPIBuilderPlugin.PLUGIN_ID, "icons/js.gif");
				}
				
				public Object[] getChildren(Object o) {
					return new Object[0];
				}
			};
		
		return null;
	}
}