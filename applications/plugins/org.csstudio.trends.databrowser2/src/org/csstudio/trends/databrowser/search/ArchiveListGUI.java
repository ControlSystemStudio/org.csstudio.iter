package org.csstudio.trends.databrowser.search;

import java.util.ArrayList;

import org.csstudio.archivereader.ArchiveInfo;
import org.csstudio.archivereader.ArchiveReader;
import org.csstudio.platform.ui.internal.dataexchange.ArchiveDataSourceDragSource;
import org.csstudio.platform.ui.swt.AutoSizeColumn;
import org.csstudio.platform.ui.swt.AutoSizeControlListener;
import org.csstudio.trends.databrowser.Messages;
import org.csstudio.trends.databrowser.archive.ConnectJob;
import org.csstudio.trends.databrowser.model.ArchiveDataSource;
import org.csstudio.trends.databrowser.preferences.Preferences;
import org.csstudio.trends.databrowser.propsheet.AddArchiveAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

/** GUI for basic list of archives: URL of server, list of archives on server
 *  <p>
 *  Used by {@link AddArchiveAction} and {@link SearchView}
 *  @author Kay Kasemir
 */
public abstract class ArchiveListGUI
{
    // GUI elements
    private Combo urls;
    private Button info;
    private TableViewer archive_table;
    
    /** Most recently selected archive reader */
    protected ArchiveReader reader;

    /** Initialize
     *  @param parent Parent widget
     */
    public ArchiveListGUI(final Composite parent)
    {
        createGUI(parent);
        
        // When URL is selected, connect to server to get info, list of archives
        urls.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(final SelectionEvent e)
            {
                connectToArchiveServer(urls.getText());
            }
    
            public void widgetSelected(final SelectionEvent e)
            {   // Same response
                widgetDefaultSelected(e);
            }
        });
    
        // Display info about selected archive
        info.addSelectionListener(new SelectionAdapter()
        {
            @SuppressWarnings("nls")
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                if (reader == null)
                    return;
                final StringBuilder buf = new StringBuilder();
                buf.append("Archive Data Server: " + reader.getServerName() + "\n\n");
                buf.append("URL:\n" + reader.getURL() + "\n\n");
                buf.append("Version: " + reader.getVersion() + "\n\n");
                buf.append("Description:\n" + reader.getDescription() + "\n\n");
                MessageDialog.openInformation(info.getShell(), "Archive Server Info", buf.toString());
            }
        });
        
        // Activate the first archive server URL
        if (urls.getEnabled())
            connectToArchiveServer(urls.getText());
        
        // Archive table: Allow dragging of archive data sources and PVs
        new ArchiveDataSourceDragSource(archive_table.getTable(), archive_table);
    }
    
    /** Set initial focus */
    public void setFocus()
    {
        urls.setFocus();
    }

    /** Create GUI elements
     *  @param parent Parent widget
     */
    private void createGUI(final Composite parent)
    {
        final GridLayout layout = new GridLayout(3, false);
        parent.setLayout(layout);
        
        // URL:  ___urls___  [info]
        Label l;
        l = new Label(parent, 0);
        l.setText(Messages.Search_URL);
        l.setLayoutData(new GridData());
        
        urls = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        urls.setToolTipText(Messages.Search_URL_TT);
        urls.setItems(Preferences.getArchiveServerURLs());
        urls.setLayoutData(new GridData(SWT.FILL, 0, true, false));
        if (urls.getItemCount() <= 0)
        {
            urls.add(Messages.ArchiveListGUI_NoArchives);
            urls.setEnabled(false);
        }
        urls.select(0);
        
        info = new Button(parent, SWT.PUSH);
        info.setText(Messages.ArchiveServerInfo);
        info.setToolTipText(Messages.ArchiveServerInfoTT);
        info.setEnabled(false);

        // Table for archives, displaying array of ArchiveDataSource entries
        archive_table = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        archive_table.setContentProvider(new ArrayContentProvider());
        TableViewerColumn col = AutoSizeColumn.make(archive_table, Messages.ArchiveName, 150, 100);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final ArchiveDataSource archive = (ArchiveDataSource) cell.getElement();
                cell.setText(archive.getName());
            }
        });
        col = AutoSizeColumn.make(archive_table, Messages.ArchiveDescription, 50, 100);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final ArchiveDataSource archive = (ArchiveDataSource) cell.getElement();
                cell.setText(archive.getDescription());
            }
        });
        col = AutoSizeColumn.make(archive_table, Messages.ArchiveKey, 35, 5);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final ArchiveDataSource archive = (ArchiveDataSource) cell.getElement();
                cell.setText(Integer.toString(archive.getKey()));
            }
        });
        final Table table = archive_table.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        new AutoSizeControlListener(table);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.numColumns, 1));
    }

    /** @param listener Listener to selections in the archives table of the GUI */
    public void addSelectionListener(final ISelectionChangedListener listener)
    {
        archive_table.addSelectionChangedListener(listener);
    }

    /** Connect to archive data server.
     *  <p>
     *  On success, set <code>server</code>, update <code>archive_table</code>
     *  @param url Server URL
     */
    private void connectToArchiveServer(final String url)
    {
        new ConnectJob(url)
        {
            @Override
            protected void archiveServerConnected(final ArchiveReader reader,
                    final ArchiveInfo infos[])
            {
                if (urls.isDisposed())
                    return;
                urls.getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        if (info.isDisposed())
                            return;
                        ArchiveListGUI.this.reader = reader;
                        final ArrayList<ArchiveDataSource> archives = new ArrayList<ArchiveDataSource>();
                        for (ArchiveInfo info : infos)
                            archives.add(new ArchiveDataSource(reader.getURL(), info.getKey(), info.getName(), info.getDescription()));
                        archive_table.setInput(archives);
                        // Enable operations on server resp. archives
                        info.setEnabled(true);
                        handleArchiveUpdate();
                    }
                });
            }
         
            @Override
            protected void archiveServerError(final String url, final Exception ex)
            {   // Called from non-UI thread
                if (info.isDisposed())
                    return;
                info.getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        if (info.isDisposed())
                            return;
                        info.setEnabled(false);
                        handleServerError(url, ex);
                    }   
                });
            }                    
        }.schedule();
    }
    
    /** @return ArchiveReader that holds the list of archives or <code>null</code>
     *          if no info available
     */
    public ArchiveReader getArchiveReader()
    {
        return reader;
    }

    /** @return Selected archive data sources or 'all' when nothing selected.
     *          Returns <code>null</code> if user decided not to search 'all',
     *          or if connection to data server is not available.
     */
    @SuppressWarnings("unchecked")
    public ArchiveDataSource[] getSelectedArchives()
    {
        if (reader == null)
            return null;
        final ArrayList<ArchiveDataSource> archives;
        final IStructuredSelection sel = (IStructuredSelection) archive_table.getSelection();
        if (sel.isEmpty())
        {
            // Use 'all' archives, but prompt for confirmation
            archives = (ArrayList<ArchiveDataSource>) archive_table.getInput();
            if (archives.size() > 1  &&
                ! MessageDialog.openConfirm(archive_table.getTable().getShell(),
                        Messages.Search,
                        NLS.bind(Messages.SearchArchiveConfirmFmt, archives.size())))
                     return null;   
        }
        else
        {
            archives = new ArrayList<ArchiveDataSource>();
            final Object[] objs = sel.toArray();
            for (Object obj : objs)
                archives.add((ArchiveDataSource) obj);
        }
        return (ArchiveDataSource[]) archives.toArray(new ArchiveDataSource[archives.size()]);
    }

    /** Called after receiving error from archive server.
     *  From now on, getServer and getSelectedArchives should function.
     *  @param url Server URL
     *  @param ex Error
     */
    abstract protected void handleServerError(String url, Exception ex);

    /** Called after receiving list of archives from a server.
     *  From now on, getServer and getSelectedArchives will not return
     *  useful data.
     */
    abstract protected void handleArchiveUpdate();
}