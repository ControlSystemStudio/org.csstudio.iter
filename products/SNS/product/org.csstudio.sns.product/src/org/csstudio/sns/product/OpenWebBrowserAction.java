package org.csstudio.sns.product;

import java.net.URL;

import org.csstudio.platform.logging.CentralLogger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/** Action that opens a web browser.
 *  <p>
 *  Used in ApplicationActionBarAdvisor.
 *  @author Kay Kasemir, Xihui Chen
 */
public class OpenWebBrowserAction extends Action
{
    final private IWorkbench workbench;

    final private String url;

    /** Create action
     *  @param window Workbench Window
     *  @param title Title of action in menu
     *  @param url URL to open in the browser
     */
    public OpenWebBrowserAction(final IWorkbenchWindow window,
            final String title,
            final String url)
    {
        super(title);
        workbench = window.getWorkbench();
        this.url = url;
    }

    /** {@inheritDoc} */
    @Override
    public void run()
    {
        try
        {
            final IWebBrowser browser =
                workbench.getBrowserSupport().createBrowser(
                		IWorkbenchBrowserSupport.NAVIGATION_BAR | IWorkbenchBrowserSupport.LOCATION_BAR, 
                		Messages.Menu_CSS_CSS, null, null);
            browser.openURL(new URL(url));
        }
        catch (Exception ex)
        {
            CentralLogger.getInstance().getLogger(this).error("No browser", ex); //$NON-NLS-1$
        }
    }
}