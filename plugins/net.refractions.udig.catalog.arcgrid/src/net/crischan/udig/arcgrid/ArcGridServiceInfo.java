/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.crischan.udig.arcgrid;

import net.refractions.udig.catalog.IServiceInfo;

public class ArcGridServiceInfo extends IServiceInfo {
	/** ArcGridServiceInfo service field */
    private final ArcGridServiceImplementation service;

    ArcGridServiceInfo(ArcGridServiceImplementation arcGridServiceImplementation) {
		super();
        service = arcGridServiceImplementation;
		this.keywords = new String[] {
				".asc",
				".grd"
		};
	}
	
	public String getTitle() {
		return service.getIdentifier().getFile();
	}
	
	public String getDescription() {
		return service.getIdentifier().toString();
	}
}