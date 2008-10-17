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

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoRedoCommand;
import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.commands.DeselectEditGeomCommand;
import net.refractions.udig.tools.edit.commands.StartEditingCommand;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.ShapeType;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Creates a new Geometry and feature
 * <p>Requirements: * <ul> * <li>current state is NONE</li>
 * <li>eventType is RELEASED</li>
 * <li>no modifiers</li>
 * <li>button1 released</li>
 * <li>no buttons down</li>
 * </ul> * </p> * <p>Action: * <ul> * <li>Sets the currentGeom to be the default geom on the black board</li> * <li>Sets the state to CREATING</li>
 * <li>Adds a point to the geom</li>
 * </ul> * </p>
 * @author jones
 * @since 1.1.0
 */
public class StartEditingBehaviour implements EventBehaviour {

    private ShapeType type;

    public StartEditingBehaviour(ShapeType type){
        this.type=type;
    }
    
    public boolean isValid( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        boolean goodState = handler.getCurrentState()!=EditState.NONE && handler.getCurrentState()==EditState.MODIFYING 
            || handler.getCurrentState()==EditState.NONE && handler.getCurrentState()!=EditState.MODIFYING;
        boolean releasedEvent = eventType==EventType.RELEASED;
        boolean noModifiers =  !(e.modifiersDown());
        boolean button1 = e.button==MapMouseEvent.BUTTON1;
        boolean noButtonsDown = !e.buttonsDown();
        return goodState && releasedEvent && noButtonsDown && noModifiers && button1;
    }

    public UndoableMapCommand getCommand( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        if( !isValid(handler, e, eventType))
            throw new IllegalArgumentException("Current State is not valid for behaviour"); //$NON-NLS-1$
        List<UndoableMapCommand> commands=new ArrayList<UndoableMapCommand>();
        commands.add(handler.getContext().getEditFactory().createNullEditFeatureCommand());
        ILayer editLayer = handler.getEditLayer();
        EditBlackboard bb = handler.getEditBlackboard(editLayer);
        commands.add(new DeselectEditGeomCommand(handler, bb.getGeoms())); 
        commands.add(new StartEditingCommand(handler, e, type));
        
        UndoableComposite undoableComposite = new UndoableComposite(commands);
        undoableComposite.setMap(handler.getContext().getMap());
        try {
            undoableComposite.run(new NullProgressMonitor());
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        return new UndoRedoCommand(undoableComposite);

    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        EditPlugin.log(""+handler, error); //$NON-NLS-1$
    }

}
