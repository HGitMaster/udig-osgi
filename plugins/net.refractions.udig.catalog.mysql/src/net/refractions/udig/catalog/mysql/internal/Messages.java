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
package net.refractions.udig.catalog.mysql.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.refractions.udig.catalog.mysql.internal.messages"; //$NON-NLS-1$

	public static String MySQLServiceExtension_badURL;

    public static String MySQLWizardPage_0;
	public static String MySQLWizardPage_title;
	public static String MySQLWizardPage_button_looseBBox_tooltip;
	public static String MySQLWizardPage_button_looseBBox_text;
	public static String MySQLWizardPage_button_wkb_tooltip;
	public static String MySQLWizardPage_button_wkb_text;
	public static String MySQLGeoResource_error_layer_bounds;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
