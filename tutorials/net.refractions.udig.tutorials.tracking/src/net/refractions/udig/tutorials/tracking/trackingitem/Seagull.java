package net.refractions.udig.tutorials.tracking.trackingitem;

import java.awt.Color;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Coordinate;

import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.ui.graphics.ViewportGraphics;

public class Seagull extends AbstractTrackingItem {
    
    public Seagull(TrackingItem parent, String id, 
            InternationalString displayname, Coordinate coordinate) {
        this(parent, id, displayname, parent.getCRS(), coordinate);
    }

    protected Seagull( TrackingItem parent, String id, InternationalString displayname, 
            CoordinateReferenceSystem crs, Coordinate coordinate ) {
        super(parent, id, displayname, crs, coordinate);
    }

    /**
     * Draw this seagull on the given context.  Just Draw red circles for now.
     */
    public void draw( MapGraphicContext context ) {
        if (coordinate == null) {
            return;
        }
        
        // initialize the graphics handle
        ViewportGraphics g = context.getGraphics();
        g.setColor(Color.RED);
        g.setStroke(ViewportGraphics.LINE_SOLID, 2);
        
        // figure out our CRS
        CoordinateReferenceSystem ourCRS = crs;
        if (ourCRS == null) {
            ourCRS = context.getLayer().getCRS();
        }
        
        // figure out how to map our coordinate to the world
        CoordinateReferenceSystem worldCRS = context.getCRS();
        MathTransform dataToWorld;
        try {
            dataToWorld = CRS.findMathTransform(ourCRS, worldCRS, false);
        } catch (FactoryException e1) {
            throw (RuntimeException) new RuntimeException( ).initCause( e1 );
        }

        Coordinate worldLocation = new Coordinate();
           
        Coordinate dataLocation = coordinate;
        try {
            JTS.transform(dataLocation, worldLocation, dataToWorld);
        } catch (TransformException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }                
              
        java.awt.Point p = context.worldToPixel(worldLocation);
        g.fillOval(p.x, p.y, 10, 10);
        String name = getDisplayName().toString();
        g.drawString(name, p.x + 15, p.y + 15, 
                ViewportGraphics.ALIGN_MIDDLE, ViewportGraphics.ALIGN_MIDDLE);

        // draw an ellipse for this seagull
//        Ellipse2D e = new Ellipse2D.Double(coordinate.x-4, coordinate.y-4,10,10);
//        g.draw(e);
       
    }

}
