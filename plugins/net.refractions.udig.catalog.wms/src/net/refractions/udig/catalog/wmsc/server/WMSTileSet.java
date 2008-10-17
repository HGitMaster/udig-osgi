/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, Refractions Research Inc.
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
package net.refractions.udig.catalog.wmsc.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.refractions.udig.catalog.internal.wms.WmsPlugin;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Class represents a WMSC tile set.  See: 
 * <p>
 * http://wiki.osgeo.org/wiki/WMS_Tiling_Client_Recommendation#GetCapabilities_Responses
 *  </p>
 *  
 * @author Emily Gouge, Graham Davis (Refractions Research, Inc.)
 * @since 1.2.0
 */
public class WMSTileSet implements TileSet {

    /** Not fear factor! The factor applied to determine what scale zoom levels should switch at */
    private static final double SCALE_FACTOR = 0.2;

    /** a unique identifies */
    private int id;
    
    /** the TiledWebMapServer **/
    private TiledWebMapServer server;

	/** Coordinate Reference System of the Tiles */
    private CoordinateReferenceSystem crs;
    /**
     * SRSName (usually of the format "EPSG:4326")
     */
    private String epsgCode;

    /** Data bounding box **/
    private ReferencedEnvelope bboxSrs;

    /** size of tiles - in pixels (often 512) */
    private int width;

    /** size of tiles - in pixels (often 512) */
    private int height;

    /** image format - MIME type? */
    private String format;

    /** List of layers (separated by comma?) */
    private String layers;

    /** Comma seperated list of resolutions in units per pixel */
    private String resolutions;

    /** Parsed out resolutions - the strict values from resolutions */
    private double[] dresolutions;

    /**
     * Parsed out resolutions - the relaxed values so we do not request more data that can be drawn
     * per pixel. (Often this amounts 1.2 real pixels per on screen pixel - see SCALE_FACTOR
     */
    private double[] mresolutions; // the scale at which we will switch zoom levels

    /** styles */
    private String styles;

    /** map of tiles 
     * NOTE:  This is a WEAKHashMap because we don't want to run out of
     * memory storing all the tiles.  The garbage collector should clean
     * up less-used keys and their objects as necessary. 
     **/
    private Map<String, Tile> tiles = new WeakHashMap<String, Tile>();

    public WMSTileSet() {
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setCoorindateReferenceSystem(java.lang.String)
	 */
    public void setCoorindateReferenceSystem( String epsg ) {
        this.epsgCode = epsg;
        try {
            this.crs = CRS.decode(epsg);
        } catch (Exception ex) {
            WmsPlugin.log("Cannot decode tile epsg code: " + epsg, ex); //$NON-NLS-1$
        }
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getCoordinateReferenceSystem()
	 */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setBoundingBox(org.geotools.data.ows.CRSEnvelope)
	 */
    public void setBoundingBox( CRSEnvelope bbox ) {
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.decode(bbox.getEPSGCode());
        } catch (Exception ex) {
            WmsPlugin.log("Cannot decode tile epsg code: " + bbox.getEPSGCode(), ex); //$NON-NLS-1$
        }
        bboxSrs = new ReferencedEnvelope(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox
                .getMaxY(), crs);
        updateID();
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setWidth(int)
	 */
    public void setWidth( int width ) {
        this.width = width;
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setStyles(java.lang.String)
	 */
    public void setStyles( String styles ) {
        this.styles = styles;
        updateID();
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setHeight(int)
	 */
    public void setHeight( int height ) {
        this.height = height;
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setFormat(java.lang.String)
	 */
    public void setFormat( String format ) {
        this.format = format;
        updateID();
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setLayers(java.lang.String)
	 */
    public void setLayers( String layers ) {
        this.layers = layers;
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setResolutions(java.lang.String)
	 */
    public void setResolutions( String res ) {
        this.resolutions = res;
        String[] sres = resolutions.split(" "); //$NON-NLS-1$

        double[] dres = new double[sres.length];
        for( int i = 0; i < sres.length; i++ ) {
            dres[i] = Double.parseDouble(sres[i]);
        }
        this.dresolutions = dres;

        // compute resolutions where the zoom will switch
        mresolutions = new double[dresolutions.length - 1];
        for( int i = 0; i < dresolutions.length - 1; i++ ) {
            mresolutions[i] = ((dresolutions[i] - dresolutions[i + 1]) * SCALE_FACTOR)
                    + dresolutions[i + 1];
        }
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getNumLevels()
	 */
    public int getNumLevels() {
        return this.dresolutions.length;
    }

    /**
     * Given a scale factor for the resulting image it finds the zoom level that matches the scale
     * factor best.
     * 
     * @param scale
     * @return resolution of the zoom level that best matches the scale factor
     */
    private double findAppropriateZoomLevel( double scale ) {

        if (scale > mresolutions[0]) {
            return dresolutions[0];
        }
        for( int i = 1; i < mresolutions.length; i++ ) {
            if (mresolutions[i - 1] >= scale && mresolutions[i] < scale) {
                return dresolutions[i];
            }
        }
        // maximum zoom
        return this.dresolutions[this.dresolutions.length - 1];
    }

    /**
     * Creates a wmsc query string from the given bounds.
     * <p>
     * This string is *very* carefully constructed with the assumption that all the getFormat(),
     * getEPSG(), getLayers() methods return Strings that are valid , consistent and ready to go.
     * 
     * @param tile
     * @return
     */
    @SuppressWarnings("nls")
    public String createQueryString( Envelope tile ) {
        String query = "service=WMS&request=getMap&tiled=true&format=" + getFormat() + "&srs=" + getEPSGCode()
                + "&layers=" + getLayers() + "&bbox=" + tile.getMinX() + "," + tile.getMinY() + ","
                + tile.getMaxX() + "," + tile.getMaxY() + "&styles=" + getStyles();
        return query;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getTilesFromViewportScale(com.vividsolutions.jts.geom.Envelope, double)
	 */
    public Map<String, Tile> getTilesFromViewportScale( Envelope bounds, double viewportScale ) {
        double scale = findAppropriateZoomLevel(viewportScale);
        return getTilesFromZoom(bounds, scale);
    }
    
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getTilesFromZoom(com.vividsolutions.jts.geom.Envelope, double)
	 */
    public Map<String, Tile> getTilesFromZoom( Envelope bounds, double zoom ) {

        double xscale = width * zoom;
        double value = bounds.getMinX() - bboxSrs.getMinX();
        
        double minx = Math.floor(value / xscale) * xscale + bboxSrs.getMinX();
        value = bounds.getMaxX() - bboxSrs.getMinX();
        double maxx = Math.ceil(value / xscale) * xscale + bboxSrs.getMinX();

        double yscale = height * zoom;
        value = bounds.getMinY() - bboxSrs.getMinY();
        double miny = Math.floor(value / yscale) * yscale + bboxSrs.getMinY();
        value = bounds.getMaxY() - bboxSrs.getMinY();
        double maxy = Math.ceil(value / yscale) * yscale + bboxSrs.getMinY();
        Map<String, Tile> viewportTiles = new HashMap<String, Tile>();
      
        for (int x = 0; x < (maxx-minx)/xscale; x++){
            for (int y = 0; y < (maxy-miny) / yscale; y ++){
                Envelope e = new Envelope(x*xscale+minx, (x+1) * xscale+minx, y * yscale+miny, (y +1)* yscale + miny);
                if (e.getMaxX() <= bboxSrs.getMinX() || e.getMinX() >= bboxSrs.getMaxX()
                        || e.getMaxY() <= bboxSrs.getMinY() || e.getMinY() >= bboxSrs.getMaxY()) {
                    // outside of bounds ignore
                } else {
                    // tile is within the bounds, create it if necessary and
                    // add it to the map
                    String tileid = WMSTile.buildId(e, zoom);
                    Tile tile;
                    synchronized (tiles) {
                        if (!tiles.containsKey(tileid) || tiles.get(tileid) == null) {
                            tile = new WMSTile(server, this, e, zoom);
                            tiles.put(tileid, tile);
                        } else {
                            tile = tiles.get(tileid);
                        }    
                    }
                    
                    // create the tile position within the tilerange grid for this scale
                    double topleft_x = bboxSrs.getMinX();
                    double topleft_y = bboxSrs.getMaxY();
                    double tileleft_x = e.getMinX();
                    double tileleft_y = e.getMaxY();
                    
                    double spacex = tileleft_x - topleft_x;
                    double spacey = topleft_y - tileleft_y;
                    
                    int posx = (int)Math.round(spacex/xscale);
                    int posy = (int)Math.round(spacey/yscale);
                    
                    String position = posx + "_" + posy; //$NON-NLS-1$
                    tile.setPosition(position);
                    viewportTiles.put(tileid, tile);
                }
            }
        }
        return viewportTiles;
    }    

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getLayers()
	 */
    public String getLayers() {
        return this.layers;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getStyles()
	 */
    public String getStyles() {
        return this.styles;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getFormat()
	 */
    public String getFormat() {
        return this.format;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getEPSGCode()
	 */
    public String getEPSGCode() {
        return this.epsgCode;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getWidth()
	 */
    public int getWidth() {
        return this.width;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getHeight()
	 */
    public int getHeight() {
        return this.height;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getBounds()
	 */
    public ReferencedEnvelope getBounds() {
        return this.bboxSrs;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getId()
	 */
    public int getId() {
        return this.id;
    }
    
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getResolutions()
	 */
    /**
     * @returns a copy of the resolutions array
     */
    public double[] getResolutions(){
        return Arrays.copyOf(this.dresolutions, this.dresolutions.length);
    }
    
    /**
     * Create a unique identifier for the tileset from the various strings that define the tile set.
     */
    private void updateID(){
        //compute a hashset from the attributes;
        StringBuffer sb = new StringBuffer();
        
        if (this.epsgCode != null)
            sb.append(epsgCode);
        
        if (this.format != null)
            sb.append(this.format);
        
        if (this.layers != null)
            sb.append(layers);
        
        if (this.resolutions != null)
            sb.append(this.resolutions);
        if (this.styles != null)
            sb.append(this.styles);
        if (this.bboxSrs != null){
            sb.append(this.bboxSrs.getMinX());
            sb.append(this.bboxSrs.getMaxX());
            sb.append(this.bboxSrs.getMinY());
            sb.append(this.bboxSrs.getMaxY());
            sb.append(this.bboxSrs.getMinX());
        }
        if (this.bboxSrs != null && this.bboxSrs.getCoordinateReferenceSystem() != null) {
            sb.append(this.bboxSrs.getCoordinateReferenceSystem().hashCode());
        }
        
        sb.append(this.width);
        sb.append(this.height);
        
        this.id = sb.toString().hashCode();
    }
    
    public TiledWebMapServer getServer() {
		return server;
	}

	public void setServer(TiledWebMapServer server) {
		this.server = server;
	}    

}
