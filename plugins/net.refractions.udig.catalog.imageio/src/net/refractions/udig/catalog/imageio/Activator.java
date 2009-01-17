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
package net.refractions.udig.catalog.imageio;

import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Provides lifecycle management for the ImageIOPlugin plugin.
 * 
 * @author mleslie
 * @author Daniele Romagnoli, GeoSolutions
 * @author Jody Garnett
 * @author Simone Giannecchini, GeoSolutions
 * 
 * @since 0.6.0
 */
public class Activator extends AbstractUIPlugin {
    private static Activator plugin;
    public final static String ID = "net.refractions.udig.catalog.imageio"; //$NON-NLS-1$
    private ResourceBundle resourceBundle;

    /**
     * Construct <code>ImageIOPlugin</code>.
     */
    public Activator() {
        super();
        plugin = this;
    }
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
    	if (!GDALUtilities.isGDALAvailable()){
    		// we perform a check here to check if gdal is actually around
            // if we fail then the plugin contributions would smoothly
            // not be applied.
    	    throw new RuntimeException("GDAL Not Available ... some image formats disabled");
    	}
    }
    
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        this.resourceBundle = null;
        super.stop(context);
    }
    
    /**
     * Retrieves the default instance of this class.
     * 
     * @return Default instance of ImageIOPlugin.
     */
    public static Activator getDefault() {
        return plugin;
    }
    
    /**
     * Retrieves the string value of the requested resource.
     * 
     * @param key
     * @return Value of the desired resource string.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = Activator.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException ex) {
            return key;
        }
    }
    
    /**
     * Retrieves the default resource bundle for this plugin.
     * 
     * @return ResourceBundle
     */
    public ResourceBundle getResourceBundle() {
        try {
            if(this.resourceBundle == null) {
                this.resourceBundle = ResourceBundle.getBundle(
                        "net.refractions.udig.catalog.imageio.MrSIDPluginResources"); //$NON-NLS-1$
            }
        } catch(MissingResourceException ex) {
            this.resourceBundle = null;
        }
        return this.resourceBundle;
    } 
    
    /**
     * Writes an info log in the plugin's log.
     * <p>
     * This should be used for user level messages.
     * </p>
     */
    public static void log( String message, Throwable e ) {
        if (message == null){
            message = ""; //$NON-NLS-1$
        }
        getDefault().getLog().log(new Status(IStatus.INFO, ID, 0, message, e));
    }
    /**
     * Messages that only engage if getDefault().isDebugging()
     * <p>
     * You can turn that on using -debug on the command line,
     * you should get more specific with a .options file
     * but life is short right now.
     */
    private static void trace( String message, Throwable e ) {
        if (getDefault().isDebugging()) {
            if (message != null){
                System.out.println(message+"\n"); //$NON-NLS-1$
            }
            if (e != null){
                e.printStackTrace(System.out);
            }
        }
    }
}
