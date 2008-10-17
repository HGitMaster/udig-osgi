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
package net.refractions.udig.project.ui.tool;

/**
 * The general interface for tools interacting with the Map Editor.
 * <p>
 * Must have a public default constructor so that the plug-in frame work can instantiate the class.
 * </p>
 * 
 * @see net.refractions.udig.project.ui.tool.AbstractTool
 * @author Jesse Eichar
 * @version $Revision: 1.9 $
 */
public interface Tool {

    /** Tool category for tools that do not modify. Examples are Pan and Zoom */
    public final static String VIEW = "view"; //$NON-NLS-1$
    /** Tool category for tools that modify. Examples are Add Vertex and Add SimpleFeature */
    public final static String EDIT = "edit"; //$NON-NLS-1$
    /** The extension point id for tools */
    public static final String EXTENSION_ID = "net.refractions.udig.project.ui.tool"; //$NON-NLS-1$

    /**
     * Releases resource, Cursor and image resources possibly.
     */
    public void dispose();

    /**
     * Called each time an eclipse editor is activated. The RenderManager and ViewportPane are those
     * that are associated with the newly actived Eclipse view. Intended to be used if something
     * other just changing the current state happens. if false the tool is set as inactive and
     * deregistered with the component.
     * 
     * @param tools The tools that the tool can use in its operations
     * @see IToolContext
     */
    public void setContext( IToolContext tools );

    /**
     * Returns the AbstractContext that a tool can use in its operations.
     * 
     * @return the AbstractContext that a tool can use in its operations.
     * @see IToolContext
     */
    public IToolContext getContext();
    
    
    /**
     * Returns the property of the particular tool implementation. 
     * <p>
     * 
     * @param key the property key.
     * @return
     */
    public Object getProperty(String key);
    
    
    /**
     * Sets the tool's property value by key.
     * 
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value);

    
    /**
     * Returns enablement statement of the tool.
     * 
     * @return
     */
    public boolean isEnabled();
    
    /**
     * Sets enablement of the tool.
     * 
     * @param enable
     */
    public void setEnabled(boolean enable);
    
    /**
     * Adds listener of tool's lifecycle.
     * 
     * @param listener
     */
    public void addListener(ToolLifecycleListener listener);
    
    /**
     * Removes a listener of tool's lifecycle.
     * 
     * @param listener
     */
    public void removeListener(ToolLifecycleListener listener);
    
    
}
