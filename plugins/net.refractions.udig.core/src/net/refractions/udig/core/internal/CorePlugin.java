package net.refractions.udig.core.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.spi.ImageReaderSpi;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.service.datalocation.Location;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.factory.Hints.Key;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.util.logging.Logging;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.osgi.framework.Bundle;
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
     * Create a URL from the provided spec; willing to create
     * a URL even if the spec does not have a registered handler.
     * Can be used to create "jdbc" URLs for example.
     *
     * @param spec
     * @return URL if possible
     * @throws RuntimeException of a MalformedURLException resulted
     */
    public static URL createSafeURL( String spec ) {
        try {
            return new URL(null, spec, RELAXED_HANDLER);
        } catch (MalformedURLException e) {
            throw (RuntimeException) new RuntimeException( e );
        }
    }
    /**
     * Create a URI from the provided spec; willing to create
     * a URI even if the spec does not have a registered handler.
     * Can be used to create "jdbc" URLs for example.
     *
     * @param spec
     * @return URI if possible
     * @throws RuntimeException of a URISyntaxException resulted
     */
    public static URI createSafeURI( String spec ){
        try {
            return new URI( spec );
        } catch (URISyntaxException e) {
            throw (RuntimeException) new RuntimeException( e );
        }
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
    
    
    public void startGeotools(final BundleContext context) throws Exception {
	    if( Platform.getOS().equals(Platform.OS_WIN32) ){
		    try {
		        // PNG native support is not very good .. this turns it off
		        ImageUtilities.allowNativeCodec("png", ImageReaderSpi.class, false);  //$NON-NLS-1$
		    }
		    catch (Throwable t){
		        // we should not die if JAI is missing; we have a warning for that...
		        System.out.println("Difficulty turnning windows native PNG support (which will result in scrambled images from WMS servers)"); //$NON-NLS-1$
		        t.printStackTrace();
		    }
        }

	    // System properites work for controlling referencing behavior
	    // not so sure about the geotools global hints
	    //
	    System.setProperty("org.geotools.referencing.forceXY", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        Map<Key, Boolean> map = new HashMap<Key, Boolean> ();
	    // these commented out hints are covered by the forceXY system property
		//
		//map.put( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true );
	    //map.put( Hints.FORCE_STANDARD_AXIS_DIRECTIONS, true );
		//map.put( Hints.FORCE_STANDARD_AXIS_UNITS, true );
	    map.put( Hints.LENIENT_DATUM_SHIFT, true );		
		Hints global = new Hints(map);
		GeoTools.init( global );
		
		// We cannot do this here - it takes too long!
		// Early startup is too late
		// functionality moved to the UDIGApplication init method
		//
		// initializeReferencingModule( context.getBundle(), null ); 		
	}
	
    public static void initializeReferencingModule( IProgressMonitor monitor ) {
        Bundle bundle = Platform.getBundle(ID);
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask( Messages.Activator_EPSG_DATABASE, 100);

        
        searchEPSGProperties( bundle, new SubProgressMonitor(monitor, 20));
        
        loadEPSG(bundle, new SubProgressMonitor(monitor, 60));

        monitor.subTask(Messages.OPERATIONS_DEFINITIONS);
        load(ReferencingFactoryFinder.getCoordinateOperationAuthorityFactories(null));
        monitor.worked(2);

        monitor.subTask(Messages.COORDINATE_REFERENCE_SYSTSMS);
        load(ReferencingFactoryFinder.getCRSFactories(null));
        monitor.worked(8);

        monitor.subTask(Messages.COORDINATE_SYSTEMS);
        load(ReferencingFactoryFinder.getCSFactories(null));
        monitor.worked(2);

        monitor.subTask(Messages.DATUM_DEFINITIONS);
        load(ReferencingFactoryFinder.getDatumAuthorityFactories(null));
        monitor.worked(2);

        monitor.subTask(Messages.DATUMS);
        load(ReferencingFactoryFinder.getDatumFactories(null));
        monitor.worked(2);

        monitor.subTask(Messages.MATH_TRANSFORMS);
        load(ReferencingFactoryFinder.getMathTransformFactories(null));
        monitor.worked(4);
    }
    @SuppressWarnings("unchecked")
    static private void load( Set coordinateOperationAuthorityFactories ) {
        for( Iterator iter = coordinateOperationAuthorityFactories.iterator(); iter.hasNext(); ) {
            iter.next();
        }
    }
    
    /**
     * Will load the EPSG database; this will trigger the unpacking of the EPSG
     * database (which may take several minutes); and check in a few locations
     * for an epsg.properties file to load: the locations are the data directory;
     * the configuration directory; and finally the libs plugin bundle itself
     * (which includes a default epsg.properties file that has a few common
     * unofficial codes for things like the google projection).
     * <p>
     * This method will trigger the geotools referencing module to "scanForPlugins"
     * and MUST be called prior to using the geotools library for anything real. I
     * am sorry we could not arrange for this method to be called in an Activator
     * as it simple takes too long and the Platform get's mad at us.
     * 
     * @param bundle
     * @param monitor
     */
	public static void searchEPSGProperties(Bundle bundle, IProgressMonitor monitor) {
	    if( monitor == null ) monitor = new NullProgressMonitor();
		
	    monitor.beginTask(Messages.EPSG_SETUP, IProgressMonitor.UNKNOWN );
		try {
			// go through and check a couple of locations
	        // for an "epsg.properties" file full of 
	        // suplementary codes
	        //
			URL epsg = null;
			Location configLocaiton = Platform.getInstallLocation();
			Location dataLocation = Platform.getInstanceLocation();
			if( dataLocation != null ){
				try {
	        	    URL url = dataLocation.getURL();
	        	    URL proposed = new URL( url, "epsg.properties"); //$NON-NLS-1$
	        	    monitor.subTask(Messages.CHECK+proposed );
	        	    if( "file".equals(proposed.getProtocol())){ //$NON-NLS-1$
	        	        File file = new File( proposed.toURI() );
	        	        if( file.exists() ){
	        	            epsg = file.toURI().toURL();
	        	        }
	        	    }
	        	    monitor.worked(1);
			    }
			    catch (Throwable t ){
			    	if( Platform.inDebugMode()){
			    		System.out.println( "Could not find data directory epsg.properties"); //$NON-NLS-1$
			    		t.printStackTrace();
			    	}			        
			    }
			}
			if( epsg == null && configLocaiton != null ){
	            try {
	                URL url = configLocaiton.getURL();
	                URL proposed = new URL( url, "epsg.properties"); //$NON-NLS-1$
	                monitor.subTask(Messages.Activator_1+proposed );
	                if( "file".equals(proposed.getProtocol())){ //$NON-NLS-1$
	                    File file = new File( proposed.toURI() );
	                    if( file.exists() ){
	                        epsg = file.toURI().toURL();
	                    }
	                }
	                monitor.worked(1);
	            }
	            catch (Throwable t ){
	            	if( Platform.inDebugMode()){
			    		System.out.println( "Could not find configuration epsg.properties"); //$NON-NLS-1$
			    		t.printStackTrace();
			    	}
	            }
			}
			if (epsg == null){
                try {
			        URL internal = bundle.getEntry("epsg.properties"); //$NON-NLS-1$
			        URL fileUrl = FileLocator.toFileURL( internal );
			        epsg = fileUrl.toURI().toURL();                    
			    }
			    catch (Throwable t ){
			    	if( Platform.inDebugMode()){
			    		System.out.println( "Could not find net.refractions.udig.libs/epsg.properties"); //$NON-NLS-1$
			    		t.printStackTrace();
			    	}
	            }		    
			}
			
			if( epsg != null ){
				monitor.subTask(Messages.LOADING+epsg);            
			    Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class);
			    ReferencingFactoryContainer referencingFactoryContainer = ReferencingFactoryContainer
	                .instance(hints);
	
			    PropertyAuthorityFactory factory = new PropertyAuthorityFactory(
	                referencingFactoryContainer, Citations.fromName("EPSG"), epsg ); //$NON-NLS-1$
	
			    ReferencingFactoryFinder.addAuthorityFactory(factory);
			    monitor.worked(1);
			    
			    monitor.subTask(Messages.REGISTER+epsg);
			    ReferencingFactoryFinder.scanForPlugins(); // hook everything up
			    monitor.worked(10);                
			}
			
			monitor.subTask(Messages.PLEASE_WAIT);
            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");  //$NON-NLS-1$
            if( wgs84 == null){
                String msg = "Unable to locate EPSG authority for EPSG:4326; consider removing temporary 'GeoTools' directory and trying again."; //$NON-NLS-1$
                System.out.println( msg );
                //throw new FactoryException(msg);
            }
            monitor.worked(1);
            
			// Show EPSG authority chain if in debug mode
			//
			if( Platform.inDebugMode() ){
	            CRS.main(new String[]{"-dependencies"}); //$NON-NLS-1$
	        }
			// Verify EPSG authority configured correctly
			// if we are in development mode
			if( Platform.inDevelopmentMode() ){
			    monitor.subTask("verify epsg definitions"); //$NON-NLS-1$
				verifyReferencingEpsg();
				monitor.subTask("verify epsg operations"); //$NON-NLS-1$
				verifyReferencingOperation();
			}
		}
		catch (Throwable t ){
		    Platform.getLog(bundle).log(
                    new Status(Status.ERROR, ID, t.getLocalizedMessage(), t));
		}
		finally {
			monitor.done();
		}
	}
	
	public static void loadEPSG(Bundle bundle, IProgressMonitor monitor) {
        if( monitor == null ) monitor = new NullProgressMonitor();
        
        monitor.beginTask(Messages.EPSG_SETUP, IProgressMonitor.UNKNOWN );
        try {
            monitor.subTask(Messages.PLEASE_WAIT);
            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");  //$NON-NLS-1$
            if( wgs84 == null){
                String msg = "Unable to locate EPSG authority for EPSG:4326; consider removing temporary 'GeoTools' directory and trying again."; //$NON-NLS-1$
                System.out.println( msg );
                //throw new FactoryException(msg);
            }
            monitor.worked(1);
            
            // Show EPSG authority chain if in debug mode
            //
            if( Platform.inDebugMode() ){
                CRS.main(new String[]{"-dependencies"}); //$NON-NLS-1$
            }
            // Verify EPSG authority configured correctly
            // if we are in development mode
            if( Platform.inDevelopmentMode() ){
                monitor.subTask("verify epsg definitions"); //$NON-NLS-1$
                verifyReferencingEpsg();
                monitor.subTask("verify epsg operations"); //$NON-NLS-1$
                verifyReferencingOperation();
            }
        }
        catch (Throwable t ){
            Platform.getLog(bundle).log(
                    new Status(Status.ERROR, ID, t.getLocalizedMessage(), t));
        }
        finally {
            monitor.done();
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
	private static void verifyReferencingEpsg() throws Exception {
        CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326"); // latlong //$NON-NLS-1$
        CoordinateReferenceSystem BC_ALBERS = CRS.decode("EPSG:3005"); //$NON-NLS-1$
        
        MathTransform transform = CRS.findMathTransform(BC_ALBERS, WGS84 );
        DirectPosition here  = new DirectPosition2D( BC_ALBERS, 1187128, 395268 );
        DirectPosition there = new DirectPosition2D( WGS84, -123.47009173007372,48.54326498732153 );
        	
        DirectPosition check = transform.transform( here, new GeneralDirectPosition(WGS84) );
        //DirectPosition doubleCheck = transform.inverse().transform( check, new GeneralDirectPosition(BC_ALBERS) );        
//        if( !check.equals(there)){
//        	String msg = "Referencing failed to produce expected transformation; check that axis order settings are correct.";
//        	System.out.println( msg );
//        	//throw new FactoryException(msg);
//        }
        double delta = Math.abs(check.getOrdinate(0) - there.getOrdinate(0))+Math.abs(check.getOrdinate(1) - there.getOrdinate(1));
		if( delta > 0.0001){
			String msg = "Referencing failed to transformation with expected accuracy: Off by "+delta + "\n"+check+"\n"+there;   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
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
	private static void verifyReferencingOperation() throws Exception {
	       // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
        // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
        CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326"); //$NON-NLS-1$
        ReferencedEnvelope pixelBounds = new ReferencedEnvelope( -0.24291497975705742, 0.24291497975711265, -0.5056179775280899, 0.0, EPSG4326 );
        CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
        
        ReferencedEnvelope latLong = pixelBounds.transform( WGS84, false );
        if( latLong == null){
        	String msg = "Unable to transform EPSG:4326 to DefaultGeographicCRS.WGS84"; //$NON-NLS-1$
        	System.out.println( msg );        		
        	//throw new FactoryException(msg);
        }
	}
	
	public void stop(BundleContext context) throws Exception {
	}
}
