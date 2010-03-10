package net.refractions.udig.catalog.wmsc.server;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Class to help read and write for Tile images to disk in a structured
 * format that can be tested and loaded each time the program is run.
 * 
 * @author GDavis
 *
 */
public class TileImageReadWriter {
	
	private String baseTileFolder = ""; //$NON-NLS-1$
	private static final String baseSubTileFolder = "tilecache"; //$NON-NLS-1$
	private TiledWebMapServer server;
	
	public TileImageReadWriter(TiledWebMapServer server, String baseDir) {
		this.server = server;
		this.baseTileFolder = baseDir + File.separator + baseSubTileFolder; 
	}

	/** 
	 * Get a file representing the given tile on disk.  Will create the file and any parent
	 * directories if they don't yet exist.
	 * 
	 * @param tile
	 * @param filetype
	 * @return
	 */
	public File getTileFile(Tile tile, String filetype) {
		String filename = getTileFileName(tile, filetype);
    	File file = new File(filename);
    	if (!file.exists()) {
    		try {
    			file.mkdirs();
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
    	}
    	//System.out.println("file: "+file.getAbsolutePath());
    	return file;
	}

	/**
	 * Fetch the name of the file for the given tile
	 * 
	 * @param tile
	 * @param filetype
	 * @return
	 */
	public String getTileFileName(Tile tile, String filetype) {
		return getTileDirectoryPath(tile)+tile.getPosition()+"."+filetype; //$NON-NLS-1$
	}
	
	/**
	 * Fetch the directory path for the given tile.  Tile directory structure is as
	 * follow:
	 * <tile folder>\<server>\<layer names>_<EPSG code>_<image format>\<scale>\
	 * 
	 * @param tile
	 * @return
	 */
	public String getTileDirectoryPath(Tile tile) {
		URL service = this.server.getService();
		String serverURL = service.getHost()+"_"+service.getPath(); //$NON-NLS-1$
		serverURL = serverURL.replace('\\', '_'); 
		serverURL = serverURL.replace('/', '_'); 
		String layers = tile.getTileSet().getLayers();
		layers += "_"+tile.getTileSet().getEPSGCode();//$NON-NLS-1$
		layers += "_"+tile.getTileSet().getFormat(); //$NON-NLS-1$
		layers = layers.replace(',', '_'); 
		layers = layers.replace(':', '_'); 
		layers = layers.replace('\\', '_');
		layers = layers.replace('/', '_'); 
		layers = layers.replace(File.separator, "_"); //$NON-NLS-1$
		Double scale = tile.getScale();
		String scaleStr = scale.toString();
		scaleStr = scaleStr.replace('.', '_'); 
		return baseTileFolder + File.separator + serverURL + File.separator + 
		    layers + File.separator + scaleStr + File.separator; 
	}	

	/**
	 * Attempt to write the tile to file
	 * 
	 * @param tile
	 * @param filetype
	 * @return true on success
	 */
	public boolean writeTile(Tile tile, String filetype) {
		try {
			// lock on the tile so we aren't trying to read it as it is being written to
			Object lock = tile.getTileLock();
			synchronized (lock) {
				ImageIO.write(tile.getBufferedImage(), filetype, getTileFile(tile, filetype));
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Check if the given tile exists on disk already
	 * 
	 * @param tile
	 * @return does file already exist
	 */
	public boolean tileFileExists(Tile tile, String filetype) {
		String filename = getTileFileName(tile, filetype);
    	File file = new File(filename);
    	return file.exists();
	}
	
	/**
	 * Attempt to read given tile's image if it exists, and store it in the tile's
	 * bufferedImage.
	 * 
	 * @param tile
	 * @param filetype
	 * @return true on success 
	 */
	public boolean readTile(Tile tile, String filetype) {
		BufferedInputStream bis = null;
		BufferedImage image = null; 
		try {
			// lock on the tile so we aren't trying to write to it as it is being read
			Object lock = tile.getTileLock();
			synchronized (lock) {
				bis = new BufferedInputStream(new FileInputStream(getTileFile(tile, filetype)));
				image = ImageIO.read(bis);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (image != null) {
			tile.setBufferedImage(image);
			return true;
		}
		return false;
	}
	
	/**
	 * Clear the entire tile cache for for the base dir of these tiles
	 * 
	 * @return true on success
	 */
	public boolean clearCache() {
		File file = new File(baseTileFolder);
		return deleteDir(file);
	}
	
	/**
	 * Recursively deletes all subdirs and files of a directory and then deletes the given
	 * directory.  Returns false at the moment any attempts to delete fail.
	 * 
	 * @param dir
	 * @return true on success
	 */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
	

}
