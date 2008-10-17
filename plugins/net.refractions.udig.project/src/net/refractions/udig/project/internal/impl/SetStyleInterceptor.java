/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.project.internal.impl;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.core.internal.ExtensionPointProcessor;
import net.refractions.udig.core.internal.ExtensionPointUtil;
import net.refractions.udig.project.StyleContent;
import net.refractions.udig.project.interceptor.LayerInterceptor;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.ProjectPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

/**
 * Sets the default style of the 
 * @author Jesse
 * @since 1.1.0
 */
public class SetStyleInterceptor implements LayerInterceptor {

    public void run( Layer layer ) {
        if( layer.getStyleBlackboard().getContent().isEmpty()){
            ExtensionPointProcessor scp = createDefaultStyles(layer.getGeoResource(), layer);
            ExtensionPointUtil.process(ProjectPlugin.getPlugin(), StyleContent.XPID, scp);
        }
    }

    /**
     * Creates a default style from each styleContent extension and puts them on the style blackboard of the layer.
     * @param theResource
     * @param theLayer
     * @return
     */
    private ExtensionPointProcessor createDefaultStyles( final IGeoResource theResource, final Layer theLayer ) {
        ExtensionPointProcessor scp = new ExtensionPointProcessor(){
            public void process( IExtension extension, IConfigurationElement element )
                    throws Exception {
                StyleContent styleContent = (StyleContent) element
                        .createExecutableExtension("class"); //$NON-NLS-1$
                Object style = styleContent.createDefaultStyle(theResource, theLayer.getDefaultColor(), null);

                if (style != null ) {
                    theLayer.getStyleBlackboard().put(styleContent.getId(), style);
                }
            }
        };
        return scp;
    }

}
