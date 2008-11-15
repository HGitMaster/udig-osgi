package net.refractions.udig.core.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ImageReaderSpi;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.factory.Hints.Key;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.osgi.framework.BundleContext;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * PlugIn for net.refractions.udig.core, used by utility classes to access workbench log.
 * 
 * @author jones
 * @since 0.3
 */
public class CorePlugin extends Plugin {

    /** Plugin <code>ID</code> field */
    public static final String ID = "net.refractions.udig.core"; //$NON-NLS-1$
    private static CorePlugin plugin;

    
    
    /**
     * A url stream handler that delegates to the default one but if it doesn't work then it returns null as the stream.
     */
    public final static URLStreamHandler RELAXED_HANDLER=new URLStreamHandler(){

        @Override
        protected URLConnection openConnection( URL u ) throws IOException {
            try{
                URL url=new URL(u.toString());
                return url.openConnection();
            }catch (MalformedURLException e){
                return null;
            }
        }
    };
    
    /**
     * creates a plugin instance
     */
    public CorePlugin() {
        super();
        plugin = this;
    }

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        startGeotools(context);
    }
	
    /**
     * Returns the system created plugin object
     * 
     * @return the plugin object
     */
    public static CorePlugin getDefault() {
        return plugin;
    }

    private static volatile MutablePicoContainer blackboard = null;

    /**
     * This is intended to return the top level Pico Container to use for blackBoarding.
     * <p>
     * For most applications, a sub container is required. I recommend the following code be
     * inserted into your main plugin class. <code>
     * private static MutablePicoContainer myContainer = null;
     * 
     * /**
     *  * Gets the container for my plugin.
     *  * 
     *  * Make it 'public' if you want to share ... protected otherwise.
     *  * /
     * public static MutablePicoContainer getMyContainer(){
     *   if(myContainer == null){
     *     // XXXPlugin is the name of the Plugin class
     *     synchronized(XXXPlugin.class){
     *       // check so see that you were not queued for double creation
     *       if(myContainer == null){
     *         // This line does it ... careful to only call it once!
     *         myContainer = CorePlugin.getBlackBoard().makeChildContainer();
     *       }
     *     }
     *   }
     *   return myContainer;
     * }
     * </code>
     * </p>
     * <p>
     * NOTE:<br>
     * Please check to ensure the child you want is not already created (important for two plugins
     * sharing one container).
     * </p>
     * 
     * @return
     */
    public static MutablePicoContainer getBlackBoard() {
        if (blackboard == null) {
            synchronized (CorePlugin.class) {
                if (blackboard == null) {
                    blackboard = new DefaultPicoContainer();
                }
            }
        }
        return blackboard;
    }

    /**
     * Takes a string, and splits it on '\n' and calls stringsToURLs(String[])
     */
    public static List<URL> stringsToURLs( String string ) {
        String[] strings = string.split("\n"); //$NON-NLS-1$

        return stringsToURLs(strings);
    }

    /**
     * Converts each element of an array from a String to a URL. If the String is not a valid URL,
     * it attempts to load it as a File and then convert it. If that fails, it ignores it. It will
     * not insert a null into the returning List, so the size of the List may be smaller than the
     * size of <code>strings</code>
     * 
     * @param strings an array of strings, each to be converted to a URL
     * @return a List of URLs, in the same order as the array
     */
    public static List<URL> stringsToURLs( String[] strings ) {
        List<URL> urls = new ArrayList<URL>();

        for( String string : strings ) {
            try {
                urls.add(new URL(string));
            } catch (MalformedURLException e) {
                // not a URL, maybe it is a file
                try {
                	urls.add( new File(string).toURI().toURL());
                } catch (MalformedURLException e1) {
                    // Not a URL, not a File. nothing to do now.
                }
            }
        }
        return urls;
    }
    
    /**
     * Writes an info log in the plugin's log.
     * <p>
     * This should be used for user level messages.
     * </p>
     */
    public static void log( String message2, Throwable e ) {
        String message=message2;
        if (message == null)
            message = ""; //$NON-NLS-1$
        getDefault().getLog().log(new Status(IStatus.INFO, ID, 0, message, e));
    }
    /**
     * Messages that only engage if getDefault().isDebugging()
     * <p>
     * It is much prefered to do this:
     * 
     * <pre><code>
     * private static final String RENDERING = &quot;net.refractions.udig.project/render/trace&quot;;
     * if (ProjectUIPlugin.getDefault().isDebugging() &amp;&amp; &quot;true&quot;.equalsIgnoreCase(RENDERING)) {
     *     System.out.println(&quot;your message here&quot;);
     * }
     * 
     */
    public static void trace( String message, Throwable e ) {
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
     * @param trace currently only RENDER is defined
     */
    public static boolean isDebugging( final String trace ) {
        return getDefault().isDebugging()
                && "true".equalsIgnoreCase(Platform.getDebugOption(trace)); //$NON-NLS-1$    
    }

    public static boolean isDeveloping() {
        return System.getProperty("UDIG_DEVELOPING") != null; //$NON-NLS-1$
    }
    
    /**
     * The Activator for net.refractions.udig.libs provides global settings
     * to help all the open source projects get along.
     * <p>
     * Currently this activator supplied:
     * <ul>
     * <li>hints about axis order for GeoTools;
     * <li>instructs java not to use native PNG support; see UDIG-1391 for details
     * </ul>
     * <p>
     * The contents of this Activator will change over time according to the needs
     * of the libraries and tool kits we are using.
     * </p>
     * @author Jody Garnett
     * @since 1.1.0
     */
    	public void startGeotools(BundleContext context) throws Exception {
    	    System.setProperty("org.geotools.referencing.forceXY", "true");

    		if( Platform.getOS().equals(Platform.OS_WIN32) ){
    		    try {
    		        // PNG native support is not very good .. this turns it off
    		        ImageUtilities.allowNativeCodec("png", ImageReaderSpi.class, false);  //$NON-NLS-1$
    		    }
    		    catch (Throwable t){
    		        // we should not die if JAI is missing; we have a warning for that...
    		    }
            }
    	    
    		// WARNING - the above hints are recommended to us by GeoServer
    		//           but they cause epsg-wkt not to work because the
    		//           various wrapper classes trip up over a CRSAuthorityFactory
    		//           that is not also a OperationAuthorityFactory (I think)
            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326"); // prime the pump - ensure EPSG factory is found
            if( wgs84 == null){
            	String msg = "Unable to locate EPSG authority for EPSG:4326; consider removing temporary geotools/epsg directory and trying again.";
            	System.out.println( msg );
            	//throw new FactoryException(msg);
            }
    		Map<Key, Boolean> map = new HashMap<Key, Boolean> ();
    		//map.put( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true );
    	    //map.put( Hints.FORCE_STANDARD_AXIS_DIRECTIONS, true );
    		//map.put( Hints.FORCE_STANDARD_AXIS_UNITS, true );
    		map.put( Hints.LENIENT_DATUM_SHIFT, true );		
    		Hints global = new Hints(map);
    		GeoTools.init( global );
    		if( false ){ // how to do debug check with OSGi bundles?
    		    CRS.main(new String[]{"-dependencies"});
    		}
    		ReferencingFactoryFinder.scanForPlugins();
    		verifyReferencingEpsg();
    		verifyReferencingOperation();	
    		
    	}
        /**
         * If this method fails it's because, the epsg jar is either 
         * not available, or not set up to handle math transforms
         * in the manner udig expects.
         * 
         * @return true if referencing is working and we get the expected result
         * @throws Exception if we cannot even get that far
         */
    	private void verifyReferencingEpsg() throws Exception {
            CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326"); // latlong
            CoordinateReferenceSystem BC_ALBERS = CRS.decode("EPSG:3005");
            
            MathTransform transform = CRS.findMathTransform(BC_ALBERS, WGS84 );
            DirectPosition here  = new DirectPosition2D( BC_ALBERS, 1187128, 395268 );
            DirectPosition there = new DirectPosition2D( WGS84, -123.47009173007372,48.54326498732153 );
            	
            DirectPosition check = transform.transform( here, new GeneralDirectPosition(WGS84) );
            //DirectPosition doubleCheck = transform.inverse().transform( check, new GeneralDirectPosition(BC_ALBERS) );        
//            if( !check.equals(there)){
//            	String msg = "Referencing failed to produce expected transformation; check that axis order settings are correct.";
//            	System.out.println( msg );
//            	//throw new FactoryException(msg);
//            }
            double delta = Math.abs(check.getOrdinate(0) - there.getOrdinate(0))+Math.abs(check.getOrdinate(1) - there.getOrdinate(1));
    		if( delta > 0.0001){
    			String msg = "Referencing failed to transformation with expected accuracy: Off by "+delta + "\n"+check+"\n"+there;
    			System.out.println( msg );
            	//throw new FactoryException(msg);
            }	
    	}
        /**
         * If this method fails it's because, the epsg jar is either 
         * not available, or not set up to handle math transforms
         * in the manner udig expects.
         * 
         * @return true if referencing is working and we get the expected result
         * @throws Exception if we cannot even get that far
         */
    	private void verifyReferencingOperation() throws Exception {
    	       // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
            // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
            CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326");
            ReferencedEnvelope pixelBounds = new ReferencedEnvelope( -0.24291497975705742, 0.24291497975711265, -0.5056179775280899, 0.0, EPSG4326 );
            CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
            
            ReferencedEnvelope latLong = pixelBounds.transform( WGS84, false );
            if( latLong == null){
            	String msg = "Unable to transform EPSG:4326 to DefaultGeographicCRS.WGS84";
            	System.out.println( msg );        		
            	//throw new FactoryException(msg);
            }
    	}
    	
}
