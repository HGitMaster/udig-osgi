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
package net.refractions.udig.tutorials.examples;

import java.io.IOException;
import java.util.Iterator;

import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.collection.AdaptorFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * This example copies features from one layer to a second layer. 
 * The second layer's FeatureType is different from the first layer so 
 * some conversion must be done during the copy.
 * <p>
 * I am assuming that I know both the source and destination feature types.  The source 
 * is geom:Geometry, name:String. The destination is geom:Geometry, name:String, ID:int 
 * </p>
 * @author Jesse
 */
public class ConvertFeatureToNewSchema {

	public void doConversion( IMap map, Filter filter, IProgressMonitor monitor ) throws IOException{
		monitor.beginTask("Copy Features", 30);
		FeatureSource<SimpleFeatureType, SimpleFeature> source=map.getMapLayers().get(0).getResource(FeatureSource.class, new SubProgressMonitor(monitor, 1));
		final FeatureStore<SimpleFeatureType, SimpleFeature> dest=map.getMapLayers().get(1).getResource(FeatureStore.class, new SubProgressMonitor(monitor, 1));
		
		final FeatureCollection<SimpleFeatureType, SimpleFeature>  features = source.getFeatures(filter);
		
		
		// we add a custom feature collection so that we can convert the feature fromt the source feature type to the destination one
		// A slightly faster implementation is to not create a new Feature each time in the iterator but wrap the 
		// source feature in a decorator that adapts the feature to the new Feature type.
		// It is a more complicated example and you can see it in action in net.refractions.udig.catalog.ui.export.FeatureWrapper
		dest.addFeatures(new AdaptorFeatureCollection("converting",dest.getSchema()){
			
			@Override
			protected void closeIterator(Iterator arg0) {
				((ConvertingIterator)arg0).iter.close();
			}

			@Override
			protected Iterator openIterator() {
				return new ConvertingIterator(features.features(), dest.getSchema());
			}

			@Override
			public int size() {
				return features.size();
			}
			
		});
	}
	
	private static class ConvertingIterator implements Iterator<SimpleFeature>{

		private FeatureIterator<SimpleFeature> iter;
		private SimpleFeatureType type;
		private static int index=0;

		public ConvertingIterator(FeatureIterator<SimpleFeature> iterator, SimpleFeatureType newSchema) {
			this.iter=iterator;
			this.type=newSchema;
		}
		
		public boolean hasNext() {
			return iter.hasNext();
		}

		public SimpleFeature next() {
			SimpleFeature oldF=iter.next();
			Object[] newAttributes=new Object[]{
					oldF.getAttribute("geom"), 
					oldF.getAttribute("name"),
					index++
			};
			try {
				SimpleFeature newF=SimpleFeatureBuilder.build(type, newAttributes, null);
				return newF;
			} catch (IllegalAttributeException e) {
				throw new RuntimeException(e);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
