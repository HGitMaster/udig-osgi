package net.refractions.udig.project.internal.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.project.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Indicates that no georesources were found for the layer
 * 
 * @author Jesse
 */
public class NullGeoResource extends IGeoResource {

    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) {
        return null;
    }
    public IService service( IProgressMonitor monitor ) throws IOException {
        return null; // where is NullService ?
    }
    public <T> boolean canResolve( Class<T> adaptee ) {
        return false;
    }

    public Status getStatus() {
        return Status.BROKEN;
    }

    public Throwable getMessage() {
        return new Exception(Messages.NullGeoResource_0); 
    }

    public URL getIdentifier() {
        try {
            return new URL("http://NULL"); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            // Can't happen
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IGeoResourceInfo getInfo( IProgressMonitor monitor ) {
        // TODO Auto-generated method stub
        return new IGeoResourceInfo(){
            @Override
            public ReferencedEnvelope getBounds() {
                // TODO Auto-generated method stub
                return new ReferencedEnvelope(new Envelope(), null);
            }
            @Override
            public CoordinateReferenceSystem getCRS() {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }
}
