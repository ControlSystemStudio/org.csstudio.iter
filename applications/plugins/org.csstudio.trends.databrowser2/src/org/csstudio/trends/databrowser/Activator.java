package org.csstudio.trends.databrowser;

import java.util.Dictionary;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/** Eclipse Plugin Activator
 *  @author Kay Kasemir
 */
public class Activator extends AbstractUIPlugin
{
    /** Plug-in ID defined in MANIFEST.MF */
    public static final String PLUGIN_ID = "org.csstudio.trends.databrowser2"; //$NON-NLS-1$

    /** Singleton instance */
    private static Activator plugin;

    /** {@inheritDoc} */
    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;
    }

    /** {@inheritDoc} */
    @Override
    public void stop(BundleContext context) throws Exception
    {
        plugin = null;
        super.stop(context);
    }

    /** @return the shared instance */
    public static Activator getDefault()
    {
        return plugin;
    }
    
    /** Obtain image descriptor from file within plugin.
     *  @param path Path within plugin to image file
     *  @return {@link ImageDescriptor}
     */
    public ImageDescriptor getImageDescriptor(final String path)
    {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /** Obtain image from file within plugin.
     *  Uses registry to avoid duplicates and for disposal
     *  @param path Path within plugin to image file
     *  @return {@link Image}
     */
    public Image getImage(final String path)
    {
        Image image = getImageRegistry().get(path);
        if (image == null)
        {
            image = getImageDescriptor(path).createImage();
            getImageRegistry().put(path, image);
        }
        return image;
    }

    /** @return Version code */
    @SuppressWarnings({ "unchecked", "nls" })
    public String getVersion()
    {
        final Dictionary<String, String> headers = getBundle().getHeaders();
        return headers.get("Bundle-Version");
    }
}