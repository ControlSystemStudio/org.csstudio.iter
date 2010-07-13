/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser.propsheet;

import org.csstudio.platform.ui.swt.AutoSizeColumn;
import org.csstudio.platform.ui.swt.AutoSizeControlListener;
import org.csstudio.swt.xygraph.undo.OperationsManager;
import org.csstudio.trends.databrowser.Activator;
import org.csstudio.trends.databrowser.Messages;
import org.csstudio.trends.databrowser.model.AxisConfig;
import org.csstudio.trends.databrowser.model.Model;
import org.csstudio.trends.databrowser.model.ModelItem;
import org.csstudio.trends.databrowser.model.ModelListener;
import org.csstudio.trends.databrowser.model.PVItem;
import org.csstudio.trends.databrowser.ui.ColorRegistry;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/** Helper for a 'Axes' TableViewer that handles the Model's axes.
 *  Each 'row' in the table is an AxisConfig.
 *  @author Kay Kasemir
 */
public class AxesTableHandler implements ILazyContentProvider
{
    final private ColorRegistry color_registry;
    final OperationsManager operations_manager;
    final private TableViewer axes_table;
    private Model model;
    
    /** Listen to model changes regarding axes.
     *  Ignore configuration of individual items.
     */
    final private ModelListener model_listener = new ModelListener()
    {
        public void changedUpdatePeriod()                { /* NOP */ }
        public void changedColors()                      { /* NOP */ }
        public void changedTimerange()                   { /* NOP */ }

        public void changedAxis(AxisConfig axis)
        {
            if (axis != null)
            {
                axes_table.refresh(axis);
                return;
            }
            // Force total refresh
            axes_table.setItemCount(model.getAxisCount());
            axes_table.refresh();
        }

        public void itemAdded(ModelItem item)            { /* NOP */ }
        public void itemRemoved(ModelItem item)          { /* NOP */ }
        public void changedItemVisibility(ModelItem item){ /* NOP */ }
        public void changedItemLook(ModelItem item)      { /* NOP */ }

        public void changedItemDataConfig(PVItem item)   { /* NOP */ }
        public void scrollEnabled(boolean scrollEnabled) { /* NOP */ }        
    };

    /** Initialize
     *  @param color_registry ColorRegistry
     */
    public AxesTableHandler(final ColorRegistry color_registry,
            final Composite parent,
            final OperationsManager operations_manager)
    {
        this.color_registry = color_registry;
        this.operations_manager = operations_manager;
        axes_table = new TableViewer(parent ,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION
                | SWT.VIRTUAL);
        final Table table = axes_table.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        createColumns();
        
        axes_table.setContentProvider(this);
        
        createContextMenu();
    }

    /** @return TableViewer for the axes table */
    TableViewer getAxesTable()
    {
        return axes_table;
    }
    
    /** Create table columns: Auto-sizable, with label provider and editor */
    private void createColumns()
    {
        TableViewerColumn col;
        // Axis Name Column ----------
        col = AutoSizeColumn.make(axes_table, Messages.ValueAxisName, 100, 100);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final AxisConfig axis = (AxisConfig) cell.getElement();
                cell.setText(axis.getName());
            }
        });
        col.setEditingSupport(new EditSupportBase(axes_table)
        {
            @Override
            protected Object getValue(final Object element)
            {
                return ((AxisConfig) element).getName();
            }

            @Override
            protected void setValue(final Object element, final Object value)
            {
                final AxisConfig axis = (AxisConfig)element;
                final String name = value.toString().trim();
                if (name.equals(axis.getName()))
                    return;
                final ChangeAxisConfigCommand config =
                    new ChangeAxisConfigCommand(operations_manager, axis);
                axis.setName(name);
                config.rememberNewConfig();
            }
        });
        
        // Color Column ----------
        col = AutoSizeColumn.make(axes_table, Messages.Color, 40, 5);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final AxisConfig axis = (AxisConfig) cell.getElement();
                cell.setBackground(color_registry.getColor(axis.getColor()));
            }
        });
        col.setEditingSupport(new EditSupportBase(axes_table)
        {
            @Override
            protected CellEditor getCellEditor(final Object element)
            {
                return new RGBCellEditor(axes_table.getTable());
            }

            @Override
            protected Object getValue(final Object element)
            {
                return ((AxisConfig) element).getColor();
            }

            @Override
            protected void setValue(final Object element, final Object value)
            {
                final AxisConfig axis = (AxisConfig)element;
                final ChangeAxisConfigCommand command =
                    new ChangeAxisConfigCommand(operations_manager, axis);
                axis.setColor((RGB)value);
                command.rememberNewConfig();
            }
        });

        // Minimum value Column ----------
        col = AutoSizeColumn.make(axes_table, Messages.AxisMin, 80, 100);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final AxisConfig axis = (AxisConfig) cell.getElement();
                cell.setText(Double.toString(axis.getMin()));
            }
        });
        col.setEditingSupport(new EditSupportBase(axes_table)
        {
            @Override
            protected Object getValue(final Object element)
            {
                return Double.toString(((AxisConfig) element).getMin());
            }

            @Override
            protected void setValue(final Object element, final Object value)
            {
                try
                {
                    final AxisConfig axis = (AxisConfig)element;
                    final double limit = Double.parseDouble(value.toString().trim());
                    if (limit == axis.getMin())
                        return;
                    final ChangeAxisConfigCommand command =
                        new ChangeAxisConfigCommand(operations_manager, axis);
                    axis.setRange(limit, axis.getMax());
                    command.rememberNewConfig();
                }
                catch (NumberFormatException ex)
                {
                    // NOP, leave as is
                }
            }
        });
        
        // Maximum value Column ----------
        col = AutoSizeColumn.make(axes_table, Messages.AxisMax, 80, 100);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final AxisConfig axis = (AxisConfig) cell.getElement();
                cell.setText(Double.toString(axis.getMax()));
            }
        });
        col.setEditingSupport(new EditSupportBase(axes_table)
        {
            @Override
            protected Object getValue(final Object element)
            {
                return Double.toString(((AxisConfig) element).getMax());
            }

            @Override
            protected void setValue(final Object element, final Object value)
            {
                try
                {
                    final AxisConfig axis = (AxisConfig)element;
                    final double limit = Double.parseDouble(value.toString().trim());
                    if (limit == axis.getMax())
                        return;
                    final ChangeAxisConfigCommand command =
                        new ChangeAxisConfigCommand(operations_manager, axis);
                    axis.setRange(axis.getMin(), limit);
                    command.rememberNewConfig();
                }
                catch (NumberFormatException ex)
                {
                    // NOP, leave as is
                }
            }
        });

        // Auto scale Column ----------
        col = AutoSizeColumn.make(axes_table, Messages.AutoScale, 80, 10);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final AxisConfig axis = (AxisConfig) cell.getElement();
                if (axis.isAutoScale())
                    cell.setImage(Activator.getDefault().getImage("icons/checked.gif")); //$NON-NLS-1$
                else
                    cell.setImage(Activator.getDefault().getImage("icons/unchecked.gif")); //$NON-NLS-1$
            }
        });
        col.setEditingSupport(new EditSupportBase(axes_table)
        {
            @Override
            protected CellEditor getCellEditor(final Object element)
            {
                return new CheckboxCellEditor(((TableViewer)getViewer()).getTable());
            }

            @Override
            protected Object getValue(final Object element)
            {
                return ((AxisConfig) element).isAutoScale();
            }

            @Override
            protected void setValue(final Object element, final Object value)
            {
                try
                {
                    final AxisConfig axis = (AxisConfig)element;
                    final ChangeAxisConfigCommand command =
                        new ChangeAxisConfigCommand(operations_manager, axis);
                    axis.setAutoScale(((Boolean)value).booleanValue());
                    command.rememberNewConfig();
                }
                catch (NumberFormatException ex)
                {
                    // NOP, leave as is
                }
            }
        });
        
        // Log scale Column ----------
        col = AutoSizeColumn.make(axes_table, Messages.LinLogScaleType, 80, 10);
        col.setLabelProvider(new CellLabelProvider()
        {
            @Override
            public void update(final ViewerCell cell)
            {
                final AxisConfig axis = (AxisConfig) cell.getElement();
                if (axis.isLogScale())
                    cell.setText(Messages.LogScale);
                else
                    cell.setText(Messages.LinacScale);
            }
        });
        col.setEditingSupport(new EditSupportBase(axes_table)
        {
            @Override
            protected CellEditor getCellEditor(final Object element)
            {
                return new CheckboxCellEditor(((TableViewer)getViewer()).getTable());
            }

            @Override
            protected Object getValue(final Object element)
            {
                return ((AxisConfig) element).isLogScale();
            }

            @Override
            protected void setValue(final Object element, final Object value)
            {
                try
                {
                    final AxisConfig axis = (AxisConfig)element;
                    final ChangeAxisConfigCommand command =
                        new ChangeAxisConfigCommand(operations_manager, axis);
                    axis.setLogScale(((Boolean)value).booleanValue());
                    command.rememberNewConfig();
                }
                catch (NumberFormatException ex)
                {
                    // NOP, leave as is
                }
            }
        });
        
        new AutoSizeControlListener(axes_table.getTable());
    }

    /** Add context menu to axes_table */
    private void createContextMenu()
    {
        final MenuManager menu = new MenuManager();
        menu.setRemoveAllWhenShown(true);
        menu.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                menu.add(new AddAxisAction(operations_manager, model));
                if (!axes_table.getSelection().isEmpty())
                    menu.add(new DeleteAxesAction(operations_manager, axes_table, model));
                if (model.getEmptyAxis() != null)
                    menu.add(new RemoveUnusedAxesAction(operations_manager, model));
            }
        });
        final Table table = axes_table.getTable();
        table.setMenu(menu.createContextMenu(table));
    }

    /** Set input to a Model
     *  @see ILazyContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(final Viewer viewer, final Object old_model, final Object new_model)
    {
        if (old_model != null)
            ((Model)old_model).removeListener(model_listener);
            
        model = (Model) new_model;
        if (model == null)
            return;

        axes_table.setItemCount(model.getAxisCount());
        model.addListener(model_listener);
    }

    /** Called by ILazyContentProvider to get the ModelItem for a table row
     *  {@inheritDoc}
     */
    public void updateElement(int index)
    {
        axes_table.replace(model.getAxis(index), index);
    }

    // ILazyContentProvider
    public void dispose()
    {
        // NOP
    }
}