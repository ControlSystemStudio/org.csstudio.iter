/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.css.product.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.csstudio.iter.css.product.Activator;
import org.csstudio.iter.css.product.preferences.Preferences;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

public class WorkbenchUtil {

    private static final String APPLIED_SYSTEM_FONT = "appliedSystemFont";
    private static final Logger LOGGER = Logger.getLogger(Activator.PLUGIN_ID);

    private static final String[] HIDE_MESSAGE_STARTS_WITH = new String[] { "Keybinding conflicts occurred.  They may interfere with normal accelerator operation.",
            "Invalid preference page path: XML Syntax", "Job found still running after platform shutdown." };

    public static final String[] IGNORE_PERSPECTIVES = new String[] {
            // "org.eclipse.debug.ui.DebugPerspective", // Used by Pydev
            "org.eclipse.wst.xml.ui.perspective" };

    private static final String[] VERBOSE_PACKAGES = new String[] { "com.sun.jersey.core.spi.component", "com.sun.jersey.core.spi.component.ProviderServices",
            "com.sun.jersey.spi.service.ServiceFinder" };

    private static final String[] IGNORE_VIEWS = new String[] { "org.csstudio.opibuilder.placeHolder", "org.csstudio.opibuilder.opiShellSummary",
            "org.csstudio.opibuilder.opiView", "org.csstudio.opibuilder.opiViewLEFT", "org.csstudio.opibuilder.opiViewRIGHT", "org.csstudio.opibuilder.opiViewTOP",
            "org.csstudio.opibuilder.opiViewBOTTOM" };

    private static final List<Logger> strongRefLoggers = new ArrayList<>();

    private static class HideUnWantedLogFilter implements Filter {

        private Filter previousFilter;

        public HideUnWantedLogFilter(Filter previousFilter) {
            this.previousFilter = previousFilter;
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            if (record.getMessage() != null) {
                for (String hideMsgStartsWith : HIDE_MESSAGE_STARTS_WITH) {
                    if (record.getMessage().startsWith(hideMsgStartsWith)) {
                        return false;
                    }
                }
            }
            if (previousFilter == null) {
                return true;
            }
            return previousFilter.isLoggable(record);
        }
    };

    public static void removeUnWantedLog() {
        // Hide unwanted message from log
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setFilter(new HideUnWantedLogFilter(rootLogger.getFilter()));
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setFilter(new HideUnWantedLogFilter(handler.getFilter()));
        }

        // Set upper log level on too verbose packages
        Level verboseLogLevel;
        try {
            verboseLogLevel = Preferences.getVerboseLogLevel();
        } catch (Exception e) {
            verboseLogLevel = Level.SEVERE;
        }
        for (String verbosePackage : VERBOSE_PACKAGES) {
            Logger logger = Logger.getLogger(verbosePackage);
            logger.setLevel(verboseLogLevel);
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(verboseLogLevel);
            }
            // keep strong references to all loggers. Otherwise the LogMaager
            // will flush them out
            strongRefLoggers.add(logger);
        }
    }

    /**
     * Removes the unwanted perspectives from your RCP application
     */
    public static void removeUnWantedPerspectives() {

        IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
        IPerspectiveDescriptor[] perspectiveDescriptors = perspectiveRegistry.getPerspectives();
        List<String> ignoredPerspectives = Arrays.asList(IGNORE_PERSPECTIVES);
        List<IPerspectiveDescriptor> removePerspectiveDesc = new ArrayList<IPerspectiveDescriptor>();

        // Add the perspective descriptors with the matching perspective ids to
        // the list
        for (IPerspectiveDescriptor perspectiveDescriptor : perspectiveDescriptors) {
            if (ignoredPerspectives.contains(perspectiveDescriptor.getId())) {
                removePerspectiveDesc.add(perspectiveDescriptor);
                // fix for RCP 4, where IExtensionChangeHandler#removeExtension
                // is not implemented
                // we could create a new PerspectiveDescriptor, but that would
                // require access to the restricted
                // eclipse code, which some compilers might not like. Instead
                // use reflection to force deletion
                // of a perspective
                if ("org.eclipse.ui.internal.registry.PerspectiveDescriptor".equals(perspectiveDescriptor.getClass().getName())) {
                    try {
                        Field f = perspectiveDescriptor.getClass().getDeclaredField("configElement");
                        f.setAccessible(true);
                        f.set(perspectiveDescriptor, null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // ignore: we don't care this will happen only if the
                        // Eclipse internals change
                    }
                }
                perspectiveRegistry.deletePerspective(perspectiveDescriptor);
            }
        }

        // just in case for any backward compatibility reasons, do the RCP 3
        // magic
        // If the list is non-empty then remove all such perspectives from the
        // IExtensionChangeHandler
        if (perspectiveRegistry instanceof IExtensionChangeHandler && !removePerspectiveDesc.isEmpty()) {
            IExtensionChangeHandler extChgHandler = (IExtensionChangeHandler) perspectiveRegistry;
            extChgHandler.removeExtension(null, removePerspectiveDesc.toArray());

        }
    }

    /**
     * Unbind F11 KeyBinding of org.eclipse.debug.ui to avoid conflict with
     * org.csstudio.opibuilder plugin
     */
    public static void unbindDuplicateBindings() {
        IBindingService bindingService = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
        BindingManager localChangeManager = new BindingManager(new ContextManager(), new CommandManager());

        final Scheme[] definedSchemes = bindingService.getDefinedSchemes();
        try {
            for (int i = 0; i < definedSchemes.length; i++) {
                final Scheme scheme = definedSchemes[i];
                final Scheme copy = localChangeManager.getScheme(scheme.getId());
                copy.define(scheme.getName(), scheme.getDescription(), scheme.getParentId());
            }
            localChangeManager.setActiveScheme(bindingService.getActiveScheme());
        } catch (final NotDefinedException e) {
            e.printStackTrace();
        }
        localChangeManager.setLocale(bindingService.getLocale());
        localChangeManager.setPlatform(bindingService.getPlatform());
        localChangeManager.setBindings(bindingService.getBindings());

        KeyBinding opiFullScreenBinding = null;
        int nbBinding = 0;

        Binding[] bArray = bindingService.getBindings();
        if (bArray != null) {
            for (Binding binding : bArray) {
                if (binding instanceof KeyBinding) {
                    KeyBinding kBind = (KeyBinding) binding;
                    if (kBind.getParameterizedCommand() != null && kBind.getParameterizedCommand().getCommand() != null) {
                        String id = kBind.getParameterizedCommand().getCommand().getId();
                        if ("org.eclipse.debug.ui.commands.DebugLast".equals(id) || "org.eclipse.jdt.ui.edit.text.java.search.declarations.in.workspace".equals(id)) {
                            KeySequence triggerSequence = kBind.getKeySequence();
                            String contextId = kBind.getContextId();
                            String schemeId = kBind.getSchemeId();
                            KeyBinding deleteBinding = new KeyBinding(triggerSequence, null, schemeId, contextId, null, null, null, Binding.USER);
                            localChangeManager.addBinding(deleteBinding);
                            try {
                                bindingService.savePreferences(localChangeManager.getActiveScheme(), localChangeManager.getBindings());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if ("org.csstudio.opibuilder.actions.fullscreen".equals(kBind.getParameterizedCommand().getCommand().getId())) {
                            if (opiFullScreenBinding == null)
                                opiFullScreenBinding = kBind;
                            nbBinding++;
                        }
                    }
                }
            }
        }

        // Rebind OPI runner full screen command if it exists only one time
        if (nbBinding == 1 && opiFullScreenBinding != null) {
            KeySequence triggerSequence = opiFullScreenBinding.getKeySequence();
            String contextId = opiFullScreenBinding.getContextId();
            String schemeId = opiFullScreenBinding.getSchemeId();

            KeyBinding updateBinding = new KeyBinding(triggerSequence, opiFullScreenBinding.getParameterizedCommand(), schemeId, contextId, null, null, null,
                    Binding.USER);
            localChangeManager.addBinding(updateBinding);
            try {
                bindingService.savePreferences(localChangeManager.getActiveScheme(), localChangeManager.getBindings());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Force the default font for all those fonts that we know are not using the
     * system font. The method has to be called before the workbench is created
     * to have any effect.
     */
    public static void setupSystemFonts() {
        String s = Activator.getDefault().getPreferenceStore().getString(APPLIED_SYSTEM_FONT);
        FontData[] fd = JFaceResources.getDefaultFont().getFontData();
        String font = fd[0].toString();
        // apply the system font if the system font has never been applied yet
        // or if it is different than the previously applied font
        if (s == null || s.isEmpty() || !font.equals(s)) {
            StringBuilder infoMessage = new StringBuilder(System.lineSeparator()).append("==== Applied new fonts from system font: ");
            IEclipsePreferences sc = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
            // only apply the new system font, if the font for that settings has
            // not been changed by the user
            String oldF = sc.get(JFaceResources.BANNER_FONT, "");
            if (oldF.equals(s)) {
                sc.put(JFaceResources.BANNER_FONT, font);
                infoMessage.append(System.lineSeparator()).append(JFaceResources.BANNER_FONT).append(": ").append(font);
            }
            oldF = sc.get(JFaceResources.DIALOG_FONT, "");
            if (oldF.equals(s)) {
                sc.put(JFaceResources.DIALOG_FONT, font);
                infoMessage.append(System.lineSeparator()).append(JFaceResources.DIALOG_FONT).append(": ").append(font);
            }
            oldF = sc.get(JFaceResources.TEXT_FONT, "");
            if (oldF.equals(s)) {
                sc.put(JFaceResources.TEXT_FONT, font);
                infoMessage.append(System.lineSeparator()).append(JFaceResources.TEXT_FONT).append(": ").append(font);
            }
            oldF = sc.get(JFaceResources.HEADER_FONT, "");
            if (oldF.equals(s)) {
                sc.put(JFaceResources.HEADER_FONT, font);
                infoMessage.append(System.lineSeparator()).append(JFaceResources.HEADER_FONT).append(": ").append(font);
            }
            oldF = sc.get("org.eclipse.ui.workbench.texteditor.blockSelectionModeFont", "");
            if (oldF.equals(s)) {
                sc.put("org.eclipse.ui.workbench.texteditor.blockSelectionModeFont", font);
                infoMessage.append(System.lineSeparator()).append("org.eclipse.ui.workbench.texteditor.blockSelectionModeFont").append(": ").append(font);
            }
            oldF = sc.get("org.eclipse.jface.consoleFont", "");
            if (oldF.equals(s)) {
                sc.put("org.eclipse.jface.consoleFont", font);
                infoMessage.append(System.lineSeparator()).append("org.eclipse.jface.consoleFont").append(": ").append(font);
            }
            Activator.getDefault().getPreferenceStore().setValue(APPLIED_SYSTEM_FONT, font);
            LOGGER.log(Level.INFO, infoMessage.append(System.lineSeparator()).append("============= ").toString());
        }
    }

    /**
     * Remove unwanted views from CSS.
     *
     * @param application
     *            the application object from which views will be removed
     */
    public static void removeUnwantedViews(MApplication application) {
        if (application == null) {
            return;
        }
        // Because of some unknown reasons the activities do not remove the
        // views from the Show View dialog. Therefore,
        // we try to brute force remove the view from the application
        List<MPartDescriptor> descriptors = application.getDescriptors();
        List<String> views = Arrays.asList(IGNORE_VIEWS);
        List<MPartDescriptor> toRemove = new ArrayList<>();
        for (MPartDescriptor d : descriptors) {
            if (views.contains(d.getElementId())) {
                toRemove.add(d);
            }
        }
        for (MPartDescriptor p : toRemove) {
            descriptors.remove(p);
        }

    }
}
