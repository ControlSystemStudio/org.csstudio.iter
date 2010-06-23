package org.csstudio.opibuilder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.feedback.IGraphicalFeedbackFactory;
import org.csstudio.opibuilder.palette.MajorCategories;
import org.csstudio.platform.logging.CentralLogger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**A service help to find the widget from extensions and help to 
 * maintain the widgets information.
 * @author Xihui Chen
 *
 */
public final class WidgetsService {

	/**
	 * The shared instance of this class.
	 */
	private static WidgetsService instance = null;
	
	private static final String DEFAULT_CATEGORY = "Others";
	
	private Map<String, WidgetDescriptor> allWidgetDescriptorsMap;
	
	private Map<String, List<String>> allCategoriesMap;
	
	private Map<String, IGraphicalFeedbackFactory> feedbackFactoriesMap;
	
	/**
	 * @return the instance
	 */
	public synchronized static final WidgetsService getInstance() {
		if(instance == null)
			instance = new WidgetsService();
		return instance;
	}	

	public WidgetsService() {
		feedbackFactoriesMap = new HashMap<String, IGraphicalFeedbackFactory>();
		allWidgetDescriptorsMap = new HashMap<String, WidgetDescriptor>();
		allCategoriesMap = new LinkedHashMap<String, List<String>>();
		for(MajorCategories mc : MajorCategories.values())
			allCategoriesMap.put(mc.toString(), new ArrayList<String>());
		loadAllWidgets();
		loadAllFeedbackFactories();
	}
	
	/**
	 * Load all widgets information from extensions. 
	 */
	private void loadAllWidgets(){
		IExtensionRegistry extReg = Platform.getExtensionRegistry();
		IConfigurationElement[] confElements = 
			extReg.getConfigurationElementsFor(OPIBuilderPlugin.EXTPOINT_WIDGET);
		for(IConfigurationElement element : confElements){
			String typeId = element.getAttribute("typeId"); //$NON-NLS-1$
			String name = element.getAttribute("name"); //$NON-NLS-1$
			String icon = element.getAttribute("icon"); //$NON-NLS-1$
			String pluginId = element.getDeclaringExtension()
					.getNamespaceIdentifier();
			String description = element.getAttribute("description");
			if (description == null || description.trim().length() == 0) {
				description = "";
			}
			String category = element.getAttribute("category");
			if (category == null || category.trim().length() == 0) {
				category = DEFAULT_CATEGORY;
			}
			
			if(typeId != null){
				List<String> list = allCategoriesMap.get(category);
				if(list == null){
					list = new ArrayList<String>();
					allCategoriesMap.put(category, list);
				}
				list.add(typeId);
				allWidgetDescriptorsMap.put(typeId, new WidgetDescriptor(
						element, typeId, name, description, icon, category, pluginId));
			}			
		}
		
		// sort the widget in the categories
		//for(List<String> list : allCategoriesMap.values())
		//	Collections.sort(list);		
	}
	
	private void loadAllFeedbackFactories(){
		IExtensionRegistry extReg = Platform.getExtensionRegistry();
		IConfigurationElement[] confElements = 
			extReg.getConfigurationElementsFor(OPIBuilderPlugin.EXTPOINT_FEEDBACK_FACTORY);
		for(IConfigurationElement element : confElements){
			String typeId = element.getAttribute("typeId");
			if(typeId != null){
				try {
					feedbackFactoriesMap.put(typeId, 
							(IGraphicalFeedbackFactory)element.createExecutableExtension("class"));
				} catch (CoreException e) {
					CentralLogger.getInstance().error(this, e);
				}
			}
		}
	}
	
	
	/**
	 * @return the allCategoriesMap the map which contains all the name of the 
	 * categories and the widgets under them. The widgets list has been sorted by string.
	 */
	public final Map<String, List<String>> getAllCategoriesMap() {
		return allCategoriesMap;
	}
	
	
	/**
	 * @param typeId the typeId of the widget.
	 * @return the {@link WidgetDescriptor} of the widget. 
	 */
	public final WidgetDescriptor getWidgetDescriptor(String typeId){
		return allWidgetDescriptorsMap.get(typeId);
	}
	
	
	public final IGraphicalFeedbackFactory getWidgetFeedbackFactory(String typeId){
		return feedbackFactoriesMap.get(typeId);
	}
	
	
	
}