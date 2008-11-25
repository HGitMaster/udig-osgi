package net.refractions.udig.tutorials.shpexport;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.ui.PlatformGIS;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ShpExportOp implements IOp {

public void op(Display display, Object target, IProgressMonitor monitor)
        throws Exception {
	FeatureSource<SimpleFeatureType, SimpleFeature> source =
		(FeatureSource<SimpleFeatureType, SimpleFeature>) target;
	
    SimpleFeatureType featureType = source.getSchema();
    GeometryDescriptor geometryType = featureType.getGeometryDescriptor();
    CoordinateReferenceSystem crs = geometryType.getCoordinateReferenceSystem();    
    
    String typeName = featureType.getTypeName();    
    
    // String filename = promptSaveDialog( typeName )
    String filename = typeName.replace(':', '_');    
    URL directory = FileLocator.toFileURL( Platform.getInstanceLocation().getURL() );
    URL shpURL = new URL(directory.toExternalForm() + filename + ".shp");
    final File file = new File( shpURL.toURI() );    
    
    // promptOverwrite( file )
    if (file.exists()){
        return;
    }
    
    // create and write the new shapefile    
    ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
    Map params = new HashMap();
    params.put( "url", file.toURL() );
    ShapefileDataStore dataStore =
        (ShapefileDataStore) factory.createNewDataStore( params );    
    dataStore.createSchema( featureType );
    
    FeatureStore store = (FeatureStore) dataStore.getFeatureSource();
    store.addFeatures( source.getFeatures() );
    dataStore.forceSchemaCRS( crs );
}
    
private void promptOverwrite(final Display display, final File file){
    if (!file.exists()) return;
    
    display.syncExec(new Runnable() {
        public void run() {
            boolean overwrite = MessageDialog.openConfirm(display
                    .getActiveShell(), "Warning",
                    "File Exists do you wish to overwrite?");
            if (overwrite){
                file.delete();
            }
        }
    });
}
    /**
     * Example of opening a save dialog in the display thread.
     * 
     * @param typeName
     * @return filename provided by the user, or null
     */
    private String promptSaveDialog( final String typeName  ){
        final String filename = typeName.replace(':', '_');
        final String[] result = new String[1];
        
        PlatformGIS.syncInDisplayThread( new Runnable(){
            public void run() {
                Display display = Display.getCurrent();
                FileDialog dialog = new FileDialog( display.getActiveShell(), SWT.SAVE );
                
                dialog.setFileName( filename+".shp");
                dialog.setText("Export "+typeName );
                dialog.setFilterExtensions( new String[]{"shp", "SHP"} );
                result[0] = dialog.open();        
            }
        });

        return result[0];
    }
}
