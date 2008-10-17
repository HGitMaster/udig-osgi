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
package net.refractions.udig.tools.edit.commands;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tool.edit.internal.Messages;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.ClosestEdge;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.EditGeom;
import net.refractions.udig.tools.edit.support.Point;

import org.eclipse.core.runtime.IProgressMonitor;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Add a vertex to the nearest edge to a point.
 * @author jones
 * @since 1.1.0
 */
public class InsertOnNearestEdgeCommand extends AbstractCommand implements UndoableMapCommand {

    private final EditBlackboard board;
    private final EditGeom geom;
    private final Point toAdd;
    private List<ClosestEdge> edges;
    private EditToolHandler handler;

    public InsertOnNearestEdgeCommand(EditToolHandler handler2, EditBlackboard board, Point toAdd) {
        this(handler2, board, null, toAdd);
    }

    public InsertOnNearestEdgeCommand(EditToolHandler handler2, EditBlackboard board, EditGeom geom, Point toAdd) {
        this.board=board;
        this.geom=geom;
        this.toAdd=toAdd;
        this.handler=handler2;
    }
    
    public void rollback( IProgressMonitor monitor ) throws Exception {
        if( edges==null )
            throw new RuntimeException("The command has not yet been run!!"); //$NON-NLS-1$
        
        if( edges.size()==0 )
            return;
        
        for( ClosestEdge edge : edges ) {
            board.removeCoordinate(edge.getIndexOfPrevious()+1, edge.getAddedCoord(), edge.getPart());
        }
        if ( getMap()!=null )
            handler.repaint();
    }

    public void run( IProgressMonitor monitor ) throws Exception {
        ILayer editLayer = handler.getEditLayer();
        Class<?> type = editLayer.getSchema().getGeometryDescriptor().getType().getBinding();
        boolean polygonLayer=Polygon.class.isAssignableFrom(type) || MultiPolygon.class.isAssignableFrom(type);
        if( geom == null ){
            this.edges=board.addToNearestEdge(toAdd.getX(), toAdd.getY(),polygonLayer);
        }else{
            this.edges=new ArrayList<ClosestEdge>();
            edges.add(board.addToNearestEdge(toAdd.getX(), toAdd.getY(), geom, polygonLayer));
        }
        if ( getMap()!=null )
            handler.repaint();
          
    }

    public String getName() {
        return Messages.AddToNearestEdgeCommand_name+toAdd;
    }

}
