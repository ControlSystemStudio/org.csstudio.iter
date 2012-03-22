/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The scan engine idea is based on the "ScanEngine" developed
 * by the Software Services Group (SSG),  Advanced Photon Source,
 * Argonne National Laboratory,
 * Copyright (c) 2011 , UChicago Argonne, LLC.
 *
 * This implementation, however, contains no SSG "ScanEngine" source code
 * and is not endorsed by the SSG authors.
 ******************************************************************************/
package org.csstudio.scan.ui.scanmonitor;

import java.util.List;

import org.csstudio.scan.client.ScanInfoModel;
import org.csstudio.scan.client.ScanInfoModelListener;
import org.csstudio.scan.data.DataFormatter;
import org.csstudio.scan.server.ScanInfo;
import org.csstudio.scan.server.ScanServerInfo;
import org.csstudio.scan.server.ScanState;
import org.csstudio.scan.ui.plot.OpenPlotAction;
import org.csstudio.scan.ui.scanmonitor.actions.AbortAction;
import org.csstudio.scan.ui.scanmonitor.actions.PauseAction;
import org.csstudio.scan.ui.scanmonitor.actions.RemoveAction;
import org.csstudio.scan.ui.scanmonitor.actions.RemoveCompletedAction;
import org.csstudio.scan.ui.scanmonitor.actions.ResumeAction;
import org.csstudio.scan.ui.scanmonitor.actions.ShowDevicesAction;
import org.csstudio.scan.ui.scantree.OpenScanTreeAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/** GUI for the {@link ScanInfoModel}
 *  @author Kay Kasemir
 */
public class GUI implements ScanInfoModelListener
{
    final private ScanInfoModel model;

    /** {@link TableViewer} for {@link ScanInfoModelContentProvider} */
    private TableViewer table_viewer;

	private Bar mem_info;

    /** Initialize
     *  @param parent Parent component
     *  @param model Model to display
     */
    public GUI(final Composite parent, final ScanInfoModel model)
    {
        this.model = model;

        createComponents(parent);
        hookActions();
        createContextMenu();
        table_viewer.setInput(model);
        model.addListener(this);
    }

    /** Create GUI elements
     *  @param parent Parent component
     */
    private void createComponents(final Composite parent)
    {
        final Display display = parent.getDisplay();

        parent.setLayout(new GridLayout(2, false));

        // Note: TableColumnLayout requires that table is only child element!
        final Composite table_box = new Composite(parent, 0);
        table_box.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        // If more components are added, the table will need to be wrapped
        // into its own Composite.
        final TableColumnLayout table_layout = new TableColumnLayout();
        table_box.setLayout(table_layout);

        table_viewer = new TableViewer(table_box, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        final Table table = table_viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createColumn(table_viewer, table_layout, Messages.ID, 30, 25, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                final ScanInfo info = (ScanInfo) element;
                return NLS.bind(Messages.ID_Fmt, info.getId());
            }

            @Override
            public void update(final ViewerCell cell)
            {
                final ScanInfo info = (ScanInfo) cell.getElement();
                cell.setText(Long.toString(info.getId()));
            }
        });
        createColumn(table_viewer, table_layout, Messages.CreateTime, 150, 25, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                final ScanInfo info = (ScanInfo) element;
                return NLS.bind(Messages.CreateTimeFmt,
                        DataFormatter.format(info.getCreated()));
            }

            @Override
            public void update(final ViewerCell cell)
            {
                final ScanInfo info = (ScanInfo) cell.getElement();
                cell.setText(DataFormatter.format(info.getCreated()));
            }
        });
        createColumn(table_viewer, table_layout, Messages.Name, 120, 100, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                final ScanInfo info = (ScanInfo) element;
                return NLS.bind(Messages.NameFmt, info.getName());
            }

            @Override
            public void update(final ViewerCell cell)
            {
                final ScanInfo info = (ScanInfo) cell.getElement();
                cell.setText(info.getName());
            }
        });
        createColumn(table_viewer, table_layout, Messages.State, 90, 50, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                final ScanInfo info = (ScanInfo) element;
                return NLS.bind(Messages.StateFmt, info.getState());
            }

            @Override
            public void update(final ViewerCell cell)
            {
                final ScanInfo info = (ScanInfo) cell.getElement();
                cell.setText(info.getState().toString());
                cell.setForeground(getStateColor(display, info));
            }
        });
        final TableViewerColumn perc_col = createColumn(table_viewer, table_layout, Messages.Percent, 25, 25, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                final ScanInfo info = (ScanInfo) element;
                return NLS.bind(Messages.PercentFmt,
                    new Object[]
                    {
                        info.getPerformedWorkUnits(),
                        info.getTotalWorkUnits(),
                        info.getPercentage()
                    });
            }

            @Override
            public void update(final ViewerCell cell)
            {
                /* Custom painted...
                final ScanInfo info = (ScanInfo) cell.getElement();
                cell.setText(Integer.toString(info.getPercentage()));
                */
            }
        });
        createColumn(table_viewer, table_layout, Messages.Runtime, 65, 5, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                return Messages.Runtime_TT;
            }

            @Override
            public void update(final ViewerCell cell)
            {
                final ScanInfo info = (ScanInfo) cell.getElement();
                cell.setText(info.getRuntimeText());
            }
        });
        createColumn(table_viewer, table_layout, Messages.CurrentCommand, 80, 100, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                final ScanInfo info = (ScanInfo) element;
                final String command = info.getCurrentCommand();
                if (command.length() > 0)
                    return NLS.bind(Messages.CurrentCommandFmt, command);
                else
                    return Messages.CurrentCommandEmpty;
            }

            @Override
            public void update(final ViewerCell cell)
            {
                final ScanInfo info = (ScanInfo) cell.getElement();
                cell.setText(info.getCurrentCommand());
            }
        });
        createColumn(table_viewer, table_layout, Messages.Error, 80, 150, new CellLabelProvider()
        {
            @Override
            public String getToolTipText(final Object element)
            {
                final ScanInfo info = (ScanInfo) element;
                final String error = info.getError();
                if (error != null)
                {
                    return NLS.bind(Messages.ErrorMsgFmt, error);
                }
                else
                    return Messages.NoError;
            }

            @Override
            public void update(final ViewerCell cell)
            {
                final ScanInfo info = (ScanInfo) cell.getElement();
                final String error = info.getError();
                if (error == null)
                    cell.setText(""); //$NON-NLS-1$
                else
                    cell.setText(error);
            }
        });

        // Custom-paint the perc_col cells to get progress bar
        table.addListener(SWT.PaintItem, new Listener()
        {
            @Override
            public void handleEvent(final Event event)
            {
                if (event.index != 4) // index of the percent column
                    return;
                final GC gc = event.gc;
                final TableItem item = (TableItem) event.item;
                final ScanInfo info = (ScanInfo) item.getData();
                final Color foreground = gc.getForeground();
                final Color background = gc.getBackground();
                gc.setForeground(getStateColor(display, info));
                gc.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
                final int width = (perc_col.getColumn().getWidth() - 1) * info.getPercentage() / 100;
                gc.fillGradientRectangle(event.x, event.y, width, event.height, false);
                gc.drawRectangle(event.x, event.y, width - 1, event.height - 1);
                gc.setForeground(foreground);
                gc.setBackground(background);
            }
        });

        ColumnViewerToolTipSupport.enableFor(table_viewer);
        table_viewer.setContentProvider(new ScanInfoModelContentProvider());

        Label l = new Label(parent, 0);
        l.setText(Messages.MemInfo);
        l.setLayoutData(new GridData());

        mem_info = new Bar(parent, 0);
        mem_info.setLayoutData(new GridData(SWT.FILL, 0, true, false));
        mem_info.setToolTipText(Messages.MemInfoTT);
    }

    /** @param display Display
     *  @param info ScanInfo
     *  @return Color associated with the state of the scan
     */
    protected Color getStateColor(final Display display, final ScanInfo info)
    {
        switch (info.getState())
        {
        case Idle:
            return display.getSystemColor(SWT.COLOR_DARK_BLUE);
        case Aborted:
        case Failed:
            return display.getSystemColor(SWT.COLOR_RED);
        case Finished:
            return display.getSystemColor(SWT.COLOR_DARK_GREEN);
        default:
            return display.getSystemColor(SWT.COLOR_BLACK);
        }
    }

    /** Helper for creating a resizable column
     *  @return {@link TableViewerColumn}
     */
    private TableViewerColumn createColumn(final TableViewer table_viewer,
            final TableColumnLayout table_layout, final String header,
            final int width, final int weight, final CellLabelProvider label)
    {
        final TableViewerColumn view_col = new TableViewerColumn(table_viewer, 0);
        final TableColumn col = view_col.getColumn();
        col.setText(header);
        col.setMoveable(true);
        col.setResizable(true);
        table_layout.setColumnData(col, new ColumnWeightData(weight, width));
        view_col.setLabelProvider(label);
        return view_col;
    }

    /** Connect actions to GUI */
    private void hookActions()
    {
        // Double-click on scan opens editor
        table_viewer.addDoubleClickListener(new IDoubleClickListener()
        {
            @Override
            public void doubleClick(final DoubleClickEvent event)
            {
                final ScanInfo info = getSelectedScan();
                if (info == null)
                    return;
                new OpenScanTreeAction(info).run();
            }
        });
    }

    /** @return Currently selected {@link ScanInfo} or <code>null</code> */
    private ScanInfo getSelectedScan()
    {
        final IStructuredSelection selection = (IStructuredSelection) table_viewer.getSelection();
        if (selection.isEmpty())
            return null;
        return (ScanInfo) selection.getFirstElement();
    }

    /** Add context menu to table */
    private void createContextMenu()
    {
        final Shell shell = table_viewer.getControl().getShell();
        final MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener()
        {
            @Override
            public void menuAboutToShow(final IMenuManager manager)
            {
                final ScanInfo info = getSelectedScan();
                if (info == null)
                    return;
                if (info.getState() == ScanState.Paused)
                {
                    manager.add(new ResumeAction(shell, model, info));
                    manager.add(new AbortAction(shell, model, info));
                }
                else if (info.getState() == ScanState.Running)
                {
                    manager.add(new PauseAction(shell, model, info));
                    manager.add(new AbortAction(shell, model, info));
                }
                else if (info.getState() == ScanState.Idle)
                {
                    manager.add(new AbortAction(shell, model, info));
                }
                else
                    manager.add(new RemoveAction(shell, model, info));
                manager.add(new Separator());
                manager.add(new RemoveCompletedAction(shell, model));
                manager.add(new Separator());
                manager.add(new OpenPlotAction(info));
                manager.add(new ShowDevicesAction(shell, model, info));
                manager.add(new OpenScanTreeAction(info));
            }
        });

        final Table table = table_viewer.getTable();
        final Menu menu = manager.createContextMenu(table);
        table.setMenu(menu);
    }

	/** @see ScanInfoModelListener */
    @Override
    public void scanServerUpdate(final ScanServerInfo server_info)
    {
    	if (mem_info.isDisposed())
    		return;
    	mem_info.getDisplay().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                if (mem_info.isDisposed())
                    return;
                mem_info.update(server_info.getMemoryInfo(),
                			server_info.getMemoryPercentage());
            }
        });
    }

	/** @see ScanInfoModelListener */
    @Override
    public void scanUpdate(final List<ScanInfo> infos)
    {
        final Table table = table_viewer.getTable();
        if (table.isDisposed())
            return;
        table.getDisplay().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                if (table.isDisposed())
                    return;
                // Received update -> enable table and display info
                table.setEnabled(true);
                table_viewer.refresh();
            }
        });
    }

    /** @see ScanInfoModelListener */
    @Override
    public void connectionError()
    {
        final Table table = table_viewer.getTable();
        if (table.isDisposed())
            return;
        table.getDisplay().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                if (table.isDisposed())
                    return;
                // Disable table to indicate communication problem
                table.setEnabled(false);
            }
        });
    }
}