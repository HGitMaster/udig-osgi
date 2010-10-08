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
package eu.udig.style.advanced.lines.widgets;

import static eu.udig.style.advanced.utils.Utilities.ff;
import static eu.udig.style.advanced.utils.Utilities.sf;

import java.awt.Color;

import net.refractions.udig.style.sld.SLD;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.geotools.styling.TextSymbolizer;
import org.opengis.filter.expression.Expression;

import eu.udig.style.advanced.common.ParameterComposite;
import eu.udig.style.advanced.common.IStyleChangesListener.STYLEEVENTTYPE;
import eu.udig.style.advanced.common.styleattributeclasses.RuleWrapper;
import eu.udig.style.advanced.common.styleattributeclasses.TextSymbolizerWrapper;
import eu.udig.style.advanced.utils.FontEditor;
import eu.udig.style.advanced.utils.StolenColorEditor;
import eu.udig.style.advanced.utils.Utilities;
import eu.udig.style.advanced.utils.VendorOptions;

/**
 * A composite that holds widgets for labels parameter setting.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class LineLabelsParametersComposite extends ParameterComposite {

    private final Composite parent;
    private final String[] numericAttributesArrays;

    private Composite mainComposite;
    private Button labelEnableButton;
    private Spinner labelOpacitySpinner;
    private Combo labelOpacityAttributecombo;
    private Button haloColorButton;
    private StolenColorEditor haloColorEditor;
    private Spinner haloRadiusSpinner;
    private Text initialGapText;
    private Text maxDisplacementText;
    private Text repeatText;
    private Text autoWrapText;
    private Text spaceAroundText;
    private FontEditor fontEditor;
    private Button fontButton;
    private StolenColorEditor fontColorEditor;
    private Button fontColorButton;
    private Text perpendicularOffsetText;
    private Text followLineText;
    private Text maxAngleDeltaText;
    private Text labelNameText;
    private Combo labelNameAttributecombo;
    private String[] allAttributesArrays;

    public LineLabelsParametersComposite( Composite parent, String[] numericAttributesArrays, String[] allAttributesArrays ) {
        this.parent = parent;
        this.numericAttributesArrays = numericAttributesArrays;
        this.allAttributesArrays = allAttributesArrays;
    }

    public Composite getComposite() {
        return mainComposite;
    }

    /**
     * Initialize the composite with values from a rule.
     * 
     * @param ruleWrapper the rule to take the info from.
     */
    public void init( RuleWrapper ruleWrapper ) {
        TextSymbolizerWrapper textSymbolizerWrapper = ruleWrapper.getTextSymbolizersWrapper();
        boolean widgetEnabled = true;
        if (textSymbolizerWrapper == null) {
            widgetEnabled = false;
            /*
             * create a dummy local one to create the widgets
             */
            TextSymbolizer newSymbolizer = Utilities.createDefaultTextSymbolizer(SLD.LINE);
            textSymbolizerWrapper = new TextSymbolizerWrapper(newSymbolizer, null, SLD.LINE);
        }

        mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(3, true));

        labelEnableButton = new Button(mainComposite, SWT.CHECK);
        GridData labelEnableButtonGD = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        labelEnableButtonGD.horizontalSpan = 3;
        labelEnableButton.setLayoutData(labelEnableButtonGD);
        labelEnableButton.setText("enable/disable labelling");
        labelEnableButton.setSelection(widgetEnabled);
        labelEnableButton.addSelectionListener(this);

        // label name
        Label labelNameLabel = new Label(mainComposite, SWT.NONE);
        labelNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        labelNameLabel.setText("label");

        labelNameText = new Text(mainComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData labelNameTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        labelNameText.setLayoutData(labelNameTextGD);
        labelNameText.addFocusListener(this);
        labelNameAttributecombo = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData labelNameAttributecomboGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        labelNameAttributecombo.setLayoutData(labelNameAttributecomboGD);
        labelNameAttributecombo.setItems(allAttributesArrays);
        labelNameAttributecombo.addSelectionListener(this);
        String labelName = textSymbolizerWrapper.getLabelName();
        if (labelName != null) {
            int index = getAttributeIndex(labelName, allAttributesArrays);
            if (index != -1) {
                labelNameAttributecombo.select(index);
            } else {
                labelNameText.setText(labelName);
            }
        } else {
            labelNameText.setText("");
        }

        // font
        Label fontLabel = new Label(mainComposite, SWT.NONE);
        fontLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fontLabel.setText("font");

        fontEditor = new FontEditor(mainComposite);
        GridData fontButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        fontButtonGD.horizontalSpan = 2;
        fontButton = fontEditor.getButton();
        fontButton.setLayoutData(fontButtonGD);
        fontEditor.setListener(this);

        FontData[] fontData = textSymbolizerWrapper.getFontData();
        if (fontData != null) {
            fontEditor.setFontList(fontData);
        }

        // font color
        Label fontColorLabel = new Label(mainComposite, SWT.NONE);
        fontColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fontColorLabel.setText("font color");

        fontColorEditor = new StolenColorEditor(mainComposite, this);
        fontColorButton = fontColorEditor.getButton();
        GridData fontColorButtonGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        fontColorButtonGD.horizontalSpan = 2;
        fontColorButton.setLayoutData(fontColorButtonGD);
        Color tmpColor = null;;
        try {
            tmpColor = Color.decode(textSymbolizerWrapper.getColor());
        } catch (Exception e) {
            tmpColor = Color.black;
        }
        fontColorEditor.setColor(tmpColor);

        // label alpha
        Label labelOpactityLabel = new Label(mainComposite, SWT.NONE);
        labelOpactityLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        labelOpactityLabel.setText("opacity");
        labelOpacitySpinner = new Spinner(mainComposite, SWT.BORDER);
        labelOpacitySpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        labelOpacitySpinner.setMaximum(100);
        labelOpacitySpinner.setMinimum(0);
        labelOpacitySpinner.setIncrement(10);
        String opacity = textSymbolizerWrapper.getOpacity();
        Double tmpOpacity = isDouble(opacity);
        int tmp = 100;
        if (tmpOpacity != null) {
            tmp = (int) (tmpOpacity.doubleValue() * 100);
        }
        labelOpacitySpinner.setSelection(tmp);
        labelOpacitySpinner.addSelectionListener(this);
        labelOpacityAttributecombo = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        labelOpacityAttributecombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        labelOpacityAttributecombo.setItems(numericAttributesArrays);
        labelOpacityAttributecombo.addSelectionListener(this);
        if (tmpOpacity == null) {
            int index = getAttributeIndex(opacity, numericAttributesArrays);
            if (index != -1) {
                labelOpacityAttributecombo.select(index);
            }
        }

        // label halo
        Label haloLabel = new Label(mainComposite, SWT.NONE);
        haloLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        haloLabel.setText("halo");

        haloColorEditor = new StolenColorEditor(mainComposite, this);
        haloColorButton = haloColorEditor.getButton();
        GridData haloColorButtonGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        haloColorButton.setLayoutData(haloColorButtonGD);
        tmpColor = null;;
        try {
            tmpColor = Color.decode(textSymbolizerWrapper.getHaloColor());
        } catch (Exception e) {
            tmpColor = Color.black;
        }
        haloColorEditor.setColor(tmpColor);

        haloRadiusSpinner = new Spinner(mainComposite, SWT.BORDER);
        haloRadiusSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        haloRadiusSpinner.setMaximum(20);
        haloRadiusSpinner.setMinimum(0);
        haloRadiusSpinner.setIncrement(1);
        String haloRadius = textSymbolizerWrapper.getHaloRadius();
        Double tmpRadius = isDouble(haloRadius);
        tmp = 0;
        if (tmpRadius != null) {
            tmp = tmpRadius.intValue();
        }
        haloRadiusSpinner.setSelection(tmp);
        haloRadiusSpinner.addSelectionListener(this);

        // perpend offset
        Label perpendicularOffsetLabel = new Label(mainComposite, SWT.NONE);
        perpendicularOffsetLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        perpendicularOffsetLabel.setText("perpendicular offset");

        perpendicularOffsetText = new Text(mainComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData perpendicularOffsetTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        perpendicularOffsetTextGD.horizontalSpan = 2;
        perpendicularOffsetText.setLayoutData(perpendicularOffsetTextGD);
        perpendicularOffsetText.addFocusListener(this);
        String perpendicularOffset = textSymbolizerWrapper.getPerpendicularOffset();
        if (perpendicularOffset != null) {
            perpendicularOffsetText.setText(perpendicularOffset);
        } else {
            perpendicularOffsetText.setText("");
        }

        // initial gap
        Label initialGapLabel = new Label(mainComposite, SWT.NONE);
        initialGapLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        initialGapLabel.setText("initial gap");

        initialGapText = new Text(mainComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData initialGapTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        initialGapTextGD.horizontalSpan = 2;
        initialGapText.setLayoutData(initialGapTextGD);
        initialGapText.addFocusListener(this);
        String initialGap = textSymbolizerWrapper.getInitialGap();
        if (initialGap != null) {
            initialGapText.setText(initialGap);
        } else {
            initialGapText.setText("");
        }

        Group vendorOptionsGroup = new Group(mainComposite, SWT.SHADOW_ETCHED_IN);
        GridData vendorOptionsGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        vendorOptionsGD.horizontalSpan = 3;
        vendorOptionsGroup.setLayoutData(vendorOptionsGD);
        vendorOptionsGroup.setLayout(new GridLayout(2, false));
        vendorOptionsGroup.setText("Vendor Options");

        // max displacement
        Label maxDisplacementLabel = new Label(vendorOptionsGroup, SWT.NONE);
        maxDisplacementLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        maxDisplacementLabel.setText(VendorOptions.VENDOROPTION_MAXDISPLACEMENT.toGuiString());
        maxDisplacementText = new Text(vendorOptionsGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData maxDisplacementTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        maxDisplacementText.setLayoutData(maxDisplacementTextGD);
        maxDisplacementText.addFocusListener(this);
        String maxDisplacementVO = textSymbolizerWrapper.getMaxDisplacementVO();
        if (maxDisplacementVO != null) {
            maxDisplacementText.setText(maxDisplacementVO);
        } else {
            maxDisplacementText.setText("");
        }

        // repeat
        Label repeatLabel = new Label(vendorOptionsGroup, SWT.NONE);
        repeatLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        repeatLabel.setText(VendorOptions.VENDOROPTION_REPEAT.toGuiString());
        repeatText = new Text(vendorOptionsGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData repeatTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        repeatText.setLayoutData(repeatTextGD);
        repeatText.addFocusListener(this);
        String repeatVO = textSymbolizerWrapper.getRepeatVO();
        if (repeatVO != null) {
            repeatText.setText(repeatVO);
        } else {
            repeatText.setText("");
        }

        // autoWrap
        Label autoWrapLabel = new Label(vendorOptionsGroup, SWT.NONE);
        autoWrapLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        autoWrapLabel.setText(VendorOptions.VENDOROPTION_AUTOWRAP.toGuiString());
        autoWrapText = new Text(vendorOptionsGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData autoWrapTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        autoWrapText.setLayoutData(autoWrapTextGD);
        autoWrapText.addFocusListener(this);
        String autoWrapVO = textSymbolizerWrapper.getAutoWrapVO();
        if (autoWrapVO != null) {
            autoWrapText.setText(autoWrapVO);
        } else {
            autoWrapText.setText("");
        }

        // spaceAround
        Label spaceAroundLabel = new Label(vendorOptionsGroup, SWT.NONE);
        spaceAroundLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        spaceAroundLabel.setText(VendorOptions.VENDOROPTION_SPACEAROUND.toGuiString());
        spaceAroundText = new Text(vendorOptionsGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData spaceAroundTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        spaceAroundText.setLayoutData(spaceAroundTextGD);
        spaceAroundText.addFocusListener(this);
        String spaceAroundVO = textSymbolizerWrapper.getSpaceAroundVO();
        if (spaceAroundVO != null) {
            spaceAroundText.setText(spaceAroundVO);
        } else {
            spaceAroundText.setText("");
        }

        // flollowLine
        Label flollowLineLabel = new Label(vendorOptionsGroup, SWT.NONE);
        flollowLineLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        flollowLineLabel.setText(VendorOptions.VENDOROPTION_FOLLOWLINE.toGuiString());
        followLineText = new Text(vendorOptionsGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData flollowLineTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        followLineText.setLayoutData(flollowLineTextGD);
        followLineText.addFocusListener(this);
        String flollowLineVO = textSymbolizerWrapper.getFollowLineVO();
        if (flollowLineVO != null) {
            followLineText.setText(flollowLineVO);
        } else {
            followLineText.setText("");
        }

        // maxAngleDelta
        Label maxAngleDeltaLabel = new Label(vendorOptionsGroup, SWT.NONE);
        maxAngleDeltaLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        maxAngleDeltaLabel.setText(VendorOptions.VENDOROPTION_MAXANGLEDELTA.toGuiString());
        maxAngleDeltaText = new Text(vendorOptionsGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData maxAngleDeltaTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        maxAngleDeltaText.setLayoutData(maxAngleDeltaTextGD);
        maxAngleDeltaText.addFocusListener(this);
        String maxAngleDeltaVO = textSymbolizerWrapper.getMaxAngleDeltaVO();
        if (maxAngleDeltaVO != null) {
            maxAngleDeltaText.setText(maxAngleDeltaVO);
        } else {
            maxAngleDeltaText.setText("");
        }
    }

    /**
     * Initialize the composite with values from a rule.
     * 
     * @param ruleWrapper the rule to take the info from.
     */
    public void update( RuleWrapper ruleWrapper ) {
        TextSymbolizerWrapper textSymbolizerWrapper = ruleWrapper.getTextSymbolizersWrapper();
        if (textSymbolizerWrapper == null) {
            labelEnableButton.setSelection(false);
            return;
        } else {
            labelEnableButton.setSelection(true);
        }

        String labelName = textSymbolizerWrapper.getLabelName();
        if (labelName != null) {
            int index = getAttributeIndex(labelName, allAttributesArrays);
            if (index != -1) {
                labelNameAttributecombo.select(index);
            } else {
                labelNameText.setText(labelName);
            }
        } else {
            labelNameText.setText("");
        }

        FontData[] fontData = textSymbolizerWrapper.getFontData();
        if (fontData != null) {
            fontEditor.setFontList(fontData);
        }

        String color = textSymbolizerWrapper.getColor();
        if (color != null) {
            fontColorEditor.setColor(Color.decode(color));
        }

        String opacity = textSymbolizerWrapper.getOpacity();
        Double tmpOpacity = isDouble(opacity);
        int tmp = 100;
        if (tmpOpacity != null) {
            tmp = (int) (tmpOpacity.doubleValue() * 100);
        }
        labelOpacitySpinner.setSelection(tmp);
        if (tmpOpacity == null) {
            int index = getAttributeIndex(opacity, numericAttributesArrays);
            if (index != -1) {
                labelOpacityAttributecombo.select(index);
            }
        }

        Color tmpColor = null;;
        try {
            tmpColor = Color.decode(textSymbolizerWrapper.getHaloColor());
        } catch (Exception e) {
            tmpColor = Color.black;
        }
        haloColorEditor.setColor(tmpColor);

        String haloRadius = textSymbolizerWrapper.getHaloRadius();
        Double tmpRadius = isDouble(haloRadius);
        tmp = 0;
        if (tmpRadius != null) {
            tmp = tmpRadius.intValue();
        }
        haloRadiusSpinner.setSelection(tmp);

        // perpend offset
        String perpendicularOffset = textSymbolizerWrapper.getPerpendicularOffset();
        if (perpendicularOffset != null) {
            perpendicularOffsetText.setText(perpendicularOffset);
        } else {
            perpendicularOffsetText.setText("");
        }

        // initial gap
        String initialGap = textSymbolizerWrapper.getInitialGap();
        if (initialGap != null) {
            initialGapText.setText(initialGap);
        } else {
            initialGapText.setText("");
        }

        // max displacement
        String maxDisplacementVO = textSymbolizerWrapper.getMaxDisplacementVO();
        if (maxDisplacementVO != null) {
            maxDisplacementText.setText(maxDisplacementVO);
        } else {
            maxDisplacementText.setText("");
        }

        // repeat
        String repeatVO = textSymbolizerWrapper.getRepeatVO();
        if (repeatVO != null) {
            repeatText.setText(repeatVO);
        } else {
            repeatText.setText("");
        }

        // autoWrap
        String autoWrapVO = textSymbolizerWrapper.getAutoWrapVO();
        if (autoWrapVO != null) {
            autoWrapText.setText(autoWrapVO);
        } else {
            autoWrapText.setText("");
        }

        // spaceAround
        String spaceAroundVO = textSymbolizerWrapper.getSpaceAroundVO();
        if (spaceAroundVO != null) {
            spaceAroundText.setText(spaceAroundVO);
        } else {
            spaceAroundText.setText("");
        }

        // followline
        String flollowLineVO = textSymbolizerWrapper.getFollowLineVO();
        if (flollowLineVO != null) {
            followLineText.setText(flollowLineVO);
        } else {
            followLineText.setText("false");
        }

        // maxAngleDelta
        String maxAngleDeltaVO = textSymbolizerWrapper.getMaxAngleDeltaVO();
        if (maxAngleDeltaVO != null) {
            maxAngleDeltaText.setText(maxAngleDeltaVO);
        } else {
            maxAngleDeltaText.setText("");
        }

    }

    public void widgetSelected( SelectionEvent e ) {
        Object source = e.getSource();
        if (source.equals(labelEnableButton)) {
            boolean selected = labelEnableButton.getSelection();
            notifyListeners(String.valueOf(selected), false, STYLEEVENTTYPE.LABELENABLE);
        } else if (source.equals(labelNameAttributecombo)) {
            int index = labelNameAttributecombo.getSelectionIndex();
            String nameField = labelNameAttributecombo.getItem(index);
            notifyListeners(nameField, true, STYLEEVENTTYPE.LABEL);
        } else if (source.equals(fontButton)) {
            FontData[] fontData = fontEditor.getFontList();
            if (fontData.length > 0) {
                FontData fd = fontData[0];
                String name = fd.getName();
                String style = String.valueOf(fd.getStyle());
                String height = String.valueOf(fd.getHeight());
                Color color = fontEditor.getAWTColor();
                Expression colorExpr = ff.literal(color);
                String fontColor = colorExpr.evaluate(null, String.class);

                notifyListeners(new String[]{name, style, height, fontColor}, false, STYLEEVENTTYPE.LABELFONT);
            }
        } else if (source.equals(fontColorButton)) {
            Color color = fontColorEditor.getColor();
            Expression colorExpr = ff.literal(color);
            String fontColor = colorExpr.evaluate(null, String.class);

            notifyListeners(fontColor, false, STYLEEVENTTYPE.LABELCOLOR);
        } else if (source.equals(haloColorButton)) {
            Color color = haloColorEditor.getColor();
            Expression colorExpr = ff.literal(color);
            String haloColor = colorExpr.evaluate(null, String.class);

            notifyListeners(haloColor, false, STYLEEVENTTYPE.LABELHALOCOLOR);
        } else if (source.equals(haloRadiusSpinner)) {
            int radius = haloRadiusSpinner.getSelection();

            notifyListeners(String.valueOf(radius), false, STYLEEVENTTYPE.LABELHALORADIUS);
        } else if (source.equals(labelOpacitySpinner)) {
            int opacity = labelOpacitySpinner.getSelection();
            String opacityStr = String.valueOf(opacity);

            notifyListeners(opacityStr, false, STYLEEVENTTYPE.LABELOPACITY);
        } else if (source.equals(labelOpacityAttributecombo)) {
            int index = labelOpacityAttributecombo.getSelectionIndex();
            String opacityField = labelOpacityAttributecombo.getItem(index);

            notifyListeners(opacityField, true, STYLEEVENTTYPE.LABELOPACITY);
        }
    }

    public void focusGained( FocusEvent e ) {
    }

    public void focusLost( FocusEvent e ) {
        Object source = e.getSource();
        if (source.equals(initialGapText)) {
            String text = initialGapText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELINITIALGAP);
        } else if (source.equals(perpendicularOffsetText)) {
            String text = perpendicularOffsetText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELPERPENDICULAROFFSET);
        } else if (source.equals(maxDisplacementText)) {
            String text = maxDisplacementText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELMAXDISPLACEMENT_VO);
        } else if (source.equals(spaceAroundText)) {
            String text = spaceAroundText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELSPACEAROUND_VO);
        } else if (source.equals(autoWrapText)) {
            String text = autoWrapText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELAUTOWRAP_VO);
        } else if (source.equals(repeatText)) {
            String text = repeatText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELREPEAT_VO);
        } else if (source.equals(followLineText)) {
            String text = followLineText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELFOLLOWLINE_VO);
        } else if (source.equals(maxAngleDeltaText)) {
            String text = maxAngleDeltaText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABELMAXANGLEDELTA_VO);
        } else if (source.equals(labelNameText)) {
            String text = labelNameText.getText();
            notifyListeners(text, false, STYLEEVENTTYPE.LABEL);
        }
    }
}
