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
package net.refractions.udig.render.internal.gridcoverage.basic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.core.MinMaxScaleCalculator;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.render.Renderer;
import net.refractions.udig.project.render.AbstractRenderMetrics;
import net.refractions.udig.project.render.IRenderContext;
import net.refractions.udig.project.render.IRenderMetricsFactory;
import net.refractions.udig.style.sld.SLDContent;
import net.refractions.udig.ui.graphics.SLDs;

import org.geotools.styling.Style;
import org.geotools.util.Range;

/**
 * Creates a Metrics object for the basic gridcoverage renderer
 * 
 * @author jeichar
 * @since 0.3
 */
public class MemoryGridCoverageMetrics extends AbstractRenderMetrics {

    /*
     * list of styles the renderer is expecting to find and use
     */
    private static List<String> listExpectedStyleIds(){
        ArrayList<String> styleIds = new ArrayList<String>();
        styleIds.add("net.refractions.udig.style.sld");
        styleIds.add("net.refractions.udig.style.cache");
        return styleIds;
    }
    
    /**
     * Construct <code>BasicGridCoverageMetrics</code>.
     *
     * @param context2
     * @param factory
     */
    public MemoryGridCoverageMetrics( IRenderContext context2, MemoryGridCoverageMetricsFactory factory) {
        super( context2, factory, listExpectedStyleIds());
        this.resolutionMetric = RES_DENSE; // reads more then is required for the screen!
        
        ID id = context.getGeoResource().getID();
        //Boolean memory = (Boolean) context.getLayer().getStyleBlackboard().get("net.refractions.udig.style.cache");        
        if( id.isMemory() ){
            // we would not really want to use GridCoverageLoader on an in memory image
            this.latencyMetric = LATENCY_MEMORY;
            this.timeToDrawMetric = DRAW_IMAGE_MEMORY;
        }
        else {
            this.latencyMetric = LATENCY_MEMORY_CACHE;        
            this.timeToDrawMetric = DRAW_IMAGE_MEMORY;
        }
    }

    public MemoryGridCoverageRenderer createRenderer() {
        MemoryGridCoverageRenderer r=new MemoryGridCoverageRenderer();
        r.setContext(context);
        return r;
    }

    /**
     * @see net.refractions.udig.project.render.RenderMetrics#getRenderContext()
     */
    public IRenderContext getRenderContext() {
        return context;
    }

    /**
     * @see net.refractions.udig.project.render.IRenderMetrics#getRenderMetricsFactory()
     */
    public IRenderMetricsFactory getRenderMetricsFactory() {
        return factory;
    }

    public boolean canAddLayer( ILayer layer ) {
        return false;
    }

    public boolean canStyle( String styleID, Object value ) {
        if( "net.refractions.udig.style.cache".equals(styleID)){
            if( Boolean.TRUE.equals( value )){
                return true; // user turned on caching
            }
            else {
                return false; // do not use caching unless user asks
            }
        }
        // although we expect SLDContent; we are willing to work with any Style
        //
        if( value != null && value instanceof Style){              
            Style style = (Style) value;
            return SLDs.rasterSymbolizer( style ) != null;
        }
        return false;
    }

    public boolean isOptimized() {
        return false;
    }

    public Set<Range<Double>> getValidScaleRanges() {
        Object value = context.getLayer().getStyleBlackboard().get(SLDContent.ID);
        if( value == null ) {
            return new HashSet<Range<Double>>();
        }
        if( value instanceof Style ){
            Style style = (Style) value;
            return MinMaxScaleCalculator.getValidScaleRanges(style);
        }
        else {
            System.out.println("Unexpected "+value.getClass()+" for "+SLDContent.ID+":"+value);            
            return new HashSet<Range<Double>>();
        }
    }

}
