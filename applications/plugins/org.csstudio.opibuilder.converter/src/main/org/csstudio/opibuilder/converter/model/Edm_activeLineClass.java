package org.csstudio.opibuilder.converter.model;

/**
 * Specific class representing activeLineClass widget.
 *
 * @author Xihui Chen
 *
 */
public class Edm_activeLineClass extends EdmWidget {


	@EdmAttributeAn private EdmColor lineColor;
	@EdmAttributeAn private EdmColor fillColor;
	@EdmAttributeAn private int numPoints;
	@EdmAttributeAn private EdmPointsList xPoints;
	@EdmAttributeAn private EdmPointsList yPoints;
	
	
	@EdmAttributeAn @EdmOptionalAn private int lineWidth;
	@EdmAttributeAn @EdmOptionalAn private EdmLineStyle lineStyle;	
	@EdmAttributeAn @EdmOptionalAn private String arrows;
	@EdmAttributeAn @EdmOptionalAn private boolean closePolygon;
	@EdmAttributeAn @EdmOptionalAn private boolean lineAlarm;
	@EdmAttributeAn @EdmOptionalAn private boolean fill;
	@EdmAttributeAn @EdmOptionalAn private boolean fillAlarm;
	@EdmAttributeAn @EdmOptionalAn private String alarmPv;
	
	@EdmAttributeAn @EdmOptionalAn private String visPv;
	@EdmAttributeAn @EdmOptionalAn private double visMax;
	@EdmAttributeAn @EdmOptionalAn private double visMin;
	@EdmAttributeAn @EdmOptionalAn private boolean visInvert;

	public Edm_activeLineClass(EdmEntity genericEntity) throws EdmException {
		super(genericEntity);
	}


	public int getNumPoints(){
		return numPoints;
	}

	public EdmPointsList getXPoints() {
		return xPoints;
	}
	
	public EdmPointsList getYPoints() {
		return yPoints;
	}
	
	public EdmColor getLineColor() {
		return lineColor;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public EdmLineStyle getLineStyle() {
		return lineStyle;
	}

	public EdmColor getFillColor() {
		return fillColor;
	}

	public boolean isFill(){
		return fill;
	}
	
	
	/**
	 * @return the arrows
	 */
	public final String getArrows() {
		return arrows;
	}


	/**
	 * @return the closePolygon
	 */
	public final boolean isClosePolygon() {
		return closePolygon;
	}


	/**
	 * @return the lineAlarm
	 */
	public final boolean isLineAlarm() {
		return lineAlarm;
	}


	/**
	 * @return the fillAlarm
	 */
	public final boolean isFillAlarm() {
		return fillAlarm;
	}


	/**
	 * @return the alarmPv
	 */
	public final String getAlarmPv() {
		return alarmPv;
	}


	public String getVisPv() {
		return visPv;
	}

	public double getVisMax() {
		return visMax;
	}

	public double getVisMin() {
		return visMin;
	}

	public boolean isVisInvert() {
		return visInvert;
	}
}