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
package net.refractions.udig.catalog;

import java.io.IOException;
import java.util.List;

import net.refractions.udig.catalog.internal.Messages;
import net.refractions.udig.ui.ErrorManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Interface to capture both the Local Catalog resources and Web Registry Service.
 * <p>
 * Conceptually provides a searchable Catalog of "Spatial Data Sources". Metadata search is
 * arbitrary.
 * </p>
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.7.0
 * @see IService
 */
public abstract class IRepository extends ISearch {

    /**
     * Adds the specified entry to this catalog.
     * <p>
     * In some cases the catalog will be backed onto an
     * external server, which may not allow for additions.
     * <p>
     * An IService may belong to more than one Catalog.
     * </p>
     * 
     * @param entry
     * @throws UnsupportedOperationException
     */
    public abstract void add( IService service ) throws UnsupportedOperationException;

    /**
     * Removes the specified entry to this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for deletions.
     * 
     * @param service
     * @throws UnsupportedOperationException
     */
    public abstract void remove( IService service ) throws UnsupportedOperationException;

    /**
     * Replaces the specified entry in this catalog.
     * <p>
     * In some cases the catalog will be backed onto a server, which may not
     * allow for deletions.
     * <p>
     * This method can be used for two things:
     * <ul>
     * <li>ResetService (Action): calls this method with id == service.getID() in order
     * to "reset" the IService handle with a fresh one. This can be used to replace a
     * catalog entry that has locked up.
     * <li>This method can also be used to *move* an existing service (the one with the
     * indicated ID) with a new replacement). 
     * </ul>
     * <p>
     * This replace method has two differences from a simple *remove( id )* and *add( replacement)* 
     * <ul>
     * <li>A difference series of events is generated; letting client code know that they
     * should update the ID they were using to track this resource.
     * <li>An new IForward( ID, replacement.getID()) is left in the catalog as a place holder to
     * order to let any client that was off-line know what happened next time they come to call.
     * </ul>
     * @param id ID of the service to replace, the service with this ID will be removed
     * @param replacement Replacement IService handle; indicating where the service has moved to
     * @throws UnsupportedOperationException
     */
    public abstract void replace( ID id, IService replacement ) throws UnsupportedOperationException;

    /**
     * Will attempt to morph into the adaptee, and return that object. Required adaptions:
     * <ul>
     * <li>ICatalogInfo.class
     * <li>List.class <IService>
     * </ul>
     * May Block.
     * 
     * @param adaptee
     * @param monitor May Be Null
     * @return
     * @see ICatalogInfo
     * @see IService
     */
    public abstract <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException;

    /**
     * Aquire info on this Catalog.
     * <p>
     * This is functionally equivalent to: <core>resolve(ICatalogInfo.class,monitor)</code>
     * </p>
     * 
     * @see IRepository#resolve(Class, IProgressMonitor)
     * @return ICatalogInfo resolve(ICatalogInfo.class,IProgressMonitor monitor);
     */
    public ICatalogInfo getInfo( IProgressMonitor monitor ) throws IOException {
        return resolve(ICatalogInfo.class, monitor);
    }

    /**
     * Find resources for this resourceId from the Catalog.
     * <p>
     * Please note that a List of resources Services, Resources, and friends
     * are returned by this method. While the first result returned
     * may be the most appropriate; you may need to try some of the other
     * values (if for example the first service is unavailable).
     * <p>
     * @param resource used to match resolves
     * @param monitor used to show the progress of the find.
     * @return List (possibly empty) of matching Resolves
     */
    public abstract List<IResolve> find( ID resourceId, IProgressMonitor monitor );
    
    /**
     * Add a listener to notice when the a resource changes.
     * 
     * @param listener
     */
    public abstract void addListener( IResolveChangeListener listener );

    /**
     * Remove a listener that was watching for resource changes.
     * 
     * @param listener
     */
    public abstract void removeListener( IResolveChangeListener listener );
    
    /**
     * Dispose any members at the end of the day.
     */
    public void dispose(IProgressMonitor monitor) {
            monitor.beginTask(Messages.ICatalog_dispose, 100);
            List< ? extends IResolve> members;
            try {
                members = members(new SubProgressMonitor(monitor,1) );
            } catch (Throwable e) {
                ErrorManager.get().displayException(e, "Error disposing members of catalog: "+getIdentifier(), CatalogPlugin.ID); //$NON-NLS-1$
                return;
            }
            int steps=(int)((double)99/(double)members.size());
            for( IResolve resolve : members ) {
                try {
                    SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, steps);
                    resolve.dispose( subProgressMonitor);
                    subProgressMonitor.done();
                } catch (Throwable e) {
                    ErrorManager.get().displayException(e, "Error disposing members of catalog: "+getIdentifier(), CatalogPlugin.ID); //$NON-NLS-1$
                }
            }
        
    }
}
