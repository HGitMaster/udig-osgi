package net.refractions.udig.catalog.mif.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class MifPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static MifPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

    public static final String MIF_TRACE_FINEST = "net.refractions.udig.catalog.shp/debug/finest"; //$NON-NLS-1$
    public static final String MIF_TRACE_FINE = "net.refractions.udig.catalog.mif/debug/fine"; //$NON-NLS-1$
    
	
    public static final String ID = "net.refractions.udig.catalog.mif"; //$NON-NLS-1$
	/**
	 * The constructor.
	 */
	public MifPlugin() {
		super();
		plugin = this;
	}

	/**
     * This method is called upon plug-in activation
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        Logger logger = ShapefileDataStoreFactory.LOGGER;
        if (MifPlugin.isDebugging(MIF_TRACE_FINEST) || MifPlugin.isDebugging(MIF_TRACE_FINE)) {
            if( MifPlugin.isDebugging(MIF_TRACE_FINE)){
                logger.setLevel(Level.FINE);
            }else{
                logger.setLevel(Level.FINEST);
            }
        } else {
            logger.setLevel(Level.SEVERE);
        }
        logger.addHandler(new ConsoleHandler());
    }

	/**
     * This method is called when the plug-in is stopped
     */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		resourceBundle = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * @return x
	 */
	public static MifPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 * @param key x
	 * @return x
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = MifPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 * @return x
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("net.refractions.udig.catalog.mif.internal.MifPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}
    public static void log( String message, Throwable t ) {
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        getDefault().getLog().log(new Status(status, ID, IStatus.OK, message, t));
    }
    /**
     * Messages that only engage if getDefault().isDebugging()
     * <p>
     * It is much prefered to do this:
     * 
     * <pre><code>
     * private static final String RENDERING = &quot;net.refractions.udig.project/render/trace&quot;;
     * if (ProjectUIPlugin.getDefault().isDebugging()
     *      &amp;&amp; &quot;true&quot;.equalsIgnoreCase(RENDERING)) {
     *  System.out.println(&quot;your message here&quot;);
     * }
     * 
     */
    public static void trace(String message, Throwable e) {
        if (getDefault().isDebugging()) {
            if (message != null)
                System.out.println(message);
            if (e != null)
                e.printStackTrace();
        }
    }

    /**
     * Performs the Platform.getDebugOption true check on the provided trace
     * <p>
     * Note: ProjectUIPlugin.getDefault().isDebugging() must also be on.
     * <ul>
     * <li>Trace.RENDER - trace rendering progress
     * </ul>
     * </p>
     * 
     * @param trace
     *            currently only RENDER is defined
     */
    public static boolean isDebugging(final String trace) {
        return getDefault().isDebugging()
                && "true".equalsIgnoreCase(Platform.getDebugOption(trace)); //$NON-NLS-1$    
    }
    
}
