/**
 * 
 */
package net.refractions.udig.catalog.mif.internal;

import java.net.URI;
import java.net.URISyntaxException;

import net.refractions.udig.catalog.IServiceInfo;

class MifServiceInfo extends IServiceInfo {

	/**
	 * 
	 */
	private final MifServiceImpl service;

	MifServiceInfo(MifServiceImpl shpServiceImpl) {
		super();
		service = shpServiceImpl;
		try {
			keywords = new String[] { ".mif", "mif", //$NON-NLS-1$ //$NON-NLS-2$
					service.ds.getTypeNames()[0] };
		} catch (Exception e) {
			MifPlugin.log(null, e);
			throw new RuntimeException(e);
		}

		try {
			schema = new URI("mif://www.opengis.net/gml"); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			MifPlugin.log(null, e);
			schema = null;
		}
	}

	public String getDescription() {
		return service.getIdentifier().toString();
	}

	public String getTitle() {
		return service.getIdentifier().getFile();
	}
}