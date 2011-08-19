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
package net.refractions.udig.project.internal.interceptor;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IResourceInterceptor;
import net.refractions.udig.project.internal.impl.UDIGFeatureStore;
import net.refractions.udig.project.internal.impl.UDIGSimpleFeatureStore;
import net.refractions.udig.project.internal.impl.UDIGStore;

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * Wraps a FeatureStore in a UDIGFeatureStore (ensuring that the transaction is only set once!).
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class WrapFeatureStore implements IResourceInterceptor<FeatureStore< ? , ? >> {

    public FeatureStore< ? , ? > run( ILayer layer, FeatureStore< ? , ? > resource,
            Class< ? super FeatureStore< ? , ? >> requestedType ) {
        
        if( resource instanceof UDIGStore ){
            return resource;
        }
        
        if (requestedType.isAssignableFrom(SimpleFeatureStore.class) ||
                requestedType.isAssignableFrom(FeatureStore.class) ){
            if( resource instanceof SimpleFeatureStore){
                return new UDIGSimpleFeatureStore(resource, layer);
            }
            else {
                @SuppressWarnings("unchecked")
                FeatureStore<FeatureType,Feature> prep = (FeatureStore<FeatureType,Feature>) resource;
                return new UDIGFeatureStore( prep, layer);
            }
        }
        return resource;
    }
}
