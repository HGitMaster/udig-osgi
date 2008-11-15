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
package net.crischan.udig.arcgrid;

import java.io.IOException;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResourceInfo;

import org.eclipse.core.runtime.IStatus;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class ArcGridGeoResourceInfo extends IGeoResourceInfo {		
		/** ArcGridGeoResourceInfo resource field */
        private final ArcGridGeoResourceImplementation resource;

        ArcGridGeoResourceInfo(ArcGridGeoResourceImplementation arcGridGeoResourceImplementation) {
			resource = arcGridGeoResourceImplementation;
            this.keywords = new String[] {
				"ASCIIGrid", //$NON-NLS-1$
				".asc", //$NON-NLS-1$
				".grd" //$NON-NLS-1$
			};
			this.title = resource.getIdentifier().getFile();
			this.description = resource.getIdentifier().toString();
			this.bounds = getBounds();
		}
		
		public ReferencedEnvelope getBounds() {
			if (this.bounds == null) {
				Envelope env = null;
				try {
					GridCoverage source = (GridCoverage) resource.findResource();
					if (source == null) {
					    Exception eek = new IOException("Bounds of ArcGrid not available"); //$NON-NLS-1$
					    resource.getService().setStatusMessage(eek);					    
					    return null;
					}
					org.opengis.geometry.Envelope ptBounds = source.getEnvelope();
					env = new Envelope(
							ptBounds.getMinimum(0),
							ptBounds.getMaximum(0),
                            ptBounds.getMinimum(1),
                            ptBounds.getMaximum(1));
					
					CoordinateReferenceSystem crs = source.getCoordinateReferenceSystem();

					CoordinateReferenceSystem geomcrs = 
                        source.getCoordinateReferenceSystem();
                    if(geomcrs == null) {
                        geomcrs=DefaultEngineeringCRS.GENERIC_2D;
                    }

					this.bounds = new ReferencedEnvelope(env, crs);
					
				} catch (Exception e) {
					System.err.println("source = exception");
					CatalogPlugin.getDefault().getLog().log(new org.eclipse.core.runtime.Status(IStatus.WARNING, 
                            "net.refractions.udig.catalog", 0, "Error while getting the bounds of a layer", e ));
				}
			}
			return this.bounds;
		}
	}