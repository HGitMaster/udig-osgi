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
package eu.udig.catalog.jgrass.operations;

import java.net.URL;

import net.refractions.udig.catalog.URLUtils;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

import eu.udig.catalog.jgrass.core.JGrassService;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * Action to add a new mapset to a location.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class AddMapsetAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IWorkbenchWindowPulldownDelegate {

    IStructuredSelection selection = null;

    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

    public void run( IAction action ) {

        Display.getDefault().syncExec(new Runnable(){
            public void run() {

                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof JGrassService) {
                    final JGrassService jgrassService = (JGrassService) firstElement;
                    URL identifier = jgrassService.getIdentifier();
                    String locationPath = URLUtils.urlToFile(identifier).getAbsolutePath();
                    InputDialog iDialog = new InputDialog(Display.getDefault().getActiveShell(), "New mapset name",
                            "Please enter the name for the new mapset to create.", "newmapset", null);
                    iDialog.open();
                    String mapsetName = iDialog.getValue();
                    if (mapsetName.indexOf(' ') != -1) {
                        MessageBox msgBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR);
                        msgBox.setMessage("Mapset names can't contain spaces. Please choose a name without spaces.");
                        msgBox.open();
                        mapsetName = null;
                        return;
                    }

                    if (mapsetName != null && mapsetName.length() > 0) {
                        JGrassCatalogUtilities.createMapset(locationPath, mapsetName, null, null);
                        JGrassCatalogUtilities.addMapsetToCatalog(locationPath, mapsetName);
                    }
                }

            }
        });

    }

    /**
    * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
    *      org.eclipse.jface.viewers.ISelection)
    */
    public void selectionChanged( IAction action, ISelection selection ) {

        if (selection instanceof IStructuredSelection)
            this.selection = (IStructuredSelection) selection;
    }

    /*
    * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
    */
    public void dispose() {
    }

    /*
    * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
    */
    public void init( IWorkbenchWindow window ) {
        // do nothing
    }

    /*
    * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
    */
    public Menu getMenu( Control parent ) {
        return null;
    }

}