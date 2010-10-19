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
 * $Id: ConfigHelper.java,v 1.8 2010/08/20 13:33:00 hrickens Exp $
 */
package org.csstudio.config.ioconfig.config.view.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Nonnull;

import org.csstudio.config.ioconfig.model.FacilityDBO;
import org.csstudio.config.ioconfig.model.IocDBO;
import org.csstudio.config.ioconfig.model.AbstractNodeDBO;
import org.csstudio.config.ioconfig.model.NodeImageDBO;
import org.csstudio.config.ioconfig.model.pbmodel.ChannelDBO;
import org.csstudio.config.ioconfig.model.pbmodel.GSDFileDBO;
import org.csstudio.config.ioconfig.model.pbmodel.MasterDBO;
import org.csstudio.config.ioconfig.model.pbmodel.ModuleDBO;
import org.csstudio.config.ioconfig.model.pbmodel.ProfibusSubnetDBO;
import org.csstudio.config.ioconfig.model.pbmodel.SlaveDBO;
import org.csstudio.config.ioconfig.view.IOConfigActivatorUI;
import org.csstudio.config.ioconfig.view.ProfiBusTreeView;
import org.csstudio.platform.security.SecurityFacade;
import org.csstudio.platform.security.User;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author hrickens
 * @author $Author: hrickens $
 * @version $Revision: 1.8 $
 * @since 20.06.2007
 */
public final class ConfigHelper {

    /**
     * @author hrickens
     * @author $Author: hrickens $
     * @version $Revision: 1.8 $
     * @since 22.07.2009
     */
    private static final class SpinnerKeyListener implements KeyListener {
        private final SpinnerModifyListener _modifyListener;

        public SpinnerKeyListener(final SpinnerModifyListener modifyListener) {
            _modifyListener = modifyListener;
        }

        public void keyPressed(final KeyEvent e) {
            Spinner spinner = (Spinner) e.widget;
            if ((e.keyCode == SWT.CR) || (e.keyCode == SWT.KEYPAD_CR)) {
                _modifyListener.doIt();
                spinner.setSelection(spinner.getSelection());
                // _modifyListener.modifyText(new ModifyEvent(new Event()));
            } else if (e.keyCode == SWT.ESC) {
                spinner.setSelection(_modifyListener.getLastvalue());
                _modifyListener.doIt();
            } else {
                _modifyListener.doItNot();
            }

        }

        public void keyReleased(final KeyEvent e) {
        }

    }

    /**
     * @author hrickens
     * @author $Author: hrickens $
     * @version $Revision: 1.8 $
     * @since 22.07.2007
     */
    private static final class SpinnerModifyListener implements ModifyListener {
        private final ProfiBusTreeView _profiBusTreeView;
        private final AbstractNodeDBO _node;
        private final Spinner _indexSpinner;
        private boolean _doIt = true;
        private int _lastValue;

        private SpinnerModifyListener(final ProfiBusTreeView profiBusTreeView,
                                      final AbstractNodeDBO node,
                                      final Spinner indexSpinner) {
            _profiBusTreeView = profiBusTreeView;
            _node = node;
            _indexSpinner = indexSpinner;
            _lastValue = indexSpinner.getSelection();
        }

        public int getLastvalue() {
            return _lastValue;
        }

        public void modifyText(final ModifyEvent e) {
            if (_doIt) {
                // TODO: Hier gibt es noch ein GDI Object leak.
                short index = (short) _indexSpinner.getSelection();
                _node.moveSortIndex(index);
                if (_node.getParent() != null) {
                    _profiBusTreeView.refresh(_node.getParent());
                } else {
                    _profiBusTreeView.refresh();
                }
                _lastValue = index;
            }
        }

        public void doIt() {
            _doIt = true;
        }

        public void doItNot() {
            _doIt = false;
        }
    }

    /**
     *
     */
    private static GSDFileDBO _gsdFile;

    /**
     * The standard Date format.
     */
    private static SimpleDateFormat _simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private static List<GSDFileDBO> _gsdFiles;

    /**
     * The Private Constructor.
     */
    private ConfigHelper() {
    }

    /**
     * @param head
     *            Headline for the Tab.
     * @param tabFolder
     *            The Tab Folder to add the Tab Item.
     * @param size
     *            the number of column
     * @return Tab Item Composite.
     */
    @Nonnull
    public static Composite getNewTabItem(@Nonnull final String head,
                                          @Nonnull final TabFolder tabFolder,
                                          final int size,
                                          final int minWidthSize,
                                          final int minHeight) {
        return getNewTabItem(head, tabFolder, size, null, minWidthSize, minHeight);
    }

    @Nonnull
    public static Composite getNewTabItem(@Nonnull final String head,
                                          @Nonnull final TabFolder tabFolder,
                                          final int size,
                                          final Composite viewer,
                                          final int minWidthSize,
                                          final int minHeight) {
        final TabItem item = new TabItem(tabFolder, SWT.NONE,0);
        item.setText(head);

        GridLayoutFactory fillDefaults = GridLayoutFactory.fillDefaults();
        ScrolledComposite scrolledComposite = new ScrolledComposite(tabFolder, SWT.H_SCROLL
                | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setExpandHorizontal(true);
        fillDefaults.numColumns(1);
        scrolledComposite.setLayout(fillDefaults.create());

        Composite comp = new Composite(scrolledComposite, SWT.NONE);
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        // _mainComposite.setLayout(fillDefaults.create());

        scrolledComposite.setContent(comp);
        scrolledComposite.setMinSize(minWidthSize, minHeight);

        comp.setLayout(new GridLayout(size, true));
        item.setControl(scrolledComposite);

        comp.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        if (viewer instanceof DocumentationManageView) {
            final DocumentationManageView docView = (DocumentationManageView) viewer;

            tabFolder.addSelectionListener(new SelectionListener() {

                public void widgetDefaultSelected(final SelectionEvent e) {
                    docTabSelectionAction(e);
                }

                public void widgetSelected(final SelectionEvent e) {
                    docTabSelectionAction(e);
                }

                private void docTabSelectionAction(final SelectionEvent e) {
                    if (e.item.equals(item)) {
                        docView.onActivate();
                    }
                }

            });

        }
        return comp;
    }

    /**
     *
     */
    private static void makeGSDFileActions(final TableViewer viewer) {
        Menu menu = new Menu(viewer.getControl());
        MenuItem showItem = new MenuItem(menu, SWT.PUSH);
        showItem.addSelectionListener(new ShowFileSelectionListener(viewer));
        showItem.setText("&Show");
        showItem.setImage(PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_OBJ_FOLDER));
//        MenuItem saveAsItem = new MenuItem(menu, SWT.PUSH);
//        saveAsItem.addSelectionListener(new SaveAsSelectionListener(viewer));
//        saveAsItem.setText("&Show");
//        saveAsItem.setImage(PlatformUI.getWorkbench().getSharedImages()
//                          .getImage(ISharedImages.IMG_ETOOL_SAVEAS_EDIT));
        viewer.getTable().setMenu(menu);
    }

    /**
     * Put a Text file into a String.
     *
     * @param file
     *            the Text file.
     * @return the Text of the File.
     */
    public static String file2String(final File file) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String tmp;
            while ( (tmp = br.readLine()) != null) {
                text = text.append(tmp + "\r\n");
            }
        } catch (FileNotFoundException e1) {
            // TODO Fehler h�ndling!
            e1.printStackTrace();
        } catch (IOException e2) {
            // TODO Fehler h�ndling!
            e2.printStackTrace();
        }
        return text.toString();
    }

    /**
     * @return the Default CSS SimpleDateFormat.
     */
    public static SimpleDateFormat getSimpleDateFormat() {
        return _simpleDateFormat;
    }

    /**
     * @return The CSS User-Name.
     */
    public static String getUserName() {
        User user = SecurityFacade.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUsername();
        }
        return "unknown";
    }

    /**
     *
     * @param parent
     *            The Parent composite.
     * @param node
     *            The Node that index the Spinner modify.
     * @param modifyListener
     *            The ModifyListener to set the Save dirty bit.
     * @param label
     *            Label text for Spinner
     * @param profiBusTreeView
     *            IO Config TreeViewer.
     * @return the Sort Index Spinner.
     */
    @Nonnull
    public static Spinner getIndexSpinner(@Nonnull final Composite parent,
                                          @Nonnull final AbstractNodeDBO node,
                                          @Nonnull final ModifyListener modifyListener,
                                          @Nonnull final String label,
                                          @Nonnull final ProfiBusTreeView profiBusTreeView) {
        int min = 0;
        int max = 99;

        // Label
        Label slotIndexLabel = new Label(parent, SWT.NONE);
        slotIndexLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, false, false, 1, 1));
        slotIndexLabel.setText(label);
        // Spinner
        final Spinner indexSpinner = new Spinner(parent, SWT.WRAP);
        indexSpinner.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, false, false, 1, 1));
        indexSpinner.setMinimum(min);
        indexSpinner.setMaximum(max);
        indexSpinner.setSelection(node.getSortIndex());
        indexSpinner.setData((short) node.getSortIndex());
        indexSpinner.addModifyListener(modifyListener);
        SpinnerModifyListener spinnerModifyListener = new SpinnerModifyListener(profiBusTreeView,
                                                                                node,
                                                                                indexSpinner);
        SpinnerKeyListener keyListener = new SpinnerKeyListener(spinnerModifyListener);
        indexSpinner.addKeyListener(keyListener);
        indexSpinner.addModifyListener(spinnerModifyListener);
        return indexSpinner;
    }

    public static Image getImageFromNode(final AbstractNodeDBO node) {
        return getImageFromNode(node, -1, -1);
    }

    public static Image getImageFromNode(final AbstractNodeDBO node, final int width, final int height) {
        if (node != null) {
            NodeImageDBO icon = node.getIcon();
            if (icon != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(icon.getImageBytes());
                Image image = new Image(null, bais);
                return image;
            }
            // Get Default Image
            if (node instanceof FacilityDBO) {
                return getImageMaxSize("icons/css.gif", width, height);
            } else if (node instanceof FacilityDBO) {
                return getImageMaxSize("icons/3055555W.bmp", width, height);
            } else if (node instanceof IocDBO) {
                return getImageMaxSize("icons/Buskopan.bmp", width, height);
            } else if (node instanceof ProfibusSubnetDBO) {
                return getImageMaxSize("icons/Profibus2020.bmp", width, height);
            } else if (node instanceof MasterDBO) {
                return getImageMaxSize("icons/ProfibusMaster2020.bmp", width, height);
            } else if (node instanceof SlaveDBO) {
                return getImageMaxSize("icons/sie80a6n.bmp", width, height);
            } else if (node instanceof ModuleDBO) {
                return getImageMaxSize("icons/3055555W.bmp", width, height);
            } else if (node instanceof ChannelDBO) {
                ChannelDBO channel = (ChannelDBO) node;
                return getChannelImage(channel.isInput(), channel.isDigital(), width, height);
            }
        }
        return null;
    }

    public static Image getImageMaxSize(final String imagePath, final int width, final int height) {
        ImageData imageData = CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(IOConfigActivatorUI.PLUGIN_ID, imagePath).getImageData();
        if ((width > 0) && (height > 0)) {
            int width2 = imageData.width;
            int height2 = imageData.height;

            if ((width2 > width) && (height2 > height)) {
                width2 = width;
                height2 = height;
            }

            return new Image(null, imageData.scaledTo(width2, height2));
        }
        return new Image(null, imageData);
    }

    private static Image getChannelImage(final boolean isInput, final boolean isDigital, final int width, final int height) {
        // DI
        if (isInput && !isDigital) {
            return getImageMaxSize("icons/Input_red16.png", width, height);
            // DO
        } else if (isInput && isDigital) {
            return getImageMaxSize("icons/Input_green16.png", width, height);
            // AI
        } else if (!isInput && !isDigital) {
            return getImageMaxSize("icons/Output_red16.png", width, height);
            // AO
        } else if (!isInput && isDigital) {
            return getImageMaxSize("icons/Output_green16.png", width, height);
        }

        return null;
    }
}