package org.csstudio.opibuilder.adl2boy.translator;

import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.model.ImageModel;
import org.csstudio.utility.adlparser.fileParser.ADLWidget;
import org.csstudio.utility.adlparser.fileParser.widgets.Image;
import org.eclipse.swt.graphics.RGB;

public class Image2Model extends AbstractADL2Model {
	ImageModel imageModel = new ImageModel();

	public Image2Model(ADLWidget adlWidget, RGB[] colorMap, AbstractContainerModel parentModel) {
		super(adlWidget, colorMap, parentModel);
		parentModel.addChild(imageModel, true);
		Image imageWidget = new Image(adlWidget);
		if (imageWidget != null) {
			setADLObjectProps(imageWidget, imageModel);
			setADLBasicAttributeProps(imageWidget, imageModel, false);
		}
		//TODO Add dynamic properties to Image2Model
		//TODO Add Image Type to Image2Model
		//TODO Add ImageName 2 Image2Model
		//TODO Add ImageCalc to Image2Model
		//TODO Figure out how to put in path to images.
	}

	@Override
	public AbstractWidgetModel getWidgetModel() {
		return imageModel;
	}

}