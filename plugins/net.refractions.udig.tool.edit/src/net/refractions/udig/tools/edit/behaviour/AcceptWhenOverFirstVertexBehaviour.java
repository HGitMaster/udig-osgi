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

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.command.UndoRedoCommand;
import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.commands.SetEditStateCommand;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.Point;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Runs the accept behaviours registered with the {@link EditToolHandler} when the mouse is clicked over
 * the first vertex of a shape
 * 
 * <p>Requirements: * <ul>
 * <li>event type == RELEASE</li>
 * <li>edit state == CREATING </li>
 * <li>no modifiers down</li>
 * <li>button 1 released</li>
 * <li>no buttons down</li>
 * <li>current shape and geom are set</li>
 * <li>mouse is over the first vertex of the currentShape</li>
 * <li>event type != DOUBLE_CLICK</li> *</ul> * </p> * @author jones
 * @since 1.1.0
 */
public class AcceptWhenOverFirstVertexBehaviour implements EventBehaviour {

    public boolean isValid( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        boolean legalState=handler.getCurrentState()==EditState.CREATING;
        
        //Aritz, Mauricio: Solution for issue: Odd effects in hole cutting mode (problem 2).
        //This event should be launched only when mouse is released and not when doing double click.
        //Also the class description has been modified.
//        boolean legalEventType=eventType==EventType.RELEASED || eventType==EventType.DOUBLE_CLICK;
        boolean legalEventType=eventType==EventType.RELEASED && !(eventType==EventType.DOUBLE_CLICK);
        boolean shapeAndGeomNotNull=handler.getCurrentShape()!=null;
        boolean button1Released=e.button==MapMouseEvent.BUTTON1;
        
        return legalState && legalEventType && shapeAndGeomNotNull && button1Released 
        && !e.buttonsDown() && !e.modifiersDown() && overFirstVertex(handler, e);
    }
    
    private boolean overFirstVertex(EditToolHandler handler, MapMouseEvent e) {
        Point vertexOver=handler.getEditBlackboard(handler.getEditLayer()).overVertex(Point.valueOf(e.x, e.y), 
                PreferenceUtil.instance().getVertexRadius());
        
        return handler.getCurrentShape().getNumPoints()>0 && handler.getCurrentShape().getPoint(0).equals(vertexOver);
    }

    public UndoableMapCommand getCommand( EditToolHandler handler, MapMouseEvent e,
            EventType eventType ) {
        if( !isValid(handler, e, eventType) )
            throw new IllegalArgumentException("Current state is not legal"); //$NON-NLS-1$
        List<UndoableMapCommand> commands=new ArrayList<UndoableMapCommand>();
                
        commands.add(handler.getCommand(handler.getAcceptBehaviours()));
        //Aritz, Mauricio: Solution for issue: Odd effects in hole cutting mode (problem 2).
        //This command isn't need because before that the AcceptChangesBehaviour is launched 
        //and its entrust of changing the state if everything goes well.
//        if( handler.getCurrentState()==EditState.CREATING)
//            commands.add(new SetEditStateCommand(handler, EditState.MODIFYING));            
        
        UndoableComposite undoableComposite = new UndoableComposite(commands);
        undoableComposite.setMap(handler.getContext().getMap());
        try {
            undoableComposite.execute(new NullProgressMonitor());
        } catch (Exception e1) {
            throw (RuntimeException) new RuntimeException().initCause(e1);
        }
        return new UndoRedoCommand(undoableComposite);
    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        EditPlugin.log("", error); //$NON-NLS-1$
    }

}
