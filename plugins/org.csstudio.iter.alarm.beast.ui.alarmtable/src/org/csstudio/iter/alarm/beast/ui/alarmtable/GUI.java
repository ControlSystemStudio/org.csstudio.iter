/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.alarm.beast.ui.alarmtable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.csstudio.alarm.beast.client.AlarmTreeItem;
import org.csstudio.alarm.beast.client.AlarmTreePV;
import org.csstudio.alarm.beast.client.AlarmTreeRoot;
import org.csstudio.alarm.beast.client.GUIUpdateThrottle;
import org.csstudio.alarm.beast.ui.AuthIDs;
import org.csstudio.alarm.beast.ui.ContextMenuHelper;
import org.csstudio.alarm.beast.ui.SelectionHelper;
import org.csstudio.alarm.beast.ui.SeverityColorProvider;
import org.csstudio.alarm.beast.ui.actions.AlarmPerspectiveAction;
import org.csstudio.alarm.beast.ui.actions.ConfigureItemAction;
import org.csstudio.alarm.beast.ui.actions.DisableComponentAction;
import org.csstudio.alarm.beast.ui.clientmodel.AlarmClientModel;
import org.csstudio.alarm.beast.ui.clientmodel.AlarmClientModelListener;
import org.csstudio.apputil.text.RegExHelper;
import org.csstudio.iter.alarm.beast.ui.alarmtable.customconfig.DoubleClickHandler;
import org.csstudio.security.SecuritySupport;
import org.csstudio.ui.util.MinSizeTableColumnLayout;
import org.csstudio.ui.util.dnd.ControlSystemDragSource;
import org.csstudio.ui.util.helpers.ComboHistoryHelper;
import org.csstudio.utility.singlesource.SingleSourcePlugin;
import org.csstudio.utility.singlesource.UIHelper.UI;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Alarm table GUI
 * 
 * @author Kay Kasemir
 * @author Jaka Bobnar - Combined/split alarm tables, configurable columns
 */
public class GUI implements AlarmClientModelListener {
    private static final Logger LOGGER = Logger.getLogger(GUI.class.getName());

    /**
     * Persistence: Tags within the memento settings, actually written to
     * WORKSPACE/.metadata/.plugins/org.eclipse.e4.workbench/workbench.xmi
     */
    final private static String ALARM_TABLE_SORT_COLUMN = "alarm_table_sort_column"; //$NON-NLS-1$
    final private static String ALARM_TABLE_SORT_UP = "alarm_table_sort_up"; //$NON-NLS-1$
    final private static String ALARM_TABLE_FILTER_ITEM = "alarm_table_filter_item"; //$NON-NLS-1$

    /**
     * Initial place holder for display of alarm counts to allocate enough screen space
     */
    final private static String ALARM_COUNT_PLACEHOLDER = "999999"; //$NON-NLS-1$

    final private Display display;

    /** Model with all the alarm information */
    final private AlarmClientModel model;

    private DoubleClickHandler[] double_click_handlers;

    /** Labels to show alarm counts */
    private Label current_alarms, acknowledged_alarms;

    /** TableViewer for active alarms */
    private TableViewer active_table_viewer;

    /** TableViewer for acknowledged alarms */
    private TableViewer acknowledged_table_viewer;

    /** PV selection filter text box */
    private Combo filter;

    /** PV un-select button */
    private Button unselect;

    private SeverityColorProvider color_provider;
    private SeverityIconProvider icon_provider;

    /** Is something displayed in <code>error_message</code>? */
    private volatile boolean have_error_message = false;

    /** Error message (no server...) */
    private Label error_message;

    private Composite baseComposite;

    private final boolean separateTables;

    private final ColumnWrapper[] columns;

    private AlarmTreeItem filterItemParent;
    
    private final boolean blinkUnacknowledged = Preferences.isBlinkUnacknowledged();
    private final int blinkPeriod = Preferences.getBlinkingPeriod();
    
    /** GUI updates are throttled to reduce flicker */
    final private GUIUpdateThrottle gui_update = new GUIUpdateThrottle() {
        @Override
        protected void fire() {
            if (display.isDisposed()) {
                return;
            }
            display.syncExec(() -> {
                if (current_alarms.isDisposed()) {
                    return;
                }

                // Display counts, update tables

                // Don't use TableViewer.setInput(), it causes flicker on Linux!
                // active_table_viewer.setInput(model.getActiveAlarms());
                // acknowledged_table_viewer.setInput(model.getAcknowledgedAlarms());
                //
                // Instead, tell ModelInstanceProvider about the data,
                // which then updates the table with setItemCount(), refresh(),
                // as that happens to not flicker.
                updateGUI();
            });
        }
    };

    /**
     * Column editor for the 'ACK' column that acknowledges or un-ack's alarm in that row
     */
    private static class AcknowledgeEditingSupport extends EditingSupport {
        public AcknowledgeEditingSupport(ColumnViewer viewer) {
            super(viewer);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            return new CheckboxCellEditor(((TableViewer) getViewer()).getTable());
        }

        @Override
        protected Object getValue(final Object element) {
            return ((AlarmTreePV) element).getSeverity().isActive();
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (!getViewer().isBusy() && value instanceof Boolean) {
                if (SecuritySupport.havePermission(AuthIDs.ACKNOWLEDGE)) {
                    ((AlarmTreePV) element).acknowledge(!(Boolean) value);
                }
            }
        }

        @Override
        protected boolean canEdit(Object element) {
            return element instanceof AlarmTreePV;
        }
    }

    /**
     * Initialize GUI
     * 
     * @param parent Parent widget
     * @param model Alarm model
     * @param site Workbench site or <code>null</code>
     * @param separateTables true if two tables should be created (one for acked and one for unacked alarms) or false
     *          if only one table should be created
     * @param columns column configuration for the tables
     * @param memento memento that provides the persisted settings (sorting)
     */
    public GUI(final Composite parent, final AlarmClientModel model, final IWorkbenchPartSite site,
            boolean separateTables, ColumnWrapper[] columns, IMemento memento) {
        this.display = parent.getDisplay();
        this.separateTables = separateTables;
        this.model = model;
        this.columns = columns;
        createComponents(parent,memento);
        
        if (memento != null) {
            String filterPath = memento.getString(ALARM_TABLE_FILTER_ITEM);
            if (filterPath != null) {
                filterItemParent = model.getConfigTree().getItemByPath(filterPath);
            }
        }
        
        if (model.isServerAlive()) {
            setErrorMessage(null);
        } else {
            setErrorMessage(org.csstudio.alarm.beast.ui.Messages.WaitingForServer);
        }

        // Subscribe to model updates, arrange to un-subscribe
        model.addListener(this);
        baseComposite.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                model.removeListener(GUI.this);
                gui_update.dispose();
            }
        });

        connectContextMenu(active_table_viewer, site);
        if (separateTables) {
            connectContextMenu(acknowledged_table_viewer, site);
        }

        // Allow 'drag' of alarm info as text
        new ControlSystemDragSource(active_table_viewer.getTable()) {
            @Override
            public Object getSelection() {
                return SelectionHelper.getAlarmTreePVsForDragging((IStructuredSelection) active_table_viewer
                        .getSelection());
            }
        };
                
        if (separateTables) {
            new ControlSystemDragSource(acknowledged_table_viewer.getTable()) {
                @Override
                public Object getSelection() {
                    return SelectionHelper.getAlarmTreePVsForDragging((IStructuredSelection) acknowledged_table_viewer
                            .getSelection());
                }
            };
        }
        updateGUI();
    }

    /** @return Table of active alarms */
    public TableViewer getActiveAlarmTable() {
        return active_table_viewer;
    }

    /** @return Table of acknowledged alarms */
    public TableViewer getAcknowledgedAlarmTable() {
        return separateTables ? acknowledged_table_viewer : active_table_viewer;
    }

    /**
     * Create GUI elements
     * 
     * @param parent Parent widget
     * @param memento the memento that provides the sorting information
     */
    private void createComponents(final Composite parent, IMemento memento) {
        parent.setLayout(new FillLayout());

        ColumnInfo sortColumn = ColumnInfo.SEVERITY;
        boolean sortUp = false;
        if (memento != null) {
            String sort = memento.getString(ALARM_TABLE_SORT_COLUMN);
            if (sort != null)
                sortColumn = ColumnInfo.valueOf(sort);
            Boolean sortDirect = memento.getBoolean(ALARM_TABLE_SORT_UP);
            if (sortDirect != null)
                sortUp = sortDirect;
        }

        if (separateTables) {
            baseComposite = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
            baseComposite.setLayout(new FillLayout());
            color_provider = new SeverityColorProvider(baseComposite);
            icon_provider = new SeverityIconProvider(baseComposite);
            addActiveAlarmSashElement(baseComposite);
            addAcknowledgedAlarmSashElement(baseComposite);
            ((SashForm) baseComposite).setWeights(new int[] { 80, 20 });
        } else {
            baseComposite = new Composite(parent, SWT.NONE);
            baseComposite.setLayout(new FillLayout());
            color_provider = new SeverityColorProvider(baseComposite);
            icon_provider = new SeverityIconProvider(baseComposite);
            addActiveAlarmSashElement(baseComposite);
        }
        syncTables(active_table_viewer, acknowledged_table_viewer, sortColumn, sortUp);
        syncTables(acknowledged_table_viewer, active_table_viewer, sortColumn, sortUp);
        
        // Update selection in active & ack'ed alarm table
        // in response to filter changes
        final ComboHistoryHelper filter_history = new ComboHistoryHelper(Activator.getDefault().getDialogSettings(),
                "FILTER", filter) //$NON-NLS-1$
        {
            @Override
            public void itemSelected(final String selection) {
                final String filter_text = filter.getText().trim();
                // Turn glob-type filter into regex, then pattern
                final Pattern pattern = Pattern.compile(RegExHelper.fullRegexFromGlob(filter_text),
                        Pattern.CASE_INSENSITIVE);
                selectFilteredPVs(pattern, active_table_viewer);
                if (separateTables) {
                    selectFilteredPVs(pattern, acknowledged_table_viewer);
                }
            }
        };
        filter_history.loadSettings();

        // Clear filter, un-select all items
        unselect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                filter.setText(""); //$NON-NLS-1$
                active_table_viewer.setSelection(null, true);
                if (separateTables) {
                    acknowledged_table_viewer.setSelection(null, true);
                }
            }
        });

        gui_update.start();
        blink();
    }
    
    /**
     * Synchronise the UI actions between the two tables. When a sorting column is selected in the primary table the
     * same column should be selected in the secondary table. Similarly, the moving and resizing of columns is also
     * synchronised.
     * 
     * @param primary the table, which is the source of the actions
     * @param secondary the table that is synchronised with the primary table
     * @param sortColumn the column that is being sorted by default
     * @param sortUp default sorting direction
     */
    private void syncTables(final TableViewer primary, final TableViewer secondary, ColumnInfo sortColumn,
            boolean sortUp) {
        if (primary == null) return;
        TableColumn[] priColumns = primary.getTable().getColumns();
        TableColumn[] secColumns = secondary == null ? priColumns : secondary.getTable().getColumns();
        
        for (int i = 0; i < priColumns.length; i++) {
            final TableColumn c = priColumns[i];
            final TableColumn s = secColumns[i];
            final ColumnWrapper w = getColumnWrapper(i);
            AlarmColumnSortingSelector listener = new AlarmColumnSortingSelector(primary, secondary, c, s,
                    w.getColumnInfo());
            c.addSelectionListener(listener);
            if (secondary != null) {
                c.addControlListener(new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e) {
                        s.setWidth(c.getWidth());
                        w.setMinWidth(c.getWidth());
                    }
                    @Override
                    public void controlMoved(ControlEvent e) {
                        secondary.getTable().setColumnOrder(primary.getTable().getColumnOrder());
                    }
                });
            }
            if (w.getColumnInfo() == sortColumn) {
                listener.setSortDirection(sortUp);
            }
        }
    }
    
    /**
     * Return the column wrapper that represents the table column at the given index.
     * @param tableColumnIndex the table column index
     * @return the column wrapper that matches the table column (the n-th visible wrapper)
     */
    private ColumnWrapper getColumnWrapper(int tableColumnIndex) {
        int idx = -1;
        for (ColumnWrapper cw : columns) {
            if (cw.isVisible()) {
                idx++;
            }
            if (tableColumnIndex == idx) {
                return cw; 
            }
        }
        return null;
    }
    
    /**
     * Updates the given columns with the order that is currently applied to the tables. Most of the time
     * the orders are the same, unless user moved the columns around by dragging their headers to a different
     * position.
     * 
     * @param columns the columns to update with the order
     */
    public void updateColumnOrder(ColumnWrapper[] columns) {
        if (active_table_viewer.getTable().isDisposed()) {
            return;
        }
        int[] order = active_table_viewer.getTable().getColumnOrder();
        ColumnWrapper[] ret = new ColumnWrapper[columns.length];
        for (int i = 0; i < order.length; i++) {
            ret[i] = columns[order[i]];
        }
        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].isVisible()) {
                for (int j = 0; j < ret.length; j++) {
                    if (ret[j] == null) {
                        ret[j] = columns[i];
                    }
                }
            } 
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = ret[i];
        }
    }
    
    private void blink() {
        if (blinkUnacknowledged) {
            display.timerExec(blinkPeriod, () -> {
                icon_provider.toggle();
                if (!active_table_viewer.getTable().isDisposed()) {
                    //because of lazy label provider refresh is faster than updating individual cells
                    active_table_viewer.refresh(); 
                    blink();
                }
            });
        } else {
            icon_provider.reset();
        }
    }

    /** 
     * Save table settings into the given memento. The method stores the selected filter item and sorting parameters.
     * 
     * @param memento the destination for the settings
     */
    public void saveState(IMemento memento) {
        if (memento == null)
            return;

        if (filterItemParent != null) {
            memento.putString(ALARM_TABLE_FILTER_ITEM, filterItemParent.getPathName());
        }

        final Table table = active_table_viewer.getTable();
        final TableColumn sort_column = table.getSortColumn();
        if (sort_column == null) {
            return;
        }
        
        final int col_count = table.getColumnCount();
        for (int column = 0; column < col_count; ++column) {
            if (table.getColumn(column) == sort_column) {
                int count = 0;
                for (int i = 0; i < columns.length; i++) {
                    if (columns[i].isVisible()) {
                        if (count == column) {
                            memento.putString(ALARM_TABLE_SORT_COLUMN, columns[i].getColumnInfo().name());
                            break;
                        }
                        count++;
                    }
                }
                memento.putBoolean(ALARM_TABLE_SORT_UP, table.getSortDirection() == SWT.UP);
                break;
            }
        }
    }

    /**
     * Add the sash element for active alarms
     * 
     * @param parent the parent composite
     */
    private void addActiveAlarmSashElement(final Composite parent) 
    {
        final Composite box = new Composite(parent, SWT.BORDER);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 5;
        box.setLayout(layout);

        current_alarms = new Label(box, SWT.NONE);
        current_alarms.setText(NLS.bind(Messages.CurrentAlarmsFmt, new Object[]{ALARM_COUNT_PLACEHOLDER, 
                ALARM_COUNT_PLACEHOLDER, ALARM_COUNT_PLACEHOLDER}));
        current_alarms.setLayoutData(new GridData());

        error_message = new Label(box, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        gd.grabExcessHorizontalSpace = true;
        error_message.setLayoutData(gd);

        Label l = new Label(box, SWT.NONE);
        l.setText(org.csstudio.alarm.beast.ui.Messages.Filter);
        l.setLayoutData(new GridData());

        filter = new Combo(box, SWT.BORDER);
        filter.setToolTipText(org.csstudio.alarm.beast.ui.Messages.FilterTT);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        filter.setLayoutData(gd);

        unselect = new Button(box, SWT.PUSH);
        unselect.setText(org.csstudio.alarm.beast.ui.Messages.Unselect);
        unselect.setToolTipText(org.csstudio.alarm.beast.ui.Messages.UnselectTT);
        gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        unselect.setLayoutData(gd);
        
        if (!separateTables) 
        {
            acknowledged_alarms = new Label(box, SWT.NONE);
            acknowledged_alarms.setText(NLS.bind(Messages.AcknowledgedAlarmsFmt, new Object[]{ALARM_COUNT_PLACEHOLDER,
                    ALARM_COUNT_PLACEHOLDER, ALARM_COUNT_PLACEHOLDER}));
            acknowledged_alarms.setLayoutData(new GridData());
        }

        // Table w/ active alarms
        active_table_viewer = createAlarmTable(box, true);
        active_table_viewer.setInput(null);
        ((AlarmTableContentProvider) active_table_viewer.getContentProvider()).setAlarms(model.getActiveAlarms());
    }

    /**
     * Add the sash element for acknowledged alarms
     * 
     * @param parent the parent composite
     */
    private void addAcknowledgedAlarmSashElement(final Composite parent) 
    {
        final Composite box = new Composite(parent, SWT.BORDER);
        box.setLayout(new GridLayout());

        acknowledged_alarms = new Label(box, SWT.NONE);
        acknowledged_alarms.setText(NLS.bind(Messages.AcknowledgedAlarmsFmt, ALARM_COUNT_PLACEHOLDER));
        acknowledged_alarms.setLayoutData(new GridData());

        // Table w/ ack'ed alarms
        acknowledged_table_viewer = createAlarmTable(box, false);
        acknowledged_table_viewer.setInput(null);
        ((AlarmTableContentProvider) acknowledged_table_viewer.getContentProvider()).setAlarms(model
                .getAcknowledgedAlarms());
    }

    /**
     * Select PVs in table that match filter expression
     * 
     * @param pattern
     *            PV name pattern ('vac', 'amp*trip')
     * @param table_viewer
     *            Table in which to select PVs
     */
    private void selectFilteredPVs(final Pattern pattern, final TableViewer table_viewer) {
        final AlarmTreePV pvs[] = ((AlarmTableContentProvider) table_viewer.getContentProvider()).getAlarms();
        final ArrayList<AlarmTreePV> selected = new ArrayList<AlarmTreePV>();
        for (AlarmTreePV pv : pvs) {
            if (pattern.matcher(pv.getName()).matches() || pattern.matcher(pv.getDescription()).matches()) {
                selected.add(pv);
            }
        }
        table_viewer.setSelection(new StructuredSelection(selected), true);
    }

    /**
     * Create a table viewer for displaying alarms
     * 
     * @param parent Parent widget, uses GridLayout
     * @param is_active_alarm_table true if the table is for the active alarms or false if for acknowledged alarms

     * @return TableViewer, still needs input
     */
    private TableViewer createAlarmTable(final Composite parent, final boolean is_active_alarm_table)
    {
        // TableColumnLayout requires the TableViewer to be in its own Composite
        final GridLayout parent_layout = (GridLayout) parent.getLayout();
        final Composite table_parent = new Composite(parent, 0);
        table_parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, parent_layout.numColumns, 1));

        // Auto-size table columns
        final TableColumnLayout table_layout = new MinSizeTableColumnLayout(10);
        table_parent.setLayout(table_layout);

        final TableViewer table_viewer = new TableViewer(table_parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
                | SWT.FULL_SELECTION | SWT.VIRTUAL);

        // Some tweaks to the underlying table widget
        final Table table = table_viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        ColumnViewerToolTipSupport.enableFor(table_viewer);

        // Connect TableViewer to the Model: Provide content from model...
        table_viewer.setContentProvider(new AlarmTableContentProvider());

        // Create the columns of the table, using a fixed initial width.
        for (ColumnWrapper cw : columns) {
            if (!cw.isVisible()) {
                continue;
            }
            // Create auto-size column
            final TableViewerColumn view_col = new TableViewerColumn(table_viewer, 0);
            final TableColumn table_col = view_col.getColumn();
            table_layout.setColumnData(table_col, new ColumnWeightData(cw.getWeight(), cw.getMinWidth()));
            table_col.setText(cw.getName());
            table_col.setMoveable(true);
            
            ColumnInfo col_info = cw.getColumnInfo();
            // Tell column how to display the model elements
            view_col.setLabelProvider(new AlarmTableLabelProvider(icon_provider, color_provider, col_info));
            // Sort support            

            if (col_info == ColumnInfo.ACK) {
                if (model.isWriteAllowed())
                    view_col.setEditingSupport(new AcknowledgeEditingSupport(table_viewer));
                table_col.setToolTipText(org.csstudio.alarm.beast.ui.Messages.AcknowledgeColumnHeaderTooltip);
            }
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Table table = (Table) e.getSource();
                TableItem item = table.getItem(new Point(e.x, e.y));
                if (item != null && item.getData() instanceof AlarmTreePV) {
                    AlarmTreePV pv = (AlarmTreePV) item.getData();
                    if (is_active_alarm_table) {
                        for (DoubleClickHandler h : getDoubleClickHandlers()) {
                            h.activeTableDoubleClicked(pv);
                        }
                    } else {
                        for (DoubleClickHandler h : getDoubleClickHandlers()) {
                            h.acknowledgedTableDoubleClicked(pv);
                        }
                    }
                }
            }
        });
        
        return table_viewer;
    }

    /**
     * Add context menu to tree
     * 
     * @param table_viewer
     *            TableViewer to which to add the menu
     * @param site
     *            Workbench site or <code>null</code>
     */
    private void connectContextMenu(final TableViewer table_viewer, final IWorkbenchPartSite site) {
        final Table table = table_viewer.getTable();
        final boolean isRcp = UI.RCP.equals(SingleSourcePlugin.getUIHelper().getUI());

        final MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            // Dynamically build menu based on current selection
            @Override
            @SuppressWarnings("unchecked")
            public void menuAboutToShow(IMenuManager manager) {
                final Shell shell = table.getShell();
                final List<AlarmTreeItem> items = ((IStructuredSelection) table_viewer.getSelection()).toList();

                new ContextMenuHelper(table_viewer, manager, shell, items, model.isWriteAllowed());
                manager.add(new Separator());
                // Add edit items
                if (items.size() == 1 && model.isWriteAllowed()) {
                    final AlarmTreeItem item = items.get(0);
                    manager.add(new ConfigureItemAction(shell, model, item));
                }
                if (items.size() >= 1 && model.isWriteAllowed()) {
                    manager.add(new DisableComponentAction(shell, model, items));
                }
                manager.add(new Separator());
                if (isRcp) {
                    manager.add(new AlarmPerspectiveAction());
                    manager.add(new Separator());
                }
                // Placeholder for CSS PV contributions
                manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        table.setMenu(manager.createContextMenu(table));

        // Allow extensions to add to the context menu
        if (site != null) {
            site.registerContextMenu(manager, table_viewer);
        }
    }

    /**
     * Set or clear error message. Setting an error message also disables the GUI.
     * <p>
     * OK to call multiple times or after disposal.
     * 
     * @param error
     *            Error message or <code>null</code> to clear error
     */
    public void setErrorMessage(final String error) {
        final Table act_table = active_table_viewer.getTable();
        if (act_table.isDisposed()) {
            return;
        }
        if (error == null) {
            if (!have_error_message) {
                // msg already cleared, GUI already enabled
                return; 
            }
            error_message.setText(""); //$NON-NLS-1$
            error_message.setBackground(null);
            act_table.setEnabled(true);
            if (separateTables) {
                acknowledged_table_viewer.getTable().setEnabled(true);
            }
            have_error_message = false;
        } else {
            // Update the message
            error_message.setText(error);
            error_message.setBackground(display.getSystemColor(SWT.COLOR_MAGENTA));
            error_message.getParent().layout();
            if (have_error_message) {
             // GUI already disabled
                return; 
            }
            act_table.setEnabled(false);
            if (separateTables) {
                acknowledged_table_viewer.getTable().setEnabled(false);
            }
            have_error_message = true;
        }
    }

    // @see AlarmClientModelListener
    @Override
    public void serverModeUpdate(AlarmClientModel model, boolean maintenanceMode) {
        // Ignored
    }

    // @see AlarmClientModelListener
    @Override
    public void serverTimeout(final AlarmClientModel model) {
        display.asyncExec(() -> setErrorMessage(org.csstudio.alarm.beast.ui.Messages.ServerTimeout));
    }

    // For now, the table responds to any changes with a full update
    // @see AlarmClientModelListener
    @Override
    public void newAlarmConfiguration(final AlarmClientModel model) {
        gui_update.trigger();
        display.asyncExec(() -> {
            if (model.isServerAlive()) {
                setErrorMessage(null);
            } else {
                setErrorMessage(org.csstudio.alarm.beast.ui.Messages.WaitingForServer);
            }
        });
    }

    // @see AlarmClientModelListener
    @Override
    public void newAlarmState(final AlarmClientModel model, final AlarmTreePV pv, final boolean parent_changed) {
        gui_update.trigger();

        if (model.isServerAlive() && have_error_message) {
            // Clear error message now that we have info from the alarm server
            display.asyncExec(() -> setErrorMessage(null));
        }
    }
    
    private AlarmTreePV[] filter(AlarmTreePV[] alarms) {
        if (filterItemParent == null || filterItemParent instanceof AlarmTreeRoot || alarms.length == 0) {
            return alarms;
        } else {
            List<AlarmTreePV> items = new ArrayList<>(alarms.length);
            AlarmTreeItem item;
            for (AlarmTreePV pv : alarms) {
                item = pv;
                do {
                    if (item == filterItemParent) {
                        items.add(pv);
                        break;
                    }
                    item = item.getParent();
                } while (item != null && !(item instanceof AlarmTreeRoot));

            }
            return items.toArray(new AlarmTreePV[items.size()]);
        }
    }

    private void updateGUI() {
        AlarmTreePV[] rawAlarms = model.getActiveAlarms();
        AlarmTreePV[] alarms = filter(rawAlarms);
        if (filterItemParent == null || filterItemParent instanceof AlarmTreeRoot) {
            current_alarms.setText(NLS.bind(org.csstudio.alarm.beast.ui.Messages.CurrentAlarmsFmt, alarms.length));    
        } else {
            current_alarms.setText(NLS.bind(Messages.CurrentAlarmsFmt, new Object[]{alarms.length, rawAlarms.length,
                    filterItemParent.getPathName()}));
        }
  
        if (alarms.length != rawAlarms.length) {
            current_alarms.setForeground(current_alarms.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else {
            current_alarms.setForeground(null);
        }
        current_alarms.pack();

        rawAlarms = model.getAcknowledgedAlarms();
        AlarmTreePV[] ackalarms = filter(rawAlarms);
        if (filterItemParent == null || filterItemParent instanceof AlarmTreeRoot) {
            acknowledged_alarms.setText(NLS.bind(org.csstudio.alarm.beast.ui.Messages.AcknowledgedAlarmsFmt, 
                    ackalarms.length));
        } else {
            acknowledged_alarms.setText(NLS.bind(Messages.AcknowledgedAlarmsFmt, new Object[]{ackalarms.length, 
                            rawAlarms.length, filterItemParent.getPathName()}));
        }
        
        if (ackalarms.length != rawAlarms.length) {
            acknowledged_alarms.setForeground(acknowledged_alarms.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else {
            acknowledged_alarms.setForeground(null);
        }
        acknowledged_alarms.pack();
        
        if (separateTables) {
            ((AlarmTableContentProvider) active_table_viewer.getContentProvider()).setAlarms(alarms);
            ((AlarmTableContentProvider) acknowledged_table_viewer.getContentProvider()).setAlarms(ackalarms);
        } else {
            AlarmTreePV[] items = new AlarmTreePV[alarms.length + ackalarms.length];
            System.arraycopy(alarms, 0, items, 0, alarms.length);
            System.arraycopy(ackalarms, 0, items, alarms.length, ackalarms.length);
            ((AlarmTableContentProvider) active_table_viewer.getContentProvider()).setAlarms(items);
        }
    }

    void dispose() {
        baseComposite.dispose();
    }

    private DoubleClickHandler[] getDoubleClickHandlers() {
        if (double_click_handlers == null) {
            final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
                    DoubleClickHandler.EXTENSION_ID);
            final List<DoubleClickHandler> list = new ArrayList<DoubleClickHandler>();
            for (IConfigurationElement e : config) {
                if (DoubleClickHandler.NAME.equals(e.getName())) {
                    try {
                        list.add((DoubleClickHandler) e.createExecutableExtension("class"));
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error loading extension point " + e.getName(), ex);
                    }
                }
            }
            double_click_handlers = list.toArray(new DoubleClickHandler[list.size()]);
        }
        return double_click_handlers;
    }

    void setFilterItem(AlarmTreeItem filterItemParent) {
        this.filterItemParent = filterItemParent;
        updateGUI();
    }
    
    AlarmTreeItem getFilterItem() {
        return this.filterItemParent;
    }
}
