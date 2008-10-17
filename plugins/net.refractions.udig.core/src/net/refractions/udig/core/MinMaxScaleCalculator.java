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
package net.refractions.udig.core;

import java.util.HashSet;
import java.util.Set;

import org.geotools.styling.AnchorPoint;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.ImageOutline;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.OverlapBehavior;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.ShadedRelief;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleVisitor;
import org.geotools.styling.StyledLayer;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.UserLayer;
import org.geotools.util.Range;

/**
 * 
 * Traverses a Style and determines the min and max scale that the style is valid for.
 * 
 * @author jesse
 * @since 1.1.0
 */
public class MinMaxScaleCalculator implements StyleVisitor {
    
    private Set<Range<Double>> ranges = new HashSet<Range<Double>>();
    private double max;
    private double min; 

    public void visit( StyledLayerDescriptor sld ) {
        StyledLayer[] layers = sld.getStyledLayers();
        for( StyledLayer styledLayer : layers ) {
            if( styledLayer instanceof NamedLayer){
                ((NamedLayer)styledLayer).accept(this);
            } else if( styledLayer instanceof UserLayer ){
                ((UserLayer)styledLayer).accept(this);
            }
        }
    }

    public void visit( NamedLayer layer ) {
        Style[] styles = layer.getStyles();
        for( Style style : styles ) {
            style.accept(this);
        }
    }

    public void visit( UserLayer layer ) {
        Style[] styles = layer.getUserStyles();
        for( Style style : styles ) {
            style.accept(this);
        }
    }

    public void visit( FeatureTypeConstraint ftc ) {
    }

    public void visit( Style style ) {
        FeatureTypeStyle[] featureTypeStyles = style.getFeatureTypeStyles();
        for( FeatureTypeStyle featureTypeStyle : featureTypeStyles ) {
            featureTypeStyle.accept(this);
        }
    }

    public void visit( Rule rule ) {
        double min = rule.getMinScaleDenominator();
        double max = rule.getMaxScaleDenominator();
        this.min = Math.min(min, this.min);
        this.max = Math.max(max, this.max);
        ranges.add(new Range(Double.class, min,max));
    }

    public void visit( FeatureTypeStyle fts ) {
        Rule[] rules = fts.getRules();
        for( Rule rule : rules ) {
            rule.accept(this);
        }
    }

    public void visit( Fill fill ) {
    }

    public void visit( Stroke stroke ) {
    }

    public void visit( Symbolizer sym ) {
    }

    public void visit( PointSymbolizer ps ) {
    }

    public void visit( LineSymbolizer line ) {
    }

    public void visit( PolygonSymbolizer poly ) {
    }

    public void visit( TextSymbolizer text ) {
    }

    public void visit( RasterSymbolizer raster ) {
    }

    public void visit( Graphic gr ) {
    }

    public void visit( Mark mark ) {
    }

    public void visit( ExternalGraphic exgr ) {
    }

    public void visit( PointPlacement pp ) {
    }

    public void visit( AnchorPoint ap ) {
    }

    public void visit( Displacement dis ) {
    }

    public void visit( LinePlacement lp ) {
    }

    public void visit( Halo halo ) {
    }

    public void visit( ColorMap colorMap ) {
    }

    public void visit( ColorMapEntry colorMapEntry ) {
    }

    public Set<Range<Double>> getRanges() {
        return ranges;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public void visit( ContrastEnhancement arg0 ) {
    }

    public void visit( ImageOutline arg0 ) {
    }

    public void visit( ChannelSelection arg0 ) {
    }

    public void visit( OverlapBehavior arg0 ) {
    }

    public void visit( SelectedChannelType arg0 ) {
    }

    public void visit( ShadedRelief arg0 ) {
    }

}
