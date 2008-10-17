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

import java.util.List;

import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tool.edit.internal.Messages;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.behaviour.IEditValidator;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.EditGeom;
import net.refractions.udig.tools.edit.support.EditUtils;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

/**
 * Checks for:
 * 
 * <ul>
 * <li>If polygon:
 * <ul>
 * <li>Self Intersection in each part of the each geometry that is flagged as
 * changed</li>
 * <li>Intersection between holes</li>
 * <li>All holes are contained within shell</li>
 * </ul>
 * </li>
 * <li>If other shapes then anything goes</li>
 * </ul>
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class LegalShapeValidator implements IEditValidator {

	String holeOverlap = Messages.LegalShapeValidator_holeOverlap;
	String holeOutside = Messages.LegalShapeValidator_holeOutside;

	public String isValid(EditToolHandler handler, MapMouseEvent event,
			EventType type) {
		EditBlackboard editBlackboard = handler.getEditBlackboard(handler
				.getEditLayer());
		List<EditGeom> geoms = editBlackboard.getGeoms();
		for (EditGeom geom : geoms) {
			String message = test(geom);
			if (message != null) {
				return message;
			}
		}
		return null;
	}

	private String test(EditGeom geom) {
		String message = testSelfIntersection(geom);
		if (message != null)
			return message;
		message = testHoleIntersection(geom);
		if (message != null)
			return message;
		message = testShellContainsHoles(geom);
		if (message != null) {
			return message;
		}

		return message;
	}

	private String testSelfIntersection(EditGeom geom) {
		if (EditUtils.instance.selfIntersection(geom.getShell())) {
			return Messages.LegalShapeValidator_shellIntersection;
		}
		for (PrimitiveShape hole : geom.getHoles()) {
			if (EditUtils.instance.selfIntersection(hole)) {
				return Messages.LegalShapeValidator_holeIntersection;
			}
		}
		return null;
	}

	private String testHoleIntersection(EditGeom geom) {
		for (PrimitiveShape hole : geom.getHoles()) {
			for (PrimitiveShape hole2 : geom.getHoles()) {
				if (hole == hole2)
					continue;

				if (hole.overlap(hole2, true, false))
					return Messages.LegalShapeValidator_holeOverlap;
			}
		}
		return null;
	}

	private String testShellContainsHoles(EditGeom geom) {
		for (PrimitiveShape shape : geom.getHoles()) {
			for (Point point : shape) {
				if (!geom.getShell().contains(point, true, true))
					return Messages.LegalShapeValidator_holeOutside;
			}
		}
		return null;
	}

}
