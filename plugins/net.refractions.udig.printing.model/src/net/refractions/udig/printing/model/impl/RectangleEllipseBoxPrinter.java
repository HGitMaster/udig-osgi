/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.printing.model.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.refractions.udig.printing.model.AbstractBoxPrinter;
import net.refractions.udig.printing.model.Page;
import net.refractions.udig.project.IProjectElement;
import net.refractions.udig.project.ui.UDIGEditorInput;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RectangleEllipseBoxPrinter extends AbstractBoxPrinter {

    private static final String LINEWIDTH_KEY = "linewidth"; //$NON-NLS-1$
    private static final String LINECOLOR_KEY = "linecolor"; //$NON-NLS-1$
    private static final String LINEALPHA_KEY = "linealpha"; //$NON-NLS-1$
    private static final String FILLCOLOR_KEY = "fillcolor"; //$NON-NLS-1$
    private static final String FILLALPHA_KEY = "fillalpha"; //$NON-NLS-1$
    private static final String SHAPETYPE_KEY = "shapetype"; //$NON-NLS-1$
    private static final String SCALEFACTOR_KEY = "scalefactor"; //$NON-NLS-1$

    public static final int RECTANGLE = 0;
    public static final int ROUNDEDRECTANGLE = 1;
    public static final int ELLIPSE = 2;

    private Color lineColor = Color.GRAY;
    private Color fillColor = Color.GRAY;
    private float lineWidth = 1f;
    private int lineAlpha = 255;
    private int fillAlpha = 128;

    private int type = RECTANGLE;

    private float scaleFactor = Float.NaN;

    public RectangleEllipseBoxPrinter() {
        super();
    }
    
    public RectangleEllipseBoxPrinter( float scaleFactor ) {
        super();
        this.scaleFactor = scaleFactor;
    }

    private float getScaleFactor() {
        if (Float.isNaN(scaleFactor)) {
            // try to get it from the page
            Page page = getBox().getPage();
            if (page != null) {
                scaleFactor = (float) page.getSize().width / (float) page.getPaperSize().height;
            }
        }
        return scaleFactor;
    }

    private void setScaleFactor( float scaleFactor ) {
        this.scaleFactor = scaleFactor;
    }

    public void draw( Graphics2D graphics, IProgressMonitor monitor ) {
        super.draw(graphics, monitor);

        int boxWidth = getBox().getSize().width - (int) lineWidth / 2;
        int boxHeight = getBox().getSize().height - (int) lineWidth / 2;
        int roundedEgde = 50;
        // if (inPreviewMode) {
        // boxWidth = (int) ((float) boxWidth * scaleFactor);
        // boxHeight = (int) ((float) boxHeight * scaleFactor);
        // roundedEgde = (int) ((float) roundedEgde * scaleFactor);
        // }

        Shape shape = null;
        if (type == ROUNDEDRECTANGLE) {
            shape = new RoundRectangle2D.Double(0, 0, boxWidth, boxHeight, roundedEgde, roundedEgde);
        } else if (type == ELLIPSE) {
            shape = new Ellipse2D.Double(0, 0, boxWidth, boxHeight);
        } else {
            shape = new Rectangle2D.Double(0, 0, boxWidth, boxHeight);
        }

        graphics.setPaint(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(),
                fillAlpha));
        graphics.fill(shape);
        BasicStroke stroke = new BasicStroke(lineWidth);
        graphics.setStroke(stroke);
        graphics.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(),
                lineAlpha));
        graphics.draw(shape);

    }

    public void createPreview( Graphics2D graphics, IProgressMonitor monitor ) {
        draw(graphics, monitor);
        setDirty(false);
    }

    public void save( IMemento memento ) {
        memento.putFloat(LINEWIDTH_KEY, lineWidth);
        memento.putString(LINECOLOR_KEY, color2String(lineColor));
        memento.putInteger(LINEALPHA_KEY, lineAlpha);
        memento.putString(FILLCOLOR_KEY, color2String(fillColor));
        memento.putInteger(FILLALPHA_KEY, fillAlpha);
        memento.putInteger(SHAPETYPE_KEY, type);
        memento.putFloat(SCALEFACTOR_KEY, getScaleFactor());
    }

    public void load( IMemento memento ) {
        lineWidth = memento.getFloat(LINEWIDTH_KEY);
        lineColor = string2Color(memento.getString(LINECOLOR_KEY));
        lineAlpha = memento.getInteger(LINEALPHA_KEY);
        fillColor = string2Color(memento.getString(FILLCOLOR_KEY));
        fillAlpha = memento.getInteger(FILLALPHA_KEY);
        type = memento.getInteger(SHAPETYPE_KEY);
        setScaleFactor(memento.getFloat(SCALEFACTOR_KEY));
    }

    private String color2String( Color color ) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }
    private Color string2Color( String string ) {
        String[] split = string.split(",");
        Color color = new Color(Integer.parseInt(split[0].trim()), Integer
                .parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
        return color;
    }

    public String getExtensionPointID() {
        return "net.refractions.udig.printing.ui.standardBoxes"; //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {
        return null;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor( Color lineColor ) {
        this.lineColor = lineColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor( Color fillColor ) {
        this.fillColor = fillColor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth( float lineWidth ) {
        this.lineWidth = lineWidth;
    }

    public int getLineAlpha() {
        return lineAlpha;
    }

    public void setLineAlpha( int lineAlpha ) {
        this.lineAlpha = lineAlpha;
    }

    public int getFillAlpha() {
        return fillAlpha;
    }

    public void setFillAlpha( int fillAlpha ) {
        this.fillAlpha = fillAlpha;
    }

    public int getType() {
        return type;
    }

    public void setType( int type ) {
        this.type = type;
    }

}
