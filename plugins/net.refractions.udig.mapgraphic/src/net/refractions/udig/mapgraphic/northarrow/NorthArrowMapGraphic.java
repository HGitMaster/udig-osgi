/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, Refractions Research Inc.
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
package net.refractions.udig.mapgraphic.northarrow;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * a styled north arrow that actually points north!
 * <p>
 *
 * </p>
 * @author Jody Garnett and Brock Anderson
 * @since 1.1.0
 */
public final class NorthArrowMapGraphic implements MapGraphic{

    private static int ARROW_HEIGHT = 35;
    private static int ARROW_WIDTH = 22;
    private static int BOTTOM_INSET = 8;
    private static int SPACE_ABOVE_N = 3;
        
	public NorthArrowMapGraphic() {
	}

	private Point start; // where we drew last time
	private Envelope look; // where we were looking last time
	
	private Point end;
	private double theta;
	
	public void draw(MapGraphicContext context) {
		IViewportModel viewport = context.getViewportModel();

		Point here = start( context );
		if( here== null ) return; // bye!
		
		Envelope bounds = viewport.getBounds();
		if( !here.equals( start ) || !bounds.equals( look )){
			start = here;
			look = bounds;
			end = null;
			
			Coordinate worldStart = context.pixelToWorld( here.x, here.y );
					
			Coordinate groundStart = toGround( context, worldStart );
			if( groundStart == null) return;
			
			Coordinate groundNorth = moveNorth( groundStart ); // move a "little" way north
			Coordinate worldNorth = fromGround( context, groundNorth );
			
			
			theta = theta( worldStart, worldNorth );
			double distance = context.getViewportModel().getPixelSize().y * 20.0;

			Coordinate destination = walk( worldStart, theta, distance );
			
			//Coordinate destination = worldNorth;
			
			end = context.worldToPixel( destination );
		}
		if( start != null && end != null ){			
			drawArrow( context, here );
		}
	}

	private void drawArrow( MapGraphicContext context, Point here ) {
		ViewportGraphics g = context.getGraphics();
		AffineTransform t = g.getTransform();
		
		try {
			int nTop = ARROW_HEIGHT + SPACE_ABOVE_N;			
			int arrowCenterX = ARROW_WIDTH / 2;
			int totalHeight = ARROW_HEIGHT + SPACE_ABOVE_N + g.getFontAscent();

            AffineTransform t1 = g.getTransform();
            AffineTransform t2 = g.getTransform();
            t1.translate( here.x + ARROW_WIDTH, here.y + totalHeight );            
            t2.translate( here.x + ARROW_WIDTH, here.y + totalHeight );
            
            t1.scale( -1.0, -1.0 );
            t1.rotate( Math.PI / 2 );
            t1.rotate( -theta );
            g.setTransform( t1 );            
            g.setStroke(ViewportGraphics.LINE_SOLID, 1);			
			
			Point tip = new Point(arrowCenterX, BOTTOM_INSET);
			Point centerBase = new Point(arrowCenterX, ARROW_HEIGHT);
			Point bottomLeft = new Point(0, 0);
			Point bottomRight = new Point(ARROW_WIDTH, 0);
			
			//This polygon is drawn on the left, but then gets rotated
			//so it actually appears on the right if North is up.
			Polygon left = new Polygon();
			left.addPoint(centerBase.x, centerBase.y);
			left.addPoint(tip.x, tip.y); 
			left.addPoint(bottomLeft.x, bottomLeft.y); 			
			g.setColor( Color.black );
			g.fill(left);
			g.draw(left);
			
			Polygon right = new Polygon();
			right.addPoint(centerBase.x, centerBase.y);
			right.addPoint(tip.x, tip.y);
			right.addPoint(bottomRight.x, bottomRight.y);
			g.setColor( Color.white );
			g.fill(right);
			g.setColor( Color.black );
			g.draw(right);

			//TODO: center the N properly.  presently it relies on the default font and font size
            // to be some particular values
            g.setColor(Color.BLACK);
            g.drawString("N", arrowCenterX-5, nTop, ViewportGraphics.ALIGN_LEFT, ViewportGraphics.ALIGN_MIDDLE); //$NON-NLS-1$
            
			
		} finally {
			g.setTransform( t );
		}
		
    }

	@SuppressWarnings("unused")
	private void drawSimpleLine( MapGraphicContext context ) {
	    context.getGraphics().setColor( new Color(255,0,0));
	    context.getGraphics().drawLine(start.x, start.y, end.x, end.y );
    }

	private Coordinate walk(Coordinate ground, double theta, double d ) {
		double dx = Math.cos(theta)*d;
		double dy = Math.sin(theta)*d;
		
		return new Coordinate( ground.x+dx, ground.y+dy);  
	}

	private double theta(Coordinate ground, Coordinate north) {
	    return Math.atan2(Math.abs(north.y - ground.y), Math.abs(north.x - ground.x));
	}

	/** A coordinate that is slightly north please */
	private Coordinate moveNorth(Coordinate ground) {
		double up = ground.y+0.1;
		if( up > 90.0 ){
			return new Coordinate( ground.x, 90.0 );
		}
		return new Coordinate( ground.x, up);
	}

	@SuppressWarnings("unused")
	private Coordinate moveNorth(Coordinate ground, Double distance) {
		double up = ground.y + distance;
		if( ground.y < 90.0 ) {
			return new Coordinate( ground.x, up );
		}
		else return null;
	}
	static CoordinateReferenceSystem GROUND;
	static {
		try {
			GROUND = CRS.decode("EPSG:4326"); //$NON-NLS-1$
		} catch (FactoryException e) {
			GROUND = DefaultGeographicCRS.WGS84;
		}
	}
	
	/** Will transform there into ground WGS84 coordinates or die (ie null) trying */
	private Coordinate toGround(MapGraphicContext context, Coordinate there) {
		
		if( GROUND.equals( context.getCRS()) ){
			return there;
		}
		try {
			MathTransform transform = CRS.findMathTransform( context.getCRS(), GROUND );
			return JTS.transform( there, null, transform );			
		} catch (FactoryException e) {
			e.printStackTrace();
			return null;
		} catch (TransformException e) {
			// yes I do
			return null;
		}
	}
	
	private Coordinate fromGround(MapGraphicContext context, Coordinate ground) {
		
		if( GROUND.equals( context.getCRS()) ){
			return ground;
		}
		try {
			MathTransform transform = CRS.findMathTransform( GROUND, context.getCRS() );
			return JTS.transform( ground, null, transform );			
		} catch (FactoryException e) {
			// I hate you
			return null;
		} catch (TransformException e) {
			// yes I do
			return null;
		}
	}
	
	/** Replace w/ lookup to style black board when the time comes */
	private Point start(MapGraphicContext context) {
		Point point = null;		
		IBlackboard style = context.getLayer().getStyleBlackboard();
		try {
			point = (Point) style.get( NorthArrowTool.STYLE_BLACKBOARD_KEY );
		}
		catch( Exception evil ){
			evil.printStackTrace();
		}
		if( point == null ){ // default!
			point = new Point( 25,25 );
			style.put(NorthArrowTool.STYLE_BLACKBOARD_KEY, point );
		}
		return point;
	}
	
}
