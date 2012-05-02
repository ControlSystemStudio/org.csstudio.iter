/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.product;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.auth.security.Credentials;
import org.csstudio.auth.security.SecurityFacade;
import org.csstudio.auth.ui.security.UiLoginCallbackHandler;
import org.csstudio.logging.LogConfigurator;
import org.csstudio.platform.workspace.RelaunchConstants;
import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.csstudio.startup.module.LoginExtPoint;
import org.csstudio.startup.module.WorkbenchExtPoint;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/** Example implementation of {@link WorkbenchExtPoint}
 *
 *  <p>If StartupParameters#SHARE_LINK_PARAM
 *  and ProjectExtPoint#PROJECTS parameters are provided,
 *  a link to that shared folder will be created.
 *
 *  <p>Uses LoginExtPoint#USERNAME and LoginExtPoint#PASSWORD
 *  to attempt authentication.
 *
 *  <p>Runs workbench using the {@link ApplicationWorkbenchAdvisor}.
 *
 *  @see StartupParameters for startup parameters as well as class loader notes.
 *
 *  @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a> - Refactoring for "common startup"
 *  @author Kay Kasemir
 */
public class Workbench implements WorkbenchExtPoint
{
	/** Attempt to authenticate user.
	 *  Allows entry of different name/password on error.
	 *  @param username
	 *  @param password
	 */
	private static void authenticate(final String username, final String password)
	{
		Credentials defaultCredentials;
		if(username == null)
			defaultCredentials = Credentials.ANONYMOUS;
		else
			defaultCredentials = new Credentials(username, password);
    	final SecurityFacade sf = SecurityFacade.getInstance();
		sf.setLoginCallbackHandler(new UiLoginCallbackHandler(Messages.StartupAuthenticationHelper_Login,
				Messages.StartupAuthenticationHelper_LoginTip, defaultCredentials));
		sf.authenticateApplicationUser();
	}

    /** {@inheritDoc} */
	@Override
    public Object afterWorkbenchCreation(final Display display, final IApplicationContext context,
    		final Map<String, Object> parameters)
	{
		return null;
	}

	/** {@inheritDoc} */
    @Override
    public Object beforeWorkbenchCreation(final Display display, final IApplicationContext context,
    		final Map<String, Object> parameters)
	{
		final Object option = parameters.get(StartupParameters.SHARE_LINK_PARAM);
		if (option instanceof String)
		{
			final Job job = new LinkedResourcesJob((String) option);
			// Delay a little to allow faster startup of workbench
			job.schedule(1000);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
    public Object runWorkbench(final Display display, final IApplicationContext context,
			final Map<String, Object> parameters)
	{
	    // Configure Logging
	    try
	    {
	        LogConfigurator.configureFromPreferences();
	    }
	    catch (Exception ex)
	    {
	        ex.printStackTrace();
	        // Continue without customized log configuration
	    }
	    final Logger logger = Logger.getLogger(getClass().getName());

        //authenticate user
        Object o = parameters.get(LoginExtPoint.USERNAME);
		final String username = o != null ? (String)o : null;
		o = parameters.get(LoginExtPoint.PASSWORD);
		final String password = o != null ? (String)o : null;
        authenticate(username, password);

        final OpenDocumentEventProcessor openDocProcessor =
    	  (OpenDocumentEventProcessor) parameters.get(
    			  OpenDocumentEventProcessor.OPEN_DOC_PROCESSOR);

        // Run the workbench
        final int returnCode = PlatformUI.createAndRunWorkbench(display,
                        new ApplicationWorkbenchAdvisor(openDocProcessor));

        // Plain exit from IWorkbench.close()
        if (returnCode != PlatformUI.RETURN_RESTART)
            return IApplication.EXIT_OK;

        // Something called IWorkbench.restart().
        // Is this supposed to be a RESTART or RELAUNCH?
        final Integer exit_code =
            Integer.getInteger(RelaunchConstants.PROP_EXIT_CODE);
        if (IApplication.EXIT_RELAUNCH.equals(exit_code))
        {   // RELAUCH with new command line
            logger.log(Level.FINE,
                    "RELAUNCH, command line: {0}", //$NON-NLS-1$
                    System.getProperty(RelaunchConstants.PROP_EXIT_DATA));
            return IApplication.EXIT_RELAUNCH;
        }
        // RESTART without changes
        return IApplication.EXIT_RESTART;
	}
}
