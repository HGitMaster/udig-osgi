/**
 * 
 */
package net.refractions.udig.catalog.imageio;

import net.refractions.udig.catalog.IServiceInfo;

/**
 * Provides metadata information about a service handling MrSID format.
 * 
 * @author mleslie
 * @author Daniele Romagnoli, GeoSolutions
 * @author Jody Garnett
 * @author Simone Giannecchini, GeoSolutions
 */
class ImageServiceInfo extends IServiceInfo {
	private final ImageServiceImpl service;

	ImageServiceInfo(ImageServiceImpl imageServiceImpl) {
		super();
		service = imageServiceImpl;
		this.keywords = new String[] { "MrSID" ,"ECW"//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$ //$NON-NLS-5$
			};
	}

	public String getTitle() {
		return service.getIdentifier().getFile();
	}

	public String getDescription() {
		return service.getIdentifier().toString();
	}
}
