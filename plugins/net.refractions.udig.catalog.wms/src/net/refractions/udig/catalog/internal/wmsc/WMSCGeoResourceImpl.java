/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, Refractions Research Inc.
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
package net.refractions.udig.catalog.internal.wmsc;

import java.io.IOException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.wmsc.server.TileSet;
import net.refractions.udig.catalog.wmsc.server.TiledWebMapServer;
import net.refractions.udig.catalog.wmsc.server.WMSTileSet;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * GeoResource to represent a WMS-C Tileset.
 * 
 * @author Emily Gouge (Refractions Research, Inc.)
 * @since 1.1.0
 */
public class WMSCGeoResourceImpl extends IGeoResource {

    private TileSet tile; // the tile set info from the capabilities document
    private URL identifier; // the unique identifier

    /**
     * Creates a new georesource for a given WMSC tile set
     * 
     * @param service
     * @param tile
     */
    public WMSCGeoResourceImpl( WMSCServiceImpl service, TileSet tile ) {
        this.service = service;
        try {
            this.identifier = new URL(service.getIdentifier().toString() + "#" + tile.getId()); //$NON-NLS-1$
        } catch (Exception ex) {
            this.identifier = service.getIdentifier();
        }

        this.tile = tile;
    }

    @Override
    public URL getIdentifier() {
        return this.identifier;
    }

    @Override
    public IResolve parent( IProgressMonitor monitor ) throws IOException {
        return service;
    }
    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    @Override
    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null) {
            return false;
        }

        if (adaptee.isAssignableFrom(TiledWebMapServer.class) ||
                adaptee.isAssignableFrom(WMSTileSet.class)  || 
                super.canResolve(adaptee)) {
            return true;
        }

        return false;
    }

    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee.isAssignableFrom(TiledWebMapServer.class)) {
            return adaptee.cast(service(monitor).getWMSC());
        }
        if (adaptee.isAssignableFrom(WMSTileSet.class)){
            return adaptee.cast(tile);
        }
        return super.resolve(adaptee, monitor);
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return service.getMessage();
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatus()
     */
    public Status getStatus() {
        return service.getStatus();
    }

    @Override
	protected IGeoResourceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        if (info == null) {
            service(monitor).rLock.lock();
            try {
                if (info == null) {
                    info = new WMSCGeoResourceInfo(this, monitor);
                }
            } finally {
                service(monitor).rLock.unlock();
            }
        }
        return info;
    }
    
    @Override
    public WMSCServiceImpl service(IProgressMonitor monitor) throws IOException {
    	return (WMSCServiceImpl) super.service(monitor);
    }

    /**
     * 
     *
     * @return  the WMSC tile set represented by the service
     */
    public TileSet getTileSet() {
        return this.tile;
    }
}
