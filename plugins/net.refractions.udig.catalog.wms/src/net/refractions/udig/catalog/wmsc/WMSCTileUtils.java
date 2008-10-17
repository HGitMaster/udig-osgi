package net.refractions.udig.catalog.wmsc;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.catalog.wmsc.server.Tile;
import net.refractions.udig.catalog.wmsc.server.TileRangeOnDisk;
import net.refractions.udig.catalog.wmsc.server.TileWorkerQueue;
import net.refractions.udig.catalog.wmsc.server.TileSet;
import net.refractions.udig.catalog.wms.internal.Messages;
import net.refractions.udig.ui.PlatformGIS;

/**
 * A collection of utility methods for managing WMS-C Tiles
 * 
 * @author GDavis
 *
 */
public class WMSCTileUtils {
	
	/**
	 * The maximum number of tile requests to send to a server at once before
	 * waiting to send the next group of requests off (used for preloading all tiles).
	 */
	private static int maxTileRequestsPerGroup = 16;
	
	/**
	 * Given a TileSet, use it's bounds to request every tile in it and store it
	 * on disk.  This is run in a blocking dialog since the thousands of continuous
	 * requests otherwise bog down udig.  It can be cancelled and does provide progress
	 * feedback.
	 * 
	 * @param tileset
	 */	
	public static void preloadAllTilesOnDisk(TileSet tileset) {
        final IRunnableWithProgress preloadTiles = new PreloadTilesClass(tileset);
        String taskname = MessageFormat.format(Messages.WMSCTileUtils_preloadtask, tileset.getLayers());
		PlatformGIS.runInProgressDialog(taskname, false, preloadTiles, true);
	}
	
	private static class PreloadTilesClass implements IRunnableWithProgress {
		
		private TileSet tileset;
		private int requestCount;
		private double percentPerTile;
		private IProgressMonitor monitor;
		private Envelope tileRangeBounds;
		private Map<String, Tile> tileRangeTiles;
		private TileWorkerQueue requestTileWorkQueue;
		private TileWorkerQueue writeTileWorkQueue;
		
		public PreloadTilesClass(final TileSet tileset) {
			this.tileset = tileset;
		}
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			this.monitor = monitor;
			String taskname = MessageFormat.format(Messages.WMSCTileUtils_preloadtask, tileset.getLayers());
			this.monitor.beginTask(taskname, 100);
			
        	// for each zoom level of tiles offered by the server, loop through
        	// and request groups of tiles at a time so we don't overload the
        	// server with too many requests at once.
			double[] resolutions = tileset.getResolutions();
			double percentPerResolution = resolutions.length/100;
    		tileRangeTiles = new HashMap<String, Tile>();
    		requestTileWorkQueue = new TileWorkerQueue(TileWorkerQueue.defaultWorkingQueueSize);
    		writeTileWorkQueue = new TileWorkerQueue(TileWorkerQueue.defaultWorkingQueueSize);
    		int resCount = 0;
    		for (double resolution : resolutions) {
    			resCount++;
    			Map<String, Tile> tilesInZoom = tileset.getTilesFromZoom(tileset.getBounds(), resolution);
    			String subname = MessageFormat.format(Messages.WMSCTileUtils_preloadtasksub, tilesInZoom.size(), resCount, resolutions.length);
    			this.monitor.setTaskName(subname);      		
    			requestCount = 0;
        		Iterator<Entry<String, Tile>> iterator = tilesInZoom.entrySet().iterator();
        		// set the percent value for tiles in this resolution
        		percentPerTile = percentPerResolution/tilesInZoom.size();
        		tileRangeBounds = new Envelope();
        		tileRangeTiles.clear();
        		while (iterator.hasNext()) {
        			if (monitor.isCanceled()) {
        				cleanup();
        				return;
        			}
        			requestCount++;
        			Entry<String, Tile> next = iterator.next();
        			tileRangeBounds.expandToInclude(next.getValue().getBounds());
        			tileRangeTiles.put(next.getKey(), next.getValue());
        			
        			// if we have 16 ready to go tiles, send them off
        			if (requestCount == maxTileRequestsPerGroup) {
        				doRequestAndResetVars();
        			}
        		}
            	// if the requests is no reset at 0 then there are remaining tiles to fetch
            	if (requestCount != 0) {
            		doRequestAndResetVars();
            	}
            	// if the percent per tile is 0, then update the monitor now with the
            	// value for a complete resolution
            	if ((int)percentPerTile < 1) {
            		this.monitor.worked((int)percentPerResolution);
            	}
            	if ((int)percentPerResolution < 1) {
            		this.monitor.worked(1);
            	}
        	}
        	
    		cleanup();
        	return;
		}
		
		/**
		 * Load the tiles in the tilerange and then reset the vars
		 * 
		 */
		private void doRequestAndResetVars() {
			TileRangeOnDisk tileRangeOnDisk = new TileRangeOnDisk(tileset.getServer(), tileset, tileRangeBounds, tileRangeTiles, requestTileWorkQueue, writeTileWorkQueue);
			tileRangeOnDisk.loadTiles(new NullProgressMonitor()); // blocks until all tiles are loaded
			
			// update monitor
			this.monitor.worked((int)percentPerTile*tileRangeOnDisk.getTileCount());
			
			// reset vars
			requestCount = 0;
			tileRangeBounds = new Envelope();
			tileRangeTiles.clear();
		}
		
		/**
		 * Task is complete or cancelled, so cleanup the threads and other objects
		 */
		private void cleanup() {
			requestTileWorkQueue.dispose();
			writeTileWorkQueue.dispose();
			requestTileWorkQueue = null;
			writeTileWorkQueue = null;
			this.monitor.done();
		}
		
	};	

}
