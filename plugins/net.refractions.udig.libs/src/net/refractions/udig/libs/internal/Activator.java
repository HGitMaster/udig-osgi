package net.refractions.udig.libs.internal;

import java.util.HashMap;
import java.util.Map;

import javax.imageio.spi.ImageReaderSpi;

import org.eclipse.core.runtime.Platform;
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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
public class Activator implements BundleActivator {
	public void start(BundleContext context) throws Exception {
	    System.setProperty("org.geotools.referencing.forceXY", "true"); //$NON-NLS-1$ //$NON-NLS-2$

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
        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326"); // prime the pump - ensure EPSG factory is found //$NON-NLS-1$
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
		    CRS.main(new String[]{"-dependencies"}); //$NON-NLS-1$
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
			String msg = "Referencing failed to transformation with expected accuracy: Off by "+delta + "\n"+check+"\n"+there;  //$NON-NLS-2$//$NON-NLS-3$
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
        CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326"); //$NON-NLS-1$
        ReferencedEnvelope pixelBounds = new ReferencedEnvelope( -0.24291497975705742, 0.24291497975711265, -0.5056179775280899, 0.0, EPSG4326 );
        CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
        
        ReferencedEnvelope latLong = pixelBounds.transform( WGS84, false );
        if( latLong == null){
        	String msg = "Unable to transform EPSG:4326 to DefaultGeographicCRS.WGS84";
        	System.out.println( msg );        		
        	//throw new FactoryException(msg);
        }
	}
	
	public void stop(BundleContext context) throws Exception {
	}

}
