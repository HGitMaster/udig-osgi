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
package net.refractions.udig.tools.edit.behaviour;

import net.refractions.udig.core.IProvider;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.ClosestEdge;
import net.refractions.udig.tools.edit.support.EditGeom;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.PlatformUI;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Sets the cursor to indicate what action can be done.
 * 
 * Also adds tips to the status bar.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class CursorControlBehaviour implements EventBehaviour {


    private IProvider<String> defaultMessage;
    private IProvider<Cursor> overVertexCursor;
    private IProvider<Cursor> overEdgeCursor;
    private IProvider<String> overEdgeMessage;
    private IProvider<String> overVertexMessage;
    
    /**
     * Configuration describing how an AbstractEditTool provides visual feedback in
     * the form of a cursor and message.
     * <p>
     * Please note that the Cursors provided to this class are not the resp
     * @param EditToolHandler being configured
     * @param defaultMessage the message display when not over a vertex or an edge.
     * @param overVertexCursor a provider that provides the cursor to show when over a vertex.  This class
     * <em>WILL NOT</em> dispose of the cursor.
     * @param overVertexMessage generates the message to display when over a vertex
     * @param overEdgeCursor a provider that provides the cursor to show when over an edge.  This class
     * <em>WILL NOT</em> dispose of the cursor.
     * @param overEdgeMessage generates the message to display when over an edge
     */
    public CursorControlBehaviour(EditToolHandler handler, IProvider<String> defaultMessage, IProvider<Cursor> overVertexCursor, 
            IProvider<String> overVertexMessage, IProvider<Cursor> overEdgeCursor, IProvider<String> overEdgeMessage){
        this.defaultMessage=defaultMessage;
        if( overVertexCursor==null )
            this.overVertexCursor=new DefaultCursorProvider(handler);
        else
            this.overVertexCursor=overVertexCursor;
        if( overEdgeCursor==null )
            this.overEdgeCursor=new DefaultCursorProvider(handler);
        else
            this.overEdgeCursor=overEdgeCursor;
        if( overVertexMessage==null )
            this.overVertexMessage=new NullStringProvider();
        else
            this.overVertexMessage=overVertexMessage;
        if( overEdgeMessage==null )
            this.overEdgeMessage=new NullStringProvider();
        else
            this.overEdgeMessage=overEdgeMessage;
    }

    public boolean isValid( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        boolean isHover=eventType==EventType.HOVERED;
        boolean isMove=eventType==EventType.MOVED;
        return isHover || isMove;
    }

    public UndoableMapCommand getCommand( EditToolHandler handler, MapMouseEvent e,
            EventType eventType ) {
        if( overVertex(handler, e) ){
            setCursorAndMessage(overVertexCursor.get(),overVertexMessage.get(), handler);
        }else if ( eventType==EventType.HOVERED && onEdge(handler,e)){
            setCursorAndMessage(overEdgeCursor.get(), overEdgeMessage.get(), handler);
        }else
            setCursorAndMessage(handler.editCursor, defaultMessage.get(), handler);

        return null;
    }
    

    private boolean onEdge( EditToolHandler handler, MapMouseEvent e ) {
        Point point = Point.valueOf(e.x,e.y);
        EditGeom geom = handler.getCurrentGeom();
        if( geom==null )
            return false;

        ILayer selectedLayer = handler.getEditLayer();
        Class type = selectedLayer.getSchema().getGeometryDescriptor().getType().getBinding();
        boolean polygonLayer=Polygon.class.isAssignableFrom(type) || MultiPolygon.class.isAssignableFrom(type);

        ClosestEdge closestEdge = geom.getClosestEdge(point, polygonLayer);
        
        return closestEdge!=null && closestEdge.getDistanceToEdge()<PreferenceUtil.instance().getVertexRadius();
    }

    private boolean overVertex(EditToolHandler handler, MapMouseEvent e) {
        if( handler.getCurrentShape()==null )
            return false;
        int radius = PreferenceUtil.instance().getVertexRadius();
        Point point = Point.valueOf(e.x,e.y);
        return handler.getCurrentGeom().overVertex(point, radius)!=null;
    }

    private void setCursorAndMessage(final Cursor cursor, final String string, final EditToolHandler handler) {
        Runnable runnable = new Runnable(){
            public void run() {
                if( PlatformUI.getWorkbench().isClosing() )
                    return;
                IActionBars2 bars = handler.getContext().getActionBars();
                if (bars!=null){
                    bars.getStatusLineManager().setErrorMessage(null);
                    bars.getStatusLineManager().setMessage(string);
                }
                handler.getContext().getViewportPane().setCursor(cursor);
                
            }
        };
        if( Display.getCurrent()!=null )
            runnable.run();
        else
            Display.getDefault().asyncExec(runnable);
    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        EditPlugin.log("", error); //$NON-NLS-1$
    }

    public static class SystemCursorProvider implements IProvider<Cursor>{
        private int id;
        /**
         * new instance
         * @param swtCursorID the id from SWT that indicates the cursor to create.
         */
        public SystemCursorProvider(int swtCursorID){
            this.id=swtCursorID;
        }
        public Cursor get(Object... params) {
            final Cursor[] cursor = new Cursor[1];
            PlatformGIS.syncInDisplayThread(new Runnable(){
                public void run() {
                    cursor[0] = Display.getDefault().getSystemCursor(id);
                }
            });
            return cursor[0];
        }
        
    }

    public static class DefaultCursorProvider implements IProvider<Cursor> {

        private EditToolHandler handler;

        public DefaultCursorProvider( EditToolHandler handler ) {
            this.handler=handler;
        }

        public Cursor get(Object... params) {
            return handler.editCursor;
        }

    }

    public static class NullStringProvider implements IProvider<String> {

        public String get(Object... params) {
            return null;
        }

    }
}
