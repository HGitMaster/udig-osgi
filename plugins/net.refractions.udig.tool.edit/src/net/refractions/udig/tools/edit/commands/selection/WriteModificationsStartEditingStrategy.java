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
package net.refractions.udig.tools.edit.commands.selection;

import java.util.List;

import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.commands.ClearEditBlackboardCommand;
import net.refractions.udig.tools.edit.commands.DeselectEditGeomCommand;
import net.refractions.udig.tools.edit.commands.DeselectionStrategy;
import net.refractions.udig.tools.edit.commands.SelectionParameter;
import net.refractions.udig.tools.edit.commands.SetEditStateCommand;
import net.refractions.udig.tools.edit.commands.StartEditingCommand;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.EditGeom;
import net.refractions.udig.tools.edit.support.ShapeType;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is a Deselection strategy that will write out all the changes that occurred and then set the
 * EditState to {@link EditState#CREATING}
 * 
 * @author jesse
 * @since 1.1.0
 */
public class WriteModificationsStartEditingStrategy implements DeselectionStrategy {

    private ShapeType typeToCreate;

    public WriteModificationsStartEditingStrategy( ShapeType typeToCreate ) {
        this.typeToCreate = typeToCreate;
    }

    public void run( IProgressMonitor monitor, SelectionParameter parameters,
            UndoableComposite commands ) {
        EditToolHandler handler = parameters.handler;
        EditBlackboard editBlackboard = handler.getEditBlackboard(handler.getEditLayer());

        if (!parameters.event.isModifierDown(MapMouseEvent.MOD1_DOWN_MASK)
                && !parameters.event.isShiftDown() && parameters.permitClear) {
            writeModifiedFeaturesAndStartEditing(monitor, parameters, commands);
            
            commands.addCommand(handler.getContext().getEditFactory()
                    .createNullEditFeatureCommand());
            commands.addCommand(new ClearEditBlackboardCommand(handler, editBlackboard));
            commands.addCommand(new SetEditStateCommand(handler, EditState.NONE));
        }

        if (typeToCreate != null) {
            if (typeToCreate == ShapeType.POINT) {
                commands.addCommand(new StartEditingCommand(handler, parameters.event,
                        typeToCreate, handler.getCurrentState()));
                commands.addCommand(handler.getCommand(handler.getAcceptBehaviours()));
            } else {
                commands.addCommand(new StartEditingCommand(handler, parameters.event,
                        typeToCreate, EditState.CREATING));
            }
        }

    }

    private void writeModifiedFeaturesAndStartEditing( final IProgressMonitor monitor,
            final SelectionParameter parameters, final UndoableComposite commands ) {
        EditToolHandler handler = parameters.handler;
        EditBlackboard editBlackboard = handler.getEditBlackboard(handler
                .getEditLayer());
        if (hasDirtyGeom(handler)) {
            commands.addCommand(handler.getCommand(handler.getAcceptBehaviours()));
            commands.addCommand(new DeselectEditGeomCommand(handler, editBlackboard.getGeoms()));
        }
    }
    
    private boolean hasDirtyGeom(EditToolHandler handler) {
        if (handler.getCurrentGeom() != null && handler.getCurrentGeom().isChanged())
            return true;
        List<EditGeom> geoms = handler.getEditBlackboard(handler.getEditLayer())
                .getGeoms();
        for( EditGeom geom : geoms ) {
            if (geom.isChanged())
                return true;
        }
        return false;
    }


}
