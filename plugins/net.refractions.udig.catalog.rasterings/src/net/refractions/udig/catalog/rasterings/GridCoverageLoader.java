/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004-2008, Refractions Research Inc.
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
package net.refractions.udig.catalog.rasterings;

import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Flag to indicate that the coverage must be kept in memory.
 * And time should not be spent reading it from disk for rendering and so on.
 * <p>
 * The IGeoResource should resolve to:
 * <ul>
 * <li>ParameterValueGroup - if provided USE_JAI_IMAGEREAD will be added</li>
 * <li>AbstractGridCoverage2DReader</li>
 * <ul>
 * 
 * @author jeichar
 * @since 1.1.0
 */
@SuppressWarnings("deprecation")
public class GridCoverageLoader {
    /** Resource being loaded ...*/
    protected IGeoResource resource;

    public GridCoverageLoader(IGeoResource resource) {
        this.resource = resource;
    }
    
    public GridCoverage load( GeneralGridGeometry geom, IProgressMonitor monitor ) throws IOException {

        ParameterValueGroup group = resource.resolve(ParameterValueGroup.class, monitor);

        AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) resource.resolve(
                AbstractGridCoverage2DReader.class, monitor);
        if (group == null)
            group = reader.getFormat().getReadParameters();
        else {
            // temporary fix for imageio
            try {
                ParameterValue< ? > tempParam = group
                        .parameter(AbstractGridFormat.USE_JAI_IMAGEREAD.getName().toString());
                if (tempParam != null)
                    tempParam.setValue(false);
            } catch (ParameterNotFoundException e) {
                // do nothing
            }
        }
        ParameterValue< ? > param = group.parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D
                .getName().toString());
        param.setValue(geom);

        GridCoverage2D coverage = (GridCoverage2D) reader.read(group.values().toArray(
                new ParameterValue[0]));
        
        return coverage;
    }
}
