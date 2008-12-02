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
package net.refractions.udig.render.gridcoverage.tool.info.internal;


import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.render.gridcoverage.basic.internal.Messages;
import net.refractions.udig.render.internal.gridcoverage.basic.RendererPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Literal;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * This class is the info panel for the ImageMosaic information.
 * <p>
 *
 * </p>
 * @author Emily Gouge (Refractions Research, Inc.)
 * @since 1.1.0
 */
public class MosaicInfoPanel {

    //GUI Components
    private Label txtFileName;
    private Label txtDate;
    private Label txtFileSize;
    private Label txtFileType;
    private ChannelViewer red;
    private ChannelViewer green;
    private ChannelViewer blue;
    private Composite grpBands ;
    
    // used to track the current selected feature and layer
    private ILayer currentLayer;
    private SimpleFeature currentFeature;
    
    private FilterFactory2 ff;
    
    private final UpdateImageMosaicJob updateJob = new UpdateImageMosaicJob();
    
    /**
     * Creates a new mosaic info panel
     */
    public MosaicInfoPanel( ) {
    }

    /**
     * Creates the panel control
     *
     * @param parent
     * @return
     */
    public Composite createControl(Composite parent){
        
        SelectionListener listener = new SelectionListener(){

            public void widgetDefaultSelected( SelectionEvent e ) {
            }

            public void widgetSelected( SelectionEvent e ) {
                applyUpdates();
            }};
            
        Composite all = new Composite(parent, SWT.NONE);
        all.setLayout(new GridLayout(1, false));
        Group g = new Group(all, SWT.SHADOW_ETCHED_IN);
        g.setText(Messages.MosaicInfoPanel_FileInformationHeader);
        g.setLayout(new GridLayout(4, false));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        g.setLayoutData(gd);
        
        createLabel(g,Messages.MosaicInfoPanel_FileLabel);
        txtFileName = createLabel(g);
        
        createLabel(g, Messages.MosaicInfoPanel_FileDateLabel);
        txtDate = createLabel(g);
        
        createLabel(g, Messages.MosaicInfoPanel_FileTypeLabel);
        txtFileType = createLabel(g);
        
        createLabel(g, Messages.MosaicInfoPanel_FileSizeLabel);
        txtFileSize = createLabel(g);
        
        grpBands = new Composite(all, SWT.SHADOW_ETCHED_IN);
        grpBands.setLayout(new GridLayout(3, false));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        grpBands.setLayoutData(gd);
        
        red = new ChannelViewer(Messages.MosaicInfoPanel_RedBandLabel, listener);
        red.createControl(grpBands);
        green = new ChannelViewer(Messages.MosaicInfoPanel_GreenBandLabel, listener);
        green.createControl(grpBands);
        blue = new ChannelViewer(Messages.MosaicInfoPanel_BlueBandLabel, listener);
        blue.createControl(grpBands);

        return all; 
    }
    
    /**
     * Creates an empty label
     */
    private Label createLabel(Composite p){
        Label l = new Label(p, SWT.NONE);
        l.setText(""); //$NON-NLS-1$
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return l;
    }
    
    /**
     * Creates a label with the given text
     *
     * @param p
     * @param text
     * @return
     */
    private Label createLabel(Composite p, String text){
        Label l = new Label(p, SWT.NONE);
        l.setText(text);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        l.setLayoutData(gd);
        return l;
    }
    
    /**
     * Updates the information displayed based on the
     * selected feature and layer
     *
     * @param f
     * @param layer
     */
    public void updateInfo(SimpleFeature f, ILayer layer){
       if (f == null){
           //nothing found clear selection
           txtFileName.setText(""); //$NON-NLS-1$
           txtFileType.setText(""); //$NON-NLS-1$
           txtFileSize.setText(""); //$NON-NLS-1$
           txtDate.setText(""); //$NON-NLS-1$
           
           red.setBands(null);
           green.setBands(null);
           blue.setBands(null);
           
           grpBands.setVisible(false);
           return;
       }
        
        try{
            ImageMosaicReader imageReader = (ImageMosaicReader)layer.getGeoResource().resolve(AbstractGridCoverage2DReader.class, null);
            
            //get File
            String locationAttributeName = imageReader.getLocationAttributeName();
            Object attribute = f.getAttribute(locationAttributeName);
            Literal eq = getFilterFactory().literal(attribute);
            Filter filter = getFilterFactory().equals(getFilterFactory().property(locationAttributeName),eq );
            
            File file = imageReader.getImageFile(filter);
            //update file info
            if (file == null){
                txtFileName.setText(Messages.MosaicInfoPanel_NoDataText);
                txtFileType.setText(""); //$NON-NLS-1$
                txtFileSize.setText(""); //$NON-NLS-1$
                txtDate.setText(""); //$NON-NLS-1$
            }else{
                txtFileName.setText(file.getName());
                Date date = new Date(file.lastModified());
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
                txtDate.setText(format.format(date));
                txtFileType.setText( file.getName().substring(file.getName().lastIndexOf('.')) );
                txtFileSize.setText(file.length() / 1000.0 + "KB"); //$NON-NLS-1$
            }
            
            //if possible update band information
            if (file != null && imageReader.hasBandColorAttributes()){
                grpBands.setVisible(true);
                String bandAttribute = imageReader.getBandsAttributeName(); 
                String colorEnhancementAttribute = imageReader.getColorCorrectionAttributeName();

                //here I am going to need to read in the file to get the possible bands
                //PlanarImage image = JAI.create("fileload", file.toString()); //$NON-NLS-1$
                final ParameterBlock pb = new ParameterBlock();
                pb.add(ImageIO.createImageInputStream(file));
                pb.add(0);
                pb.add(new Boolean(false));
                pb.add(new Boolean(false));
                pb.add(new Boolean(false));
                pb.add(null);
                pb.add(null);
                pb.add(null);
                pb.add(null);
                RenderedImage tmp = JAI.create("ImageRead", pb); //$NON-NLS-1$
                PlanarImage image = PlanarImage.wrapRenderedImage(tmp);
                            
                String[] bands = new String[image.getNumBands()];
                for( int i = 0; i < image.getNumBands(); i++ ) {
                    bands[i] = Messages.MosaicInfoPanel_BandLabel + i ;
                }
                                                
                red.setBands(bands);
                green.setBands(bands);
                blue.setBands(bands);
                
                int[] selectedBands = parseBands((String)f.getAttribute(bandAttribute));
                double[] colorEnhancement =  parseColors((String)f.getAttribute(colorEnhancementAttribute));
                
                red.set(selectedBands[0], colorEnhancement[0]);
                green.set(selectedBands[1], colorEnhancement[1]);
                blue.set(selectedBands[2], colorEnhancement[2]);
                
                this.currentLayer = layer;
                this.currentFeature = f;
            }else{
                //doesn't support band and color enhancements
                grpBands.setVisible(false);
            }
        }catch (Exception ex){
            RendererPlugin.log("Cannot update feature information", ex); //$NON-NLS-1$
        }
    }
    
    
    private int[] parseBands(String bands){
        String[] sband = bands.split(","); //$NON-NLS-1$
        int[] iband = new int[3];
        for( int i = 0; i < sband.length; i++ ) {
            iband[i] = Integer.parseInt(sband[i]);
        }
        return iband;
    }
    
    private double[] parseColors(String bands){
        String[] sband = bands.split(","); //$NON-NLS-1$
        double[] colors = new double[3];
        for( int i = 0; i < sband.length; i++ ) {
            colors[i] = Double.parseDouble(sband[i]);
        }
        return colors;
    }
    
    /**
     * Applies the modifications to the image mosaic and redraw
     * the changed tile
     */
    private void applyUpdates() {

        final int redBand = red.getBandIndex();
        final int greenBand = green.getBandIndex();
        final int blueBand = blue.getBandIndex();

        if (redBand == greenBand || redBand == blueBand || greenBand == blueBand){
            //display error message and do nothing
            String error = Messages.MosaicInfoPanel_InvalidBandSelectionMessage;
            red.setError(error);
            green.setError(error);
            blue.setError(error);
            return;
        }
        red.setError(null);
        green.setError(null);
        blue.setError(null);
        
        final double redColor = red.getColorCorrectionValue();
        final double greenColor = green.getColorCorrectionValue();
        final double blueColor = blue.getColorCorrectionValue();
        
        try {

            final ImageMosaicReader imageReader = (ImageMosaicReader) currentLayer.getGeoResource()
                    .resolve(GridCoverageReader.class, null);
            updateJob.setData(imageReader, currentLayer, currentFeature, redBand, greenBand, blueBand, redColor, greenColor, blueColor);
            updateJob.schedule(100);
        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public FilterFactory2 getFilterFactory(){
        if (ff == null){
            ff = CommonFactoryFinder.getFilterFactory2(null);
        }
        return ff;
    }
    
    /**
     * Class to track an work of the update job so only one job is scheduled and 
     * it is always updated to the latest
     */
    private class UpdateImageMosaicJob extends Job{
        
        private ImageMosaicReader imageReader = null;
        private int[] bands;
        private double colors[];
        private ILayer layer;
        private SimpleFeature feature;
        public UpdateImageMosaicJob(){
            super("Update Image Moasic Job"); //$NON-NLS-1$
        }
        
        public void setData(ImageMosaicReader reader, ILayer layer, SimpleFeature f, int redBand, int greenBand, int blueBand, double redColor, double greenColor, double blueColor){
            this.imageReader = reader;
            this.bands = new int[]{redBand, greenBand, blueBand};
            this.colors = new double[]{redColor, greenColor, blueColor};
            this.layer = layer;
            this.feature = f;
        }
        
        protected IStatus run( IProgressMonitor monitor ) {
            
            Filter filter = getFilterFactory().equals(
                    ff.property(this.imageReader.getLocationAttributeName()),
                    ff.literal(this.feature.getAttribute(this.imageReader
                            .getLocationAttributeName())));
            
            imageReader.updateBandSelection(filter,this.bands, this.colors);

            layer.refresh(((Geometry) this.feature.getDefaultGeometry())
                    .getEnvelopeInternal());
            return Status.OK_STATUS;
        }
    }
}

