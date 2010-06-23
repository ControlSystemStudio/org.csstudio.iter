package org.csstudio.trends.databrowser.exportview;

import org.csstudio.apputil.time.SecondsParser;
import org.csstudio.apputil.time.StartEndTimeParser;
import org.csstudio.apputil.ui.swt.ScrolledContainerHelper;
import org.csstudio.apputil.ui.time.StartEndDialog;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.IValue;
import org.csstudio.platform.data.TimestampFactory;
import org.csstudio.trends.databrowser.model.IModelItem;
import org.csstudio.trends.databrowser.model.IPVModelItem;
import org.csstudio.trends.databrowser.model.Model;
import org.csstudio.trends.databrowser.ploteditor.PlotAwareView;
import org.csstudio.trends.databrowser.ploteditor.PlotEditor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

/** View to configure and start "export" of samples to a file.
 *  @see ExportJob
 *  @author Kay Kasemir
 */
public class ExportView extends PlotAwareView
{
    public static final String ID = "org.csstudio.trends.databrowser.exportview.ExportView"; //$NON-NLS-1$

    // GUI Elements
    private Text start_txt;
    private Text end_txt;
    private Button use_plot_time;
    private Button time_config;
    private Button source_plot, source_raw, source_avg;
    private Text   avg_seconds;
    private Button format_spreadsheet;
    private Button format_severity;
    private Button format_default;
    private Button format_decimal;
    private Button format_exponential;
    private Text precision;
    private Text filename_txt;
    private Button browse;
    private Button export;
    
    // Stuff that's updated by the GUI
    // (rest directly read from the GUI elements)
    private ExportJob.Source source;
    private IValue.Format format;

    /** Saved settings */
    private IMemento memento = null;
    // Memento tags
    private static final String TAG_FILENAME = "FILENAME"; //$NON-NLS-1$
    private static final String TAG_AVG_SECS = "AVG_SECS"; //$NON-NLS-1$
    
    /** Get the memento for GUI initialization */
    @Override
    public void init(final IViewSite site, final IMemento memento)
         throws PartInitException
    {
        this.memento = memento;
        super.init(site, memento);
    }

    /** Save state of selected GUI elements */
    @Override
    public void saveState(final IMemento memento)
    {
        memento.putString(TAG_FILENAME, filename_txt.getText());
        memento.putString(TAG_AVG_SECS, avg_seconds.getText());
    }

    /** Restore saved settings */
    private void restoreSavedSettings()
    {
        if (memento != null)
        {
            String txt = memento.getString(TAG_FILENAME);
            if (txt != null)
                filename_txt.setText(txt);
            txt = memento.getString(TAG_AVG_SECS);
            if (txt != null)
                avg_seconds.setText(txt);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doCreatePartControl(final Composite parent)
    {
        final Composite pane =
            ScrolledContainerHelper.create(parent, 500, 260);
        
        final GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        pane.setLayout(layout);
        GridData gd;

        /*           [x] from plot
         * Start:    ____________________________ [ ... ]
         * End  :    ____________________________ 
         *
         * Source:   ( ) plot (*) raw ... ( ) averaged archive ____ time
         * 
         * Output:   [x] Spreadsheet  [x] .. with Severity/Status
         * Format:   (*) default ( ) decimal ( ) exponential __ fractional digits
         *           
         * Filename: ________________ [ Browse ]
         *                            [ Export ]
         */
        // 'use plot time' row
        Label l = new Label(pane, 0); // placeholder
        l.setLayoutData(new GridData());

        use_plot_time = new Button(pane, SWT.CHECK);
        use_plot_time.setText(Messages.UsePlotTime);
        use_plot_time.setToolTipText(Messages.UsePlotTime_TT);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = layout.numColumns - 1;
        use_plot_time.setLayoutData(gd);
        use_plot_time.setSelection(true);
        use_plot_time.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {   conditionallyEnableTimeConfig();  }
        });

        // 'start' row
        l = new Label(pane, 0);
        l.setText(Messages.StartLabel);
        gd = new GridData();
        l.setLayoutData(gd);
        
        start_txt = new Text(pane, SWT.BORDER);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        start_txt.setLayoutData(gd);
        
        time_config = new Button(pane, SWT.CENTER);
        time_config.setText(Messages.SelectTime);
        time_config.setToolTipText(Messages.SelectTime_TT);
        gd = new GridData();
        time_config.setLayoutData(gd);
        time_config.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                StartEndDialog dlg = new StartEndDialog(
                                start_txt.getShell(),
                                start_txt.getText(), end_txt.getText());
                if (dlg.open() != StartEndDialog.OK)
                    return;
                // Update GUI with start/end from dialog
                start_txt.setText(dlg.getStartSpecification());
                end_txt.setText(dlg.getEndSpecification());
            }
        });
       
        // 'end' row        
        l = new Label(pane, 0);
        l.setText(Messages.EndLabel);
        gd = new GridData();
        l.setLayoutData(gd);
        
        end_txt = new Text(pane, SWT.BORDER);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        end_txt.setLayoutData(gd);
        
        l = new Label(pane, 0); // placeholder
        l.setLayoutData(new GridData());
        
        // 'Source' row
        l = new Label(pane, 0);
        l.setText(Messages.SourceLabel);
        gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        l.setLayoutData(gd);

        // ... 'source' radio button group
        Composite frame = new Composite(pane, 0);
        RowLayout row_layout = new RowLayout();
        row_layout.marginLeft = 0;
        row_layout.marginTop = 0;
        frame.setLayout(row_layout);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = layout.numColumns - 1;
        gd.horizontalAlignment = SWT.FILL;
        frame.setLayoutData(gd);
        
        source_plot = new Button(frame, SWT.RADIO);
        source_plot.setText(Messages.Source_Plot);
        source_plot.setToolTipText(Messages.Source_Plot_TT);
        
        source_raw = new Button(frame, SWT.RADIO);
        source_raw.setText(Messages.Source_Raw);
        source_raw.setToolTipText(Messages.Source_Raw_TT);

        source_avg = new Button(frame, SWT.RADIO);
        source_avg.setText(Messages.Source_Average);
        source_avg.setToolTipText(Messages.Source_Average_TT);
        
        avg_seconds = new Text(frame, SWT.BORDER);
        avg_seconds.setText(" 00:01:00");  //$NON-NLS-1$
        avg_seconds.setToolTipText(Messages.Avg_Time_TT);
        l = new Label(frame, 0);
        l.setText(Messages.Avg_Time);
        
        source_plot.addSelectionListener(new SelectionAdapter()
        {
            @Override public void widgetSelected(SelectionEvent e)
            { 
                source = ExportJob.Source.Plot;
                avg_seconds.setEnabled(false);
            }
        });
        source_raw.addSelectionListener(new SelectionAdapter()
        {
            @Override public void widgetSelected(SelectionEvent e)
            {  
                source = ExportJob.Source.Raw;
                avg_seconds.setEnabled(false);
            }
        });
        source_avg.addSelectionListener(new SelectionAdapter()
        {
            @Override public void widgetSelected(SelectionEvent e)
            { 
                source = ExportJob.Source.Average;
                avg_seconds.setEnabled(true);
            }
        });
        
        // ... end of radio buttons
        
        // 'Output' row
        l = new Label(pane, 0);
        l.setText(Messages.OutputLabel);
        gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        l.setLayoutData(gd);

        frame = new Composite(pane, 0);
        row_layout = new RowLayout();
        row_layout.marginLeft = 0;
        row_layout.marginTop = 0;
        frame.setLayout(row_layout);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = layout.numColumns - 1;
        frame.setLayoutData(gd);

        format_spreadsheet = new Button(frame, SWT.CHECK);
        format_spreadsheet.setText(Messages.Spreadsheet);
        format_spreadsheet.setToolTipText(Messages.Spreadsheet_TT);

        format_severity = new Button(frame, SWT.CHECK);
        format_severity.setText(Messages.ShowSeverity);
        format_severity.setToolTipText(Messages.ShowSeverity_TT);

        // Number format row
        l = new Label(pane, 0);
        l.setText(Messages.FormatLabel);
        l.setLayoutData(new GridData());
        
        frame = new Composite(pane, 0);
        row_layout = new RowLayout();
        row_layout.pack = true;
        row_layout.marginLeft = 0;
        row_layout.marginTop = 0;
        frame.setLayout(row_layout);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = layout.numColumns - 1;
        gd.horizontalAlignment = SWT.FILL;
        frame.setLayoutData(gd);
        
        format_default = new Button(frame, SWT.RADIO);
        format_default.setText(Messages.FormatDefaultLabel);
        format_default.setToolTipText(Messages.FormatDefault_TT);
        format_default.addSelectionListener(new SelectionAdapter()
        {
            @Override public void widgetSelected(SelectionEvent e)
            {   format = IValue.Format.Default; }
        });
        
        format_decimal = new Button(frame, SWT.RADIO);
        format_decimal.setText(Messages.FormatDecimalLabel);
        format_decimal.setToolTipText(Messages.FormatDecimal_TT);
        format_decimal.addSelectionListener(new SelectionAdapter()
        {
            @Override public void widgetSelected(SelectionEvent e)
            {   format = IValue.Format.Decimal; }
        });

        format_exponential = new Button(frame, SWT.RADIO);
        format_exponential.setText(Messages.FormatExponentialLabel);
        format_exponential.setToolTipText(Messages.FormatExponential_TT);
        format_exponential.addSelectionListener(new SelectionAdapter()
        {
            @Override public void widgetSelected(SelectionEvent e)
            {   format = IValue.Format.Exponential; }
        });
        // ... end of radio buttons
        
        precision = new Text(frame, SWT.BORDER);
        precision.setTextLimit(3);
        precision.setToolTipText(Messages.FormatPrecision_TT);

        l = new Label(frame, 0);
        l.setText(Messages.FormatPrecisionLabel);
        
        // 'filename' row
        l = new Label(pane, 0);
        l.setText(Messages.FilenameLabel);
        gd = new GridData();
        l.setLayoutData(gd);
        
        filename_txt = new Text(pane, SWT.BORDER);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        filename_txt.setLayoutData(gd);
        filename_txt.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                export.setEnabled(filename_txt.getText().length() > 0);
            }
        });
                
        browse = new Button(pane, SWT.CENTER);
        browse.setText(Messages.Browse);
        browse.setToolTipText(Messages.Browse_TT);
        gd = new GridData();
        browse.setLayoutData(gd);
        
        browse.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dlg = new FileDialog(browse.getShell(), SWT.SAVE);
                // Unclear, how well these two calls work across systems
                dlg.setFilterExtensions(new String [] { "*.dat" }); //$NON-NLS-1$
                
                String full = filename_txt.getText();
                int end = full.lastIndexOf('/');
                if (end >= 0)
                {
                    dlg.setFilterPath(full.substring(0, end));
                    dlg.setFileName(full.substring(end+1));
                }
                else
                    dlg.setFileName(full);
                String name = dlg.open();
                if (name != null)
                    filename_txt.setText(name);
                /* Maybe better to use the workspace dialogs?
                SaveAsDialog dlg = new SaveAsDialog(pane);
                if (dlg.open() == SaveAsDialog.OK)
                {
                    filename_txt.setText(dlg.getResult().toString());
                }
                */
            }
        });
        
        // 'export' row        
        export = new Button(pane, SWT.CENTER);
        export.setText(Messages.Export);
        export.setToolTipText(Messages.Export_TT);
        gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalSpan = layout.numColumns;
        gd.verticalAlignment = SWT.BOTTOM;
        export.setLayoutData(gd);
        export.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {   exportRequested();  }
        });
        
        // Plot/raw/averaged data?
        source_raw.setSelection(true);
        source = ExportJob.Source.Raw;
        avg_seconds.setEnabled(source_avg.getSelection());
        
        // Format
        precision.setText("  4"); //$NON-NLS-1$
        // precision 'enables' whenever non-default format selected
        format_default.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {   precision.setEnabled(! format_default.getSelection());   }
        });
        format_default.setSelection(true);
        format = IValue.Format.Default;
        format_spreadsheet.setSelection(true);
        format_severity.setSelection(true);
        
        restoreSavedSettings();
    }

    /** Set the initial focus. */
    @Override
    public void setFocus()
    {
        filename_txt.setFocus();
    }
    
    /** Enable the time start/end/select GUI unless we get that info from plot.
     */
    private void conditionallyEnableTimeConfig()
    {
        final boolean allow_config = !use_plot_time.getSelection();
        start_txt.setEnabled(allow_config);
        end_txt.setEnabled(allow_config);
        time_config.setEnabled(allow_config);
        if (allow_config)
            setStartEndFromModel();
    }

    /** Another Model becomes current.
     *  @see PlotAwareView
     */
    @Override
    protected void updateModel(Model old_model, Model new_model)
    {
        boolean enable;
        if (new_model == null)
        {
            enable = false;
            start_txt.setEnabled(false);
            end_txt.setEnabled(false);
            time_config.setEnabled(false);
            precision.setEnabled(false);
            export.setEnabled(false);
        }
        else
        {
            enable = true;
            conditionallyEnableTimeConfig();
            precision.setEnabled(! format_default.getSelection());
            export.setEnabled(filename_txt.getText().length() > 0);
        }
        use_plot_time.setEnabled(enable);
        source_plot.setEnabled(enable);
        source_raw.setEnabled(enable);
        source_avg.setEnabled(enable);
        avg_seconds.setEnabled(enable);
        format_spreadsheet.setEnabled(enable);
        format_severity.setEnabled(enable);
        format_default.setEnabled(enable);
        format_decimal.setEnabled(enable);
        format_exponential.setEnabled(enable);
        filename_txt.setEnabled(enable);
        browse.setEnabled(enable);
    }

    /** Update GUI with start/end time spec from Model. */
    private void setStartEndFromModel()
    {
    	final PlotEditor editor = getPlotEditor();
    	if (editor == null)
    		return;
		final Model model = editor.getModel();
        if (model == null)
            return;
        start_txt.setText(model.getStartSpecification());
        end_txt.setText(model.getEndSpecification());
    }
    
    /** Start data export with current settings. */
    private void exportRequested()
    {
    	final PlotEditor editor = getPlotEditor();
    	if (editor == null)
    		return;
		final Model model = editor.getModel();
        if (model == null)
            return;
        
        ITimestamp start = null, end = null;
        if (use_plot_time.getSelection())
        {
            setStartEndFromModel();
            start = model.getStartTime();
            end = model.getEndTime();
        }
        else
        {   // Update start/end from text boxes
            try
            {
                StartEndTimeParser parser =
                    new StartEndTimeParser(start_txt.getText(),
                                           end_txt.getText());
                start = TimestampFactory.fromCalendar(parser.getStart());
                end = TimestampFactory.fromCalendar(parser.getEnd());
            }
            catch (Exception ex)
            {
                MessageBox msg = new MessageBox(getSite().getShell(), SWT.OK);
                msg.setText(Messages.Error);
                msg.setMessage(String.format("%s\n%s %s", //$NON-NLS-1$
                               Messages.CannotDecodeStartEnd,
                               Messages.Error,
                               ex.getMessage()));
                msg.open();
                return;
            }
        }
        double seconds;
        try
        {
            seconds = SecondsParser.parseSeconds(avg_seconds.getText());
        }
        catch (Exception e)
        {
            if (source == ExportJob.Source.Average)
            {
                MessageBox msg = new MessageBox(getSite().getShell(), SWT.OK);
                msg.setText(Messages.Error);
                msg.setMessage(Messages.CannotDecodeSeconds);
                msg.open();
                return;
            }
            seconds = 0;
        }
        int prec;
        try
        {
            prec = Integer.parseInt(precision.getText().trim());
        }
        catch (Exception e)
        {
            prec = 0;
        }
        
        // Check for use of formulas
        if (source != ExportJob.Source.Plot)
        {
            for (int i=0; i<model.getNumItems(); ++i)
            {
                final IModelItem item = model.getItem(i);
                if (! (item instanceof IPVModelItem))
                {
                    if (! MessageDialog.openQuestion(getSite().getShell(),
                        Messages.FormulaWarningTitle,
                        NLS.bind(Messages.FormulaWarningMessage,
                                 item.getName())))
                             return;
                    break;
                }
            }
        }
        
        // Launch the actual export
        Job job = new ExportJob(getSite().getShell(),
                        model, start, end,
                        source,
                        seconds,
                        format_spreadsheet.getSelection(),
                        format_severity.getSelection(),
                        format, prec,
                        filename_txt.getText().trim());
        job.schedule();
    }
}