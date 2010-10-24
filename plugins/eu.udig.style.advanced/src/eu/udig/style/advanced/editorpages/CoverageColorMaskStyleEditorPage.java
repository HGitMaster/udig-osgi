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
package eu.udig.style.advanced.editorpages;

import java.awt.Color;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.internal.StyleBlackboard;
import net.refractions.udig.style.sld.editor.StyleEditorPage;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.geotools.coverage.grid.GridCoverage2D;

import eu.udig.style.advanced.utils.StolenColorEditor;

/**
 * The style editor for single banded {@link GridCoverage2D coverages};
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class CoverageColorMaskStyleEditorPage extends StyleEditorPage implements SelectionListener {

    public static String COVERAGE_COLORMASK_ID = "raster-color-mask"; //$NON-NLS-1$

    private Button colorMaskButton;
    private boolean doColorMask;
    private StolenColorEditor maskColorEditor;

    public CoverageColorMaskStyleEditorPage() {
        super();
        setSize(new Point(500, 450));
    }

    public void createPageContent( Composite parent ) {
        IBlackboard styleBlackboard = getSelectedLayer().getStyleBlackboard();
        String maskColorString = styleBlackboard.getString(COVERAGE_COLORMASK_ID);


        Group colorMaskGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        colorMaskGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        colorMaskGroup.setLayout(new GridLayout(2, false));
        colorMaskGroup.setText("Color mask");

        colorMaskButton = new Button(colorMaskGroup, SWT.CHECK);
        colorMaskButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        colorMaskButton.setText("Enable color mask");
        colorMaskButton.addSelectionListener(this);

        maskColorEditor = new StolenColorEditor(colorMaskGroup);
        if (maskColorString instanceof String) {
            String[] colorSplit = maskColorString.split(":"); //$NON-NLS-1$
            Color color = new Color(Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]),
                    Integer.parseInt(colorSplit[2]));
            maskColorEditor.setColor(color);
            colorMaskButton.setSelection(true);
        }
    }

    public void widgetSelected( SelectionEvent e ) {
        Object source = e.getSource();
        if (source.equals(colorMaskButton)) {
            doColorMask = colorMaskButton.getSelection();
        }
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
    }

    public String getErrorMessage() {
        return null;
    }

    public String getLabel() {
        return null;
    }

    public void gotFocus() {
    }

    public boolean performCancel() {
        return false;
    }

    public boolean okToLeave() {
        return true;
    }

    public boolean performApply() {
        return applyCurrentStyle();
    }

    public boolean performOk() {
        return applyCurrentStyle();
    }

    private boolean applyCurrentStyle() {

        StyleBlackboard styleBlackboard = getSelectedLayer().getStyleBlackboard();

        if (colorMaskButton.getSelection()) {
            Color maskColor = maskColorEditor.getColor();
            String colorStr = maskColor.getRed() + ":" + maskColor.getGreen() + ":" + maskColor.getBlue(); //$NON-NLS-1$ //$NON-NLS-2$
            styleBlackboard.putString(COVERAGE_COLORMASK_ID, colorStr);
        } else {
            styleBlackboard.remove(COVERAGE_COLORMASK_ID);
        }
        return true;
    }

    public void refresh() {
    }

    public void dispose() {
        super.dispose();
    }

    public void styleChanged( Object source ) {

    }

}
