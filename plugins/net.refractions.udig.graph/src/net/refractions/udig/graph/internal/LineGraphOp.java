package net.refractions.udig.graph.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.util.GeoToolsAdapters;
import net.refractions.udig.core.internal.CorePlugin;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.SetLayerVisibilityCommand;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.MultiLineString;

public class LineGraphOp implements IOp {

    public void op( Display display, Object target, IProgressMonitor monitor ) throws Exception {
        if( monitor == null ) monitor = new NullProgressMonitor();
        monitor.beginTask("build graph", 100);
        Layer layer = (Layer) target;
        Filter filter = layer.getFilter();
        if( filter == Filter.EXCLUDE ){
            // nothing selected? let us work with everything then...
            filter = Filter.INCLUDE;
        }
        monitor.subTask("grab features for "+filter);
        
        FeatureSource<SimpleFeatureType,SimpleFeature> source =
                layer.getResource(FeatureSource.class, new SubProgressMonitor(monitor,10));

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( filter );
        
        FeatureType schema = features.getSchema();
        Class<?> binding = schema.getGeometryDescriptor().getType().getBinding();
        Graph graph = null;
        if( MultiLineString.class.isAssignableFrom( binding )){
            graph = buildFromMultiLineString( features, new SubProgressMonitor(monitor, 90) );
        }        
        if( graph == null ){
            // prompt or otherwise anny user?   
            System.out.println("Could not create a graph from the current selection");
            return;
        }
        IBlackboard blackboard = layer.getMap().getBlackboard();
        blackboard.put("graph", graph );
        
        try {
            makeGraphVisible( layer.getMap() );
        }
        catch (IOException notAvailable){            
        }        
    }

    private void makeGraphVisible( IMap map ) throws IOException {
        ILayer graphLayer = null;
        ILayer pathLayer = null;
        
        ID GRAPH_ID = new ID(new URL( null, GraphMapGraphic.ID, CorePlugin.RELAXED_HANDLER ));            
        ID PATH_ID =  new ID(new URL( null, PathMapGraphic.ID, CorePlugin.RELAXED_HANDLER ));            
         
        for( ILayer look : map.getMapLayers() ){
            URL id = look.getGeoResource().getIdentifier();
            if(GRAPH_ID.equals(id) ){
                graphLayer = look;
                break;
            }
        }
        if( graphLayer == null ){            
            ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
            IGeoResource resource = catalog.getById(IGeoResource.class, GRAPH_ID, new NullProgressMonitor() );
            if( resource == null ){
                return; // not available?
            }
            List<IGeoResource> resourceList = Collections.singletonList( resource );            
            List< ? extends ILayer> added = ApplicationGIS.addLayersToMap(map, resourceList, 0 );
            if( added.isEmpty() ){
                return; // not available?
            }
            graphLayer = (ILayer) added.get(0);            
        }
        if( !graphLayer.isVisible() ){
            map.sendCommandASync( new SetLayerVisibilityCommand(graphLayer, true) );
        }
        
        for( ILayer look : map.getMapLayers() ){
            URL id = look.getGeoResource().getIdentifier();
            if(PATH_ID.equals(id) ){
                pathLayer = look;
                break;
            }
        }
        if( pathLayer == null ){            
            ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
            IGeoResource resource = catalog.getById(IGeoResource.class, PATH_ID, new NullProgressMonitor() );
            if( resource == null ){
                return; // not available?
            }
            List<IGeoResource> resourceList = Collections.singletonList( resource );            
            List< ? extends ILayer> added = ApplicationGIS.addLayersToMap(map, resourceList, 0 );
            if( added.isEmpty() ){
                return; // not available?
            }
            pathLayer = (ILayer) added.get(0);            
        }
        if( !pathLayer.isVisible() ){
            map.sendCommandASync( new SetLayerVisibilityCommand(pathLayer, true) );
        }
        pathLayer.refresh(null);
    }

    private Graph buildFromMultiLineString( FeatureCollection<SimpleFeatureType, SimpleFeature> features, IProgressMonitor monitor ) {        
        //create a linear graph generate
        LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();

        //wrap it in a feature graph generator
        final FeatureGraphGenerator featureGen = new FeatureGraphGenerator( lineStringGen );

        //throw all the features into the graph generator
        try {
            features.accepts( new FeatureVisitor(){
                public void visit( Feature feature ) {
                    Graphable added;
                    added = featureGen.add( feature );
                }           
            }, GeoToolsAdapters.progress( monitor ));
            return featureGen.getGraph();
        } catch (IOException e) {
            return null;
        }
    }

}
