/**
 * <copyright>
 * </copyright>
 *
 * $Id: PicoBlackboardItemProvider.java 24145 2007-02-01 18:03:34Z jeichar $
 */
package net.refractions.udig.project.internal.provider;

import java.util.List;

import net.refractions.udig.project.internal.PicoBlackboard;
import net.refractions.udig.project.internal.ProjectPackage;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.picocontainer.MutablePicoContainer;

/**
 * This is the item provider adapter for a
 * {@link net.refractions.udig.project.internal.PicoBlackboard} object. <!-- begin-user-doc --> <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class PicoBlackboardItemProvider extends ItemProviderAdapter
        implements
            IEditingDomainItemProvider,
            IStructuredItemContentProvider,
            ITreeItemContentProvider,
            IItemLabelProvider,
            IItemPropertySource {
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final String copyright = "uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details."; //$NON-NLS-1$

    /**
     * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public PicoBlackboardItemProvider( AdapterFactory adapterFactory ) {
        super(adapterFactory);
    }

    /**
     * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public List getPropertyDescriptors( Object object ) {
        if (itemPropertyDescriptors == null) {
            super.getPropertyDescriptors(object);

            addPicoContainerPropertyDescriptor(object);
        }
        return itemPropertyDescriptors;
    }

    /**
     * This adds a property descriptor for the Pico Container feature. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected void addPicoContainerPropertyDescriptor( Object object ) {
        itemPropertyDescriptors
                .add(createItemPropertyDescriptor(
                        ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                        getResourceLocator(),
                        getString("_UI_PicoBlackboard_picoContainer_feature"), //$NON-NLS-1$
                        getString(
                                "_UI_PropertyDescriptor_description", "_UI_PicoBlackboard_picoContainer_feature", "_UI_PicoBlackboard_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        ProjectPackage.eINSTANCE.getPicoBlackboard_PicoContainer(), false,
                        ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This returns PicoBlackboard.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public Object getImage( Object object ) {
        return getResourceLocator().getImage("full/obj16/PicoBlackboard"); //$NON-NLS-1$
    }

    /**
     * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated NOT
     */
    public String getText( Object object ) {
        MutablePicoContainer labelValue = ((PicoBlackboard) object).getPicoContainer();
        String label = labelValue == null ? null : labelValue.toString();
        return label == null || label.length() == 0 ? "PicoBlackboard" : 
               label;
    }

    /**
     * This handles model notifications by calling {@link #updateChildren} to update any cached
     * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void notifyChanged( Notification notification ) {
        updateChildren(notification);

        switch( notification.getFeatureID(PicoBlackboard.class) ) {
        case ProjectPackage.PICO_BLACKBOARD__PICO_CONTAINER:
            fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(),
                    false, true));
            return;
        }
        super.notifyChanged(notification);
    }

    /**
     * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public ResourceLocator getResourceLocator() {
        return ProjectEditPlugin.INSTANCE;
    }

}
