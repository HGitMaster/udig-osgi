/**
 * <copyright>
 * </copyright>
 *
 * $Id: PicoBlackboardImpl.java 27775 2007-11-07 05:05:40Z jeichar $
 */
package net.refractions.udig.project.internal.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.refractions.udig.project.BlackboardEvent;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IBlackboardListener;
import net.refractions.udig.project.internal.PicoBlackboard;
import net.refractions.udig.project.internal.ProjectPackage;
import net.refractions.udig.project.internal.ProjectPlugin;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * TODO Purpose of net.refractions.udig.project.internal.impl
 * <p>
 * </p>
 * 
 * @author Jesse
 * @since 1.0.0
 * @generated
 * @deprecated
 */
public class PicoBlackboardImpl extends EObjectImpl implements PicoBlackboard {
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    public static final String copyright = "uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details."; //$NON-NLS-1$

    /**
     * The default value of the '{@link #getPicoContainer() <em>Pico Container</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getPicoContainer()
     * @generated NOT
     * @ordered
     */
    protected static final MutablePicoContainer PICO_CONTAINER_EDEFAULT = new DefaultPicoContainer();

    /**
     * The cached value of the '{@link #getPicoContainer() <em>Pico Container</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see #getPicoContainer()
     * @generated
     * @ordered
     */
    protected MutablePicoContainer picoContainer = PICO_CONTAINER_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected PicoBlackboardImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return ProjectPackage.eINSTANCE.getPicoBlackboard();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public MutablePicoContainer getPicoContainer() {
        return picoContainer;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public Object eGet( EStructuralFeature eFeature, boolean resolve ) {
        switch( eDerivedStructuralFeatureID(eFeature) ) {
        case ProjectPackage.PICO_BLACKBOARD__PICO_CONTAINER:
            return getPicoContainer();
        }
        return eDynamicGet(eFeature, resolve);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public boolean eIsSet( EStructuralFeature eFeature ) {
        switch( eDerivedStructuralFeatureID(eFeature) ) {
        case ProjectPackage.PICO_BLACKBOARD__PICO_CONTAINER:
            return PICO_CONTAINER_EDEFAULT == null
                    ? picoContainer != null
                    : !PICO_CONTAINER_EDEFAULT.equals(picoContainer);
        }
        return eDynamicIsSet(eFeature);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public String toString() {
        if (eIsProxy())
            return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (picoContainer: "); //$NON-NLS-1$
        result.append(picoContainer);
        result.append(')');
        return result.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#contains(java.lang.String)
     */
    public boolean contains( String key ) {
        return picoContainer.getComponentInstance(key) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#get(java.lang.String)
     */
    public Object get( String key ) {
        return picoContainer.getComponentInstance(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#put(java.lang.String, java.lang.Object)
     */
    public void put( String key, Object value ) {
        
        ComponentAdapter oldValue = picoContainer.registerComponentInstance(key, value);
        
        BlackboardEvent event=new BlackboardEvent(this, key, oldValue.getComponentInstance(picoContainer), value);
        for( IBlackboardListener l : listeners ) {
            try{
                l.blackBoardChanged(event);
            } catch (Exception e) {
                ProjectPlugin.log("", e); //$NON-NLS-1$
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#getFloat(java.lang.String)
     */
    public Float getFloat( String key ) {
        Object o = picoContainer.getComponentInstance(key);
        if (o != null && o instanceof Float)
            return (Float) o;

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#getInteger(java.lang.String)
     */
    public Integer getInteger( String key ) {
        Object o = picoContainer.getComponentInstance(key);
        if (o != null && o instanceof Integer)
            return (Integer) o;

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#getString(java.lang.String)
     */
    public String getString( String key ) {
        Object o = picoContainer.getComponentInstance(key);
        if (o != null && o instanceof String)
            return (String) o;

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#putFloat(java.lang.String, float)
     */
    public void putFloat( String key, float value ) {
        put(key, new Float(value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#putInteger(java.lang.String, int)
     */
    public void putInteger( String key, int value ) {
        put(key, value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.refractions.udig.project.IBlackboard#putString(java.lang.String, java.lang.String)
     */
    public void putString( String key, String value ) {
        put(key, value);
    }

    CopyOnWriteArraySet<IBlackboardListener> listeners=new CopyOnWriteArraySet<IBlackboardListener>(); 
    public boolean addListener( IBlackboardListener listener ) {
        return listeners.add(listener);
    }
    

    public boolean removeListener( IBlackboardListener listener ) {
        return listeners.remove(listener);
    }

    public void clear() {
        // TODO: not sure if this is the correct way to clear, dispose is a
        // lifecycle call
        picoContainer.dispose();
        picoContainer = new DefaultPicoContainer();

        for( IBlackboardListener l : listeners ) {
            try{
                l.blackBoardCleared(this);
            } catch (Exception e) {
                ProjectPlugin.log("", e); //$NON-NLS-1$
            }
        }
    }

    public void flush() {
        // do nothing, does not support persistance
    }

    public void addAll( IBlackboard blackboard ) {
    }

    public Set<String> keySet() {
        return null;
    }

} // PicoBlackboardImpl
