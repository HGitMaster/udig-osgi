package net.refractions.udig.catalog.internal.geotiff;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class GeoTiffPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static GeoTiffPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public GeoTiffPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 * @param context 
	 * @throws Exception 
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 * @param context 
	 * @throws Exception 
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.resourceBundle = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * @return Default implementation of this plugin.
	 */
	public static GeoTiffPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 * @param key 
	 * @return String value of the requsted resource.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = GeoTiffPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 * @return Default ResourceBundle for this plugin.
	 */
	public synchronized ResourceBundle getResourceBundle() {
		try {
			if (this.resourceBundle == null)
				this.resourceBundle = ResourceBundle.getBundle(
                        "net.refractions.udig.catalog.internal.geotiff.GeoTiffPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			this.resourceBundle = null;
		}
		return this.resourceBundle;
	}
}
