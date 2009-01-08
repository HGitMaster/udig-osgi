package net.refractions.udig.tutorials.raster;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class RasterTutorial {

    public static void main( String[] args ) throws Exception {
        URL imageURL = RasterTutorial.class.getResource("image.png");
        
        BufferedImage image = ImageIO.read(imageURL);
        
        //coordinates of our raster image, in lat/lon
        double minx = -92.36918018580701;
        double miny = -49.043520894708884;
        
        double maxx = -42.25153935511384;
        double maxy = 2.1002762835725868;
        
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        ReferencedEnvelope envelope = new ReferencedEnvelope(minx,maxx,miny,maxy,crs);
        
        String name = "GridCoverage";
        
        GridCoverageFactory factory = new GridCoverageFactory();        
        GridCoverage2D gridCoverage = (GridCoverage2D) factory.create(name,image,envelope);
        
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:24882");

        RenderingHints hints = new RenderingHints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);        
        DefaultProcessor processor = new DefaultProcessor(hints);
        
        GridCoverage2D reprojected = gridCoverage.view(ViewType.GEOPHYSICS);
                
        ParameterValueGroup param = processor.getOperation("Resample").getParameters();
        param.parameter("Source").setValue( reprojected );
        param.parameter("CoordinateReferenceSystem").setValue(targetCRS);
        param.parameter("InterpolationType").setValue("NearestNeighbor");
        
        reprojected = (GridCoverage2D) processor.doOperation(param);
        
        reprojected = reprojected.view(ViewType.RENDERED);
        
        ViewerOld.show(gridCoverage, "Normal Grid Coverage");
        ViewerOld.show(reprojected, "Reprojected Grid Coverage");
    }    
}
