package org.csstudio.iter.opibuilder.schema.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.util.ConsoleService;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.util.SchemaService;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Display;

public class SchemaVerifier {

    public static void verifyAgainstSchema(IPath opi) throws IOException {
        IPath schemaPath = PreferencesHelper.getSchemaOPIPath();
        if (schemaPath == null) {
            throw new IllegalStateException("There is no OPI schema defined.");
        }

        Map<String,AbstractWidgetModel> schema = load(schemaPath);
    }

    private static Map<String,AbstractWidgetModel> load(IPath path) throws IOException {
        try {
            InputStream inputStream = ResourceUtil.pathToInputStream(path, false);
            DisplayModel displayModel = new DisplayModel(path);
            XMLUtil.fillDisplayModelFromInputStream(inputStream, displayModel, Display.getDefault());
    
            Map<String, AbstractWidgetModel> map = new HashMap<>();
            map.put(displayModel.getTypeID(), displayModel);
            loadModelFromContainer(displayModel,map);
            if (!displayModel.getConnectionList().isEmpty()) {
                map.put(ConnectionModel.ID, displayModel.getConnectionList().get(0));
            }
            return map;
        } catch (Exception e) {
            throw new IOException("Unable to load the OPI from " + path + ".",e);
        }
    }

    private static void loadModelFromContainer(AbstractContainerModel containerModel,
            Map<String, AbstractWidgetModel> map) {
        for (AbstractWidgetModel model : containerModel.getChildren()) {
            if (!map.containsKey(model.getTypeID())) {
                map.put(model.getTypeID(), model);
            }
            if (model instanceof AbstractContainerModel) {
                loadModelFromContainer((AbstractContainerModel) model, map);
            }
        }
    }
}
