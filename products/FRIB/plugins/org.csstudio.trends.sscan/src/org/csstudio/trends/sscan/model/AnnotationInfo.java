/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.sscan.model;

import java.io.PrintWriter;
import java.util.Calendar;

import org.csstudio.apputil.time.AbsoluteTimeParser;
import org.csstudio.apputil.xml.DOMHelper;
import org.csstudio.apputil.xml.XMLWriter;
import org.csstudio.data.values.ITimestamp;
import org.csstudio.data.values.TimestampFactory;
import org.csstudio.swt.xygraph.figures.Annotation.CursorLineStyle;
import org.csstudio.trends.sscan.ui.Plot;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Element;

/** Information about a Plot Annotation
 *
 *  <p>In the current implementation the plot library actually
 *  tracks the currently active annotations.
 *
 *  <p>This class is used by the model to read initial annotations
 *  from the {@link Model}'s XML file,
 *  and to later read them back from the {@link Plot} for writing
 *  them to the XML file.
 *
 *  <p>The info keeps less detail than the actual annotation in the XYGraph
 *  to reduce dependence on the plotting library.
 *
 *  @author Kay Kasemir
 */
public class AnnotationInfo
{
	final private ITimestamp timestamp;
	final private double value;
	final private int axis;
	final private String title;
	
	//ADD Laurent PHILIPPE 
	final private CursorLineStyle cursorLineStyle;
	final private boolean showName;
	final private boolean showPosition;
	/**
	 * Add because getTitleFont send a SWTERROR if the receiver is dispose. 
	 * It is the case when you save the plt file after ask to close CSS.
	 */
	final private FontData FontData;
	public FontData getFontData() {
		return FontData;
	}

	public RGB getColor() {
		return Color;
	}

	final private RGB Color;
	
	
	//No need to save Sample Information and show sample information
	
	public boolean isShowName() {
		return showName;
	}

	public boolean isShowPosition() {
		return showPosition;
	}

	public AnnotationInfo(final ITimestamp timestamp, final double value, final int axis,
			final String title, CursorLineStyle lineStyle, final boolean showName, final boolean showPosition, final FontData fontData, final RGB color)
    {
		
		this.timestamp = timestamp;
		this.value = value;
		this.axis = axis;
		this.title = title;
		this.cursorLineStyle = lineStyle;
		this.showName = showName;
		this.showPosition = showPosition;
		this.FontData = fontData;
		this.Color = color;
		
    }

	public AnnotationInfo(final ITimestamp timestamp, final double value, final int axis,
			final String title)
    {
		this(timestamp, value, axis, title, CursorLineStyle.NONE, false, false, null, null);
    }

	/** @return Time stamp */
	public ITimestamp getTimestamp()
	{
		return timestamp;
	}

	/** @return Title */
    public double getValue()
    {
        return value;
    }

    /** @return Title */
    public String getTitle()
    {
        return title;
    }

	/** @return Axis index */
    public int getAxis()
    {
        return axis;
    }

	@SuppressWarnings("nls")
    @Override
	public String toString()
	{
		return "Annotation for axis " + axis + ": '" + title + "' @ " + timestamp + ", " + value;
	}

    /** Write XML formatted annotation configuration
     *  @param writer PrintWriter
     */
	public void write(final PrintWriter writer)
    {
        XMLWriter.start(writer, 2, Model.TAG_ANNOTATION);
        writer.println();
        XMLWriter.XML(writer, 3, Model.TAG_X, timestamp);
        XMLWriter.XML(writer, 3, Model.TAG_VALUE, value);

        XMLWriter.XML(writer, 3, Model.TAG_NAME, title);
        XMLWriter.XML(writer, 3, Model.TAG_AXIS, axis);
        XMLWriter.XML(writer, 3, Model.TAG_ANNOTATION_CURSOR_LINE_STYLE, cursorLineStyle.name());
        XMLWriter.XML(writer, 3, Model.TAG_ANNOTATION_SHOW_NAME, showName);
        XMLWriter.XML(writer, 3, Model.TAG_ANNOTATION_SHOW_POSITION, showPosition);
        
        if(Color != null)
	    	 Model.writeColor(writer, 3, Model.TAG_ANNOTATION_COLOR, Color);
	   
	    
	     if(FontData != null)
	    	 XMLWriter.XML(writer, 3, Model.TAG_ANNOTATION_FONT, FontData);
        
        XMLWriter.end(writer, 2, Model.TAG_ANNOTATION);
        
        
        writer.println();
    }
  
    /** Create {@link AnnotationInfo} from XML document
     *  @param node XML node with item configuration
     *  @return PVItem
     *  @throws Exception on error
     */
	public static AnnotationInfo fromDocument(final Element node) throws Exception
    {
        final String timetext = DOMHelper.getSubelementString(node, Model.TAG_X, TimestampFactory.now().toString());
        final Calendar calendar = AbsoluteTimeParser.parse(timetext);
        final ITimestamp timestamp = TimestampFactory.fromCalendar(calendar);
        final double value = DOMHelper.getSubelementDouble(node, Model.TAG_VALUE, 0.0);
        final int axis = DOMHelper.getSubelementInt(node, Model.TAG_AXIS, 0);
		final String title = DOMHelper.getSubelementString(node, Model.TAG_NAME, "Annotation"); //$NON-NLS-1$
		final String lineStyle = DOMHelper.getSubelementString(node, Model.TAG_ANNOTATION_CURSOR_LINE_STYLE, CursorLineStyle.NONE.name()); //$NON-NLS-1$
		
		final boolean showName = DOMHelper.getSubelementBoolean(node, Model.TAG_ANNOTATION_SHOW_NAME, false); 
		final boolean showPosition = DOMHelper.getSubelementBoolean(node, Model.TAG_ANNOTATION_SHOW_POSITION, false); 
		
		final RGB Color = Model.loadColorFromDocument(node, Model.TAG_ANNOTATION_COLOR);	
		String fontInfo = DOMHelper.getSubelementString(node, Model.TAG_ANNOTATION_FONT);
	
		FontData fontData = null;
		if(fontInfo != null && !fontInfo.trim().equals("")){
			fontData = new FontData(fontInfo);
		}
		
        return new AnnotationInfo(timestamp, value, axis, title, CursorLineStyle.valueOf(lineStyle), showName, showPosition, fontData, Color);
    }

	public CursorLineStyle getCursorLineStyle() {
		return cursorLineStyle;
	}
}
