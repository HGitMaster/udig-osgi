/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.render.internal.feature.shapefile;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.internal.render.Renderer;
import net.refractions.udig.project.render.IRenderContext;
import net.refractions.udig.render.internal.feature.basic.BasicFeatureMetrics;
import net.refractions.udig.style.sld.SLDContent;



/**
 * The metrics object for the BasicFeatureRenderer
 *
 * @author Jesse Eichar
 * @version $Revision: 1.9 $
 */
public class ShapefileFeatureMetrics extends BasicFeatureMetrics {

    /*
     * list of styles the basic wms renderer is expecting to find and use
     */
    protected static List<String> listExpectedStyleIds(){
        ArrayList<String> styleIds = new ArrayList<String>();
        styleIds.add(SLDContent.ID);
        return styleIds;
    }
    
   /**
     * Construct <code>BasicFeatureMetrics</code>.
     *
     * @param context2
     * @param factory
     */
    public ShapefileFeatureMetrics( IRenderContext context2, ShapefileFeatureMetricsFactory factory) {
        super(context2,factory);
        this.timeToDrawMetric = DRAW_DATA_INDEX;
    }

    /**
     * @see net.refractions.udig.project.render.IRenderMetrics#createRenderer()
     */
    public Renderer createRenderer() {
        Renderer renderer=new ShapefileFeatureRenderer();
        renderer.setContext(context);
        renderer.setName(context.getLayer().getName());
        return renderer;
    }
  

}
