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
package net.refractions.udig.project.ui.internal.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.core.AdapterUtil;
import net.refractions.udig.project.IEditManager;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.internal.Messages;
import net.refractions.udig.ui.FeatureTypeEditor;
import net.refractions.udig.ui.FeatureTypeEditorDialog;
import net.refractions.udig.ui.ProgressManager;
import net.refractions.udig.ui.FeatureTypeEditorDialog.ValidateFeatureType;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.geotools.data.FeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

/**
 * Allows a SimpleFeatureType to be created and adds the feature type to the current map (or creates a new map if there is no current map)
 * 
 * @author Jesse
 * @since 1.0.0
 */
public class NewLayerAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {
    private IGeoResource resource=null;

    private final ValidateFeatureType performOK=new ValidateFeatureType(){

        public String validate( SimpleFeatureType featureType ) {
            try {
                resource = CatalogPlugin.getDefault().getLocalCatalog().
                    createTemporaryResource(featureType);
                return null;
            } catch (Exception e) {
                return Messages.NewLayerAction_duplicateName;
            }            
        }
        
    };

	private IWorkbenchWindow window;

    @Override
    public void runWithEvent( IAction action, Event event ) {
        // Open a dialog for user to create featuretype
        FeatureTypeEditorDialog dialog = new FeatureTypeEditorDialog(event.display.getActiveShell(), performOK);
        setDefaultGeomType(dialog);

        dialog.setBlockOnOpen(true);
        int code = dialog.open();

        if (code == Window.CANCEL)
            return;

        if (resource != null) {
            IMap map = ApplicationGIS.getActiveMap();
            int index = 0;
            if (map != ApplicationGIS.NO_MAP) {
                index = map.getMapLayers().size();
            }
            ApplicationGIS.addLayersToMap(map, Collections.singletonList(resource), index);
        }

    }

	/**
	 * @param dialog
	 */
	private void setDefaultGeomType(FeatureTypeEditorDialog dialog) {
		if( window.getSelectionService()==null ){
			return;
		}
		
		ISelection selection = window.getSelectionService().getSelection();
		
		GeometryDescriptor geom = findGeometryType(selection);
		
		if( geom==null ){
		    IMap map = ApplicationGIS.getActiveMap();
		    if( map==ApplicationGIS.NO_MAP ){
		        return;
		    }
		    
		    IEditManager editManager = map.getEditManager();
		    if( editManager == null ){
		        return;
		    }
            ILayer selectedLayer = editManager.getSelectedLayer();
            if( selectedLayer == null ){
                return;
            }
            SimpleFeatureType schema = selectedLayer.getSchema();
		    if( schema ==null ){
		        return;
		    }
		    geom = schema.getGeometryDescriptor();		    
		}		
		FeatureTypeEditor editor = dialog.getEditor();
        SimpleFeatureType ft = editor.createDefaultFeatureType();
        SimpleFeatureTypeBuilder builder = editor.builderFromFeatureType(ft);
        String defaultGeometry = ft.getGeometryDescriptor().getLocalName();
        if( defaultGeometry == null ){
            return;
        }
        builder.remove(defaultGeometry);
        builder.add(geom);
        builder.setDefaultGeometry(geom.getLocalName());
        dialog.setDefaultFeatureType(builder.buildFeatureType());
	}

    @SuppressWarnings("unchecked")
	private GeometryDescriptor findGeometryType(ISelection selection) {

        if( selection.isEmpty() ){
            return null;
        }
    	if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			Iterator iter = structured.iterator();
			while(iter.hasNext()){
				Object elem = iter.next();
				try {
					FeatureSource<SimpleFeatureType, SimpleFeature> source = AdapterUtil.instance.adaptTo(FeatureSource.class, elem, ProgressManager.instance().get());
					if( source != null ){
						return source.getSchema().getGeometryDescriptor();
					}
					if (elem instanceof IMap) {
						IMap map = (IMap) elem;
						ILayer layer = map.getEditManager().getSelectedLayer();
						if( layer!=null ){
							source = AdapterUtil.instance.adaptTo(FeatureSource.class, layer, ProgressManager.instance().get());
							if( source != null ){
								return source.getSchema().getGeometryDescriptor();
							}
						}
					}
				} catch (IOException e) {
					// continue trying
				}
			}
		}
    	return null;
	}

	public void init( IWorkbenchWindow window ) {
    	this.window = window;
    }

}
