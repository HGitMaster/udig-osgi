/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.udig.style.jgrass.legend;

import java.awt.Color;

import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.StyleBlackboard;
import net.refractions.udig.project.ui.internal.dialogs.ColorEditor;
import net.refractions.udig.style.IStyleConfigurator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.udig.style.jgrass.messages.Messages;

public class VectorLegendGraphicStyleConfigurator extends IStyleConfigurator
        implements
            SelectionListener,
            ModifyListener {

    private Text xposText;
    private ColorEditor fontColour;
    private ColorEditor backgroundColour;
    private Text yposText;
    private Text legHeightText;
    private Text legWidthText;
    private Text boxWidthText;
    private Button isroundedButton;
    private Text backgroundAlphaText;
    private Text forgroundAlphaText;
    private ColorEditor foregroundColor;
    private VectorLegendStyle style;

    /*
     * verticalMargin = 3; horizontalMargin = 2; verticalSpacing = 5; horizontalSpacing = 3;
     * indentSize = 10; imageHeight = 16; imageWidth = 16; maxWidth = -1; maxHeight = -1;
     * foregroundColour = Color.BLACK; backgroundColour = Color.WHITE; location = new Point(30, 10);
     */

    public void createControl( Composite parent ) {

        ScrolledComposite scrollComposite = new ScrolledComposite(parent, SWT.H_SCROLL
                | SWT.V_SCROLL);
        Composite c = new Composite(scrollComposite, SWT.None);
        c.setLayout(new GridLayout());
        c.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL));

        GridData layoutData = null;
        Group propertiesGroup = new Group(c, SWT.BORDER);

        GridLayout layout2 = new GridLayout(2, true);
        propertiesGroup.setLayout(layout2);

        Label xposLabel = new Label(propertiesGroup, SWT.NONE);
        xposLabel.setLayoutData(layoutData);
        xposLabel.setText(Messages.getString("LegendGraphicStyleConfigurator.xpos")); //$NON-NLS-1$
        xposText = new Text(propertiesGroup, SWT.BORDER);
        xposText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Label yposLabel = new Label(propertiesGroup, SWT.NONE);
        yposLabel.setLayoutData(layoutData);
        yposLabel.setText(Messages.getString("LegendGraphicStyleConfigurator.ypos")); //$NON-NLS-1$
        yposText = new Text(propertiesGroup, SWT.BORDER);
        yposText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Label legWidthLabel = new Label(propertiesGroup, SWT.NONE);
        legWidthLabel.setLayoutData(layoutData);
        legWidthLabel.setText(Messages.getString("LegendGraphicStyleConfigurator.legendwidth")); //$NON-NLS-1$
        legWidthText = new Text(propertiesGroup, SWT.BORDER);
        legWidthText
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Label legHeightLabel = new Label(propertiesGroup, SWT.NONE);
        legHeightLabel.setLayoutData(layoutData);
        legHeightLabel.setText(Messages.getString("LegendGraphicStyleConfigurator.legendheight")); //$NON-NLS-1$
        legHeightText = new Text(propertiesGroup, SWT.BORDER);
        legHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        Label boxWidthLabel = new Label(propertiesGroup, SWT.NONE);
        boxWidthLabel.setLayoutData(layoutData);
        boxWidthLabel.setText(Messages.getString("LegendGraphicStyleConfigurator.boxwidth")); //$NON-NLS-1$
        boxWidthText = new Text(propertiesGroup, SWT.BORDER);
        boxWidthText
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        isroundedButton = new Button(propertiesGroup, SWT.BORDER | SWT.CHECK);
        isroundedButton.setText(Messages.getString("LegendGraphicStyleConfigurator.roundedrect")); //$NON-NLS-1$
        GridData gdata = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gdata.horizontalSpan = 2;
        isroundedButton.setLayoutData(gdata);

        Label fontColourLabel = new Label(propertiesGroup, SWT.NONE);
        fontColourLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        fontColourLabel.setText(Messages.getString("LegendGraphicStyleConfigurator.fontcolor")); //$NON-NLS-1$
        fontColour = new ColorEditor(propertiesGroup);

        Label backgroundColourLabel = new Label(propertiesGroup, SWT.NONE);
        backgroundColourLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        backgroundColourLabel.setText(Messages
                .getString("LegendGraphicStyleConfigurator.backgroundcolor")); //$NON-NLS-1$
        backgroundColour = new ColorEditor(propertiesGroup);

        Label backgroundAlphaLabel = new Label(propertiesGroup, SWT.NONE);
        backgroundAlphaLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        backgroundAlphaLabel.setText(Messages
                .getString("LegendGraphicStyleConfigurator.backgroundalpha")); //$NON-NLS-1$
        backgroundAlphaText = new Text(propertiesGroup, SWT.BORDER);
        backgroundAlphaText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        Label foregroundColourLabel = new Label(propertiesGroup, SWT.NONE);
        foregroundColourLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        foregroundColourLabel.setText(Messages
                .getString("LegendGraphicStyleConfigurator.foregroundcolor")); //$NON-NLS-1$
        foregroundColor = new ColorEditor(propertiesGroup);

        Label forgroundAlphaLabel = new Label(propertiesGroup, SWT.NONE);
        forgroundAlphaLabel.setLayoutData(layoutData);
        forgroundAlphaLabel.setText(Messages
                .getString("LegendGraphicStyleConfigurator.foregroundalpha")); //$NON-NLS-1$
        forgroundAlphaText = new Text(propertiesGroup, SWT.BORDER);
        forgroundAlphaText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        c.layout();
        Point size = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        c.setSize(size);
        scrollComposite.setContent(c);

        xposText.addModifyListener(this);
        yposText.addModifyListener(this);
        legWidthText.addModifyListener(this);
        legHeightText.addModifyListener(this);
        boxWidthText.addModifyListener(this);
        backgroundAlphaText.addModifyListener(this);
        forgroundAlphaText.addModifyListener(this);
        isroundedButton.addSelectionListener(this);
        foregroundColor.addSelectionListener(this);
        backgroundColour.addSelectionListener(this);
        fontColour.addSelectionListener(this);
    }

    @Override
    public boolean canStyle( Layer aLayer ) {
        return aLayer.hasResource(VectorLegendGraphic.class);
    }

    @Override
    protected void refresh() {
        StyleBlackboard styleBlackboard = getLayer().getStyleBlackboard();
        style = (VectorLegendStyle) styleBlackboard.get(VectorLegendStyleContent.ID);

        if (style == null) {
            style = VectorLegendStyleContent.createDefault();
            styleBlackboard.put(VectorLegendStyleContent.ID, style);
            styleBlackboard.setSelected(new String[]{VectorLegendStyleContent.ID});
        }

        fontColour.setColorValue(new RGB(style.fontColor.getRed(), style.fontColor.getGreen(),
                style.fontColor.getBlue()));
        foregroundColor.setColorValue(new RGB(style.foregroundColor.getRed(), style.foregroundColor
                .getGreen(), style.foregroundColor.getBlue()));
        backgroundColour.setColorValue(new RGB(style.backgroundColor.getRed(),
                style.backgroundColor.getGreen(), style.backgroundColor.getBlue()));

        xposText.setText(Integer.toString(style.xPos));
        yposText.setText(Integer.toString(style.yPos));
        legWidthText.setText(Integer.toString(style.legendWidth));
        legHeightText.setText(Integer.toString(style.legendHeight));
        boxWidthText.setText(Integer.toString(style.boxWidth));
        forgroundAlphaText.setText(Integer.toString(style.fAlpha));
        backgroundAlphaText.setText(Integer.toString(style.bAlpha));
        isroundedButton.setSelection(style.isRoundedRectangle);

    }

    public void preApply() {
        updateBlackboard();
    }

    private void updateBlackboard() {
        StyleBlackboard styleBlackboard = getLayer().getStyleBlackboard();
        style = (VectorLegendStyle) styleBlackboard.get(VectorLegendStyleContent.ID);

        if (style == null) {
            style = VectorLegendStyleContent.createDefault();
            styleBlackboard.put(VectorLegendStyleContent.ID, style);
            styleBlackboard.setSelected(new String[]{VectorLegendStyleContent.ID});
        }

        RGB bg = backgroundColour.getColorValue();
        try {
            int bAlpha = Integer.parseInt(backgroundAlphaText.getText());
            style.backgroundColor = new Color(bg.red, bg.green, bg.blue, bAlpha);
        } catch (Exception e) {
            style.backgroundColor = new Color(bg.red, bg.green, bg.blue);
        }
        bg = foregroundColor.getColorValue();
        try {
            int fAlpha = Integer.parseInt(forgroundAlphaText.getText());
            style.foregroundColor = new Color(bg.red, bg.green, bg.blue, fAlpha);
        } catch (Exception e) {
            style.foregroundColor = new Color(bg.red, bg.green, bg.blue);
        }
        bg = fontColour.getColorValue();
        style.fontColor = new Color(bg.red, bg.green, bg.blue);

        style.xPos = Integer.parseInt(xposText.getText());
        style.yPos = Integer.parseInt(yposText.getText());
        style.legendHeight = Integer.parseInt(legHeightText.getText());
        style.legendWidth = Integer.parseInt(legWidthText.getText());
        style.boxWidth = Integer.parseInt(boxWidthText.getText());
        style.isRoundedRectangle = isroundedButton.getSelection();

        styleBlackboard.put(VectorLegendStyleContent.ID, style);
    }

    public void widgetSelected( SelectionEvent e ) {
        updateBlackboard();
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
        updateBlackboard();
    }

    public void modifyText( ModifyEvent e ) {
        // updateBlackboard();
    }
}
