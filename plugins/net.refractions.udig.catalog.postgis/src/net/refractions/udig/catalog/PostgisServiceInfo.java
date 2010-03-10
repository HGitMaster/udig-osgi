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
package net.refractions.udig.catalog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The service info for the PostgisService
 * 
 * @author jesse
 * @since 1.1.0
 */
public class PostgisServiceInfo extends IServiceInfo {

    private PostgisService2 service;
    public PostgisServiceInfo( PostgisService2 service ) throws IOException {
        this.service = service;
        List<String> tmpKeywords = new ArrayList<String>();
        tmpKeywords.add("Postgis"); //$NON-NLS-1$
        
        List<PostgisGeoResource2> resources = service.resources(new NullProgressMonitor());
        for( PostgisGeoResource2 postgisGeoResource2 : resources ) {
            tmpKeywords.add(postgisGeoResource2.typename);
        }
        keywords = tmpKeywords.toArray(new String[0]);
        
        try {
            schema = new URI("jdbc://postgis/gml"); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            PostgisPlugin.log(null, e);
        }
        
        icon = AbstractUIPlugin.imageDescriptorFromPlugin(PostgisPlugin.ID,
            "icons/obj16/postgis_16.gif"); //$NON-NLS-1$
    }

    public String getDescription() {
        return service.getIdentifier().toString();
    }

    public URI getSource() {
        try {
            return service.getIdentifier().toURI();
        } catch (URISyntaxException e) {
            // This would be bad 
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }

    public String getTitle() {
        URL id = service.getIdentifier();
		return ("PostGIS " +id.getHost()+ "/" +id.getPath()).replaceAll("//","/"); //$NON-NLS-1$
    }

}
