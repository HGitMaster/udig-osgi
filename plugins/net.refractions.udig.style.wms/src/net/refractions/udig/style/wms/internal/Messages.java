package net.refractions.udig.style.wms.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.refractions.udig.style.wms.internal.messages"; //$NON-NLS-1$

	public static String WMSStyleConfigurator_abstract_format;

	public static String WMSStyleConfigurator_featureStyles_format;

	public static String WMSStyleConfigurator_no_info;

	public static String WMSStyleConfigurator_style_label;

	public static String WMSStyleConfigurator_styleURL_format;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
