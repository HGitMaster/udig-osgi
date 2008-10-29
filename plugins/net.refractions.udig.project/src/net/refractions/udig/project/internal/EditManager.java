/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.project.internal;

import java.io.IOException;

import net.refractions.udig.project.IEditManager;

import org.eclipse.emf.ecore.EObject;
import org.geotools.data.Transaction;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

/**
 * TODO Purpose of net.refractions.udig.project.internal
 * <p>
 * </p>
 * 
 * @author Jesse
 * @since 1.0.0
 * @model
 */
public interface EditManager extends EObject, IEditManager {
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    String copyright = "uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details."; //$NON-NLS-1$

    /**
     * returns the map this LayerManager is associated with
     * 
     * @return the map this LayerManager is associated with
     * @model opposite="editManagerInternal" many="false"
     */
    public Map getMapInternal();

    /**
     * Sets the value of the '{@link net.refractions.udig.project.internal.EditManager#getMapInternal <em>Map Internal</em>}' container reference.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @param value the new value of the '<em>Map Internal</em>' container reference.
     * @see #getMapInternal()
     * @generated
     */
    void setMapInternal( Map value );

    /**
     * Gets the SimpleFeature that that is currently being edited.
     * 
     * @return the SimpleFeature that that is currently being edited.
     * @model changeable="false" transient="true"
     */
    public SimpleFeature getEditFeature();

    /**
     * Sets the value of the '{@link net.refractions.udig.project.internal.EditManager#getEditFeature <em>Edit SimpleFeature</em>}'
     * attribute. The Layer indicates which Layer the feature is part of. If the layer is
     * isEditLayerLocked() returns true then an exception will be thrown if the value of layer is
     * not null or equal to the current editlayer.
     * 
     * @param value the new value of the '<em>Edit SimpleFeature</em>' attribute.
     * @param layer A layer that the feature is part of.
     * @see #getEditFeature()
     * @model
     */
    void setEditFeature( SimpleFeature value, Layer layer ) throws IllegalArgumentException;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @model
     * @generated
     */
    void startTransaction();

    /**
     * Commits the transaction if possible otherwise rollsback the transaction.
     * 
     * @throws IOException throws IoException if there is a problem committing.
     * @model
     */
    public void commitTransaction() throws IOException;

    /**
     * Rollsback the current transaction.
     * 
     * @throws IOException
     * @model
     */
    public void rollbackTransaction() throws IOException;

    /**
     * Returns a layer that contains the edit feature in its feature store.
     * 
     * @return a layer that contains the edit feature in its feature store.
     * @model changeable="false" transient="true" resolveProxies="false"
     */
    public Layer getEditLayerInternal();

    /**
     * Returns the value of the '<em><b>Transaction Type</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Transaction Type</em>' attribute isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Transaction Type</em>' attribute.
     * @see net.refractions.udig.project.internal.ProjectPackage#getEditManager_TransactionType()
     * @model transient="true" changeable="false" volatile="true"
     * @generated
     */
    Class getTransactionType();

    /**
     * Adds a feature to the layer and sets the current edit feature to be the newly added feature.
     * The layer becomes the new Edit layer.
     * 
     * @param feature the feature to be added.
     * @throws IOException
     * @throws IllegalAttributeException
     * @throws IllegalAttributeException
     */
    public void addFeature( SimpleFeature feature, Layer layer ) throws IOException,
            IllegalAttributeException, IllegalAttributeException;

    /**
     * Reobtains the edit feature from the datastore to ensure that the currently stored feature
     * matches the datastore's copy.
     */
    public void refreshEditFeature();

    /**
     * Returns the currently selected Layer
     * 
     * @return the currently selected Layer
     * @model
     */
    public Layer getSelectedLayer();

    /**
     * Sets the value of the '{@link net.refractions.udig.project.internal.EditManager#getSelectedLayer <em>Selected Layer</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Selected Layer</em>' reference.
     * @see #getSelectedLayer()
     * @generated
     */
    void setSelectedLayer( Layer value );

    /**
     * Indicates whether the editlayer can be changed.
     * 
     * @return true if the current editlayer is locked and cannot be changed.
     * @model
     */
    public boolean isEditLayerLocked();

    /**
     * Sets the value of the '{@link net.refractions.udig.project.internal.EditManager#isEditLayerLocked <em>Edit Layer Locked</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @param value the new value of the '<em>Edit Layer Locked</em>' attribute.
     * @see #isEditLayerLocked()
     * @generated
     */
    void setEditLayerLocked( boolean value );

    /**
     * Gets the Map's transaction object.
     */
    public Transaction getTransaction();

}
