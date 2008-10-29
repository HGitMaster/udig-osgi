package net.refractions.udig.tutorials.rcp;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import net.refractions.udig.project.internal.ContextModelListenerAdapter;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.render.IViewportModelListener;
import net.refractions.udig.project.render.ViewportModelEvent;
import net.refractions.udig.project.render.ViewportModelEvent.EventType;
import net.refractions.udig.project.render.displayAdapter.IMapDisplayListener;
import net.refractions.udig.project.render.displayAdapter.MapDisplayEvent;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.commands.IDrawCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.project.ui.tool.AbstractModalTool;
import net.refractions.udig.project.ui.viewers.MapViewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
    private IMapDisplayListener mapDisplayListener = null;
    
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
            throw new IllegalStateException("Cannot set the map until the main map has a viewport model."); //$NON-NLS-1$
        }
        mapviewer.setMap(map);
        addContextListener();
        addViewportModelListener();
        addMapDisplayListener();
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
    public void createLocationBox(ViewportPane mainMapViewportPane){
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
        //add tool to the location box
        mapviewer.setModalTool(new LocationBoxMoveTool( mainMapViewportPane ));
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
                if (event.getType() == EventType.CRS){
                    //need to update the overview map crs
                    CoordinateReferenceSystem newcrs = (CoordinateReferenceSystem)event.getNewValue();
                    mapviewer.getMap().getViewportModelInternal().setCRS(newcrs);
                }else{
                    // repaint to update the box representing the location
                    mapviewer.getViewport().repaint();
                }
            }

        };
        mainmap.getViewportModelInternal().addViewportModelListener(viewportListener);
        
        mapviewer.getViewport().addPaneListener(new IMapDisplayListener(){

            public void sizeChanged( MapDisplayEvent event ) {
                //update the bounds
                mapviewer.getMap().getViewportModelInternal().setBounds(mainmap.getBounds(new NullProgressMonitor()));
            }});
    }
    
    /**
     * Adds a map display listener that listens to map resize events.
     */
    private void addMapDisplayListener() {
        mapDisplayListener = new IMapDisplayListener(){
            public void sizeChanged( MapDisplayEvent event ) {
                // update the bounds
                mapviewer.getMap().getViewportModelInternal().setBounds(
                        mainmap.getBounds(new NullProgressMonitor()));
            }
        };

        mapviewer.getViewport().addPaneListener(mapDisplayListener);
    }
    
    
    public void dispose(){
        mapviewer.dispose();
        if(viewportListener != null){
            mainmap.getViewportModelInternal().removeViewportModelListener(viewportListener);
        }
        if (contextListener != null){
            mainmap.getContextModel().eAdapters().remove(contextListener);
        }
        if (mapDisplayListener != null){
            mapviewer.getViewport().removePaneListener(mapDisplayListener);
        }
        removeLocationBox();
    }
    
    
    
    /**
     * Tool to apply to the overview map viewer to 
     * allow the viewbox to be moved around and
     * move the main box as well.
     *    
     *
     * @author Emily Gouge
     * @since 1.2.0
     */
    class LocationBoxMoveTool extends AbstractModalTool{
        ViewportPane mainMapVP;

        public LocationBoxMoveTool( ViewportPane mainMapVP){
            super(MOUSE | MOTION);
            this.mainMapVP = mainMapVP;

        }
        
        private boolean dragging = false;
        private org.eclipse.swt.graphics.Point startp = null;
        
        /**
         * @see net.refractions.udig.project.ui.tool.AbstractTool#mouseDragged(net.refractions.udig.project.render.displayAdapter.MapMouseEvent)
         */
        public void mouseDragged( MapMouseEvent e ) {
            if (dragging){
                //determining the number of pixels moved in the overview
                org.eclipse.swt.graphics.Point p = Display.getCurrent().map((Canvas)mapviewer.getViewport(), null, e.x, e.y);
                int xdiff = p.x - startp.x;
                int ydiff = p.y - startp.y;
                //determine the coordinate difference
                Coordinate oldc = context.pixelToWorld(startp.x, startp.y);
                Coordinate newc = context.pixelToWorld(p.x, p.y);
                double xoffset = newc.x - oldc.x;
                double yoffset = newc.y - oldc.y;
                
                //determine the number of pixels on the main map
                Point p1 = mainmap.getViewportModel().worldToPixel(newc);
                Point p2 = mainmap.getViewportModel().worldToPixel(oldc);
                xdiff = p2.x - p1.x;
                ydiff = p2.y - p1.y;
                
                //compute and update bounds
                final ReferencedEnvelope bounds = mainmap.getViewportModel().getBounds();

                ReferencedEnvelope newbounds = new ReferencedEnvelope(bounds.getMinX() + xoffset,
                        bounds.getMaxX() + xoffset, bounds.getMinY() + yoffset, bounds.getMaxY()
                                + yoffset, bounds.getCoordinateReferenceSystem());

                mainmap.getViewportModelInternal().setBounds(newbounds);

                //scroll
                ((Canvas) mainMapVP).scroll(xdiff, ydiff, 0, 0, mainMapVP.getWidth(), mainMapVP.getHeight(), true);
                startp = p;                
            }
               
        }

        /**
         * @see net.refractions.udig.project.ui.tool.AbstractTool#mousePressed(net.refractions.udig.project.render.displayAdapter.MapMouseEvent)
         */
        public void mousePressed( MapMouseEvent e ) {
            Coordinate start = getContext().pixelToWorld(e.x, e.y);
            if (mainmap.getViewportModelInternal().getBounds().contains(start)){
                dragging = true;
                startp = Display.getCurrent().map((Canvas) mapviewer.getViewport(), null, e.x, e.y);
                mainmap.getViewportModelInternal().setIsBoundsChanging(true);
            }
       
        }
        /**
         * @see net.refractions.udig.project.ui.tool.AbstractTool#mouseReleased(net.refractions.udig.project.render.displayAdapter.MapMouseEvent)
         */
        public void mouseReleased( MapMouseEvent e ) {
            if (dragging) {
                mainmap.getViewportModelInternal().setIsBoundsChanging(false);
                org.eclipse.swt.graphics.Point p = Display.getCurrent().map((Canvas)mapviewer.getViewport(), null, e.x, e.y);
                //determine the coordinate difference
                Coordinate oldc = context.pixelToWorld(startp.x, startp.y);
                Coordinate newc = context.pixelToWorld(p.x, p.y);
                double xoffset = newc.x - oldc.x;
                double yoffset = newc.y - oldc.y;
                                
                //compute and update bounds
                final ReferencedEnvelope bounds = mainmap.getViewportModel().getBounds();
                ReferencedEnvelope newbounds = new ReferencedEnvelope(bounds.getMinX() + xoffset,
                        bounds.getMaxX() + xoffset, bounds.getMinY() + yoffset, bounds.getMaxY()
                                + yoffset, bounds.getCoordinateReferenceSystem());
                mainmap.getViewportModelInternal().setBounds(newbounds);
                dragging = false;
            }
        }
    }
}
