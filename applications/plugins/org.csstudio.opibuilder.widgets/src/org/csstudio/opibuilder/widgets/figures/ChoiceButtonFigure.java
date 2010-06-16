package org.csstudio.opibuilder.widgets.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Toggle;
import org.eclipse.draw2d.ToggleButton;

/**The figure of choice button.
 * @author Xihui Chen
 *
 */
public class ChoiceButtonFigure extends AbstractChoiceFigure {

	
	public ChoiceButtonFigure() {
		selectedColor = ColorConstants.buttonLightest;
	}
	
	@Override
	protected Toggle createToggle(String text) {
		return new ColorToggleButton(text);
	}
	
	class ColorToggleButton extends ToggleButton{
		
		/**
		 * Constructs a ToggleButton with the passed string as its text.
		 * 
		 * @param text the text to be displayed on the button
		 * @since 2.0
		 */
		public ColorToggleButton(String text) {
			super(text, null);
		}
		
		/**
		 * Draws a checkered pattern to emulate a toggle button that is in the selected state.
		 * @param graphics	The Graphics object used to paint
		 */
		protected void fillCheckeredRectangle(Graphics graphics) {
			graphics.setBackgroundColor(selectedColor);
			graphics.setForegroundColor(ColorConstants.buttonLightest);
			graphics.fillRectangle(getClientArea());

			graphics.restoreState();
		}
	}
	

}