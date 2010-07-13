package org.csstudio.platform.ui.swt.stringtable;

import java.util.Arrays;
import java.util.List;

import org.csstudio.platform.ui.CSSPlatformUiPlugin;
import org.csstudio.platform.ui.internal.localization.Messages;
import org.csstudio.platform.ui.swt.AutoSizeColumn;
import org.csstudio.platform.ui.swt.AutoSizeControlListener;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

/** Editor for table (list) of String or String[] entries,
 *  allows up/down ordering, add and delete
 *  @author Kay Kasemir, Xihui Chen
 */
public class StringTableEditor extends Composite
{
	private static final String DELETE = "delete"; //$NON-NLS-1$
	private static final String DOWN = "down"; //$NON-NLS-1$
	private static final String UP = "up"; //$NON-NLS-1$
	private static final String EDIT = "edit"; //$NON-NLS-1$
	private final TableViewer tableViewer;
	private final static ImageRegistry images = new ImageRegistry();
	private Button editButton;
	private Button upButton;
	private Button downButton;
	private Button deleteButton;
	
	static {
		// Buttons: edit/up/down/delete		
		images.put(EDIT, CSSPlatformUiPlugin.getImageDescriptor("icons/edit.gif")); //$NON-NLS-1$
		images.put(UP, CSSPlatformUiPlugin.getImageDescriptor("icons/up.gif")); //$NON-NLS-1$
		images.put(DOWN, CSSPlatformUiPlugin.getImageDescriptor("icons/down.gif")); //$NON-NLS-1$
		images.put(DELETE, CSSPlatformUiPlugin.getImageDescriptor("icons/delete.gif")); //$NON-NLS-1$
	}
	
	/** Creates an editable table.  The size of headers array implies the number of columns. 
	 * @param parent The composite which the table resides in
	 * @param headers Contains the header for each column
	 * @param editable Whether it is editable for each column. The size must be same as headers.  
	 * @param items The items to be displayed and manipulated in the table. 
	 * Each element in the list, which is an array of string, represents the data in a row.  
	 * In turn, each element in the string array represents the data in a cell. 
	 * So it is required that every string array in the list must has the same size as headers.  
	 */
	public StringTableEditor(final Composite parent, final String[] headers, 
			final boolean[] editable, final List<String[]> items, 
			final RowEditDialog rowEditDialog,
			final int[] columnsMinWidth) 
	{
		super(parent, 0);

		final int table_columns = headers.length;
		if (editable.length != table_columns ||
		    columnsMinWidth.length != table_columns)
		    throw new Error("Inconsistent table column count"); //$NON-NLS-1$

		// StringTableEditor itself is a Composite that contains 2 columns:
		// Left column: Table
		// Right column: Edit/up/down/delete buttons
		setLayout(new GridLayout(2, false));
		
		//Edit-able Table
		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION |
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		final Table table = tableViewer.getTable();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
		gd.heightHint = 100;
		table.setLayoutData(gd);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		//Create edit-able columns
		for(int i = 0; i < table_columns; i++) {
			final TableViewerColumn col = 
				AutoSizeColumn.make(tableViewer, headers[i], columnsMinWidth[i], 100, false);
			col.setLabelProvider(new StringMultiColumnsLabelProvider(tableViewer, editable[i]));
			//col.setLabelProvider(new StringColumnLabelProvider(tableViwer));
			if(editable[i]) {
				col.setEditingSupport(new StringMultiColumnsEditor(tableViewer,
				        table_columns, i));	
			}
		}
		tableViewer.setContentProvider(new StringTableContentProvider<String[]>());
		tableViewer.setInput(items);
		new AutoSizeControlListener(table);		
		editButton = createEditButton(table_columns, rowEditDialog);
		upButton = createUpButton();
		downButton = createDownButton();
		deleteButton = createDeleteButton();		
		
		// Enable buttons when items are selected
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				setButtonsEnable();
			}
		});
	}
	
	/** Initialize
	 *  @param parent Parent widget
	 *  @param items Array of items, will be changed in-place
	 */
	public StringTableEditor(final Composite parent, final List<String> items)
	{
		super(parent, 0);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		setLayout(layout);
		// Edit-able List
		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION |
								SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		final Table table = tableViewer.getTable();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
		table.setLayoutData(gd);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		// Create edit-able column
		final TableViewerColumn col = 
			AutoSizeColumn.make(tableViewer, Messages.StringTableEditor_DefaultColumnHeader, 200, 100, false);
		col.setLabelProvider(new StringColumnLabelProvider(tableViewer));
		col.setEditingSupport(new StringColumnEditor(tableViewer));
		tableViewer.setContentProvider(new StringTableContentProvider<String>());
		tableViewer.setInput(items);
		new AutoSizeControlListener(table);
		
		upButton = createUpButton();
		downButton = createDownButton();
		deleteButton = createDeleteButton();		
		
		// Enable buttons when items are selected
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				setButtonsEnable();
			}
		});
	}
	
	/** Update the input to the table.
	 *  @param new_items New items. Must be either List of String or String[],
	 *               and type must match whatever was used to construct
	 *               the StringTableEditor.
	 */
    public void updateInput(List<?> new_items)
	{
	    tableViewer.setInput(new_items);
    }

	/** Refresh the editor after the list of items was changed */
	public void refresh()
	{
		tableViewer.refresh();
	}
	
	private Button createEditButton(final int numColumns, 
			final RowEditDialog rowEditDialog) 
	{
		final Button edit = new Button(this, SWT.PUSH);
		edit.setImage(images.get(EDIT));
		edit.setToolTipText(Messages.StringTableEditor_EditToolTip);
		edit.setLayoutData(new GridData());
		edit.setEnabled(false);
		edit.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				final List<String[]> items = (List<String[]>) tableViewer.getInput();
				final Integer index = (Integer)
					((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
				
				if(index == StringTableContentProvider.ADD_ELEMENT) {
					String[] emptyData = new String[numColumns];
					Arrays.fill(emptyData, ""); //$NON-NLS-1$
					rowEditDialog.setRowData(emptyData);					
				}else
					rowEditDialog.setRowData(items.get(index));
				
				if(rowEditDialog.open() != RowEditDialog.OK) {
					return;
				}
				
				if(index == StringTableContentProvider.ADD_ELEMENT)
					//when you click <Add>, it already added a new row.
					items.set(items.size()-1, rowEditDialog.getRowData());
				else
					items.set(index, rowEditDialog.getRowData());

				tableViewer.refresh();
				
			}
		});
		return edit;
	}
	
	private Button createUpButton() {
		final Button up = new Button(this, SWT.PUSH);
		up.setImage(images.get(UP));
		up.setToolTipText(Messages.StringTableEditor_MoveUpToolTip);
		up.setLayoutData(new GridData());
		up.setEnabled(false);
		up.addSelectionListener(new SelectionAdapter()
		{
			@Override
		    @SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent e)
			{
				final List items = (List)tableViewer.getInput();
				final Integer index = (Integer)
				((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
				if (index == StringTableContentProvider.ADD_ELEMENT  ||
				    index < 1)
					return;
				//final String[] item = items.get(index);
				items.add(index-1, items.get(index));
				items.remove(index + 1);
				tableViewer.refresh();
				tableViewer.getTable().setSelection(index-1);
			}
		});
		return up;
	}
	
	private Button createDownButton() {
		final Button down = new Button(this, SWT.PUSH);
		down.setImage(images.get(DOWN));
		down.setToolTipText(Messages.StringTableEditor_MoveDownToolTip);
		down.setLayoutData(new GridData());
		down.setEnabled(false);
		down.addSelectionListener(new SelectionAdapter()
		{
		    @SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e)
			{	
				final List items = (List) tableViewer.getInput();
				final Integer index = (Integer)
				((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
				if (index == StringTableContentProvider.ADD_ELEMENT  ||
				    index >= items.size()-1)
					return;
				items.add(index+2, items.get(index));
				items.remove(index.intValue());
				tableViewer.refresh();
				tableViewer.getTable().setSelection(index+1);
			}
		});
		return down;
	}
	
	private Button createDeleteButton() {
		final Button delete = new Button(this, SWT.PUSH);
		delete.setImage(images.get(DELETE));
		delete.setToolTipText(Messages.StringTableEditor_DeleteToolTip);
		delete.setLayoutData(new GridData());
		delete.setEnabled(false);
		delete.addSelectionListener(new SelectionAdapter()
		{
		    @SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e)
			{	
                final List items = (List) tableViewer.getInput();
				final Object sel[] =
				 ((IStructuredSelection) tableViewer.getSelection()).toArray();
				int adjust = 0;
				for (Object s : sel)
				{
					final Integer index = (Integer)s;
					if (index == StringTableContentProvider.ADD_ELEMENT)
						continue;
					items.remove(index.intValue() - adjust);
					// What used to be index N is now N-1...
					++adjust;
				}
				tableViewer.refresh();
			}
		});
		return delete;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if(enabled) {
			tableViewer.getTable().setEnabled(enabled);
			setButtonsEnable();
		}
		else
			for(Control control: this.getChildren())
				control.setEnabled(enabled);
	}

	private void setButtonsEnable() {
		final IStructuredSelection sel = (IStructuredSelection)tableViewer.getSelection();
		final int count = sel.size();
		if(editButton != null)
			editButton.setEnabled(count == 1);
		upButton.setEnabled(count == 1);
		downButton.setEnabled(count == 1);
		deleteButton.setEnabled(count > 0);
	}
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}
}	
	
