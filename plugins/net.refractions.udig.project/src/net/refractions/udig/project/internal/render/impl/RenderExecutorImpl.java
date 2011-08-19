/**
 * <copyright></copyright> $Id$
 */
package net.refractions.udig.project.internal.render.impl;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.Messages;
import net.refractions.udig.project.internal.ProjectPackage;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.internal.impl.SynchronizedEList;
import net.refractions.udig.project.internal.render.ExecutorVisitor;
import net.refractions.udig.project.internal.render.RenderContext;
import net.refractions.udig.project.internal.render.RenderExecutor;
import net.refractions.udig.project.internal.render.RenderManager;
import net.refractions.udig.project.internal.render.RenderPackage;
import net.refractions.udig.project.internal.render.Renderer;
import net.refractions.udig.project.internal.render.SelectionLayer;
import net.refractions.udig.project.render.ILabelPainter;
import net.refractions.udig.project.render.IRenderContext;
import net.refractions.udig.project.render.IRenderer;
import net.refractions.udig.project.render.RenderException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Runs a renderer in its own thread. Is responsible for stopping the thread. Does not do composite
 * renderer or multilayer renderers.
 * 
 * @author Jesse
 * @since 1.0.0
 */
public class RenderExecutorImpl extends RendererImpl implements RenderExecutor {
    /**
     * Listens to a layer for visibility events and styling events. <b>Public ONLY for testing
     * purposes</b>
     * 
     * @author Jesse
     * @since 1.0.0
     */
    public static class LayerListener extends AdapterImpl {
        protected RenderExecutorImpl executor;

        LayerListener( RenderExecutorImpl executor ) {
            this.executor = executor;
        }

        /**
         * @see org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.common.notify.Notification)
         */
        public void notifyChanged( Notification msg ) {
            Layer layer = (Layer) msg.getNotifier();
            switch( msg.getFeatureID(Layer.class) ) {
            case ProjectPackage.LAYER__STYLE_BLACKBOARD:
                //dealt with by the layer listener created in the RenderManagerAdapters (added to the render manager)
                //therefore we don't need to deal with this here.
//                if (executor.getContext().getLayer() instanceof SelectionLayer)
//                    return;
//
//                styleBlackboardChanged(msg);
                break;
            case ProjectPackage.LAYER__VISIBLE:
                if (executor.getContext().getLayer() instanceof SelectionLayer)
                    return;
                if (msg.getNewBooleanValue())
                    layerVisible(layer, msg);
                else
                    layerNotVisible(layer, msg);
                break;

            default:
                break;
            }
        }

        protected void layerNotVisible( Layer layer, Notification msg ) {
            RenderContext context2 = executor.getContext();
            context2.getLabelPainter().disableLayer(context2.getLayer().getID().toString());
            if (executor.getState() == RENDERING) {
                executor.stopRendering();
                context2.setStatus(ILayer.DONE);
                context2.setStatusMessage(""); //$NON-NLS-1$
                executor.setState(NEVER);
            } else {
            	RenderManager renderManager = (RenderManager) layer.getMapInternal().getRenderManager();
            	if (renderManager != null) {
                    renderManager.refreshImage();
                }
            }
        }

        protected void layerVisible( Layer layer, Notification msg ) {
            RenderContext context2 = executor.getContext();
            context2.getLabelPainter().enableLayer(context2.getLayer().getID().toString());
            if (executor.getState() == IRenderer.RENDERING)
                return;
            if (executor.getState() != IRenderer.DONE || executor.dirty) {
                RenderManager renderManager = (RenderManager) layer.getMapInternal().getRenderManager();
                renderManager.refresh(layer, null); //ensures the entire layer and all tiles/selection layers are refreshed
                //executor.getRenderer().setState(RENDER_REQUEST);
            } else{
                RenderManager renderManager = (RenderManager) layer.getMapInternal().getRenderManager();
                if(renderManager != null){
                    renderManager.refreshImage();
                }
            }
        }

//        protected void styleBlackboardChanged( Notification msg ) {
//            executor.getContext().getRenderManager().refresh((ILayer) msg.getNotifier(), null);
//        }
    }
    /**
     * Calls the render() function when a RENDER_REQUEST goes by...
     * <p>
     * This listener will call the following for a RENDER_REQUEST in the following order:
     * <ul>
     * <li>executor.setRenderBounds
     * <li>executor.render()
     * <li>executor.setState
     * </ul>
     * Right now we assume it is listening to EVERTHING (or at least everything
     * in this Map).
     */
    protected static class RendererListener extends AdapterImpl {

        protected RenderExecutor executor;

        RendererListener( RenderExecutor executor ) {
            this.executor = executor;
        }

        /**
         * @see org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.common.notify.Notification)
         */
        public void notifyChanged( Notification msg ) {
            if (msg.getNotifier() instanceof Renderer) {
                if (msg.getFeatureID(Renderer.class) == RenderPackage.RENDERER__STATE){
                    if( msg.getNewIntValue()==IRenderer.RENDER_REQUEST){
                        executor.setRenderBounds(executor.getRenderer().getRenderBounds());
                    }
                    stateChanged(msg);
                }
            }

        }

        protected void stateChanged( Notification msg ) {
            executor.setState(msg.getNewIntValue());
            if( msg.getNewIntValue()==IRenderer.RENDER_REQUEST){
                try {
                    executor.render();
                } catch (RenderException e) {
                    // won't happen
                    throw (RuntimeException) new RuntimeException( ).initCause( e );
                }
            }
            

        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final String copyright = "uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details."; //$NON-NLS-1$

    protected static void clearImage( Envelope bounds2, RenderExecutor executor ) {
        if (bounds2 != null
                && !bounds2.contains(executor.getContext().getViewportModel().getBounds())) {
            Point min = executor.getContext().worldToPixel(
                    new Coordinate(bounds2.getMinX(), bounds2.getMinY()));
            Point max = executor.getContext().worldToPixel(
                    new Coordinate(bounds2.getMaxX(), bounds2.getMaxY()));
            int width = Math.abs(max.x - min.x);
            int height = Math.abs(max.y - min.y);
            Rectangle paintArea = new Rectangle(Math.min(min.x, max.x), Math.min(min.y, max.y),
                    width, height);
            executor.getContext().clearImage(paintArea);
        } else {
            executor.getContext().clearImage();
        }
    }


    /**
     * The cached value of the '{@link #getRenderer() <em>Renderer</em>}' reference. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getRenderer()
     * @generated
     * @ordered
     */
    protected Renderer renderer = null;

    protected Adapter renderListener = getRendererListener();

    /**
     * A listener that triggers an update when the style of its layer changes or the visibility
     * changes.
     */
    protected Adapter layerListener = getLayerListener();

    protected RenderJob renderJob;

    protected volatile boolean dirty;
    
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    public RenderExecutorImpl() {
        super();
        renderJob = new RenderJob(this);
    }
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected EClass eStaticClass() {
        return RenderPackage.eINSTANCE.getRenderExecutor();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    public void dispose() {
        while( getRenderer().eAdapters().remove(renderListener) );
        eAdapters().clear();
        removeLayerListener(getContext());
        stopRendering();
        getRenderer().dispose();
        
        try{
            //Clear label cache for this layer.
            ILayer renderingLayer = getContext().getLayer();
            if(renderingLayer != null){
                //Only if it as a usual layer's renderer
                ILabelPainter labelPainter = getContext().getLabelPainter();
                String layerId = renderingLayer.getID().toString();
                if ( getContext().getLayer() instanceof SelectionLayer )
                    layerId = layerId+"-Selection"; //$NON-NLS-1$
                labelPainter.clear(layerId);
            }
        }catch(Exception exc){
            exc.printStackTrace();
        }

        if (getRenderer().getState() != DISPOSED)
            getRenderer().setState(DISPOSED);
    }

    /**
     * @see net.refractions.udig.project.internal.render.impl.RendererImpl#setState(int)
     */
    public void setState( int newState ) {
        super.setState(newState);
    }


    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    public void stopRendering() {
        if(renderJob.cancel())
            return;
        final AtomicBoolean done=new AtomicBoolean(renderJob.cancel());
        IJobChangeListener listener=new JobChangeAdapter(){
            @Override
            public void done( IJobChangeEvent event ) {
                done.set(true);
                synchronized (done) {
                    done.notify();
                }

            }
        };
        renderJob.addJobChangeListener(listener);
        try{
            long start= System.currentTimeMillis();
        while( !done.get() && !renderJob.cancel() 
                && start+2000<System.currentTimeMillis() )
            synchronized (done) {
                try {
                    done.wait(200);
                } catch (InterruptedException e) {
                    throw (RuntimeException) new RuntimeException( ).initCause( e );
                }
            }
            if( !done.get() && !renderJob.cancel() ){
                ProjectPlugin.log("After 2 seconds unable to cancel "+getRenderer().getName()+" Renderer"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }finally{
            renderJob.removeJobChangeListener(listener);
        }
    
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @throws RenderException
     * @generated NOT
     */
    public void render( Graphics2D destination, IProgressMonitor monitor ) throws RenderException {

        if (getState() == DISPOSED)
            return;
        getRenderer().render(destination, validateMonitor(monitor));
    }

    private IProgressMonitor validateMonitor( IProgressMonitor monitor ) {
        if (monitor != null)
            return monitor;

        return new NullProgressMonitor();
    }

    /**
     * @see net.refractions.udig.project.internal.render.Renderer#getContext()
     */
    public RenderContext getContext() {
        if (getRenderer() == null)
            return null;
        return (RenderContext) getRenderer().getContext();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public Renderer getRenderer() {
        return renderer;
    }


    protected String getRenderJobName() {
        return MessageFormat.format(
                Messages.RenderExecutorImpl_message, new Object[]{getName()}); 
    }

    /**
     * @see net.refractions.udig.project.internal.render.RenderExecutor#setRenderer(net.refractions.udig.project.render.Renderer)
     * @uml.property name="renderer"
     */
    @SuppressWarnings("unchecked")
    public void setRenderer( Renderer newRenderer ) {
        if (getRenderer() != null) {
            getRenderer().eAdapters().remove(renderListener);
            removeLayerListener((RenderContext) getRenderer().getContext());
        }
        setRendererGen(newRenderer);
        // registerFeatureListener();
        if (newRenderer != null) {
            newRenderer.eAdapters().add(renderListener);
            addLayerListener((RenderContext) newRenderer.getContext());
        }
    }

    @SuppressWarnings("unchecked")
    protected void removeLayerListener( IRenderContext context ) {
        if (context.getLayer() != null) {
            Layer layer = ((Layer)context.getLayer());
            List<Adapter> adapters = layer.eAdapters();
            if( adapters instanceof SynchronizedEList ){
                ((SynchronizedEList)adapters).lock(); 
            }
            try{
                ArrayList<Adapter> toRemove = new ArrayList<Adapter>();
                for( Iterator<Adapter> iter=adapters.iterator(); iter.hasNext(); ) {
                    Adapter t = iter.next();
                    if (t instanceof RenderExecutorImpl.LayerListener){
//                        iter.remove();
                        //iter.remove() doesn't seem to work here; nothing gets removed.
                        toRemove.add(t);
                    }
                }
                adapters.removeAll(toRemove);
            }finally{
                if( adapters instanceof SynchronizedEList ){
                    ((SynchronizedEList)adapters).unlock(); 
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    protected void addLayerListener( IRenderContext context ) {

        if (context.getLayer() != null && !(context.getLayer() instanceof SelectionLayer)) {
            removeLayerListener(context);
            ((Layer)context.getLayer()).eAdapters().add(layerListener);
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setRendererGen( Renderer newRenderer ) {
        Renderer oldRenderer = renderer;
        renderer = newRenderer;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET,
                    RenderPackage.RENDER_EXECUTOR__RENDERER, oldRenderer, renderer));
    }
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public Object eGet( EStructuralFeature eFeature, boolean resolve ) {
        switch( eDerivedStructuralFeatureID(eFeature) ) {
        case RenderPackage.RENDER_EXECUTOR__STATE:
            return new Integer(getState());
        case RenderPackage.RENDER_EXECUTOR__NAME:
            return getName();
        case RenderPackage.RENDER_EXECUTOR__CONTEXT:
            return getContext();
        case RenderPackage.RENDER_EXECUTOR__RENDERER:
            return getRenderer();
        }
        return eDynamicGet(eFeature, resolve);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void eSet( EStructuralFeature eFeature, Object newValue ) {
        switch( eDerivedStructuralFeatureID(eFeature) ) {
        case RenderPackage.RENDER_EXECUTOR__STATE:
            setState(((Integer) newValue).intValue());
            return;
        case RenderPackage.RENDER_EXECUTOR__NAME:
            setName((String) newValue);
            return;
        case RenderPackage.RENDER_EXECUTOR__CONTEXT:
            setContext((IRenderContext) newValue);
            return;
        case RenderPackage.RENDER_EXECUTOR__RENDERER:
            setRenderer((Renderer) newValue);
            return;
        }
        eDynamicSet(eFeature, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */

    public void eUnset( EStructuralFeature eFeature ) {
        switch( eDerivedStructuralFeatureID(eFeature) ) {
        case RenderPackage.RENDER_EXECUTOR__STATE:
            setState(STATE_EDEFAULT);
            return;
        case RenderPackage.RENDER_EXECUTOR__NAME:
            setName(NAME_EDEFAULT);
            return;
        case RenderPackage.RENDER_EXECUTOR__CONTEXT:
            setContext((IRenderContext) null);
            return;
        case RenderPackage.RENDER_EXECUTOR__RENDERER:
            setRenderer((Renderer) null);
            return;
        }
        eDynamicUnset(eFeature);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public boolean eIsSet( EStructuralFeature eFeature ) {
        switch( eDerivedStructuralFeatureID(eFeature) ) {
        case RenderPackage.RENDER_EXECUTOR__STATE:
            return state != STATE_EDEFAULT;
        case RenderPackage.RENDER_EXECUTOR__NAME:
            return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
        case RenderPackage.RENDER_EXECUTOR__CONTEXT:
            return context != null;
        case RenderPackage.RENDER_EXECUTOR__RENDERER:
            return renderer != null;
        }
        return eDynamicIsSet(eFeature);
    }

    /**
     * @see net.refractions.udig.project.internal.render.impl.RendererImpl#getInfo(Point, Layer)
     *      public InfoList getInfo(Point screenLocation) throws IOException { if
     *      (!(getContext().getLayer() instanceof SelectionLayer)) return
     *      renderer.getInfo(screenLocation); return new InfoList(screenLocation.x,
     *      screenLocation.y, null); }
     */

    /**
     * @see net.refractions.udig.project.internal.render.RenderExecutor#visit(net.refractions.udig.project.render.ExecutorVisitor)
     */
    public void visit( ExecutorVisitor visitor ) {
        visitor.visit(this);
    }

    /**
     * @return Returns the layerListener.
     * @uml.property name="layerListener"
     */
    protected LayerListener getLayerListener() {
        return new LayerListener(this);
    }

    protected RendererListener getRendererListener() {
        return new RendererListener(this);
    }

    /**
     * @see net.refractions.udig.project.internal.render.Renderer#render(com.vividsolutions.jts.geom.Envelope)
     */
    public synchronized void render( ) {
        if (getState() == DISPOSED || !getRenderer().getContext().isVisible()){
            dirty = true;
            getContext().getLayer().setStatus(ILayer.DONE);
            if( getRenderer().getState()!=IRenderer.DONE)
                getRenderer().setState(IRenderer.DONE);
            return;
        }
        
        dirty = false;
        if( getRenderer().getState()!=STARTING){
            getRenderer().setState(IRenderer.STARTING);
        }
        renderJob.addRequest(getRenderBounds());
    }
    
    /**
     * @see net.refractions.udig.project.internal.render.impl.RendererImpl#render(com.vividsolutions.jts.geom.Envelope,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void render(IProgressMonitor monitor ) {
        render();
    }

    @Override
    public String toString() {
        String selection = ""; //$NON-NLS-1$
        if (getContext().getLayer() instanceof SelectionLayer)
            selection = "Selection "; //$NON-NLS-1$
        return getContext().getMap().getName()
                + ":" + selection + (getRenderer() != null ? getRenderer().getName() : "null"); //$NON-NLS-1$ //$NON-NLS-2$
    }

} // RenderExecutorImpl
