/**
 * <copyright>
 * </copyright>
 *
 * $Id: ProjectRegistryItemProvider.java 24145 2007-02-01 18:03:34Z jeichar $
 */
package net.refractions.udig.project.internal.provider;

import java.util.Collection;
import java.util.List;

import net.refractions.udig.project.internal.ProjectPackage;
import net.refractions.udig.project.internal.ProjectRegistry;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a
 * {@link net.refractions.udig.project.internal.ProjectRegistry} object. <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * 
 * @generated
 */
public class ProjectRegistryItemProvider extends ItemProviderAdapter
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
    public ProjectRegistryItemProvider( AdapterFactory adapterFactory ) {
        super(adapterFactory);
    }

    /**
     * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated NOT
     */
    public List getPropertyDescriptors( Object object ) {
        if (itemPropertyDescriptors == null) {
            super.getPropertyDescriptors(object);

            // addProjectsPropertyDescriptor(object);
            addCurrentProjectPropertyDescriptor(object);
        }
        return itemPropertyDescriptors;
    }

    public Collection getChildrenFeatures( Object object ) {
        if (childrenFeatures == null) {
            super.getChildrenFeatures(object);
            childrenFeatures.add(ProjectPackage.eINSTANCE.getProjectRegistry_Projects());
        }
        return childrenFeatures;
    }

    /**
     * @see org.eclipse.emf.edit.provider.ITreeItemContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren( Object object ) {
        return true;
    }

    /**
     * This adds a property descriptor for the Projects feature. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected void addProjectsPropertyDescriptor( Object object ) {
        itemPropertyDescriptors
                .add(createItemPropertyDescriptor(
                        ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                        getResourceLocator(),
                        getString("_UI_ProjectRegistry_projects_feature"), //$NON-NLS-1$
                        getString(
                                "_UI_PropertyDescriptor_description", "_UI_ProjectRegistry_projects_feature", "_UI_ProjectRegistry_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        ProjectPackage.eINSTANCE.getProjectRegistry_Projects(), true, null, null,
                        null));
    }

    /**
     * This adds a property descriptor for the Current Project feature. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected void addCurrentProjectPropertyDescriptor( Object object ) {
        itemPropertyDescriptors
                .add(createItemPropertyDescriptor(
                        ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                        getResourceLocator(),
                        getString("_UI_ProjectRegistry_currentProject_feature"), //$NON-NLS-1$
                        getString(
                                "_UI_PropertyDescriptor_description", "_UI_ProjectRegistry_currentProject_feature", "_UI_ProjectRegistry_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        ProjectPackage.eINSTANCE.getProjectRegistry_CurrentProject(), true, null,
                        null, null));
    }

    /**
     * This returns ProjectRegistry.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public Object getImage( Object object ) {
        return getResourceLocator().getImage("full/obj16/ProjectRegistry"); //$NON-NLS-1$
    }

    /**
     * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated NOT
     */
    public String getText( Object object ) {
        return "ProjectRegistry";
    }

    /**
     * This handles model notifications by calling {@link #updateChildren} to update any cached
     * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    public void notifyChanged( Notification notification ) {
        updateChildren(notification);

        switch( notification.getFeatureID(ProjectRegistry.class) ) {
        case ProjectPackage.PROJECT_REGISTRY__PROJECTS:
            fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(),
                    true, true));
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
