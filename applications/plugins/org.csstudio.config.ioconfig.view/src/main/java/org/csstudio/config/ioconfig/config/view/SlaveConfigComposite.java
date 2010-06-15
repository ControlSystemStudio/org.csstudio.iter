/*
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
/*
 * $Id$
 */
package org.csstudio.config.ioconfig.config.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.csstudio.config.ioconfig.config.view.helper.ConfigHelper;
import org.csstudio.config.ioconfig.config.view.helper.ProfibusHelper;
import org.csstudio.config.ioconfig.model.Document;
import org.csstudio.config.ioconfig.model.Keywords;
import org.csstudio.config.ioconfig.model.Node;
import org.csstudio.config.ioconfig.model.pbmodel.Channel;
import org.csstudio.config.ioconfig.model.pbmodel.ChannelStructure;
import org.csstudio.config.ioconfig.model.pbmodel.GSDFile;
import org.csstudio.config.ioconfig.model.pbmodel.Master;
import org.csstudio.config.ioconfig.model.pbmodel.Module;
import org.csstudio.config.ioconfig.model.pbmodel.Ranges;
import org.csstudio.config.ioconfig.model.pbmodel.Slave;
import org.csstudio.config.ioconfig.model.pbmodel.gsdParser.ExtUserPrmDataConst;
import org.csstudio.config.ioconfig.model.pbmodel.gsdParser.GSD2Module;
import org.csstudio.config.ioconfig.model.pbmodel.gsdParser.GsdFactory;
import org.csstudio.config.ioconfig.model.pbmodel.gsdParser.GsdModuleModel;
import org.csstudio.config.ioconfig.model.pbmodel.gsdParser.GsdSlaveModel;
import org.csstudio.config.ioconfig.view.ProfiBusTreeView;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * @author hrickens
 * @author $Author$
 * @version $Revision$
 * @since 05.06.2007
 */
public class SlaveConfigComposite extends NodeConfig {

    /**
     * The Slave which displayed.
     */
    private Slave _slave;
    /**
     * The GSD File of the Slave.
     */
    private GSDFile _gsdFile;
    /**
     * The Text field for the Vendor.
     */
    private Text _vendorText;
    /**
     * Slave ID Number.
     */
    private Text _iDNo;
    /**
     * The Text field for the Revision.
     */
    private Text _revisionsText;
    /**
     * The Module max size of this Slave.
     */
    private int _maxSize;
    /**
     * The number of max slots.
     */
    private Text _maxSlots;
    /**
     * List with User Prm Data's.
     */
    private TableViewer _userPrmDataList;
    /**
     * Inputs.
     */
    private Text _inputsText;
    /**
     * Outputs.
     */
    private Text _outputsText;
    /**
     * Die Bedeutung dieses Feldes ist noch unbekannt.
     */
    // private Text _unbekannt;
    /**
     * The minimum Station Delay Time.
     */
    private Text _minStationDelayText;
    /**
     * The Watchdog Time.
     */
    private Text _watchDogText;
    /**
     * Marker of Background Color for normal use. Get from widget by first use.
     */
    private Color _defaultBackgroundColor;
    /**
     * Check Button to de.-/activate Station Address.
     */
    private Button _stationAddressActiveCButton;
    private Button _freezeButton;
    private Button _failButton;
    private Button _watchDogButton;
    private Button _syncButton;
    private short _groupIdent;
    private short _groupIdentStored;
    private Group _groupsRadioButtons;
    private ComboViewer _indexCombo;

    /**
     * 
     * @author hrickens
     * @author $Author$
     * @version $Revision$
     * @since 14.08.2007
     */
    class RowNumLabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        public void addListener(final ILabelProviderListener listener) {
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
            if (_defaultBackgroundColor != null) {
                _defaultBackgroundColor.dispose();
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean isLabelProperty(final Object element, final String property) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public void removeListener(final ILabelProviderListener listener) {
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof Slave) {
                Slave slave = (Slave) element;
                switch (columnIndex) {
                    case 1:
                        return slave.getName();
                    case 2:
                        StringBuffer sb = new StringBuffer();
                        for (ExtUserPrmDataConst eupdc : slave.getGSDSlaveData()
                                .getExtUserPrmDataConst().values()) {
                            sb.append(eupdc.getValue());
                        }
                        return sb.toString();

                    default:
                        break;
                }

            } else if (element instanceof Module) {
                Module module = (Module) element;
                switch (columnIndex) {
                    case 0:
                        return module.getSortIndex().toString();
                    case 1:
                        return module.getName();
                    case 2:
                        return module.getExtUserPrmDataConst();

                    default:
                        break;
                }
            }
            return null;
        }
    }

    /**
     * Open a slave configuration view for a exist {@link Slave}.
     * 
     * @param parent
     *            Parent Composite.
     * @param slave
     *            the Profibus Slave to Configer.
     */
    public SlaveConfigComposite(final Composite parent, final ProfiBusTreeView profiBusTreeView,
            final Slave slave) {
        this(parent,profiBusTreeView,slave,"");
    }
    public SlaveConfigComposite(final Composite parent, final ProfiBusTreeView profiBusTreeView,
            final Slave slave, final String nameOffer) {
        super(parent, profiBusTreeView, "Profibus Slave Configuration", slave, slave == null);
        profiBusTreeView.setConfiguratorName("Slave Configuration");
        makeSlaveKonfiguration(parent, slave,nameOffer);
    }

    /**
     * @param parent
     *            Parent Composite.
     * @param style
     *            Style of the Composite.
     * @param slave
     *            the Profibus Slave to Configer.
     */
    private void makeSlaveKonfiguration(final Composite parent, final Slave slave, String nameOffer) {
        boolean nevv = false;
        _slave = slave;
        if (_slave == null) {
            if (!newNode(nameOffer)) {
                // this.dispose();
                setSaveButtonSaved();
                getProfiBusTreeView().getTreeViewer().setSelection(
                        getProfiBusTreeView().getTreeViewer().getSelection());
                return;
            }
            nevv=true;
            _slave.setMinTsdr((short) 11);
            _slave.setWdFact1((short) 100);
        }
        setSavebuttonEnabled(null, getNode().isPersistent());
        String[] heads = { "Basics", "Settings", "Overview" };
        basics(heads[0]);
        settings(heads[1]);
        overview(heads[2]);
        documents();
        ConfigHelper.makeGSDFileChooser(getTabFolder(), "GSD File List", this,
                Keywords.GSDFileTyp.Slave);
        if (_slave.getGSDFile() != null) {
            fill(_slave.getGSDFile());
        }

        getTabFolder().pack();
        if(nevv) {
            getTabFolder().setSelection(4);
        }
    }

    @SuppressWarnings("unchecked")
    private void overview(String headline) {
        Composite comp = getNewTabItem(headline, 1);
        comp.setLayout(new GridLayout(1, false));

        TableViewer overViewer = new TableViewer(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
                | SWT.FULL_SELECTION);
        overViewer.setContentProvider(new ArrayContentProvider());
        overViewer.setLabelProvider(new OverviewLabelProvider());
        overViewer.getTable().setHeaderVisible(true);
        overViewer.getTable().setLinesVisible(true);
        overViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        TableColumn c0 = new TableColumn(overViewer.getTable(), SWT.RIGHT);
        c0.setText("Adr");
        TableColumn c0b = new TableColumn(overViewer.getTable(), SWT.RIGHT);
        c0b.setText("Adr");
        TableColumn c1 = new TableColumn(overViewer.getTable(), SWT.LEFT);
        c1.setText("Name");
        TableColumn c2 = new TableColumn(overViewer.getTable(), SWT.LEFT);
        // c2.setWidth(200);
        c2.setText("IO Name");
        TableColumn c3 = new TableColumn(overViewer.getTable(), SWT.LEFT);
        // c3.setWidth(200);
        c3.setText("IO Epics Address");
        TableColumn c4 = new TableColumn(overViewer.getTable(), SWT.LEFT);
        // c4.setWidth(200);
        c4.setText("Desc");
        TableColumn c5 = new TableColumn(overViewer.getTable(), SWT.LEFT);
        // c5.setWidth(60);
        c5.setText("Type");
        TableColumn c6 = new TableColumn(overViewer.getTable(), SWT.LEFT);
        // c5.setWidth(60);
        c6.setText("DB Id");
        
        
        overViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                // IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                getProfiBusTreeView().getTreeViewer().setSelection(event.getSelection(), true);
            }

        });

        ArrayList<Node> children = new ArrayList<Node>();
        Collection<Module> modules = (Collection<Module>) _slave.getChildrenAsMap().values();
        for (Module module : modules) {
            children.add(module);
            Collection<ChannelStructure> channelStructures = module.getChannelStructsAsMap()
                    .values();
            for (ChannelStructure channelStructure : channelStructures) {
                Collection<Channel> channels = channelStructure.getChannelsAsMap().values();
                for (Channel channel : channels) {
                    children.add(channel);
                }

            }
        }
        overViewer.setInput(children);
        c0.pack();
        c0b.pack();
        c1.pack();
        c2.pack();
        c3.pack();
        c4.pack();
        c5.pack();
        c6.pack();
    }

    /**
     * @param head
     *            The Tab text.
     */
    private void basics(final String head) {

        Composite comp = getNewTabItem(head, 2);

        /*
         * Name
         */
        Group gName = new Group(comp, SWT.NONE);
        gName.setText("Name");
        gName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
        gName.setLayout(new GridLayout(3, false));

        Text nameText = new Text(gName, SWT.BORDER | SWT.SINGLE);
        setText(nameText, _slave.getName(), 255);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        setNameWidget(nameText);

        // Label
        Label slotIndexLabel = new Label(gName, SWT.NONE);
        slotIndexLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        slotIndexLabel.setText("Station Adress:");

        _indexCombo = new ComboViewer(gName, SWT.DROP_DOWN | SWT.READ_ONLY);
        _indexCombo.getCombo().setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, false, false));
        _indexCombo.setContentProvider(new ArrayContentProvider());
        _indexCombo.setLabelProvider(new LabelProvider());
        Collection<Short> freeStationAddress = _slave.getProfibusDPMaster().getFreeStationAddress();
        Short sortIndex = _slave.getSortIndex();
        if (sortIndex >= 0) {
            if (!freeStationAddress.contains(sortIndex)) {
                freeStationAddress.add(sortIndex);
            }
            _indexCombo.setInput(freeStationAddress);
            _indexCombo.setSelection(new StructuredSelection(sortIndex));
        } else {
            _indexCombo.setInput(freeStationAddress);
            _indexCombo.getCombo().select(0);
            _slave.setSortIndexNonHibernate((Short) ((StructuredSelection) _indexCombo
                    .getSelection()).getFirstElement());
        }
        _indexCombo.getCombo().setData(_indexCombo.getCombo().getSelectionIndex());
        _indexCombo.getCombo().addModifyListener(getMLSB());
        _indexCombo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                short index = (Short) ((StructuredSelection) _indexCombo.getSelection())
                        .getFirstElement();
                getNode().moveSortIndex(index);
                if (getNode().getParent() != null) {
                    getProfiBusTreeView().refresh(getNode().getParent());
                } else {
                    getProfiBusTreeView().refresh();
                }
            }
        });

        // setIndexSpinner(ConfigHelper.getIndexSpinner(gName, _slave, getMLSB(),
        // "Station Adress:",getProfiBusTreeView()));
        // _defaultBackgroundColor = getIndexSpinner().getBackground();

        /*
         * Slave Information
         */
        Group slaveInfoGroup = new Group(comp, SWT.NONE);
        slaveInfoGroup.setText("Slave Information");
        slaveInfoGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
        slaveInfoGroup.setLayout(new GridLayout(4, false));
        slaveInfoGroup.setTabList(new Control[0]);

        _vendorText = new Text(slaveInfoGroup, SWT.SINGLE | SWT.BORDER);
        _vendorText.setEditable(false);
        _vendorText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));

        _iDNo = new Text(slaveInfoGroup, SWT.SINGLE);
        _iDNo.setEditable(false);
        _iDNo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

        Label revisionsLable = new Label(slaveInfoGroup, SWT.NONE);
        revisionsLable.setText("Revision:");

        _revisionsText = new Text(slaveInfoGroup, SWT.SINGLE);
        _revisionsText.setEditable(false);
        _revisionsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        new Label(slaveInfoGroup, SWT.None).setText("Max. available slots:");
        _maxSlots = new Text(slaveInfoGroup, SWT.BORDER);
        _maxSlots.setEditable(false);

        /*
         * DP/FDL Access Group
         */
        Group dpFdlAccessGroup = new Group(comp, SWT.NONE);
        dpFdlAccessGroup.setText("DP / FDL Access");
        dpFdlAccessGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        dpFdlAccessGroup.setLayout(new GridLayout(2, false));

        Label stationAdrLabel = new Label(dpFdlAccessGroup, SWT.None);
        stationAdrLabel.setText("Station Address");

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd.minimumWidth = 50;

        _stationAddressActiveCButton = new Button(dpFdlAccessGroup, SWT.CHECK);
        _stationAddressActiveCButton.setText("Active");
        _stationAddressActiveCButton.setSelection(false);
        _stationAddressActiveCButton.setData(false);
        _stationAddressActiveCButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(final SelectionEvent e) {
                change();
            }

            public void widgetSelected(final SelectionEvent e) {
                change();

            }

            private void change() {
                setSavebuttonEnabled(
                        "Button:" + _stationAddressActiveCButton.hashCode(),
                        (Boolean) _stationAddressActiveCButton.getData() != _stationAddressActiveCButton
                                .getSelection());
            }

        });

        /*
         * Input / Output Group
         */
        Group ioGroup = new Group(comp, SWT.NONE);
        ioGroup.setText("Inputs / Outputs");
        ioGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        ioGroup.setLayout(new GridLayout(3, false));
        ioGroup.setTabList(new Control[0]);

        int input = 0;
        int output = 0;

        if (_slave.hasChildren()) {
            Iterator<Module> iterator = _slave.getModules().iterator();
            while (iterator.hasNext()) {
                Module module = (Module) iterator.next();
                input += module.getInputSize();
                output += module.getOutputSize();
            }
        }
        Label inputLabel = new Label(ioGroup, SWT.RIGHT);
        inputLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        inputLabel.setText("Inputs: ");
        _inputsText = new Text(ioGroup, SWT.SINGLE);
        _inputsText.setEditable(false);
        _inputsText.setText(Integer.toString(input));

        Label outputsLabel = (Label) new Label(ioGroup, SWT.RIGHT);
        outputsLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        outputsLabel.setText("Outputs: ");
        _outputsText = new Text(ioGroup, SWT.SINGLE);
        _outputsText.setEditable(false);
        _outputsText.setText(Integer.toString(output));

        /*
         * Description Group
         */
        makeDescGroup(comp,3);
    }

    /**
     * 
     */
    private void setSlots() {
        Formatter slotFormarter = new Formatter();
        slotFormarter.format(" %2d / %2d", _slave.getChildren().size(), _maxSize);
        _maxSlots.setText(slotFormarter.toString());
        if (_maxSize < _slave.getChildren().size()) {
            if (_defaultBackgroundColor == null) {
                _defaultBackgroundColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
            }
            _maxSlots.setBackground(WARN_BACKGROUND_COLOR);
        } else {
            _maxSlots.setBackground(_defaultBackgroundColor);
        }
        slotFormarter = null;
    }

    /**
     * @param head
     *            the tabItemName
     */
    private void settings(final String head) {
        Composite comp = getNewTabItem(head, 2);
        comp.setLayout(new GridLayout(2, false));
        // Operation Mode
        Group operationModeGroup = new Group(comp, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        layoutData.minimumWidth = 170;
        operationModeGroup.setLayoutData(layoutData);
        operationModeGroup.setText("Operation Mode");
        operationModeGroup.setLayout(new GridLayout(3, false));
        Label delayLabel = new Label(operationModeGroup, SWT.NONE);
        delayLabel.setText("Min. Station Delay");
        _minStationDelayText = ProfibusHelper.getTextField(operationModeGroup, true, _slave
                .getMinTsdr()
                + "", Ranges.WATCHDOG, ProfibusHelper.VL_TYP_U16);
        Label bitLabel = new Label(operationModeGroup, SWT.NONE);
        bitLabel.setText("Bit");

        _syncButton = new Button(operationModeGroup, SWT.CHECK);
        _syncButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
        _syncButton.addTraverseListener(ProfibusHelper.getNETL());
        _syncButton.setText("Sync Request");
        _syncButton.setData(false);
        _syncButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(final SelectionEvent e) {
                change(_syncButton);
            }

            public void widgetSelected(final SelectionEvent e) {
                change(_syncButton);
            }
        });
        _freezeButton = new Button(operationModeGroup, SWT.CHECK);
        _freezeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
        _freezeButton.addTraverseListener(ProfibusHelper.getNETL());
        _freezeButton.setText("Freeze Request");
        _freezeButton.setSelection(false);
        _freezeButton.setData(false);
        _freezeButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(final SelectionEvent e) {
                change(_freezeButton);
            }

            public void widgetSelected(final SelectionEvent e) {
                change(_freezeButton);
            }
        });
        _failButton = new Button(operationModeGroup, SWT.CHECK);
        _failButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
        _failButton.addTraverseListener(ProfibusHelper.getNETL());
        _failButton.setText("Fail Save");
        _failButton.setEnabled(false);
        _failButton.setData(false);
        _failButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(final SelectionEvent e) {
                change(_failButton);
            }

            public void widgetSelected(final SelectionEvent e) {
                change(_failButton);
            }
        });
        _watchDogButton = new Button(operationModeGroup, SWT.CHECK);
        _watchDogButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        _watchDogButton.addTraverseListener(ProfibusHelper.getNETL());
        _watchDogButton.setText("Watchdog Time");
        _watchDogButton.setSelection(true);
        _watchDogButton.setData(true);
        _watchDogText = ProfibusHelper.getTextField(operationModeGroup, _watchDogButton
                .getSelection(), Short.toString(_slave.getWdFact1()), Ranges.TTR,
                ProfibusHelper.VL_TYP_U32);
        _watchDogText.addModifyListener(getMLSB());
        Label timeLabel = new Label(operationModeGroup, SWT.NONE);
        timeLabel.setText("ms");
        _watchDogButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(final SelectionEvent e) {
                change();
            }

            public void widgetSelected(final SelectionEvent e) {
            }

            private void change() {
                SlaveConfigComposite.this.change(_watchDogButton);
                _watchDogText.setEnabled(_watchDogButton.getSelection());
            }
        });

        // User PRM Mode
        Group userPrmData = new Group(comp, SWT.V_SCROLL);
        userPrmData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        userPrmData.setLayout(new GridLayout(2, false));
        userPrmData.setTabList(new Control[0]);
        userPrmData.setText("User PRM Mode");
        // _userPrmDataList = new ListViewer(userPrmData, SWT.BORDER|SWT.V_SCROLL);
        _userPrmDataList = new TableViewer(userPrmData, SWT.BORDER | SWT.V_SCROLL);

        _userPrmDataList.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 1, 15));
        _userPrmDataList.getTable().setHeaderVisible(true);
        _userPrmDataList.getTable().setLinesVisible(true);
        _userPrmDataList.setContentProvider(new ArrayContentProvider());
        _userPrmDataList.setLabelProvider(new RowNumLabelProvider());

        TableColumn tc = new TableColumn(_userPrmDataList.getTable(), SWT.RIGHT);
        tc.setText("");
        tc.setWidth(20);
        tc = new TableColumn(_userPrmDataList.getTable(), SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(130);
        tc = new TableColumn(_userPrmDataList.getTable(), SWT.LEFT);
        tc.setText("Ext User Prm Data Const");
        tc.setWidth(450);

        // Groups
        _groupsRadioButtons = new Group(comp, SWT.NONE);
        _groupsRadioButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        _groupsRadioButtons.setLayout(new GridLayout(4, true));
        _groupsRadioButtons.setText("Groups");
        _groupIdentStored = _slave.getGroupIdent();
        if (_groupIdentStored < 0 || _groupIdentStored > 7) {
            _groupIdentStored = 0;
        }
        _groupIdent = _groupIdentStored;
        for (int i = 0; i <= 7; i++) {
            final Button b = new Button(_groupsRadioButtons, SWT.RADIO);
            b.setText(Integer.toString(i + 1));
            if (i == _groupIdent) {
                b.setSelection(true);
            }
            b.addSelectionListener(new SelectionListener() {

                public void widgetDefaultSelected(final SelectionEvent e) {
                    check();
                }

                public void widgetSelected(final SelectionEvent e) {
                    check();
                }

                private void check() {
                    _groupIdent = Short.parseShort(b.getText());
                    _groupIdent--;
                    setSavebuttonEnabled("groupButton" + _groupsRadioButtons.hashCode(),
                            _groupIdent != _groupIdentStored);
                }

            });
        }

    }

    /** {@inheritDoc} */
    @Override
    public final boolean fill(final GSDFile gsdFile) {
        /*
         * Read GSD-File
         */
        if (gsdFile == null) {
            return false;
        } else if (gsdFile.equals(_gsdFile)) {
            return true;
        }

        _gsdFile = gsdFile;
        GsdSlaveModel slaveModel = GsdFactory.makeGsdSlave(_gsdFile);

        // setGSDData
        HashMap<Integer, GsdModuleModel> moduleList = GSD2Module.parse(_gsdFile,
                slaveModel);
        slaveModel.setGsdModuleList(moduleList);
        _slave.setGSDSlaveData(slaveModel);

        /*
         * Head
         */
        ((Text) this.getData("version")).setText(slaveModel.getGsdRevision() + "");

        /*
         * Basic - Slave Discription (read only)
         */
        _vendorText.setText(slaveModel.getVendorName());
        _iDNo.setText(String.format("0x%04X", slaveModel.getIdentNumber()));
        _revisionsText.setText(slaveModel.getRevision());

        /*
         * Basic - Inputs / Outputs (read only)
         */

        /*
         * Set all GSD-File Data to Slave.
         */
        _slave.setMinTsdr(_slave.getMinTsdr());
        _slave.setModelName(slaveModel.getModelName());
        _slave.setPrmUserData(slaveModel.getUserPrmData());
        _slave.setProfibusPNoID(slaveModel.getIdentNumber());
        _slave.setRevision(slaveModel.getRevision());

        /*
         * Basic - DP / FDL Access
         */

        /*
         * Modules
         */
        _maxSize = slaveModel.getMaxModule();
        setSlots();

        /*
         * Settings - Operation Mode
         */
        /*
         * Settings - Groups
         */
        /*
         * Settings - USER PRM MODE
         */

        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(_slave);
        nodes.addAll(_slave.getChildrenAsMap().values());
        _userPrmDataList.setInput(nodes);
        TableColumn[] columns = _userPrmDataList.getTable().getColumns();
        for (TableColumn tableColumn : columns) {
            if (tableColumn != null) {
                tableColumn.pack();
            }
        }

        layout();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final GSDFile getGSDFile() {
        return _slave.getGSDFile();
    }

    /**
     * Store all Data in {@link Slave} DB object.
     */
    public final void store() {
        super.store();
        // Name
        _slave.setName(getNameWidget().getText());
        getNameWidget().setData(getNameWidget().getText());

        // _slave.moveSortIndex((short) getIndexSpinner().getSelection());
        Short stationAddress = (Short) ((StructuredSelection) _indexCombo.getSelection())
                .getFirstElement();
        _slave.setSortIndexNonHibernate(stationAddress);
        _slave.setFdlAddress(stationAddress);
        _indexCombo.getCombo().setData(_indexCombo.getCombo().getSelectionIndex());
        short minTsdr = 0;
        try {
            minTsdr = Short.parseShort(_minStationDelayText.getText());
        } catch (NumberFormatException e) {
        }
        _slave.setMinTsdr(minTsdr);

        _slave.setGroupIdent(_groupIdent);
        _groupIdentStored = _groupIdent;

        _slave.setSlaveFlag((short) 192);
        short wdFact = Short.parseShort(_watchDogText.getText());
        _watchDogText.setData(_watchDogText.getText());
        _slave.setWdFact1(wdFact);
        _slave.setWdFact2((short) (wdFact / 10));

        // Static Station status 136
        _slave.setStationStatus((short) 136);
        // GSD File
        _slave.setGSDFile(_gsdFile);
        fill(_gsdFile);

        // Document
        Set<Document> docs = getDocumentationManageView().getDocuments();
        _slave.setDocuments(docs);

        _slave.update();
        save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Node getNode() {
        if (_slave == null) {
            StructuredSelection selection = (StructuredSelection) getProfiBusTreeView()
                    .getTreeViewer().getSelection();
            if (selection.getFirstElement() instanceof Master) {
                Master master = (Master) selection.getFirstElement();
                _slave = new Slave(master);
                _slave.moveSortIndex((short) 0);
            } else if (selection.getFirstElement() instanceof Slave) {
                Slave slave = (Slave) selection.getFirstElement();
                _slave = new Slave(slave.getProfibusDPMaster());
                _slave.moveSortIndex((short) (slave.getSortIndex() + 1));
            }
        }
        return _slave;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.csstudio.config.ioconfig.config.view.NodeConfig#cancel()
     */
    @Override
    public void cancel() {
        super.cancel();
        // getIndexSpinner().setSelection((Short) getIndexSpinner().getData());
        if (_indexCombo != null) {
            _indexCombo.getCombo().select((Integer) _indexCombo.getCombo().getData());
            getNameWidget().setText((String) getNameWidget().getData());
            _stationAddressActiveCButton.setSelection((Boolean) _stationAddressActiveCButton
                    .getData());
            _minStationDelayText.setText((String) _minStationDelayText.getData());
            _syncButton.setSelection((Boolean) _syncButton.getData());
            _failButton.setSelection((Boolean) _failButton.getData());
            _freezeButton.setSelection((Boolean) _freezeButton.getData());
            _watchDogButton.setSelection((Boolean) _watchDogButton.getData());
            _watchDogText.setEnabled(_watchDogButton.getSelection());
            _watchDogText.setText((String) _watchDogText.getData());
            _groupIdent = _groupIdentStored;
            for (Control control : _groupsRadioButtons.getChildren()) {
                if (control instanceof Button) {
                    Button button = (Button) control;
                    button
                            .setSelection(Short.parseShort(button.getText()) == _groupIdentStored + 1);
                }
            }

            if (_slave != null) {
                if (_slave.getGSDFile() != null) {
                    fill(_slave.getGSDFile());
                } else {
                    ((Text) SlaveConfigComposite.this.getData("version")).setText("");
                    _vendorText.setText("");
                    getNameWidget().setText("");
                    _revisionsText.setText("");
                }
            } else {
                _gsdFile = null;
                fill(_gsdFile);
            }
        }
    }

    private void change(final Button button) {
        setSavebuttonEnabled("Button:" + button.hashCode(), (Boolean) button.getData() != button
                .getSelection());
    }

}