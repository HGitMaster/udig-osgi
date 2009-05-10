package net.crischan.udig.arcgrid;

import java.io.IOException;

import net.crischan.udig.arcgrid.internal.Messages;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.rasterings.AbstractRasterGeoResource;
import net.refractions.udig.catalog.rasterings.AbstractRasterService;

import org.eclipse.core.runtime.IProgressMonitor;


public class ArcGridGeoResourceImplementation extends AbstractRasterGeoResource {
	public ArcGridGeoResourceImplementation(AbstractRasterService service, String name) {
		super(service, name);
	}

	@Override
	protected IGeoResourceInfo createInfo(IProgressMonitor monitor) throws IOException {
		if (monitor != null) {
			monitor.beginTask(Messages.ArcGridGeoResourceImplementation_Connecting, 2);
			monitor.worked(1);
		}
		
		if (this.info == null) {
			this.info = new ArcGridGeoResourceInfo(this);
			if (monitor != null)
				monitor.worked(1);
		}
		
		if (monitor != null)
			monitor.done();
		
		return this.info;
	}
	
	public ArcGridServiceImplementation getService(){
	    return (ArcGridServiceImplementation) service;
	}
}