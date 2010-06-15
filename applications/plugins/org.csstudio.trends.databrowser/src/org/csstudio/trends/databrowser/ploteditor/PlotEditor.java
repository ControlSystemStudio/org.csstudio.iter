package org.csstudio.trends.databrowser.ploteditor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.csstudio.apputil.ui.workbench.OpenPerspectiveAction;
import org.csstudio.platform.ui.workbench.OpenViewAction;
import org.csstudio.trends.databrowser.Perspective;
import org.csstudio.trends.databrowser.Plugin;
import org.csstudio.trends.databrowser.archiveview.ArchiveView;
import org.csstudio.trends.databrowser.configview.ConfigView;
import org.csstudio.trends.databrowser.exportview.ExportView;
import org.csstudio.trends.databrowser.model.IModelItem;
import org.csstudio.trends.databrowser.model.Model;
import org.csstudio.trends.databrowser.model.ModelListener;
import org.csstudio.trends.databrowser.plotpart.AddFormulaAction;
import org.csstudio.trends.databrowser.plotpart.AddPVAction;
import org.csstudio.trends.databrowser.plotpart.PlotPart;
import org.csstudio.trends.databrowser.sampleview.SampleView;
import org.csstudio.trends.databrowser.waveformview.WaveformView;
import org.csstudio.util.editor.EmptyEditorInput;
import org.csstudio.util.editor.PromptForNewXMLFileDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/** An Eclipse 'editor' for the data browser plot.
 *  <p>
 *  Displays the plot, and allows interaction with ConfigView, ExportView
 *  etc.
 *  @author Kay Kasemir
 */
public class PlotEditor extends EditorPart
{
    public static final String ID = PlotEditor.class.getName();
    
    private final PlotPart plot_part = new PlotPart();
    private boolean is_dirty = false;
    /** In case of a model init (load) problem, we won't get to register
     *  the model listener, and hence we shouldn't remove it in dispose().
     *  This helps us to keep track.
     */
    private boolean added_model_listener = false;
    // Update 'dirty' state whenever anything changes
    private final ModelListener model_listener = new ModelListener()
    {
        public void plotColorsChangedChanged()
        {   entriesChanged();  }
        
        public void markersChanged()
        {   entriesChanged();  }

        public void timeSpecificationsChanged()
        {   entriesChanged();  }
        
        // "current" start/end time changes are ignored
        public void timeRangeChanged()
        { /* NOP */ }
        
        public void samplingChanged()
        {   entriesChanged();  }

        public void entriesChanged()
        {
            if (is_dirty)
                return;
            is_dirty = true;
            firePropertyChange(IEditorPart.PROP_DIRTY);
            updateTitle();
        }
        
        public void entryAdded(IModelItem new_item) 
        {   entriesChanged();  }
        
        public void entryConfigChanged(IModelItem item) 
        {   entriesChanged();  }
        
        public void entryMetaDataChanged(IModelItem item) 
        {   /* so what */ }
        
        public void entryArchivesChanged(IModelItem item)
        {   entriesChanged();  }

        public void entryRemoved(IModelItem removed_item) 
        {   entriesChanged();  }
    };

    /** Create a new, empty editor, not attached to a file.
     *  @return Returns the new editor or <code>null</code>.
     */
    public static PlotEditor createInstance()
    {
        return createInstance(new EmptyEditorInput());
    }
    
    /** Create a new editor for the given input.
     *  @return Returns the new editor or <code>null</code>.
     */
    public static PlotEditor createInstance(IEditorInput input)
    {
        try
        {
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            PlotEditor editor =
                (PlotEditor) page.openEditor(input, PlotEditor.ID);
            return editor;
        }
        catch (Exception ex)
        {
            Plugin.getLogger().error("Cannot create Plot", ex); //$NON-NLS-1$
        }
        return null;
    }
    
    /** @return Returns the model. */
    public Model getModel()
    {   return plot_part.getModel(); }
    
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException
    {
        setSite(site);
        setInput(input);
        final IFile file = getEditorInputFile();
        plot_part.init(file);
    }
    
    /** @return Returns the <code>IFile</code> for the current editor input.
     *  The file is 'relative' to the workspace, not 'absolute' in the
     *  file system. However, the file might be a linked resource to a
     *  file that physically resides outside of the workspace tree.
     */
    IFile getEditorInputFile()
    {  
        IEditorInput input = getEditorInput();
        if (input instanceof EmptyEditorInput)
            return null;
        // Side Note:
        // After some back and forth, trying to avoid the resource/workspace/
        // project/container/file stuff and instead sticking with the 
        // java.io.file, I found it best to give up and use the Eclipse
        // resource API, since otherwise one keeps converting between those
        // two APIs anyway, plus runs into errors with 'resources' being
        // out of sync....        
        IFile file = (IFile) input.getAdapter(IFile.class);
        if (file != null)
            return file;
        Plugin.getLogger().error("getEditorInputFile got " //$NON-NLS-1$
                        + input.getClass().getName());
        return null;
    }

    @Override
    public void doSave(IProgressMonitor monitor)
    {
        IFile file = getEditorInputFile();
        if (file != null)
            saveToFile(monitor, file);
        else
            doSaveAs();
    }
    
    /** Save current model content to given file, mark editor as clean.
     * 
     *  @param monitor <code>IProgressMonitor</code>, may be null.
     *  @param file The file to use. May not exist, but I think its container has to.
     *  @return Returns <code>true</code> when successful.
     */
    private boolean saveToFile(IProgressMonitor monitor, IFile file)
    {
        boolean ok = true;
        if (monitor != null)
            monitor.beginTask(Messages.SaveBrowserConfig,
                            IProgressMonitor.UNKNOWN);
        InputStream stream =
            new ByteArrayInputStream(plot_part.getModel().getXMLContent().getBytes());
        try
        {
            if (file.exists())
                file.setContents(stream, true, false, monitor);
            else
                file.create(stream, true, monitor);
            if (monitor != null)
                monitor.done();
            // Mark as clean
            is_dirty = false;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        catch (Exception e)
        {
            ok = false;
            if (monitor != null)
                monitor.setCanceled(true);
            Plugin.getLogger().error("Save error", e); //$NON-NLS-1$
        }
        finally
        {
            try
            {   stream.close(); }
            catch (Exception e)
            { /* NOP */ }
        }
        return ok;
    }

    @Override
    public void doSaveAs()
    {
        IFile file = PromptForNewXMLFileDialog.run(getSite().getShell(),
                                                   Plugin.FileExtension,
                                                   getEditorInputFile());
        if (file == null  ||  !saveToFile(null, file))
            return;
        // Update input and title
        setInput(new FileEditorInput(file));
        updateTitle();
    }

    @Override
    public boolean isDirty()
    {   return is_dirty;  }

    @Override
    public boolean isSaveAsAllowed()
    {   return true;  }
    
    @Override
    public void createPartControl(Composite parent)
    {
        plot_part.createPartControl(parent, true);
        createContextMenu();
        
        // The above causes the model to fire entriesChanged.
        // By only registering the listener at this time,
        // we avoid getting into the 'dirty' state from that intial event.
        plot_part.getModel().addListener(model_listener);
        added_model_listener = true;
        updateTitle();
    }

    @Override
    public void setFocus()
    {  
        plot_part.setFocus();
    }

    /** Must be called to clean up. */
    @Override
    public void dispose()
    {
        if (added_model_listener)
        {
            plot_part.getModel().removeListener(model_listener);
            added_model_listener = false;
        }
        plot_part.dispose();
        super.dispose();
    }
    
    /** Set the editor part's title and tool-tip. */
    private void updateTitle()
    {   // See plugin book p.332.
        final IEditorInput input = getEditorInput();
        final String title = getEditorInput().getName();
        setPartName(title);
        setTitleToolTip(input.getToolTipText());
    }

    /** Create and connect the context menu. */
    private void createContextMenu()
    {
        final Control ctl = plot_part.getInteractiveChart().getChart();
        final MenuManager context_menu = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        plot_part.addContextMenuPlotActions(context_menu);
        context_menu.add(new AddPVAction(ctl.getShell(), plot_part.getModel()));
        context_menu.add(new AddFormulaAction(ctl.getShell(), plot_part.getModel()));
        
        context_menu.add(new Separator());
        final ImageDescriptor icon =
        	Plugin.getImageDescriptor("icons/chart.gif"); //$NON-NLS-1$
        context_menu.add(new OpenViewAction(ConfigView.ID,
        		Messages.OpenConfigView, icon));
        context_menu.add(new OpenViewAction(ArchiveView.ID,
        		Messages.OpenArchiveView, icon));
        context_menu.add(new OpenViewAction(SampleView.ID,
        		Messages.OpenSampleView, icon));
        context_menu.add(new OpenViewAction(ExportView.ID,
        		Messages.OpenExportView,
                Plugin.getImageDescriptor("icons/export.gif"))); //$NON-NLS-1$
        context_menu.add(new OpenViewAction(WaveformView.ID,
        		Messages.OpenWaveformView,
        		Plugin.getImageDescriptor("icons/wavesample.gif"))); //$NON-NLS-1$
        plot_part.addContextMenuExportActions(context_menu);
        
        context_menu.add(new Separator());
        final Action view_action = new OpenPlotEditorAsViewAction(this);
        context_menu.add(view_action);
        context_menu.add(new OpenPerspectiveAction(icon,
                                    Messages.OpenPerspective, Perspective.ID));
        context_menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        context_menu.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                view_action.setEnabled(getEditorInputFile() != null);
            }
        });
        
        Menu menu = context_menu.createContextMenu(ctl);
        ctl.setMenu(menu);
    }
}