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
package net.refractions.udig.catalog.internal.wms;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IResolveManager;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.ISharedImages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;

/**
 * Since WMSFolder is not a IGeoResource but it shares most of its code with
 * {@link WMSGeoResourceImpl} this class exists for sharing that code. If mixins were permitted in
 * Java this wouldn't be necessary... But it is.
 * 
 * @author jesse
 * @since 1.1.0
 */
public class WMSFolder implements IResolveFolder {

    private WMSServiceImpl service;
    private IResolve parent;
    private Layer layer;
    private ArrayList<IResolve> members;
    private URL identifier;
    private ImageDescriptor icon;
    private Lock iconLock = new ReentrantLock();

    /**
     * Construct <code>WMSGeoResourceImpl</code>.
     * 
     * @param service
     * @param parent the parent Georesource may be null if parent is the service.
     * @param layer
     */
    public WMSFolder( WMSServiceImpl service, IResolve parent, org.geotools.data.ows.Layer layer ) {
        this.service = service;
        if (parent == null) {
            this.parent = service;
        } else {
            this.parent = parent;
        }
        this.layer = layer;
        members = new ArrayList<IResolve>();
        for( Layer child : layer.getChildren() ) {
            if (child != layer) {
                if (child.getName() == null) {
                    members.add(new WMSFolder(service, this, child));
                } else {
                    members.add(new WMSGeoResourceImpl(service, this, child));
                }
            }
        }

        try {
            String name = layer.getTitle();
            if (name == null) {
                name = String.valueOf(service.nextFolderID());
            }
            identifier = new URL(service.getIdentifier().toString() + "#" + name); //$NON-NLS-1$

        } catch (Throwable e) {
            WmsPlugin.log(null, e);
            identifier = service.getIdentifier();
        }
    }

    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null) {
            return false;
        }

        if (adaptee.isAssignableFrom(WMSFolder.class)
                || adaptee.isAssignableFrom(WebMapServer.class)
                || adaptee.isAssignableFrom(org.geotools.data.ows.Layer.class)
                || adaptee.isAssignableFrom(ImageDescriptor.class)
                || adaptee.isAssignableFrom(IService.class)) {
            return true;
        }

        return CatalogPlugin.getDefault().getResolveManager().canResolve(this, adaptee);
    }
    
    public void dispose( IProgressMonitor monitor ) {
    }

    public URL getIdentifier() {
        return identifier;
    }
    public ID getID() {
        return new ID( getIdentifier() );        
    }
    public Throwable getMessage() {
        return null;
    }

    public Status getStatus() {
        return Status.CONNECTED;
    }

    public List<IResolve> members( IProgressMonitor monitor ) throws IOException {
        return members;
    }

    public IResolve parent( IProgressMonitor monitor ) throws IOException {
        return parent;
    }

    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null) {
            throw new NullPointerException();
        }

        if (adaptee.isAssignableFrom(WMSFolder.class)) {
            return adaptee.cast(this);
        }

        if (adaptee.isAssignableFrom(WebMapServer.class)) {
            return adaptee.cast(service.getWMS(monitor));
        }

        if (adaptee.isAssignableFrom(org.geotools.data.ows.Layer.class)) {
            return adaptee.cast(layer);
        }
        if (adaptee.isAssignableFrom(ImageDescriptor.class)) {
            return adaptee.cast(getIcon(monitor));
        }

        IResolveManager rm = CatalogPlugin.getDefault().getResolveManager();
        if (rm.canResolve(this, adaptee)) {
            return rm.resolve(this, adaptee, monitor);
        }
        return null; // no adapter found (check to see if ResolveAdapter is registered?)
    }

    /** Must be the same as resolve( ImageDescriptor.class ) */
    public ImageDescriptor getIcon( IProgressMonitor monitor ) {
        iconLock.lock();
        try {
            if (icon == null) {
                icon = WMSGeoResourceImpl.fetchIcon(monitor, layer, service);
                if (icon == null) {
                    icon = CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
                            ISharedImages.GRID_OBJ);
                }
            }
            return icon;
        } finally {
            iconLock.unlock();
        }
    }

    public String getTitle() {
        return layer.getTitle();
    }

    public IService getService( IProgressMonitor monitor ) {
        return service;
    }

}
