/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004-2008, Refractions Research Inc.
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
package net.refractions.udig.tutorials.examples;

import java.io.File;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

/**
 * This example code shows the use of the Catalog plug-in.
 * <p>
 * For more information please visit the wiki
 * <a href="http://udig.refractions.net/confluence/display/DEV/2+Catalog">Catalog</a>
 * page.
 * 
 * @author Jody Garnett (Refractions Research, Inc.)
 * @since 1.1.0
 */
public class CatalogExample {

	/**
	 * 
	 */
	public CatalogExample() throws Exception {
		
		IStatus status = new Status(IStatus.ERROR, CatalogPlugin.ID, "error message");
		CatalogPlugin.getDefault().getLog().log( status );
		
		CatalogPlugin.getDefault().getBundle();		
		Bundle bundle = Platform.getBundle( CatalogPlugin.ID );
		
		File file = new File( "C:\\data\\cities.shp" );
		
		IServiceFactory serviceFactory = CatalogPlugin.getDefault().getServiceFactory();
		List<IService> created = serviceFactory.createService( file.toURL() );
				
	}

}
