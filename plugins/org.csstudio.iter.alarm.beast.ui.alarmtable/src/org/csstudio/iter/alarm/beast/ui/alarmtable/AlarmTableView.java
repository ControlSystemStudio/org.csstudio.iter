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
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.ResetColumnsAction;
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.SeparateCombineTablesAction;
import org.csstudio.iter.alarm.beast.ui.alarmtable.actions.ShowFilterAction;
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
    /** Property ID for the synchronise with tree property change events */
    public static final int PROP_SYNC_WITH_TREE = 555444;
    /** Property ID for the lock tree selection property change events */
    public static final int PROP_LOCK_TREE = 555445;
    
    private static AtomicInteger secondaryId = new AtomicInteger(1);
    
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
    /** Should severity icons blink or not */
    private boolean blinkingIcons;

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
        this.blinkingIcons = Preferences.isBlinkUnacknowledged(); 
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
            
//            Boolean blinkSet = memento.getBoolean(Preferences.ALARM_TABLE_BLINK_UNACKNOWLEDGED);
//            this.blinkingIcons = blinkSet == null ? Preferences.isBlinkUnacknowledged() : blinkSet;
          
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
//        memento.putBoolean(Preferences.ALARM_TABLE_BLINK_UNACKNOWLEDGED, blinkingIcons);
        
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
        menu.add(new NewTableAction(this));
        menu.add(new Separator());
        menu.add(new SeparateCombineTablesAction(this, true, combinedTables));
        menu.add(new SeparateCombineTablesAction(this, false, !combinedTables));
        menu.add(new Separator());
        menu.add(new ColumnConfigureAction(this));
        menu.add(new ResetColumnsAction(this));
        menu.add(new Separator());
        menu.add(new ShowFilterAction(this));
        //no need to allow users to enable 
//        menu.add(new Separator());
//        menu.add(new BlinkingToggleAction(this, blinkingIcons));
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
        setFilterItem(item,false);
    }
    
    /**
     * Set the filter item by its path. The path is transformed to an actual item, which is then applied as the 
     * filter item. If the item does not exist a null filter is applied.
     * 
     * @see AlarmTableView#setFilterItem(AlarmTreeItem)
     * 
     * @param path the path to filter on
     */
    public void setFilterItem(String path) {
        AlarmTreeRoot root = model.getConfigTree().getRoot();
        AlarmTreeItem item = root.getItemByPath(path);
        setFilterItem(item,true);
    }
    
    /**
     * Set the filter item. Only the alarms that are descendants of the given item will be displayed in the table.
     * The item must match the actual item from the shared model. A clone or a copy with the same path might result
     * in strange behaviour.
     * 
     * @param item the item to filter on
     * @param alwaysIfNotNull if true the item will be set as a filter even if the tree is locked
     */
    private void setFilterItem(AlarmTreeItem item, boolean alwaysIfNotNull) {
        if (getFilterItem() == item) return;
        if (syncWithTree) 
        {
            if (item != null && (!lockTreeSelection || gui.getFilterItem() == null || alwaysIfNotNull))                 
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
     * @return the currently applied filter item
     */
    public AlarmTreeItem getFilterItem() {
        return gui.getFilterItem();
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
        gui.setBlinking(blinkingIcons);
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
        firePropertyChange(PROP_SYNC_WITH_TREE);
    }
    
    /**
     * @return true if the filter item is synchronised with alarm tree selection
     */
    public boolean isSynchAlarmsWithTreeSelection() 
    {
        return this.syncWithTree;
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
        firePropertyChange(PROP_LOCK_TREE);
    }
    
    /**
     * @return true if the filter item is locked on some alarm tree item
     */
    public boolean isLockTreeSelection() 
    {
        return this.lockTreeSelection;
    }
    
    /**
     * Enables or disables blinking of icons of the unacknowledged alarms.
     * 
     * @param blinking true if the icons should be blinking or false otherwise
     */
    public void setBlinkingIcons(boolean blinking)
    {
        this.blinkingIcons = blinking;
        if (gui != null) {
            gui.setBlinking(blinking);
        }
    }
    
    /**
     * @return the alarm client model used by this table
     */
    public AlarmClientModel getModel() {
        return model;
    }

}
