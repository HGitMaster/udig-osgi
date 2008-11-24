package net.refractions.udig.catalog.imageio;

import java.io.File;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.rasterings.AbstractRasterService;

import org.eclipse.core.runtime.IStatus;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Describes this Resource.
 * 
 * @author mleslie
 * @author Daniele Romagnoli, GeoSolutions
 * @author Jody Garnett
 * @author Simone Giannecchini, GeoSolutions
 * 
 * @since 0.6.0
 */
public class ImageGeoResourceInfo extends IGeoResourceInfo {
	private final ImageGeoResourceImpl resource;

	ImageGeoResourceInfo(ImageGeoResourceImpl imageGeoResourceImpl) {
		resource = imageGeoResourceImpl;
		this.keywords = new String[] { "MrSID","ECW" }; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		final File file = new File(resource.getIdentifier().getFile());
		this.name = file.getName();
		this.title = name;
		this.description = resource.getIdentifier().toString();
		this.bounds = getBounds();
	}

	/**
	 * @see net.refractions.udig.catalog.IGeoResourceInfo#getBounds()
	 */
	public synchronized ReferencedEnvelope getBounds() {
		if (this.bounds == null) {
			try {
				// Get a GridCoverage for this resource
				AbstractGridCoverage2DReader source =((AbstractRasterService) resource.service(null)).getReader(null);
                if (source == null) {
                    return null;
                }

                GeneralEnvelope ptBounds = source.getOriginalEnvelope();
                Envelope env = new Envelope(ptBounds.getMinimum(0), ptBounds.getMaximum(0),
                        ptBounds.getMinimum(1), ptBounds.getMaximum(1));

                CoordinateReferenceSystem geomcrs = source.getCrs();
                if (geomcrs == null) {
                    geomcrs = DefaultEngineeringCRS.GENERIC_2D;
                }

                this.bounds = new ReferencedEnvelope(env, geomcrs);
			} catch (Exception e) {
				CatalogPlugin
						.getDefault()
						.getLog()
						.log(
								new org.eclipse.core.runtime.Status(
										IStatus.WARNING,
										"net.refractions.udig.catalog", 0, //$NON-NLS-1$
										"Error while getting the bounds of a layer", e)); //$NON-NLS-1$

			}
		}
		return this.bounds;
	}
}
