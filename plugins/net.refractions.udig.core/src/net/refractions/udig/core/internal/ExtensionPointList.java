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
package net.refractions.udig.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

/**
 * Process an extention point.
 * 
 * @author jeichar
 * @since 0.6.0
 */
public class ExtensionPointList extends ArrayList<IConfigurationElement> {

    /** <code>serialVersionUID</code> field */
    private static final long serialVersionUID = ExtensionPointList.class.hashCode();
    /**
     * Construct <code>ExtensionPointIterator</code>.
     */
    private ExtensionPointList( String xpid ) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(xpid);
        if (extensionPoint == null)
            return;

        // For each extension ...
        for( IExtension extension : extensionPoint.getExtensions() ) {
            try {
                addAll(Arrays.asList(extension.getConfigurationElements()));
            } catch (Exception e) {
                Bundle bundle = Platform.getBundle(extensionPoint.getNamespaceIdentifier());
                if (bundle == null) {
                    if (CorePlugin.getDefault().isDebugging()) {
                        System.out.println("Could not locate bundle for " + extensionPoint.getUniqueIdentifier()); //$NON-NLS-1$
                    }
                    bundle = CorePlugin.getDefault().getBundle();
                }
                ILog log = Platform.getLog(bundle);
                log.log(new Status(Status.ERROR, extension.getNamespaceIdentifier(), 0, extensionPoint
                        .getUniqueIdentifier()
                        + Messages.ExtensionPointList_problem + e, e) 
                        );
            }
        }
    }
    
    private static Map<String, List<IConfigurationElement>> cache=new WeakHashMap<String, List<IConfigurationElement>>(); 
    
    /**
     * Gets a ExtensionPointList for the provided extensionPoint
     *
     * @param extensionPointId id of the extension point to get the list of extensions for.
     * @return 
     */
    public static List<IConfigurationElement> getExtensionPointList( String extensionPointId ) {
       List<IConfigurationElement> list= cache.get(extensionPointId);
       if( list==null ){
           list=new ExtensionPointList(extensionPointId);
           cache.put(extensionPointId, list);
       }
       
       list=new ArrayList<IConfigurationElement>(list);
       return list;
    }
}