/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.style.sld.internal;

import java.awt.Color;

import net.refractions.udig.style.sld.SLDEditorPart;
import net.refractions.udig.ui.graphics.SLDs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.StyleBuilder;

/**
 * Simple view part for editing a Marker.
 * 
 * @author aalam
 * @since 1.0.0
 */
public class SLDMarkerEditorPart extends SLDEditorPart implements SelectionListener {

    private int opacityMaxValue = 100;
    private double opacityMaxValueFloat = 100.0;

    private StolenColorEditor borderColour;
    private Button borderEnabled;
    private Spinner borderWidth;
    private Spinner borderOpacity;

    private StolenColorEditor markerColour;
    private Button markerEnabled;
    private Combo markerType;
    private Spinner markerWidth;
    private Spinner markerOpacity;

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.style.sld.SLDEditorPart#getContentType()
     */
    public Class getContentType() {
        return PointSymbolizer.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.style.sld.SLDEditorPart#init()
     */
    public void init() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.style.sld.SLDEditorPart#reset()
     */
    public void reset() {
        // initialize the ui
        setStylingElements((PointSymbolizer) getContent());
    }

    private void setStylingElements( PointSymbolizer symbolizer ) {
        Color fill = SLDs.pointFill(symbolizer);
        Color border = SLDs.pointColor(symbolizer);
        int markerSize = SLDs.pointSize(symbolizer);
        int borderSize = SLDs.pointWidth(symbolizer);
        String wellKnownName = SLDs.pointWellKnownName(symbolizer);

        if (markerSize == SLDs.NOTFOUND) {
            markerSize = SymbolizerContent.DEFAULT_MARKER_WIDTH;
        }

        markerWidth.setSelection(markerSize);

        RGB colour = null;
        if (fill != null) {
            colour = new RGB(fill.getRed(), fill.getGreen(), fill.getBlue());
            markerEnabled.setSelection(true);
        } else {
            colour = new RGB(SymbolizerContent.DEFAULT_MARKER_COLOR.getRed(),
                    SymbolizerContent.DEFAULT_MARKER_COLOR.getGreen(),
                    SymbolizerContent.DEFAULT_MARKER_COLOR.getBlue());
            markerEnabled.setSelection(false);
        }
        markerColour.setColorValue(colour);

        borderWidth.setSelection(borderSize);

        if (border != null) {
            colour = new RGB(border.getRed(), border.getGreen(), border.getBlue());
            borderEnabled.setSelection(true);
        } else {
            colour = new RGB(SymbolizerContent.DEFAULT_BORDER_COLOR.getRed(),
                    SymbolizerContent.DEFAULT_BORDER_COLOR.getGreen(),
                    SymbolizerContent.DEFAULT_BORDER_COLOR.getBlue());
            borderEnabled.setSelection(false);
        }
        borderColour.setColorValue(colour);

        markerType.setItems(getStyleBuilder().getWellKnownMarkNames());
        if (wellKnownName == null) {
            wellKnownName = SymbolizerContent.DEFAULT_MARKER_TYPE;
        }
        int index = markerType.indexOf(wellKnownName);
        if (index == -1) {
            markerType.add(wellKnownName);
            markerType.select(0);
        } else {
            markerType.select(index);
        }

        double opacity = SLDs.pointBorderOpacity(symbolizer);
        if (Double.isNaN(opacity)) {
            opacity = SymbolizerContent.DEFAULT_MARKER_BORDER_OPACITY;
        }
        borderOpacity.setSelection((int) (opacity * opacityMaxValue));

        opacity = SLDs.pointOpacity(symbolizer);
        if (Double.isNaN(opacity)) {
            opacity = SymbolizerContent.DEFAULT_MARKER_OPACITY;
        }
        markerOpacity.setSelection((int) (opacity * opacityMaxValue));
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.style.StyleConfigurator#apply()
     */
    private void apply() {
        PointSymbolizer symbolizer = (PointSymbolizer) getContent();
        StyleBuilder styleBuilder = getStyleBuilder();

        Graphic g = symbolizer.getGraphic();

        Mark[] mark = new Mark[1];
        mark[0] = styleBuilder.createMark(markerType.getText());
        RGB colour = markerColour.getColorValue();
        if (markerEnabled.getSelection()) {
            mark[0].setFill(styleBuilder
                    .createFill(new Color(colour.red, colour.green, colour.blue)));
            mark[0].getFill().setOpacity(
                    styleBuilder.literalExpression(markerOpacity.getSelection()
                            / opacityMaxValueFloat));
        } else {
            mark[0].setFill(null);
        }
        colour = borderColour.getColorValue();
        g.setSize(styleBuilder.literalExpression(new Integer(markerWidth.getSelection())
                .doubleValue()));
        colour = borderColour.getColorValue();
        if (borderEnabled.getSelection()) {
            mark[0].setStroke(styleBuilder.createStroke(new Color(colour.red, colour.green,
                    colour.blue), (new Integer(borderWidth.getSelection())).doubleValue()));
            mark[0].getStroke().setOpacity(
                    styleBuilder.literalExpression(borderOpacity.getSelection()
                            / opacityMaxValueFloat));
        } else {
            mark[0].setStroke(null);
        }
        g.setMarks(mark);
    }

    /**
     * Construct a subpart labeled with the provided tag.
     * <p>
     * Creates a composite with a grid layout of the specifed columns, and a label with text from
     * tag.
     * </p>
     * 
     * @param parent
     * @param tag
     * @param numColumns number of columns (usually 2_
     * @return Composite with one label
     */
    private Composite subpart( Composite parent, String tag, int width ) {
        Composite subpart = new Composite(parent, SWT.NONE);
        RowLayout across = new RowLayout();
        across.type = SWT.HORIZONTAL;
        across.wrap = true;
        across.pack = true;
        across.fill = true;
        across.marginBottom = 1;
        across.marginRight = 2;

        subpart.setLayout(across);

        Label label = new Label(subpart, SWT.NONE);
        label.setText(tag);
        label.setAlignment(SWT.RIGHT);
        RowData data = new RowData();
        data.width = 40;
        data.height = 10;
        label.setLayoutData(data);

        return subpart;
    }

    /**
     * Create a row layout, with individual rows provided by sub part.
     * 
     * @see net.refractions.udig.style.StyleConfigurator#createControl(org.eclipse.swt.widgets.Composite)
     */
    protected Control createPartControl( Composite parent ) {
        RowLayout layout = new RowLayout();
        layout.pack = false;
        layout.wrap = true;
        layout.type = SWT.HORIZONTAL;
        layout.fill = true;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.spacing = 0;
        parent.setLayout(layout);

        borderPart(parent);
        fillPart(parent);
        markerPart(parent);

        return parent;
    }

    private void markerPart( Composite parent ) {
        Composite marker = subpart(parent, Messages.SLDMarkerEditorPart_label_marker, 2);

        markerType = new Combo(marker, SWT.READ_ONLY);
        markerType.addSelectionListener(this);

        markerWidth = new Spinner(marker, SWT.NONE);
        markerWidth.setMinimum(1);
        markerWidth.setMaximum(30);
        markerWidth.setPageIncrement(5);
        markerWidth.addSelectionListener(this);
        markerWidth.setToolTipText(Messages.SLDMarkerEditorPart_width_tooltip); 
    }

    private void borderPart( Composite parent ) {
        Composite border = subpart(parent, Messages.SLDMarkerEditorPart_label_border, 4);

        borderEnabled = new Button(border, SWT.CHECK);
        borderEnabled.addSelectionListener(this);
        borderEnabled.setToolTipText(Messages.SLDMarkerEditorPart_border_enabled_tooltip); 

        borderColour = new StolenColorEditor(border, this);

        borderWidth = new Spinner(border, SWT.NONE);
        borderWidth.setMinimum(1);
        borderWidth.setMaximum(30);
        borderWidth.setPageIncrement(5);
        borderWidth.addSelectionListener(this);
        borderWidth.setToolTipText(Messages.SLDMarkerEditorPart_border_width_tooltip); 

        borderOpacity = new Spinner(border, SWT.NONE);
        borderOpacity.setMinimum(0);
        borderOpacity.setMaximum(opacityMaxValue);
        borderOpacity.setPageIncrement(10);
        borderOpacity.setToolTipText(Messages.SLDMarkerEditorPart_border_opacity_tooltip); 
    }
    private void fillPart( Composite parent ) {
        Composite fill = subpart(parent, Messages.SLDMarkerEditorPart_label_fill , 3);
        markerEnabled = new Button(fill, SWT.CHECK);
        markerEnabled.addSelectionListener(this);
        markerEnabled.setToolTipText(Messages.SLDMarkerEditorPart_marker_enabled_tooltip); 

        markerColour = new StolenColorEditor(fill, this);

        markerOpacity = new Spinner(fill, SWT.NONE);
        markerOpacity.setMinimum(0);
        markerOpacity.setMaximum(opacityMaxValue);
        markerOpacity.setPageIncrement(10);
        markerOpacity.setToolTipText(Messages.SLDMarkerEditorPart_fill_opacity_tooltip); 
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected( SelectionEvent e ) {
        // Don't care.
        // TODO: Commit style here
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected( SelectionEvent e ) {
        apply();
    }

}
