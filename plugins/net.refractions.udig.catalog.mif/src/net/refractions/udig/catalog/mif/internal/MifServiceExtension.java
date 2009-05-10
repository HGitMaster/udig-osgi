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
package net.refractions.udig.catalog.mif.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.AbstractDataStoreServiceExtension;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.internal.ui.UiPlugin;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.mif.MIFDataStoreFactory;

/**
 * Service Extension implementation for Shapefiles.
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class MifServiceExtension extends AbstractDataStoreServiceExtension
		implements ServiceExtension {

	private static MIFDataStoreFactory dsFactory = new MIFDataStoreFactory();

	public IService createService(URL id, Map<String, Serializable> params) {
		if (params.containsKey(MIFDataStoreFactory.PARAM_PATH.key)) {
			URL url = null;
				try {
					url = new File((String) MIFDataStoreFactory.PARAM_PATH.lookUp(params)).toURI().toURL();
				} catch (Throwable e1) {
					// log this?
					e1.printStackTrace();
					return null;
				}
			if (!isSupportedExtension(url) || !dsFactory.canProcess(params)) //$NON-NLS-1$
				return null;
			return new MifServiceImpl(url, params);
		}
		return null;
	}

	public Map<String, Serializable> createParams(URL url) {
		URL url2 = url;
		if (!isSupportedExtension(url))
			return null;

		url2 = toMifURL(url2);
		if (url2 == null)
			return null;

		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(MIFDataStoreFactory.PARAM_PATH.key, URLUtils.urlToFile(url2).getPath());
		params.put(MIFDataStoreFactory.PARAM_CHARSET.key, defaultCharset());
		params.put(MIFDataStoreFactory.PARAM_DBTYPE.key, (Serializable) MIFDataStoreFactory.PARAM_DBTYPE.sample);

		if (dsFactory.canProcess(params))
			return params;
		return null;
	}

	private boolean isSupportedExtension(URL url) {
		String file = url.getFile();
		file = file.toLowerCase();

		return (file.endsWith(".mif") || file.endsWith(".mid")); //$NON-NLS-1$
	}

	private URL toMifURL(URL url) {
		File baseFile = URLUtils.urlToFile(url);
		if( baseFile.getPath().toLowerCase().endsWith(".mif") ){
			return url;
		}
		
		final String base = baseFile.getPath().substring(0,baseFile.getPath().lastIndexOf('.'));
		
		File[] files = baseFile.getParentFile().listFiles(new FilenameFilter(){

			public boolean accept(File dir, String name) {
				return name.startsWith(base) && name.toLowerCase().endsWith(".mif");
			}
			
		});
		
		if(files.length>0){
			try {
				return files[0].toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	protected String doOtherChecks(Map<String, Serializable> params) {
		return null;
	}

	@Override
	protected DataStoreFactorySpi getDataStoreFactory() {
		return dsFactory;
	}

	public String reasonForFailure(URL url) {
		if (!isSupportedExtension(url))
			return Messages.ShpServiceExtension_badExtension;
		if (toMifURL(url) == null)
			return Messages.ShpServiceExtension_cantCreateURL;
		return reasonForFailure(createParams(url));
	}

	public static String defaultCharset() {
        return UiPlugin.getDefault().getPreferenceStore().getString(net.refractions.udig.ui.preferences.PreferenceConstants.P_DEFAULT_CHARSET);
    }

}
