/**
 * 
 */
package net.refractions.udig.catalog.mif.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResourceInfo;

import org.eclipse.core.runtime.IStatus;
import org.geotools.data.FeatureSource;
import org.geotools.data.mif.MIFDataStore;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

class MifGeoResourceInfo extends IGeoResourceInfo {
	private final MifGeoResourceImpl mifResource;
	private SimpleFeatureType featureType = null;
    MifGeoResourceInfo(MifGeoResourceImpl mifGeoResourceImpl) throws IOException{
        mifResource = mifGeoResourceImpl;
		MIFDataStore dataStore = mifResource.parent.getDS(null);
		String typeName = dataStore.getTypeNames()[0];
		featureType = dataStore.getSchema(typeName);
            try {
                FeatureSource<SimpleFeatureType, SimpleFeature> source =dataStore.getFeatureSource(typeName);
                Envelope tmpBounds=source.getBounds();
                if( tmpBounds instanceof ReferencedEnvelope)
                	bounds=(ReferencedEnvelope) tmpBounds;
                else
                	bounds=new ReferencedEnvelope(tmpBounds, getCRS());
                if( bounds==null ){
                    bounds=new ReferencedEnvelope(new Envelope(), getCRS());
                    FeatureIterator<SimpleFeature> iter=source.getFeatures().features();
                    try{
                        while(iter.hasNext() ) {
                            SimpleFeature element = iter.next();
                            if( bounds.isNull() )
                                bounds.init(element.getBounds());
                            else
                                bounds.include(element.getBounds());
                        }
                    }finally{
                        iter.close();
                    }
                }
            } catch (Exception e) {
                CatalogPlugin.getDefault().getLog().log(new org.eclipse.core.runtime.Status(IStatus.WARNING, 
                       "net.refractions.udig.catalog", 0, Messages.ShpGeoResourceImpl_error_layer_bounds, e ));   //$NON-NLS-1$
                bounds = new ReferencedEnvelope(new Envelope(), getCRS());
            }

            keywords = new String[]{
                ".shp","Shapefile", //$NON-NLS-1$ //$NON-NLS-2$
                featureType.getName().getLocalPart(),
                featureType.getName().getNamespaceURI()
            };
    }
        
    public CoordinateReferenceSystem getCRS() {
        return featureType.getCoordinateReferenceSystem();
    }    

    public String getName() {
        return featureType.getName().getLocalPart();
    }

    public URI getSchema() {
    	try {
			return new URI( featureType.getName().getNamespaceURI());
		} catch (URISyntaxException e) {
			return null;
		}
    }

    public String getTitle() {
        return featureType.getName().getLocalPart();
    }
    
    /**
     * Description of shapefile contents.
     * 
     * @return description of Shapefile Contents
     */
    public SimpleFeatureType getFeatureType(){
    	return featureType;
    }
}