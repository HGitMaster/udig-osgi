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
package net.refractions.udig.catalog;

/**
 * Captures changes to the Catalog.
 * <p>
 * For those familiar with IResourceChangeEvent and IResourceDelta from eclipse development there is
 * one <b>important addition</b>. The constant REPLACE indicates a reaname, or substiution, you
 * will need to replace any references you have to the oldObject with the newObject.
 * </p>
 * 
 * @author jgarnett
 * @since 0.6.0
 */
public interface IResolveChangeEvent {

    public enum Type {
        /**
         * Event type constant (bit mask) indicating an after-the-fact report of replacements,
         * creations, deletions, and modifications to one or more resources expressed as a
         * hierarchical resource delta as returned by <code>getDelta</code>.
         * 
         * @see #getType()
         * @see #getDelta()
         */
        POST_CHANGE,

        /**
         * Event type constant (bit mask) indicating a before-the-fact report of the impending
         * closure of a single service as returned by <code>getService</code>.
         * 
         * @see #getType()
         * @see #getService()
         */
        PRE_CLOSE,

        /**
         * Event type constant (bit mask) indicating a before-the-fact report of the impending
         * deletion of a single service, as returned by <code>getService</code>.
         * 
         * @see #getType()
         * @see #getService()
         */
        PRE_DELETE;
    }

    /**
     * Returns a delta, rooted at the catalog, describing the set of changes that happened to
     * resources in the workspace. Returns <code>null</code> if not applicable to this type of
     * event.
     * 
     * @return the resource delta, or <code>null</code> if not applicable
     */
    public IResolveDelta getDelta();

    /**
     * Returns the handle in question. Returns <code>null</code> if not applicable to this type of
     * event.
     * 
     * @return the resource, or <code>null</code> if not applicable
     */
    public IResolve getResolve();

    /**
     * Returns an object identifying the source of this event.
     * 
     * @return an object identifying the source of this event
     * @see java.util.EventObject
     */
    public Object getSource();

    /**
     * Returns the type of event being reported.
     * 
     * @return one of the event type constants
     * @see #POST_CHANGE
     * @see #PRE_CLOSE
     * @see #PRE_DELETE
     */
    public Type getType();
}