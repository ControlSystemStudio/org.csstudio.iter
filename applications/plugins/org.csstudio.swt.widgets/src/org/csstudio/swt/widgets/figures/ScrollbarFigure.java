/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.csstudio.swt.widgets.figures;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.csstudio.swt.widgets.datadefinition.IManualValueChangeListener;
import org.csstudio.swt.widgets.introspection.DefaultWidgetIntrospector;
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.swt.widgets.util.OPITimer;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.ArrowButton;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.FocusListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.KeyEvent;
import org.eclipse.draw2d.KeyListener;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Orientable;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.ScrollBarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**ScrollBar Figure which supports double value.
 * @author Xihui Chen
 *
 */
public class ScrollbarFigure extends Figure implements Orientable, Introspectable{
	


	class ThumbDragger
	extends MouseMotionListener.Stub
	implements MouseListener
{
	protected Point start;
	protected double dragRange;
	protected double revertValue;
	protected boolean armed;
	public ThumbDragger() { }
	
	public void mouseDoubleClicked(MouseEvent me) { }
	
	public void mouseDragged(MouseEvent me) {
		if (!armed) 
			return;
		Dimension difference = transposer.t(me.getLocation().getDifference(start));
		double change = (getValueRange()+getExtent()) * difference.height / dragRange;		
		manualSetValue(revertValue + change);
		me.consume();
	}
	
	public void mousePressed(MouseEvent me) {
		armed = true;
		start = me.getLocation();
		Rectangle area = new Rectangle(transposer.t(getClientArea()));
		Dimension thumbSize = transposer.t(thumb.getSize());
		if (buttonUp != null)
			area.height -= transposer.t(buttonUp.getSize()).height;
		if (buttonDown!= null)
			area.height -= transposer.t(buttonDown.getSize()).height;
		Dimension sizeDifference = new Dimension(area.width, 
													area.height - thumbSize.height);
		dragRange = sizeDifference.height;
		revertValue = getValue();
		me.consume();
	}
	
	public void mouseReleased(MouseEvent me) {
		if (!armed) 
			return;
		armed = false;
		me.consume();
	}
}
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("############.##"); //$NON-NLS-1$  
	
	private final static Color GRAY_COLOR = CustomMediaFactory.getInstance().getColor(
			CustomMediaFactory.COLOR_GRAY);
	private final static Color LABEL_COLOR = CustomMediaFactory.getInstance().getColor(
			new RGB(255, 255, 150));
	private boolean horizontal;
	private boolean showValueTip = true;
	private Label label;
	
	private OPITimer labelTimer;

	private Runnable timerTask;

	private double value = 50;
	
	private double minimum = 0;
	
	private double maximum = 100;
	private double extent = 30;
	
	private double stepIncrement = 1;
	
	private double pageIncrement = 10;
	private List<IManualValueChangeListener> listeners;
	private IFigure thumb;
	
	
	private Clickable pageUp, pageDown;
	
	private Clickable buttonUp, buttonDown;

	/**
	 * Transposes from vertical to horizontal if needed.
	 */
	protected final Transposer transposer = new Transposer();
	
	/**
	 * Listens to mouse events on the scrollbar to take care of scrolling.
	 */
	protected ThumbDragger thumbDragger = new ThumbDragger();
	
	private boolean valueIncreased;

	private static final Color COLOR_TRACK = FigureUtilities.mixColors(
			ColorConstants.white,
			ColorConstants.button);
	
	public ScrollbarFigure() {
		
		listeners = new ArrayList<IManualValueChangeListener>();
		
		
		initializeListeners();
		initializeParts();
		
	}
	
	public void addManualValueChangeListener(IManualValueChangeListener listener){
		if(listener != null)
			listeners.add(listener);
	}
	
	/**
	 * Creates the default 'Down' ArrowButton for the ScrollBar.
	 * 
	 * @return the down button
	 * @since 2.0
	 */
	protected Clickable createDefaultDownButton() {
		Button buttonDown = new ArrowButton();
		buttonDown.setBorder(new ButtonBorder(ButtonBorder.SCHEMES.BUTTON_SCROLLBAR));
		return buttonDown;
	}
	
	/**
	 * Creates the Scrollbar's "thumb", the draggable Figure that indicates the Scrollbar's 
	 * position.
	 * 
	 * @return the thumb figure
	 * @since 2.0
	 */
	protected IFigure createDefaultThumb() {
		Panel thumb = new Panel();
		thumb.setMinimumSize(new Dimension(6, 6));
		thumb.setBackgroundColor(ColorConstants.button);

		thumb.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.BUTTON_CONTRAST));
		return thumb;
	}
	

	/**
	 * Creates the default 'Up' ArrowButton for the ScrollBar.
	 * 
	 * @return the up button
	 * @since 2.0
	 */
	protected Clickable createDefaultUpButton() {
		Button buttonUp = new ArrowButton();
		buttonUp.setBorder(new ButtonBorder(ButtonBorder.SCHEMES.BUTTON_SCROLLBAR));
		return buttonUp;
	}

	/**
	 * Creates the pagedown Figure for the Scrollbar.
	 * 
	 * @return the page down figure
	 * @since 2.0 
	 */
	protected Clickable createPageDown() {
		return createPageUp();
	}

	/**
	 * Creates the pageup Figure for the Scrollbar.
	 * 
	 * @return the page up figure
	 * @since 2.0 
	 */
	protected Clickable createPageUp() {
		final Clickable clickable = new Clickable();
		clickable.setOpaque(true);
		clickable.setBackgroundColor(COLOR_TRACK);
		clickable.setRequestFocusEnabled(false);
		clickable.setFocusTraversable(false);
		clickable.addChangeListener(new ChangeListener() {
			public void handleStateChanged(ChangeEvent evt) {
				if (clickable.getModel().isArmed())
					clickable.setBackgroundColor(ColorConstants.black);
				else
					clickable.setBackgroundColor(COLOR_TRACK);
			}
		});
		return clickable;
	}

	private void fireManualValueChange(double value) {
		for(IManualValueChangeListener listener : listeners)
			listener.manualValueChanged(value);
	}

	public BeanInfo getBeanInfo() throws IntrospectionException {
		return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
	}

	
	
	
	public double getCoercedValue(){
		return value < minimum ? minimum : (value > maximum ? maximum : value);
	}
	/**
	 * @return the extent
	 */
	public double getExtent() {
		return extent;
	}

	/**
	 * @return the maximum
	 */
	public double getMaximum() {
		return maximum;
	}


	
	/**
	 * @return the minimum
	 */
	public double getMinimum() {
		return minimum;
	}

	/**
	 * @return the pageIncrement
	 */
	public final double getPageIncrement() {
		return pageIncrement;
	}

	/**
	 * @return the stepIncrement
	 */
	public final double getStepIncrement() {
		return stepIncrement;
	}
	public double getValue() {
		return value;
	}

	

	/**
	 * Returns the size of the range of allowable values.
	 * @return the value range
	 */
	protected double getValueRange() {
		return getMaximum() - getExtent() - getMinimum();
	}


	/**
	 * @param up
	 */
	private void hookFocusListener(Clickable up) {
		up.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				if(!hasFocus())
					requestFocus();				
			}
		});
	}

	
	/**
	 * 
	 */
	private void initializeListeners() {
		setRequestFocusEnabled(true);
		setFocusTraversable(true);		
		addKeyListener(new KeyListener() {
				
				public void keyPressed(KeyEvent ke) {
					if((ke.keycode == SWT.ARROW_UP && !isHorizontal()) || 
							(ke.keycode == SWT.ARROW_LEFT && isHorizontal()))
						stepUp();
					else if((ke.keycode == SWT.ARROW_DOWN && !isHorizontal()) || 
							(ke.keycode == SWT.ARROW_RIGHT && isHorizontal()))
						stepDown();
					else if((ke.keycode == SWT.PAGE_DOWN && !isHorizontal())||
							(ke.keycode == SWT.PAGE_UP && isHorizontal()))
						pageDown();
					else if((ke.keycode == SWT.PAGE_UP && !isHorizontal())||
							(ke.keycode == SWT.PAGE_DOWN && isHorizontal()))						
						pageUp();
				}
				
				public void keyReleased(KeyEvent ke) {				
				}
			});
			
		addFocusListener(new FocusListener() {
				
				public void focusGained(FocusEvent fe) {
					repaint();
				}
				
				public void focusLost(FocusEvent fe) {
					repaint();
				}
		});
	}


	/**
	 * Initilization of the ScrollBar. Sets the Scrollbar to have a ScrollBarLayout with 
	 * vertical orientation. Creates the Figures that make up the components of the ScrollBar.
	 * 
	 * @since 2.0
	 */
	protected void initializeParts() {
		setLayoutManager(new ScrollBarFigureLayout(transposer));
		setUpClickable(createDefaultUpButton());
		setDownClickable(createDefaultDownButton());
		setPageUp(createPageUp());
		setPageDown(createPageDown());
		setThumb(createDefaultThumb());
		label = new Label();
		label.setBackgroundColor(LABEL_COLOR);
		label.setBorder(new LineBorder(GRAY_COLOR));	
		label.setVisible(false);
		add(label, "Label"); //$NON-NLS-1$
	}


	private void initLabelTimer(){
		if(labelTimer == null){
			labelTimer = new OPITimer();
			timerTask = new Runnable() {
				
				public void run() {
					Display.getDefault().asyncExec(new Runnable() {
						
						public void run() {
							label.setVisible(false);
						}
					});					
				}
			};
		}			
	}
	
	/**
	 * @return the horizontal
	 */
	public boolean isHorizontal() {
		return horizontal;
	}

	/**
	 * @return the showValueTip
	 */
	public boolean isShowValueTip() {
		return showValueTip;
	}
	
	@Override
	protected void layout() {
		super.layout();
		if(label.isVisible()){
			Rectangle thumbBounds = thumb.getBounds();
			Dimension size = label.getPreferredSize();
			if(isHorizontal())
				label.setBounds(new Rectangle(
						thumbBounds.x + (valueIncreased ? -size.width : thumbBounds.width), 
						thumbBounds.y, size.width, size.height));
			else
				label.setBounds(new Rectangle(thumbBounds.x, 
						thumbBounds.y + (valueIncreased ? -size.height : thumbBounds.height),
						size.width, size.height));
		}
	}
	/**Set Value from manual control of the widget. Value will be coerced in range.
	 * @param value
	 */
	public void manualSetValue(double value){
		value = Math.max(getMinimum(), Math.min(getMaximum(), value));
		if (this.value == value)
			return;
		if(showValueTip){
			valueIncreased = value > this.value;
				
			label.setText("" + DECIMAL_FORMAT.format(value));
			label.setVisible(true);
			initLabelTimer();
			if(!labelTimer.isDue())
				labelTimer.reset();
			else
				labelTimer.start(timerTask, 1000);
		}
		setValue(value);
		fireManualValueChange(getValue());
		
	}
	
	public void pageDown(){
		manualSetValue(getValue() + pageIncrement);
	}

	public void pageUp(){
		manualSetValue(getValue() - pageIncrement);
	}
	
	@Override
	protected void paintClientArea(Graphics graphics) {
		super.paintClientArea(graphics);
		if(hasFocus()){
			graphics.setForegroundColor(ColorConstants.black);
			graphics.setBackgroundColor(ColorConstants.white);

			Rectangle area = getClientArea();					
			graphics.drawFocus(area.x, area.y, area.width-1, area.height-1);
		}
	}
	
	public void removeManualValueChangeListener(IManualValueChangeListener listener){
		if(listeners.contains(listener))
			listeners.remove(listener);
	}
	
	/**
	 * @see IFigure#revalidate()
	 */
	public void revalidate() {
		// Override default revalidate to prevent going up the parent chain. Reason for this 
		// is that preferred size never changes unless orientation changes.
		invalidate();
		getUpdateManager().addInvalidFigure(this);
	}
	
	public void setDirection(int direction) {
		
	}

	/**
	 * Sets the Clickable that represents the down arrow of the Scrollbar to <i>down</i>.
	 * 
	 * @param down the down button
	 * @since 2.0
	 */
	public void setDownClickable(Clickable down) {
		hookFocusListener(down);
		if (buttonDown != null) {
			remove(buttonDown);
		}
		buttonDown = down;
		if (buttonDown != null) {
			if (buttonDown instanceof Orientable)
				((Orientable)buttonDown).setDirection(isHorizontal() 
														? Orientable.EAST 
														: Orientable.SOUTH);
			buttonDown.setFiringMethod(Clickable.REPEAT_FIRING);
			buttonDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stepDown();
				}
			});
			add(buttonDown, ScrollBarLayout.DOWN_ARROW);
		}
	}
	
	
	/**
	 * @see IFigure#setEnabled(boolean)
	 */
	public void setEnabled(boolean value) {
		if (isEnabled() == value)
			return;
		super.setEnabled(value);
		setChildrenEnabled(value);
		if (thumb != null) {
			thumb.setVisible(value);
			revalidate();
		}
	}

	/**
	 * @param extent the extent to set
	 */
	public void setExtent(double extent) {
		if(this.extent == extent)
			return;
		this.extent = extent;
		revalidate();
	}

	/**
	 * Sets the orientation of the ScrollBar. If <code>true</code>, the Scrollbar will have 
	 * a horizontal orientation. If <code>false</code>, the scrollBar will have a vertical 
	 * orientation.
	 * 
	 * @param value <code>true</code> if the scrollbar should be horizontal
	 * @since 2.0
	 */
	public final void setHorizontal(boolean value) {
		setOrientation(value ? HORIZONTAL : VERTICAL);
	}

	/**
	 * @param maximum the maximum to set
	 */
	public void setMaximum(double maximum) {
		if(this.maximum == maximum)
			return;
		this.maximum = maximum;
		revalidate();
	}

	/**
	 * @param minimum the minimum to set
	 */
	public void setMinimum(double minimum) {
		if(this.minimum == minimum)
			return;
		this.minimum = minimum;
		revalidate();

	}

	/**
	 * @see Orientable#setOrientation(int)
	 */
	public void setOrientation(int value) {
		if ((value == HORIZONTAL) == isHorizontal())
			return;
		horizontal = value == HORIZONTAL;
		transposer.setEnabled(horizontal);

		setChildrenOrientation(value);
		super.revalidate();
	}

	/**
	 * Sets the pagedown button to the passed Clickable. The pagedown button is the figure 
	 * between the down arrow button and the ScrollBar's thumb figure.
	 * 
	 * @param down the page down figure
	 * @since 2.0
	 */
	public void setPageDown(Clickable down) {
		hookFocusListener(down);

		if (pageDown != null)
			remove(pageDown);
		pageDown = down;
		if (pageDown != null) {
			pageDown.setFiringMethod(Clickable.REPEAT_FIRING);
			pageDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					pageDown();
				}
			});
			add(down, ScrollBarLayout.PAGE_DOWN);
		}
	}

	public void setPageIncrement(double pageIncrement) {
		this.pageIncrement = pageIncrement;
	}

	/**
	 * Sets the pageup button to the passed Clickable. The pageup button is the rectangular 
	 * figure between the down arrow button and the ScrollBar's thumb figure.
	 * 
	 * @param up the page up figure
	 * @since 2.0
	 */
	public void setPageUp(Clickable up) {
		hookFocusListener(up);

		if (pageUp != null)
			remove(pageUp);
		pageUp = up;
		if (pageUp != null) {
			pageUp.setFiringMethod(Clickable.REPEAT_FIRING);
			pageUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					pageUp();
				}
			});
			add(pageUp, ScrollBarLayout.PAGE_UP);
		}
	}

	/**
	 * @param showValueTip the showValueTip to set
	 */
	public void setShowValueTip(boolean showValueTip) {
		this.showValueTip = showValueTip;
	}
	

	/**
	 * @param stepIncrement the stepIncrement to set
	 */
	public final void setStepIncrement(double stepIncrement) {
		this.stepIncrement = stepIncrement;
	}
	
	
	/**
	 * Sets the ScrollBar's thumb to the passed Figure. The thumb is the draggable component 
	 * of the ScrollBar that indicates the ScrollBar's position.
	 * 
	 * @param figure the thumb figure
	 * @since 2.0
	 */
	public void setThumb(IFigure figure) {
		figure.addMouseListener(new MouseListener.Stub(){
			@Override
			public void mousePressed(MouseEvent me) {
				if(!hasFocus())
					requestFocus();
			}
		});
		if (thumb != null) {
			thumb.removeMouseListener(thumbDragger);
			thumb.removeMouseMotionListener(thumbDragger);
			remove(thumb);
		}
		thumb = figure;
		if (thumb != null) {
			thumb.addMouseListener(thumbDragger);
			thumb.addMouseMotionListener(thumbDragger);
			add(thumb, ScrollBarLayout.THUMB);
		}
	}

	/**
	 * Sets the Clickable that represents the up arrow of the Scrollbar to <i>up</i>.
	 * 
	 * @param up the up button
	 * @since 2.0
	 */
	public void setUpClickable(Clickable up) {
		hookFocusListener(up);

		if (buttonUp != null) {
			remove(buttonUp);
		}
		buttonUp = up;
		if (up != null) {
			if (up instanceof Orientable)
				((Orientable)up).setDirection(isHorizontal() 
												? Orientable.WEST 
												: Orientable.NORTH);
			buttonUp.setFiringMethod(Clickable.REPEAT_FIRING);
			buttonUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stepUp();
				}
			});
			add(buttonUp, ScrollBarLayout.UP_ARROW);
		}
	}


	/**
	 * @param value the value to set
	 */
	public void setValue(final double value) {
		if(this.value == value)
			return;
		this.value = value;
		revalidate();
		repaint();
	}

	public void stepDown(){
		manualSetValue(getValue() + stepIncrement);
	}

	public void stepUp(){
		manualSetValue(getValue() - stepIncrement);
	}
	
	
}