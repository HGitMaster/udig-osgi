package net.refractions.udig.tutorials.mapgraphic;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.List;

import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import com.vividsolutions.jts.geom.Coordinate;

public class CoordinateGraphic implements MapGraphic {

@SuppressWarnings("unchecked")
public void draw( MapGraphicContext context ) {
    //initialize the graphics handle
     ViewportGraphics g = context.getGraphics();
     g.setColor(Color.RED);
     g.setStroke(ViewportGraphics.LINE_SOLID, 2);
     
     //get the map blackboard
     IMap map = context.getLayer().getMap();
     IBlackboard blackboard = context.getLayer().getMap().getBlackboard();
     
     List<Coordinate> coordinates = 
         (List<Coordinate>) blackboard.get("locations");
     
     if (coordinates == null) {
         return; //no coordinates to draw
     }
         
     //for each coordnate, create a circle and draw
     for (Coordinate coordinate : coordinates) {
        Ellipse2D e = new Ellipse2D.Double(
                coordinate.x-4,
                coordinate.y-4,
                10,10);
        g.draw(e);
     }
 }


}
