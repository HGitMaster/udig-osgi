/**
 * <copyright></copyright> $Id: RenderAdapterFactory.java 31424 2009-08-07 11:12:23Z aantonello $
 */
package net.refractions.udig.project.internal.render.util;

import net.refractions.udig.project.IAbstractContext;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.AbstractContext;
import net.refractions.udig.project.internal.render.CompositeRenderContext;
import net.refractions.udig.project.internal.render.MultiLayerRenderer;
import net.refractions.udig.project.internal.render.RenderContext;
import net.refractions.udig.project.internal.render.RenderExecutor;
import net.refractions.udig.project.internal.render.RenderManager;
import net.refractions.udig.project.internal.render.RenderPackage;
import net.refractions.udig.project.internal.render.Renderer;
import net.refractions.udig.project.internal.render.RendererCreator;
import net.refractions.udig.project.internal.render.ViewportModel;
import net.refractions.udig.project.render.ICompositeRenderContext;
import net.refractions.udig.project.render.IMultiLayerRenderer;
import net.refractions.udig.project.render.IRenderContext;
import net.refractions.udig.project.render.IRenderManager;
import net.refractions.udig.project.render.IRenderer;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.render.displayAdapter.IMapDisplayListener;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * TODO Purpose of net.refractions.udig.project.internal.render.util
 * <p>
 * </p>
 * 
 * @author Jesse
 * @since 1.0.0
 * @generated
 */
public class RenderAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    static RenderPackage modelPackage;

    /**
     * Creates an instance of the adapter factory.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public RenderAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = RenderPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object.
     * <!-- begin-user-doc
     * --> This implementation returns <code>true</code> if the object is either the model's
     * package or is an instance object of the model. <!-- end-user-doc -->
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    @Override
    public boolean isFactoryForType( Object object ) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject) object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch that delegates to the <code>createXXX</code> methods.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected RenderSwitch<Adapter> modelSwitch = new RenderSwitch<Adapter>(){
        @Override
        public Adapter caseIRenderManager( IRenderManager object ) {
            return createIRenderManagerAdapter();
        }
        @Override
        public Adapter caseIViewportModel( IViewportModel object ) {
            return createIViewportModelAdapter();
        }
        @Override
        public Adapter caseIMultiLayerRenderer( IMultiLayerRenderer object ) {
            return createIMultiLayerRendererAdapter();
        }
        @Override
        public Adapter caseIRenderer( IRenderer object ) {
            return createIRendererAdapter();
        }
        @Override
        public Adapter caseILayer( ILayer object ) {
            return createILayerAdapter();
        }
        @Override
        public Adapter caseMultiLayerRenderer( MultiLayerRenderer object ) {
            return createMultiLayerRendererAdapter();
        }
        @Override
        public Adapter caseRenderExecutor( RenderExecutor object ) {
            return createRenderExecutorAdapter();
        }
        @Override
        public Adapter caseRenderManager( RenderManager object ) {
            return createRenderManagerAdapter();
        }
        @Override
        public Adapter caseViewportModel( ViewportModel object ) {
            return createViewportModelAdapter();
        }
        @Override
        public Adapter caseComparable( Comparable object ) {
            return createComparableAdapter();
        }
        @Override
        public Adapter caseRenderer( Renderer object ) {
            return createRendererAdapter();
        }
        @Override
        public Adapter caseIMapDisplayListener( IMapDisplayListener object ) {
            return createIMapDisplayListenerAdapter();
        }
        @Override
        public Adapter caseIRenderContext( IRenderContext object ) {
            return createIRenderContextAdapter();
        }
        @Override
        public Adapter defaultCase( EObject object ) {
            return createEObjectAdapter();
        }
    };

    /**
     * Creates an adapter for the <code>target</code>. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param target the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    @Override
    public Adapter createAdapter( Notifier target ) {
        return modelSwitch.doSwitch((EObject) target);
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.render.IRenderManager <em>IRender Manager</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.render.IRenderManager
     * @generated
     */
    public Adapter createIRenderManagerAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.render.IViewportModel <em>IViewport Model</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.render.IViewportModel
     * @generated
     */
    public Adapter createIViewportModelAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.render.IMultiLayerRenderer <em>IMulti Layer Renderer</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.render.IMultiLayerRenderer
     * @generated
     */
    public Adapter createIMultiLayerRendererAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.render.IRenderContext <em>IRender Context</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.render.IRenderContext
     * @generated
     */
    public Adapter createIRenderContextAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.render.IRenderer <em>IRenderer</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.render.IRenderer
     * @generated
     */
    public Adapter createIRendererAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.ILayer <em>ILayer</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.ILayer
     * @generated
     */
    public Adapter createILayerAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link net.refractions.udig.project.render.RenderManager <em>Manager</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * 
     * @return the new adapter.
     * @see net.refractions.udig.project.internal.render.RenderManager
     * @generated
     */
    public Adapter createRenderManagerAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.internal.render.Renderer <em>Renderer</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.internal.render.Renderer
     * @generated
     */
    public Adapter createRendererAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link net.refractions.udig.project.render.ViewportModel <em>Viewport Model</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * 
     * @return the new adapter.
     * @see net.refractions.udig.project.internal.render.ViewportModel
     * @generated
     */
    public Adapter createViewportModelAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link net.refractions.udig.project.render.RenderExecutor <em>Executor</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * 
     * @return the new adapter.
     * @see net.refractions.udig.project.internal.render.RenderExecutor
     * @generated
     */
    public Adapter createRenderExecutorAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link Comparable <em>Comparable</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * 
     * @return the new adapter.
     * @see Comparable
     * @generated
     */
    public Adapter createComparableAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.internal.render.MultiLayerRenderer <em>Multi Layer Renderer</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.internal.render.MultiLayerRenderer
     * @generated
     */
    public Adapter createMultiLayerRendererAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link net.refractions.udig.project.render.displayAdapter.IMapDisplayListener <em>IMap Display Listener</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     * @return the new adapter.
     * @see net.refractions.udig.project.render.displayAdapter.IMapDisplayListener
     * @generated
     */
    public Adapter createIMapDisplayListenerAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case.
     * <!-- begin-user-doc --> This default
     * implementation returns null. <!-- end-user-doc -->
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} // RenderAdapterFactory
