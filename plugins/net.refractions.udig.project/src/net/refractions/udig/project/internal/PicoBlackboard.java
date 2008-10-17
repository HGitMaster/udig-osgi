package net.refractions.udig.project.internal;

import net.refractions.udig.project.IBlackboard;

import org.eclipse.emf.ecore.EObject;
import org.picocontainer.MutablePicoContainer;

/**
 * A blackboard backed by pico container.
 * 
 * @author Justin Deoliveira,Refractions Reasearch Inc,jdeolive@refractions.net
 * @deprecated
 */
public interface PicoBlackboard extends EObject, IBlackboard {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    String copyright = "uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details."; //$NON-NLS-1$

    /**
     * @return the underlying pico conainer.
     * @model changeable="false"
     */
    MutablePicoContainer getPicoContainer();

}
