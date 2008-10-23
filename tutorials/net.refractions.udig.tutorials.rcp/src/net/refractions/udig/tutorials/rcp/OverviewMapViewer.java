package net.refractions.udig.tutorials.rcp;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import net.refractions.udig.project.internal.ContextModelListenerAdapter;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.render.IViewportModelListener;
import net.refractions.udig.project.render.ViewportModelEvent;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.commands.IDrawCommand;
import net.refractions.udig.project.ui.viewers.MapViewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * An overview map viewer.
 * 
 *  <p> This viewer will track the the main map and add/remove layers.
 *  It also displays a blue box around the current area that is being 
 *  viewed.
 *  </p>
 *  
 * 
 * @author Emily Gouge
 * @since 1.2.0
 */
public class OverviewMapViewer {
    
    private MapViewer mapviewer = null;
    private Map mainmap = null;

    private IViewportModelListener viewportListener = null;
    private ContextModelListenerAdapter contextListener = null;
    private IDrawCommand rectDrawCommand = null;
    
    public OverviewMapViewer( Composite parent, int style, Map mainMap) {
        mapviewer = new MapViewer(parent, style);
        this.mainmap = mainMap;        
    }
    
    /**
     * Sets the map to use for the overview map.  Note that this should
     * only be done once the main map has a viewport model so
     * that the correct listeners can be created an added.
     *
     * @param map
     */
    public void setMap(Map map){
        if (mainmap.getViewportModel() == null){
            throw new IllegalStateException("Cannot set the map until the main map has a viewport model.");
        }
        mapviewer.setMap(map);
        addContextListener();
        addViewportModelListener();
    }
    
    public Control getControl(){
        return mapviewer.getControl();
    }
    public void removeLocationBox(){
        if (rectDrawCommand != null){
            rectDrawCommand.setValid(false);
            rectDrawCommand = null;
        }
    }
    
    /**
     * Creates a location box - which tracks the viewport of the main map
     * in the overview window.
     */
    public void createLocationBox(){
        final Map overviewMap = this.mapviewer.getMap();
        rectDrawCommand = new AbstractDrawCommand(){

            public Rectangle getValidArea() {
                return null;
            }

            public void run( IProgressMonitor monitor ) throws Exception {
                //draw a bounding box around the area that is associated
                //with the main map
                ReferencedEnvelope bounds = mainmap.getViewportModel().getBounds();
                
                double xmin = bounds.getMinX();
                double xmax = bounds.getMaxX();
                double ymin = bounds.getMinY();
                double ymax = bounds.getMaxY();
                
                 Point ll = overviewMap.getViewportModel().worldToPixel(new Coordinate(xmin, ymin));
                 Point ur = overviewMap.getViewportModel().worldToPixel(new Coordinate(xmax, ymax));
                 
                 
                 int width = ur.x - ll.x;
                 int height = ur.y - ll.y;
                 if (width < 2) width = 2;
                 if (height > -2) height = -2;
                 
                 graphics.setLineWidth(1);
                 graphics.setColor(new Color(255,255,255,120));
                 graphics.drawRect(ll.x-1, ll.y+1, width+2, height-2);
                 graphics.drawRect(ll.x+1, ll.y-1, width-2, height+2);
                 
                 graphics.setColor(Color.BLUE);
                 graphics.setLineWidth(1); 
                 graphics.drawRect(ll.x, ll.y, width, height);
            }
            
        };
        mapviewer.getViewport().addDrawCommand(rectDrawCommand);
        
    }
    
    /**
     * Add a context listener that listens to when layers are added/removed
     * from the map.
     */
    private void addContextListener(){
        final Map overviewmap = this.mapviewer.getMap();
        contextListener = new ContextModelListenerAdapter(){
            protected void layerAdded( Notification msg ) {
                updateMapAndRefresh(msg);
            }

            protected void manyLayersAdded( Notification msg ) {
                updateMapAndRefresh(msg);
            }

            protected void layerRemoved( Notification msg ) {
                updateMapAndRefresh(msg);
            }

            protected void manyLayersRemoved( Notification msg ) {
                updateMapAndRefresh(msg);
            }

            protected void zorderChanged( Notification msg ) {
                updateMapAndRefresh(msg);
            }

            protected void glyphChanged( Notification msg ) {
                updateMapAndRefresh(msg);
            }

            protected void styleChanged( Notification msg ) {
                updateMapAndRefresh(msg);
            }

            protected void visibilityChanged( Notification msg ) {
                updateMapAndRefresh(msg);
            }
           
            private void updateMapAndRefresh(Notification msg){
                System.out.println("added");
                overviewmap.getContextModel().eNotify(msg);
                overviewmap.getViewportModelInternal().setBounds(mainmap.getBounds(new NullProgressMonitor()));                
            }
        };
        mainmap.getContextModel().eAdapters().add(contextListener);
    }
    
    /**
     * Adds a viewport model listener to call repaint when the viewport
     * is changed.
     */
    private void addViewportModelListener() {
        viewportListener = new IViewportModelListener(){
            public void changed( ViewportModelEvent event ) {
                // repaint to update the box representing the location
                mapviewer.getViewport().repaint();
            }

        };
        mainmap.getViewportModelInternal().addViewportModelListener(viewportListener);
    }
    
    public void dispose(){
        mapviewer.dispose();
        if(viewportListener != null){
            mainmap.getViewportModelInternal().removeViewportModelListener(viewportListener);
        }
        if (contextListener != null){
            mainmap.getContextModel().eAdapters().remove(contextListener);
        }
        removeLocationBox();
    }

}
