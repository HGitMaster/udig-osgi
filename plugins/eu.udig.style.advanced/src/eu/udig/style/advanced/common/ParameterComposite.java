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
package eu.udig.style.advanced.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import eu.udig.style.advanced.common.IStyleChangesListener.STYLEEVENTTYPE;
import eu.udig.style.advanced.utils.Utilities;

/**
 * Composite blueprint for parameters interaction widget.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ParameterComposite extends SelectionAdapter implements KeyListener, FocusListener {

    protected List<IStyleChangesListener> listeners = new ArrayList<IStyleChangesListener>();

    public ParameterComposite() {
        super();
    }

    public Composite getComposite() {
        return null;
    }

    protected int getAttributeIndex( String str, String[] attributesArray ) {
        int index = -1;
        if (str != null) {
            for( int i = 0; i < attributesArray.length; i++ ) {
                if (str.equals(attributesArray[i])) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    protected Double isDouble( String value ) {
        return Utilities.isNumber(value, Double.class);
    }

    /**
     * Check if the supplied combo is placed on the NONE value.
     * 
     * @param combo the comnbo to check.
     * @return true if the combo is placed on NONE.
     */
    protected boolean comboIsNone( Combo combo ) {
        int index = combo.getSelectionIndex();
        try {
            String value = combo.getItem(index);
            if (value.equals(Utilities.NONE)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            combo.select(0);
            return false;
        }
    }

    @SuppressWarnings("nls")
    protected String[] getOffset( String text ) {
        String[] defaultOffset = new String[]{"0.0", "0.0"};
        if (text.indexOf(',') == -1) {
            return defaultOffset;
        }
        String[] split = text.split(",");
        if (split.length != 2) {
            return defaultOffset;
        }
        try {
            double xOffset = Double.parseDouble(split[0].trim());
            double yOffset = Double.parseDouble(split[1].trim());
            return new String[]{String.valueOf(xOffset), String.valueOf(yOffset)};
        } catch (Exception ex) {
            return defaultOffset;
        }
    }

    public void addListener( IStyleChangesListener listener ) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener( IStyleChangesListener listener ) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void notifyListeners( String[] values, boolean fromField, STYLEEVENTTYPE styleEventType ) {
        for( IStyleChangesListener listener : listeners ) {
            listener.onStyleChanged(this, values, fromField, styleEventType);
        }
    }

    public void notifyListeners( String value, boolean fromField, STYLEEVENTTYPE styleEventType ) {
        notifyListeners(new String[]{value}, fromField, styleEventType);
    }

    public void focusGained( FocusEvent e ) {
    }

    public void focusLost( FocusEvent e ) {
    }

    public void keyPressed( KeyEvent e ) {
    }

    public void keyReleased( KeyEvent e ) {
    }

}