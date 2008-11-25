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

import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.commands.SelectFeatureAsEditFeatureCommand;
import net.refractions.udig.tools.edit.commands.SelectFeatureCommand;
import net.refractions.udig.tools.edit.commands.SelectionParameter;
import net.refractions.udig.tools.edit.commands.SelectionStrategy;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.Point;

import org.eclipse.core.runtime.IProgressMonitor;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A strategy for adding the features to the edit blackboard if no modifiers are down.  The first feature
 * is selected for editing each of the others are not.
 * <p>
 * Recognises the following SelectionParameter settings:
 * <ul>
 * <li>onlyAdd - true to add the feature to the existing selection
 *    (same effect can be had by using MOD1, ie control key on windows)
 * <li>acceptableClasses
 * <li>event   
 * </ul>
 * 
 * @author jesse
 * @since 1.1.0
 */
public class SelectFeatureStrategy implements SelectionStrategy {
    /**
     * @param monitor report progress
     * @param commands UndoableComposte; any commands will be added to this composite
     * @param parameters information controlling the selection process see class description for details
     * @param feature Feature being selected
     * @param firstFeature true if this is the first feature returned for selection
     */
    public void run( IProgressMonitor monitor, UndoableComposite commands,
            SelectionParameter parameters, SimpleFeature feature, boolean firstFeature ) {
        EditToolHandler handler = parameters.handler;
        Class< ? extends Geometry>[] acceptableClasses = parameters.acceptableClasses;
        boolean onlyAdd = parameters.onlyAdd;
        MapMouseEvent event = parameters.event;

        for( Class< ? extends Geometry> clazz : acceptableClasses ) {
            if (clazz.isAssignableFrom(feature.getDefaultGeometry().getClass())) {
                EditPlugin.trace(EditPlugin.SELECTION,
                        "Feature is one of the acceptable classes " + feature.getID(), null); //$NON-NLS-1$

                if (firstFeature && !keyboardModifierIndicatesAdd(handler, event) && !onlyAdd ) {
                    commands.addCommand(handler.getCommand(handler.getAcceptBehaviours()));
                    commands.addCommand(new SelectFeatureAsEditFeatureCommand(handler, feature,
                            handler.getEditLayer(), Point.valueOf(event.x, event.y)));
                } else {
                    if (onlyAdd || keyboardModifierIndicatesAdd(handler, event)) {
                        // call SelectFeaturecommand so that it is the CurrentEditGeom
                        commands.addCommand(new SelectFeatureCommand(handler, feature, Point
                                .valueOf(event.x, event.y)));
                    } else {
                        // call SelectFeaturecommand so that it is just added to the EditBlackboard
                        // but
                        // not as the CurrentEditGeom
                        commands.addCommand(new SelectFeatureCommand(handler, feature, null));
                    }
                }
            } else {
                EditPlugin.trace(EditPlugin.SELECTION,
                        "Feature is not one of the acceptable classes " + feature.getID(), null); //$NON-NLS-1$
            }
        }
    }

    /**
     * Returns true if shift is down or MOD1 is down and the mouse is over the currently selected geometry
     */
    private boolean keyboardModifierIndicatesAdd( EditToolHandler handler, MapMouseEvent event ) {
        EditBlackboard editBlackboard = handler.getEditBlackboard(handler.getEditLayer());
        boolean noIntersectingSelection = editBlackboard.getGeoms(event.x, event.y).isEmpty();
        return event.isShiftDown()
                || (noIntersectingSelection && event
                        .isModifierDown(MapMouseEvent.MOD1_DOWN_MASK));
    }

}
