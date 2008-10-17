package net.crischan.udig.arcgrid;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.rasterings.AbstractRasterGeoResource;
import net.refractions.udig.catalog.rasterings.AbstractRasterService;

import org.eclipse.core.runtime.IProgressMonitor;

public class ArcGridServiceImplementation extends AbstractRasterService {	
	private ArcGridServiceInfo info;

	public ArcGridServiceImplementation(URL id, org.geotools.coverage.grid.io.GridFormatFactorySpi factory) {
		super(id, factory);
	}

	@Override
	public IServiceInfo getInfo(IProgressMonitor monitor) throws IOException {
		if (monitor != null)
			monitor.beginTask("ArcGrid loading", 2);

		if (this.info == null) {
			if (monitor != null)
				monitor.worked(1);
			this.info = new ArcGridServiceInfo(this);
		}
		
		if (monitor != null)
			monitor.done();
		
		return this.info;
	}

    @Override
    public Map<String, Serializable> getConnectionParams() {
        return new ArcGridServiceExtension().createParams(getIdentifier());
    }
	@Override
	public List<AbstractRasterGeoResource> resources(IProgressMonitor monitor) throws IOException {
		if (monitor != null) {
			String msg = MessageFormat.format("Connecting to", new Object[]  {});
			monitor.beginTask(msg, 5);
		}
		
		if (reader != null && monitor != null) {
			monitor.worked(3);
		}
		
		ArcGridGeoResourceImplementation res = new ArcGridGeoResourceImplementation(this, getTitle());

		List<AbstractRasterGeoResource> list = new ArrayList<AbstractRasterGeoResource>();
		list.add(res);
		
		if (monitor != null)
			monitor.done();
		
		return list;
	}
	/**
	 * Used by ArcGridGeoResourceImplementation to set
	 * connection status.
	 *
	 * @param message
	 */
	void setStatusMessage( Exception message ){
	    if( message == null ){
	        super.status = Status.CONNECTED;
	    }
	    else {
	        super.status = Status.BROKEN;
	        super.message = message;
	    }
	}
}