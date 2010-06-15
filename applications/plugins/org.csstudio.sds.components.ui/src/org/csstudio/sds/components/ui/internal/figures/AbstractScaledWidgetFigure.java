package org.csstudio.sds.components.ui.internal.figures;

import org.csstudio.sds.components.model.AbstractScaledWidgetModel;
import org.csstudio.sds.ui.figures.BorderAdapter;
import org.csstudio.sds.ui.figures.CrossedPaintHelper;
import org.csstudio.sds.ui.figures.IBorderEquippedWidget;
import org.csstudio.sds.ui.figures.ICrossedFigure;
import org.csstudio.swt.xygraph.linearscale.AbstractScale;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Base figure for a widget based on {@link AbstractScaledWidgetModel}.
 * 
 * @author Xihui Chen
 *
 */
public abstract class AbstractScaledWidgetFigure extends Figure implements
    ICrossedFigure {

	protected AbstractScale scale;
	
	protected boolean transparent;
	
	protected double value;
	
	protected double minimum;	
	
	protected double maximum;
	
	protected double majorTickMarkStepHint;
	
	protected boolean showMinorTicks;
	
	protected boolean showScale;
	
	protected boolean logScale;	
	

	/** A border adapter, which covers all border handlings. */
	private IBorderEquippedWidget _borderAdapter;

    private CrossedPaintHelper _crossedPaintHelper;
	
	@Override
	public boolean isOpaque() {
		return false;
	}
	/**
	 * {@inheritDoc}
	 */
	public void paintFigure(final Graphics graphics) {		
	    Rectangle figureBounds = getBounds().getCopy();
		if (!transparent) {
			graphics.setBackgroundColor(this.getBackgroundColor());
			Rectangle bounds = this.getBounds().getCopy();
			bounds.crop(this.getInsets());
			graphics.fillRectangle(bounds);
		}
		super.paintFigure(graphics);
        getCrossedPaintHelper().paintCross(graphics, figureBounds);
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(final double value) {
		this.value = 
			Math.max(scale.getRange().getLower(), Math.min(scale.getRange().getUpper(), value));
	}
	
	/**
	 * set the range of the scale
	 * @param min
	 * @param max
	 */
	public void setRange(final double min, final double max) {
		this.minimum = min;
		this.maximum = max;
		scale.setRange(new Range(min, max));
	}
	
	/**
	 * @param majorTickMarkStepHint the majorTickMarkStepHint to set
	 */
	public void setMajorTickMarkStepHint(double majorTickMarkStepHint) {
		this.majorTickMarkStepHint = majorTickMarkStepHint;
		scale.setMajorTickMarkStepHint((int) majorTickMarkStepHint);
	}

	/**
	 * @param showMinorTicks the showMinorTicks to set
	 */
	public void setShowMinorTicks(final boolean showMinorTicks) {
		this.showMinorTicks = showMinorTicks;
		scale.setMinorTicksVisible(showMinorTicks);
	}

	
	/**
	 * @param showScale the showScale to set
	 */
	public void setShowScale(final boolean showScale) {
		this.showScale = showScale;
		scale.setVisible(showScale);
	}

	/**
	 * @param logScale the logScale to set
	 */
	public void setLogScale(final boolean logScale) {
		this.logScale = logScale;
		scale.setLogScale(logScale);
		scale.setRange(new Range(minimum, maximum));
	}	

	/**
	 * Sets, if this widget should have a transparent background.
	 * @param transparent
	 * 				The new value for the transparent property
	 */
	public void setTransparent(final boolean transparent) {
		this.transparent = transparent;
	}	
	

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(final Class adapter) {
		if (adapter == IBorderEquippedWidget.class) {
			if(_borderAdapter==null) {
				_borderAdapter = new BorderAdapter(this);
			}
			return _borderAdapter;
		}
		return null;
	}
	
	protected CrossedPaintHelper getCrossedPaintHelper() {
        if(_crossedPaintHelper==null) {
            _crossedPaintHelper = new CrossedPaintHelper();
        }
        return _crossedPaintHelper;
    }

	public void setCrossedOut(boolean newValue) {
	    getCrossedPaintHelper().setCrossed(newValue);
	}
	
}