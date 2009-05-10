/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2008, Refractions Research Inc.
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
package net.refractions.udig.catalog.wmsc.server;

import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A TileRangeInMemory represents a set of Tiles in a given bounds and stores them in memory.  
 * This TileRange is responsible for fetching the individual tiles if they are not yet
 * "complete" and ready to paint, which it does through a TiledWebMapServer.  The 
 * TileRange really only lives for the cycle of fetching all the tiles.  Once the 
 * TileRange has finished loading (when all the tiles are complete), its use is 
 * pretty much done.
 * 
 * Every time a tile is "completed", all listeners will be notified.
 * 
 * @author GDavis
 * @since 1.2.0
 */
public class TileRangeInMemory extends AbstractTileRange {
     
    /**
     * TileRange that holds the tiles in memory using a soft hash map.
     * 
     * @param server The Server to fetch the tiles from
     * @param tileset TileSet of rendered content
     * @param bounds Optional bounds (may be null) that contain the provided tiles
     * @param tiles The tiles that we wish to fetch; must be from the provided tileset
     * @param requestTileWorkQueue Queue of worker threads that can be used to fetch tiles
     */
    public TileRangeInMemory(TiledWebMapServer server, TileSet tileset, Envelope bounds, 
            Map<String, Tile> tiles, TileWorkerQueue requestTileWorkQueue) {
        super(server, tileset, bounds, tiles, requestTileWorkQueue);
    }

    /**
     * Cache the tile in the required method, but we are not caching anything beyong
     * what is currently on screen, so nothing to cache here.
     */
	public void cacheTile(Tile tile) {
		// TODO: cache in memory more than just the tiles on screen 
	}


    
}
