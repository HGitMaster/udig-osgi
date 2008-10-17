package net.refractions.udig.catalog.wmsc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import net.refractions.udig.catalog.internal.wmsc.WMSCGeoResourceImpl;
import net.refractions.udig.catalog.wmsc.server.TileSet;

import net.refractions.udig.ui.operations.IOp;

public class WMSCPreloadTilesOp implements IOp {

	public void op(Display display, Object target, IProgressMonitor monitor)
			throws Exception {
		WMSCGeoResourceImpl wmscResource = (WMSCGeoResourceImpl) target;
		TileSet tileSet = wmscResource.getTileSet();
		WMSCTileUtils.preloadAllTilesOnDisk(tileSet);

	}

}
