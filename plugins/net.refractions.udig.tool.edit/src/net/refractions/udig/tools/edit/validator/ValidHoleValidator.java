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
package net.refractions.udig.tools.edit.validator;

import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tool.edit.internal.Messages;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.behaviour.IEditValidator;
import net.refractions.udig.tools.edit.support.EditUtils;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

/**
 * Returns true if:
 * <ul>
 * <li>The new vertex does not cause a self intersect</li>
 * <li>The new vertex does not the hole to intersect with another hole</li>
 * <li>The new event is within the shell</li>
 * </ul>
 * @author Jesse
 * @since 1.1.0
 */
public class ValidHoleValidator implements IEditValidator {

    public String isValid( EditToolHandler handler, MapMouseEvent event, EventType type ) {
        PrimitiveShape shell = handler.getCurrentGeom().getShell();
        PrimitiveShape hole = handler.getCurrentShape();

        assert hole!=shell;
        
        // check the new edge (that will be created by event) to see if it intersect with the 
        // rest of the hole
        
        Point newPoint = Point.valueOf(event.x, event.y);
        int lastPointIndex = hole.getNumPoints()-1;
        if( hole.getNumPoints()>2 && EditUtils.instance.intersection(hole.getPoint(lastPointIndex), newPoint, hole, 0, lastPointIndex) ){
            return Messages.ValidHoleValidator_selfIntersection;
        }
        if( !shell.contains(newPoint, true) )
            return Messages.ValidHoleValidator_outsideShell;
        
        for( PrimitiveShape hole2 : shell.getEditGeom().getHoles() ) {
            if( hole!=hole2 && hole2.contains(newPoint, true) )
                return Messages.ValidHoleValidator_holeOverlap;
        }
        
        return null;
    }

}
