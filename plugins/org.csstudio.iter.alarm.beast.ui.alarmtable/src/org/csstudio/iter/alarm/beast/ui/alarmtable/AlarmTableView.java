/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.alarm.beast.ui.alarmtable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.csstudio.alarm.beast.client.AlarmTreeItem;
import org.csstudio.alarm.beast.client.AlarmTreeRoot;
import org.csstudio.alarm.beast.ui.actions.AcknowledgeAction;
import org.csstudio.alarm.beast.ui.actions.MaintenanceModeAction;
import org.csstudio.alarm.beast.ui.clientmodel.AlarmClientModel;
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.ColumnConfigureAction;
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.LockTreeSelectionAction;
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.NewTableAction;
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.SeparateCombineTablesAction;
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.SynchronizeWithTreeAction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * Eclipse View for the alarm table.
 * 
 * @author Kay Kasemir
 * @author Jaka Bobnar - Combined/split alarm tables, configurable columns
 */
public class AlarmTableView extends ViewPart
{
    
    public static AtomicInteger secondaryId = new AtomicInteger(1);
    
    /**
     * Return the next secondary id that has not been opened.
     *
     * @return part
     */
    public static String newSecondaryID(IViewPart part) {
        while (part.getSite().getPage().findViewReference(
                part.getSite().getId(), String.valueOf(secondaryId.get())) != null) {
            secondaryId.incrementAndGet();
        }

        return String.valueOf(secondaryId.get());
    }
    
    /** ID of view, defined in plugin.xml */
    public static final String ID = "org.csstudio.iter.alarm.beast.ui.alarmtable.view"; //$NON-NLS-1$
    private static final String ALARM_TREE_ID = "org.csstudio.alarm.beast.ui.alarmtree.View";

    private AlarmClientModel model;

    private Composite parent;
    private GUI gui;

    @Inject
    private ESelectionService selectionService;

    private IMemento memento;
    
    /** Combined active and acknowledge alarms, group into separate tables? */
    private boolean combinedTables;
    /** Show alarms belonging to the selected alarm tree item or all alarms*/
    private boolean syncWithTree;
    /** Update the selected filter when the selection change or keep the previous item */
    private boolean lockTreeSelection;

    private ColumnWrapper[] columns = ColumnWrapper.getNewWrappers();

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site);
        this.memento = memento;
        IEclipseContext context = (IEclipseContext) getSite().getService(IEclipseContext.class);
        ContextInjectionFactory.inject(this, context);
    }
    
    @Override
    public void dispose()
    {
        if (secondaryId.get() > 1) 
            secondaryId.decrementAndGet();
        super.dispose();
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        this.parent = parent;
        
        try
        {
            model = AlarmClientModel.getInstance();
        } catch (final Throwable ex)
        { // Instead of actual GUI, create error message
            final String error = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            final String message = NLS.bind(org.csstudio.alarm.beast.ui.Messages.ServerErrorFmt, error);
            // Add to log, also display in text widget
            Logger.getLogger(Activator.ID).log(Level.SEVERE, "Cannot load alarm model", ex); //$NON-NLS-1$
            parent.setLayout(new FillLayout());
            new Text(parent, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI).setText(message);
            return;
        }

        // Arrange for model to be released
        parent.addDisposeListener(new DisposeListener()
        {
            @Override
            public void widgetDisposed(DisposeEvent e)
            {
                model.release();
                model = null;
            }
        });

        applyPreferences();
        makeGUI();
        createToolbar();
        
        selectionService.addSelectionListener(ALARM_TREE_ID, new ISelectionListener()
        {
            public void selectionChanged(MPart part, Object selection)
            {
                selectFromTree(selection);
            }
        });
        selectFromTree(selectionService.getSelection(ALARM_TREE_ID));
    }
    
    private void applyPreferences() 
    {
        if (memento == null)
        {
            this.combinedTables = Preferences.isCombinedAlarmTable();
            this.syncWithTree = Preferences.isSynchronizeWithTree();
            this.lockTreeSelection = Preferences.isLockTreeSelection();
            this.columns = ColumnWrapper.fromSaveArray(Preferences.getColumns()); 
        } 
        else 
        {
            Boolean groupSet = memento.getBoolean(Preferences.ALARM_TABLE_COMBINED_TABLES);
            this.combinedTables = groupSet == null ? Preferences.isCombinedAlarmTable() : groupSet; 
            
            Boolean syncWithTreeSet = memento.getBoolean(Preferences.ALARM_TABLE_SYNC_WITH_TREE);
            this.syncWithTree = syncWithTreeSet == null ? Preferences.isSynchronizeWithTree() : syncWithTreeSet; 
            
            Boolean lockSelectionSet = memento.getBoolean(Preferences.ALARM_TABLE_LOCK_SELECTION);
            this.lockTreeSelection = lockSelectionSet == null ? Preferences.isLockTreeSelection() : lockSelectionSet; 
          
            this.columns = ColumnWrapper.restoreColumns(memento.getChild(Preferences.ALARM_TABLE_COLUMN_SETTING));       
        } 
    }
    
    @Override
    public void saveState(IMemento memento)
    {
        super.saveState(memento);
        memento.putBoolean(Preferences.ALARM_TABLE_COMBINED_TABLES, combinedTables);
        memento.putBoolean(Preferences.ALARM_TABLE_SYNC_WITH_TREE, syncWithTree);
        memento.putBoolean(Preferences.ALARM_TABLE_LOCK_SELECTION, lockTreeSelection);
        
        IMemento columnsMemento = memento.createChild(Preferences.ALARM_TABLE_COLUMN_SETTING);
        ColumnWrapper.saveColumns(columnsMemento, getUpdatedColumns());
        if (gui != null) 
            gui.saveState(memento);

    }

    private void createToolbar()
    {
        final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        if (model.isWriteAllowed())
        {
            toolbar.add(new MaintenanceModeAction(model));
            toolbar.add(new Separator());
            AcknowledgeAction action = new AcknowledgeAction(true, gui.getActiveAlarmTable());
            action.clearSelectionOnAcknowledgement(gui.getActiveAlarmTable());
            toolbar.add(action);
            action = new AcknowledgeAction(false, gui.getAcknowledgedAlarmTable());
            action.clearSelectionOnAcknowledgement(gui.getAcknowledgedAlarmTable());
            toolbar.add(action);
            toolbar.add(new Separator());
        }

        final SynchronizeWithTreeAction syncAction = new SynchronizeWithTreeAction(this, syncWithTree);
        final LockTreeSelectionAction lockTreeSelectionAction = new LockTreeSelectionAction(this, lockTreeSelection);
        syncAction.addPropertyChangeListener(new IPropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent event)
            {
                if (IAction.CHECKED.equals(event.getProperty()))
                    lockTreeSelectionAction.setEnabled((Boolean) event.getNewValue());
            }
        });
        lockTreeSelectionAction.setEnabled(syncAction.isChecked());
        toolbar.add(syncAction);
        toolbar.add(lockTreeSelectionAction);
        toolbar.add(new Separator());

        final IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        menu.add(new SeparateCombineTablesAction(this, true, combinedTables));
        menu.add(new SeparateCombineTablesAction(this, false, !combinedTables));
        menu.add(new Separator());
        menu.add(new ColumnConfigureAction(this));
        menu.add(new Separator());
        menu.add(new NewTableAction(this));
    }

    private void selectFromTree(Object selection)
    {
        AlarmTreeItem item = null;
        if (selection instanceof TreeSelection)
        {
            item = (AlarmTreeItem) ((TreeSelection) selection).getFirstElement();
        } 
        else if (selection instanceof AlarmTreeItem)
        {
            item = (AlarmTreeItem)selection;
        }
        if (syncWithTree) 
        {
            if (item != null && (!lockTreeSelection || gui.getFilterItem() == null))                 
                gui.setFilterItem(item);    
            else if (!lockTreeSelection)
                gui.setFilterItem(null);
        } 
        else 
        {
            gui.setFilterItem(null);
        }
        item = gui.getFilterItem();
        if (item == null || item instanceof AlarmTreeRoot) 
        {
            setPartName(NLS.bind(Messages.AlarmTablePartName, Messages.AlarmTableRootName));
            setTitleToolTip(NLS.bind(Messages.AlarmTableTitleTT, Messages.AlarmTableRootName));
        }
        else
        {
            setPartName(NLS.bind(Messages.AlarmTablePartName, item.getName()));
            setTitleToolTip(NLS.bind(Messages.AlarmTableTitleTT, item.getPathName()));
        }
            
        
    }

    /**
     * @return the columns as they are currently visible and ordered in the table
     */
    public ColumnWrapper[] getUpdatedColumns()
    {
        ColumnWrapper[] columns = ColumnWrapper.getCopy(this.columns); 
        if (gui != null) {
            gui.updateColumnOrder(columns);
        }
        return columns;
    }

    /**
     * Set the columns for the table. The table will display the columns in the provided order and will show only those
     * columns that have the visible flag set to true
     * 
     * @param columns the columns to set on the table
     */
    public void setColumns(ColumnWrapper[] columns)
    {
        this.columns = columns;
        redoGUI();
    }

    private void makeGUI()
    {
        if (parent.isDisposed())
            return;
        if (gui != null)
        {
            gui.saveState(memento);
            gui.dispose();
        }
        gui = new GUI(parent, model, getSite(), !combinedTables, columns, memento);
        selectFromTree(selectionService.getSelection(ALARM_TREE_ID));
    }

    private void redoGUI()
    {
        if (gui != null)
        {
            parent.getDisplay().asyncExec(() ->
            {
                makeGUI();
                parent.layout();
            });
        }
    }

    @Override
    public void setFocus()
    {
        // NOP
    }

    /**
     * Combine all alarms into a single table or group the alarms into two separate tables (by the acknowledge status).
     * 
     * @param combinedTables true if the acknowledged and unacknowledged alarms should be displayed in a single table,
     *          or false if they should be displayed in separate tables
     */
    public void setCombinedTables(boolean separated)
    {
        this.combinedTables = separated;
        redoGUI();
    }

    /**
     * Sets the flag whether the list of alarms should be synchronized with the selected alarm tree item. When 
     * true the table will only display those alarms that are descendants of the tree item selected in the Alarm Tree.
     * If false the table will display all alarms from the currently selected alarm root.
     * 
     * @param syncWithTree true if only the alarms that are descendants of the selected tree item should be displayed or
     *          false if all alarms should be displayed
     */
    public void setSyncAlarmsWithTreeSelection(boolean syncWithTree)
    {
        this.syncWithTree = syncWithTree;
        selectFromTree(selectionService.getSelection(ALARM_TREE_ID));
    }

    /**
     * Toggles the lock on the selected tree item. When locked the table will always display the alarms that belong to
     * the tree item that was selected in the alarm tree at the time when the lock was pressed. When unlocked the table
     * will display the alarms according to what was set by {@link #setSyncAlarmsWithTreeSelection(boolean)}.
     * 
     * @param lock true if the table should be locked to a tree item or false if the content should change when the tree
     *            selection changes
     */
    public void setLockTreeSelection(boolean lock)
    {
        this.lockTreeSelection = lock;
        selectFromTree(selectionService.getSelection(ALARM_TREE_ID));
    }

}
