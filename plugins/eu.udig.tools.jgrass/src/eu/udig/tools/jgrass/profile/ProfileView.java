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
package eu.udig.tools.jgrass.profile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

/**
 * The view that shows the coverage profiles created by the {@link ProfileTool}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ProfileView extends ViewPart {

    public static final String ID = "eu.udig.tools.jgrass.profileview";

    private double max = Double.NEGATIVE_INFINITY;
    private double min = Double.POSITIVE_INFINITY;


    public ProfileView() {
    }

    public void createPartControl( Composite parent ) {
    }

    public void setFocus() {
    }

    public void addToSeries( final double x, final double y ) {
    }

    public void setRangeToDataBounds() {
    }

    public void clearSeries() {
    }

    public boolean seriesIsEmpty() {
        return true;
    }

    public void addStopLine( double x ) {
    }

    public void clearMarkers() {
        
    }
        
}
