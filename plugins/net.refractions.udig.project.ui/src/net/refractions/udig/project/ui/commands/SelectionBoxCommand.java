/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.project.ui.commands;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;

import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.preferences.PreferenceConstants;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * A command that draws the indicated shape onto the viewport in the correct "selection" style.  The default Shape is 
 * a rectangle.
 * 
 * @author Jesse
 */
public class SelectionBoxCommand extends AbstractDrawCommand implements
		IDrawCommand {

	private Shape shape;
	
	
	/* (non-Javadoc)
	 * @see net.refractions.udig.project.ui.commands.IDrawCommand#getValidArea()
	 */
	public Rectangle getValidArea() {
		return shape.getBounds();
	}

	/* (non-Javadoc)
	 * @see net.refractions.udig.project.command.Command#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws Exception {
		if( shape!=null ){
			graphics.setColor(getSelectionColor(75));
			graphics.fill(shape);
			graphics.setColor(getSelectionColor2(75));
			graphics.setStroke(ViewportGraphics.LINE_SOLID, 3);
			graphics.draw(shape);
			graphics.setStroke(ViewportGraphics.LINE_SOLID, 1);
			graphics.setColor(getSelectionColor(255));
			graphics.draw(shape);
		}
	}

	private Color getSelectionColor(int alpha) {
		IPreferenceStore store = ProjectPlugin.getPlugin().getPreferenceStore();
		String name = PreferenceConstants.P_SELECTION_COLOR;
		RGB rgb = PreferenceConverter.getColor(store, name );
		return new Color( rgb.red, rgb.green, rgb.blue, alpha);
	}

	private Color getSelectionColor2(int alpha) {
		IPreferenceStore store = ProjectPlugin.getPlugin().getPreferenceStore();
		String name = PreferenceConstants.P_SELECTION2_COLOR;
		RGB rgb = PreferenceConverter.getColor(store, name );
		return new Color( rgb.red, rgb.green, rgb.blue, alpha);
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public Shape getShape() {
		return shape;
	}

}
