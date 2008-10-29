/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.project.element;

import net.refractions.udig.project.IProjectElement;
import net.refractions.udig.project.internal.ProjectElement;

import org.eclipse.ui.IMemento;

/**
 * Interface to objects that can be added to a project.  
 * 
 * The complete and most flexible method for creating a {@link IProjectElement} is to use EMF and 
 * extend the {@link ProjectElement} class.  However that is often more work than an extender wishes
 * to do so the IGenericProjectElement provides a simpler way of creating project elements and 
 * 
 * @author jesse
 */
public interface IGenericProjectElement {

    /**
     * Called when an {@link IGenericProjectElement} is by uDig after being persisted this has the
     * information.
     * 
     * @param memento the memento with the persistence information provided by save
     */
    public void init( IMemento memento );

    /**
     * Called when the element is to be persisted by the uDig framework
     * 
     * @param memento the persistence data must be written to the memento.
     */
    public void save( IMemento memento );

    /**
     * Returns the id of the extension that is associated with this element type
     *
     * @return the id of the extension that is associated with this element type
     */
    public String getExtensionId();
    
    /**
     * Sets the id
     */
    public void setExtensionId(String extId);
}
