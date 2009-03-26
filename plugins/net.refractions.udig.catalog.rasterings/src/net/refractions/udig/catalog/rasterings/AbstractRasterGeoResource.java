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
package net.refractions.udig.catalog.rasterings;

import java.awt.Rectangle;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.rasterings.internal.Messages;
import net.refractions.udig.ui.ProgressManager;
import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;

/**
 * Provides a handle to a raster resource allowing the service to be lazily
 * loaded.
 * <p>
 * This class provides functionality common to GridCoverage based resources.
 * 
 * @author mleslie
 * @since 0.6.0
 */
public abstract class AbstractRasterGeoResource extends IGeoResource {
	private volatile SoftReference<GridCoverage> coverage;

	private ParameterGroup readParams;

	private String name;

	private Throwable msg;

	protected Lock lock = new UDIGDisplaySafeLock();

	/**
	 * Construct <code>AbstractRasterGeoResource</code>.
	 * 
	 * @param service
	 *            The service creating this resource.
	 * @param name
	 *            Human readable name of this resource.
	 */
	public AbstractRasterGeoResource(AbstractRasterService service, String name) {
		this.service = service;
		if (name == null) {
			name = service.getIdentifier().getFile();
			int slash = name.lastIndexOf('/');
			name = name.substring((slash == -1 && slash < name.length() - 1 ? 0
					: name.lastIndexOf('/')) + 1,
					(name.lastIndexOf('.') == -1 ? name.length() : name
							.lastIndexOf('.')));

		}
		this.name = name;
	}

	public Status getStatus() {
		return service.getStatus();
	}

	public Throwable getMessage() {
		if (msg != null) {
			return msg;
		} else {
			return service.getMessage();
		}
	}

	/**
	 * Retrieves the parameters used to create the GridCoverageReader for this
	 * resource. This simply delegates the creation of these parameters to a
	 * GridFormat.
	 * 
	 * @return ParameterGroup describing the GeoResource
	 */
	public synchronized ParameterGroup getReadParameters() {
		if (this.readParams == null) {

			DefaultParameterDescriptor<GridGeometry> gridGeometryDescriptor = getWorldGridGeomDescriptor();

			// Stolen from WorldImageFormat, as mInfo is not externally
			// accesible
			HashMap<String, Object> info1 = new HashMap<String, Object>();
			info1.put("name", "Raster"); //$NON-NLS-1$//$NON-NLS-2$
			info1.put("description", //$NON-NLS-1$
					"A raster file accompanied by a spatial data file"); //$NON-NLS-1$
			info1.put("vendor", "Geotools"); //$NON-NLS-1$ //$NON-NLS-2$
			info1
					.put(
							"docURL", "http://www.geotools.org/WorldImageReader+formats"); //$NON-NLS-1$ //$NON-NLS-2$
			info1.put("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			this.readParams =  new ParameterGroup(new DefaultParameterDescriptorGroup(
					info1, new GeneralParameterDescriptor[] { gridGeometryDescriptor }));
		}
		return this.readParams;
	}

	/**
	 * Finds or creates the GridCoverage for this resource.
	 * 
	 * @return GridCoverage for this GeoResource
	 * @throws IOException
	 */
	public synchronized Object findResource() throws IOException {
		lock.lock();
		try {
			if (this.coverage == null  || this.coverage.get()==null ) {
				try {
					AbstractGridCoverage2DReader reader = this.service(new NullProgressMonitor()).getReader(null);
					ParameterGroup pvg = getReadParameters();
					List list = pvg.values();
					@SuppressWarnings("unchecked") GeneralParameterValue[] values = 
					(GeneralParameterValue[]) list
							.toArray(new GeneralParameterValue[0]);
					this.coverage = new SoftReference<GridCoverage>(reader.read(values));
				} catch (Throwable t) {
					msg = t;
					RasteringsPlugin.log("error reading coverage", t);
					return null;
				}
			}
			return this.coverage.get();
		} finally {
			lock.unlock();
		}
	}

	public URL getIdentifier() {
		try {
			return new URL(this.service.getIdentifier().toString()
					+ "#" + this.name); // $NON_NLS-1$
			// //$NON-NLS-1$
		} catch (MalformedURLException ex) {
			msg = ex;
			return this.service.getIdentifier();
		}
	}

	public <T> T resolve(Class<T> adaptee, IProgressMonitor monitor)
			throws IOException {
		if (monitor == null)
			monitor = ProgressManager.instance().get();
		try {
			if (monitor != null)
				monitor
						.beginTask(Messages.AbstractRasterGeoResource_resolve,
								3);
			if (adaptee == null) {
				return null;
			}
			if (adaptee.isAssignableFrom(AbstractGridCoverage2DReader.class)) {
				return adaptee.cast(service(monitor).getReader(monitor));
			}
			if (adaptee.isAssignableFrom(GridCoverage.class)) {
				return adaptee.cast(findResource());
			}
			if (adaptee.isAssignableFrom(IGeoResourceInfo.class)) {
				if (monitor != null)
					monitor.done();
				return adaptee.cast(createInfo(monitor));
			}
			if (adaptee.isAssignableFrom(ParameterGroup.class)) {
				if (monitor != null)
					monitor.done();
				return adaptee.cast(getReadParameters());
			}			
			return super.resolve(adaptee, monitor);
		} finally {
			monitor.done();
		}
	}

	public <T> boolean canResolve(Class<T> adaptee) {
		if (adaptee == null)
			return false;
		return adaptee.isAssignableFrom(IGeoResourceInfo.class)
				|| adaptee.isAssignableFrom(IService.class)
				|| adaptee.isAssignableFrom(GridCoverage.class)
				|| adaptee.isAssignableFrom(AbstractGridCoverage2DReader.class)
				|| super.canResolve(adaptee);
	}

	protected abstract IGeoResourceInfo createInfo(IProgressMonitor monitor)
			throws IOException;

	/**
	 * Returns A recommended {@link ParameterDescriptor} for all {@link GridCoverageReader}s. 
	 * This parameter requests an overview that is 100,100 of the image.
	 * <p>
	 * This is not intended to be overridden rather it is a useful method for getReadParamaters to call. 
	 * </p>
	 * 
	 * @return parameter requesting an overview that is 100,100 of the image.
	 */
	protected DefaultParameterDescriptor<GridGeometry> getWorldGridGeomDescriptor() {
		// this is a little dumb
		GridRange gridRange = new GeneralGridRange(new Rectangle(0,0,100,100));
		ReferencedEnvelope env = new ReferencedEnvelope(-180.0, 180.0,-90.0, 90.0, DefaultGeographicCRS.WGS84);
		GridGeometry2D world = new GridGeometry2D(gridRange, env);
	
		DefaultParameterDescriptor<GridGeometry> gridGeometryDescriptor = new DefaultParameterDescriptor<GridGeometry>(
				AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString(),
				GridGeometry.class, null, world); //$NON-NLS-1$ //$NON-NLS-2$
		return gridGeometryDescriptor;
	}

	@Override
	public AbstractRasterService service(IProgressMonitor monitor) throws IOException {
		return (AbstractRasterService) super.service(monitor);
	}
}
