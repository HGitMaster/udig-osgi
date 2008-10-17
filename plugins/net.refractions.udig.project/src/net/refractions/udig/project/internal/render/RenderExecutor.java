/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.project.internal.render;

import net.refractions.udig.project.render.RenderException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Each renderer has an executor that runs the renderer in a separate thread. 
 * There are currently 3 implementations. 
 * One for each type of renderer. (Composite/MultiLayer/Renderer). 
 * 
 * The CompositeRendererExecutor provides the incremental update functionality.
 * 
 * 
 * @author Jesse
 * @since 1.0.0
 * @model
 */
public interface RenderExecutor extends Renderer {
    /** The Extension point id declaring available RenderExecutors */
    String EXTENSION_ID = "net.refractions.udig.project.renderExecutor"; //$NON-NLS-1$

    /** The name of the RenderExecutor class attribute in the Extension point */
    String EXECUTOR_ATTR = "executorClass"; //$NON-NLS-1$

    /** The name of the Renderer class attribute in the Extension point */
    String RENDERER_ATTR = "rendererClass"; //$NON-NLS-1$

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String copyright = "uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details."; //$NON-NLS-1$

    /**
     * Returns the value of the '<em><b>Renderer</b></em>' reference. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Renderer</em>' reference isn't clear, there really should be
     * more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Renderer</em>' reference.
     * @see #setRenderer(Renderer)
     * @see net.refractions.udig.project.internal.render.RenderPackage#getRenderExecutor_Renderer()
     * @model resolveProxies="false" required="true" transient="true"
     * @generated
     */
    Renderer getRenderer();

    /**
     * Sets the value of the '{@link net.refractions.udig.project.internal.render.RenderExecutor#getRenderer <em>Renderer</em>}'
     * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Renderer</em>' reference.
     * @see #getRenderer()
     * @generated
     */
    void setRenderer( Renderer value );

    /**
     * Method calls visitor.visit().
     * 
     * @param visitor the visitor object
     */
    void visit( ExecutorVisitor visitor );

    /**
     * This method does not use the monitor parameter. It is the same as calling render(bounds);
     * 
     * @see net.refractions.udig.project.internal.render.Renderer#render(com.vividsolutions.jts.geom.Envelope,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void render(IProgressMonitor monitor ) throws RenderException;

    /**
     * @see net.refractions.udig.project.internal.render.Renderer#render(com.vividsolutions.jts.geom.Envelope,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void render( ) throws RenderException;

    /**
     * This method is called when the rendering is interrupted. If the rendering has to restart or
     * must stop. The dispose method called then the rendering thread is interrupted. Because the
     * dispose method is called while the rendering thread is still running it <b>MUST BE
     * THREADSAFE!!!! </b>
     */
    public void stopRendering();

}
