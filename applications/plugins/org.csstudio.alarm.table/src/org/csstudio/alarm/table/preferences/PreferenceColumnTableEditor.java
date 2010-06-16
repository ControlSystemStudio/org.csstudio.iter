package org.csstudio.alarm.table.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Preference Table to set the names and widths of columns in message tables.
 * 
 * TODO: Previously this class has handled different column settings for each
 * topic set (now it is handles by the derived class
 * {@link ExchangeablePreferenceColumnTableEditor} ). There are still some parts
 * that should be moved to {@link ExchangeablePreferenceColumnTableEditor}.
 * 
 * @author jhatje
 * 
 */
public class PreferenceColumnTableEditor extends PreferenceTableEditor {

	/**
	 * Current row selected in topic table. For each row in topic table this
	 * table shows the related column names.
	 */
	int _row = 0;

	List<List<String[]>> _columnTableSettings;
	// private String[] _columnSettingsArray;

	/**
	 * Label to display the current selection of topic set in TopicSetTable.
	 */
	Label _topicSetName;

	/**
	 * Current selection of column set for selected column set.
	 */
	List<String[]> _currentColumnTableSet = null;
	ColumnTableEditorMouseListener _mouseListener;

	/**
	 * Creates a new list field editor
	 */
	protected PreferenceColumnTableEditor() {
	}

	/**
	 * Creates a list field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public PreferenceColumnTableEditor(String name, String labelText,
			Composite parent) {
		super(name, labelText, parent);
	}

	/**
	 * Notifies that the Add button has been pressed. A new tableItem is set at
	 * the end of the table with initial stings that the user has to adjust.
	 */
	void addPressed() {
		setPresentsDefaultValue(false);
		int[] selectionIndices = tableViewer.getTable().getSelectionIndices();
		int newItemIndex;
		if (selectionIndices.length == 0) {
			newItemIndex = tableViewer.getTable().getItemCount();
		} else {
			newItemIndex = selectionIndices[0];
		}
		TableItem item = new TableItem(tableViewer.getTable(), SWT.NONE,
				newItemIndex);
		item.setText(0, "Name");
		item.setText(1, "100");
	}

	/**
	 * Combines the given list of items into a single string. This method is the
	 * converse of <code>parseString</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param items
	 *            the list of items
	 * @return the combined string
	 * @see #parseString
	 */
	protected String createList(TableItem[] items) {
		StringBuffer preferenceString = new StringBuffer();
		for (TableItem tableItem : items) {
			// Name
			preferenceString.append(tableItem.getText(0));
			preferenceString.append("?");
			// Width
			preferenceString.append(tableItem.getText(1));
			preferenceString.append(";");
		}
		return preferenceString.toString();
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		_topicSetName = new Label(parent, SWT.NONE);
		GridData gdata = new GridData();
		gdata.horizontalSpan = 2;
		_topicSetName.setLayoutData(gdata);
		_topicSetName.setText("                                ");
		super.doFillIntoGrid(parent, numColumns);
	}

	/**
	 * Set the file path and menu name set by the user from preferences in the
	 * table rows.
	 */
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doLoad() {
		// if (tableViewer != null) {
		String s = getPreferenceStore().getString(getPreferenceName());
		_columnTableSettings = parseString(s);
		List<String[]> columnSet = _columnTableSettings.get(0);
		if (columnSet == null) {
			return;
		}
		TableItem item;
		for (String[] column : columnSet) {
			item = new TableItem(tableViewer.getTable(), SWT.NONE);
			item.setText(column);
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doStore() {
		// String s = createList(tableViewer.getTable().getItems());
		// if (s != null) {
		setTableSettingsToPreferenceString(tableViewer.getTable());
		StringBuffer buffer = new StringBuffer();
		for (List<String[]> columnSetting : _columnTableSettings) {
			for (String[] strings : columnSetting) {
				buffer.append(strings[0]);
				buffer.append(",");
				buffer.append(strings[1]);
				buffer.append(";");
			}
			buffer.append("?");
		}
		// buffer.deleteCharAt(buffer.length());
		String string = buffer.toString();
		getPreferenceStore().setValue(getPreferenceName(), string);

	}

	/**
	 * Returns this field editor's table control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list control
	 */
	public TableViewer getTableControl(Composite parent) {
		if (tableViewer == null) {
			int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
					| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
			Table table = new Table(parent, style);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			TableColumn column;
			column = new TableColumn(table, SWT.LEFT, 0);
			column.setText("Name");
			column.setWidth(100);
			column = new TableColumn(table, SWT.LEFT, 1);
			column.setText("Width");
			column.setWidth(100);

			// Create an editor object to use for text editing
			final TableEditor editor = new TableEditor(table);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			tableViewer = new TableViewer(table);
			tableViewer.getTable().setFont(parent.getFont());
			tableViewer.getTable().addSelectionListener(getSelectionListener());
			tableViewer.getTable().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					tableViewer = null;
				}
			});
			_mouseListener = new ColumnTableEditorMouseListener(editor, this);
			table.addMouseListener(_mouseListener);

		} else {
			checkParent(tableViewer.getTable(), parent);
		}
		return tableViewer;
	}

	/**
	 * Creates and returns a new item for the list.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @return a new item
	 */
	protected String getNewInputObject() {
		return null;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Splits the given string into a list of strings. This method is the
	 * converse of <code>createList</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param stringList
	 *            the string
	 * @return an array of <code>String</code>
	 * @see #createList
	 */
	protected List<List<String[]>> parseString(String stringList) {
		List<List<String[]>> columnsSets = new ArrayList<List<String[]>>();

		String[] tableColumns = stringList.split("\\?");
		for (String tableColumnSet : tableColumns) {
			String[] columnWidth = tableColumnSet.split(";");
			List<String[]> columns = new ArrayList<String[]>();
			for (String columnWidthSet : columnWidth) {
				String[] tmp = columnWidthSet.split(",");
				columns.add(tmp);
			}
			columnsSets.add(columns);
		}
		return columnsSets;
	}

	void setTableSettingsToPreferenceString(Table table) {
		if (0 <= _row) {
			_currentColumnTableSet = new ArrayList<String[]>();
			for (int i = 0; i < table.getItemCount(); i++) {
				String[] tableItemContent = new String[2];
				TableItem item = table.getItem(i);
				tableItemContent[0] = item.getText(0);
				tableItemContent[1] = item.getText(1);
				_currentColumnTableSet.add(tableItemContent);
			}
			try {
				_columnTableSettings.remove(_row);
			} catch (IndexOutOfBoundsException e) {
				// no problem, new element
			}
			try {
				_columnTableSettings.add(_row, _currentColumnTableSet);
			} catch (IndexOutOfBoundsException e) {
				// add new element at the end of the list
				_columnTableSettings.add(_currentColumnTableSet);
			}
		}
	}

	public void updateColumnSettings() {
		_currentColumnTableSet = new ArrayList<String[]>();
		Table table = tableViewer.getTable();
		for (int i = 0; i < table.getItemCount(); i++) {
			String[] tableItemContent = new String[2];
			TableItem item = table.getItem(i);
			tableItemContent[0] = item.getText(0);
			tableItemContent[1] = item.getText(1);
			_currentColumnTableSet.add(tableItemContent);
		}
		try {
			_columnTableSettings.remove(_row);
		} catch (IndexOutOfBoundsException e) {
			// no problem, new element
		}
		try {
			_columnTableSettings.add(_row, _currentColumnTableSet);
		} catch (IndexOutOfBoundsException e) {
			// add new element at the end of the list
			_columnTableSettings.add(_currentColumnTableSet);
		}
	}

	public Table getTable() {
		return tableViewer.getTable();
	}

}