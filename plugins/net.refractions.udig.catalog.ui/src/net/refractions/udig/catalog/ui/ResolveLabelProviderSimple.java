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
package net.refractions.udig.catalog.ui;

import java.io.IOException;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IProcess;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveChangeListener;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.ISearch;
import net.refractions.udig.catalog.IService;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Label generation for IResolve (must be non-blocking and quick).
 * <p>
 * Compare and contrast with ResovleLabelDecorator which is allowed to block.
 * </p>
 * 
 * @author jgarnett
 * @since 0.7.0
 */
public class ResolveLabelProviderSimple extends LabelProvider implements IResolveChangeListener {

    public ResolveLabelProviderSimple() {
        CatalogPlugin.addListener(this);
    }
    /*
     * @see net.refractions.udig.catalog.IResolveChangeListener#changed(net.refractions.udig.catalog.IResolveChangeEvent)
     */
    public void changed( final IResolveChangeEvent event ) {
        if (event.getType() != IResolveChangeEvent.Type.POST_CHANGE)
            return;

        final IResolve resolve = event.getResolve();
        if (resolve == null)
            return;

        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                fireLabelProviderChanged(new LabelProviderChangedEvent(
                        ResolveLabelProviderSimple.this, resolve));
            }
        });
    }

    /**
     * Generate text from the resolve.getURI()
     * <p>
     * Note this name is only used as a first try, the ResolveLabelDecorator is expected to provide
     * a label based on Name or Title information.
     * </p>
     * 
     * @param element
     * @return label based on IResolve.getIdentifier
     */
    public String getText( Object element ) {
        if (element instanceof IResolve) {
        	try {
        	if(element instanceof IGeoResource) {
        		IGeoResource resource = (IGeoResource) element;
        		String title = resource.getInfo(new NullProgressMonitor()).getTitle();
        		IService service = resource.service(new NullProgressMonitor());
        		service.getPersistentProperties().put(resource.getID() + "_title", title);
        		return title;
        	} else if(element instanceof IService) {
        		IService service = (IService) element;
        		String title = service.getInfo(new NullProgressMonitor()).getTitle();
        		service.getPersistentProperties().put("title", title);
        		return title;
        	} else if(element instanceof IProcess) {
        		IProcess proc = (IProcess) element;
        		return proc.getInfo(new NullProgressMonitor()).getTitle();
        	} else if(element instanceof ISearch) {
        		ISearch search = (ISearch) element;
        		return search.getInfo(new NullProgressMonitor()).getTitle();
        	} else {
        		IResolveFolder folder = (IResolveFolder) element;
        		return folder.getID().toString();
        	}
        	} catch(IOException e) {
        		CatalogUIPlugin.log("Error fetching the Title for the resource", e); //$NON-NLS-1$
        	}
        }
        return super.getText(element);
    }

    /**
     * Obtain image for the provided element.
     * <p>
     * To accomplish this quickly we simply make use of constants from CatalogUIPlugin. We need a
     * second pass that makes use of the real icon from the real resource.
     * </p>
     * 
     * @param element is expeced to be IResolve
     * @return the image used to label the element, or <code>null</code> if there is no image for
     *         the given object
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    public Image getImage( Object element ) {
        if (element instanceof IResolve) {
            return CatalogUIPlugin.image((IResolve) element);
        }
        return super.getImage(element);
    }
}