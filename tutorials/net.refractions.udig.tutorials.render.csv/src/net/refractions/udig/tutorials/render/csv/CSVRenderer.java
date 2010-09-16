package net.refractions.udig.tutorials.render.csv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.render.impl.RendererImpl;
import net.refractions.udig.project.render.RenderException;
import net.refractions.udig.tutorials.catalog.csv.CSV;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class CSVRenderer extends RendererImpl {

    @Override
    public void render( IProgressMonitor monitor ) throws RenderException {
        Graphics2D g = getContext().getImage().createGraphics();
        render(g, monitor);
    }

    /**
     * This is an example of making a renderer that is capable of transforming from the data crs to
     * the world crs.
     * <p>
     * Of note:
     * <ul>
     * <li>Use layer.getCRS() for the data CRS - this lets your users "correct" data for which no
     * CRS is provided.
     * <li>transform 1) data to world 2) worldToPixel
     * </ul>
     */
    public void render( Graphics2D g, IProgressMonitor monitor ) throws RenderException {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask("csv render", 100);

        CsvReader reader = null;
        try {
            g.setColor(Color.BLACK);
            ILayer layer = getContext().getLayer();
            IGeoResource resource = layer.findGeoResource(CSV.class);
            if (resource == null)
                return;

            CoordinateReferenceSystem dataCRS = layer.getCRS();
            CoordinateReferenceSystem worldCRS = context.getCRS();
            MathTransform dataToWorld = CRS.findMathTransform(dataCRS, worldCRS, false);

            ReferencedEnvelope bounds = getRenderBounds();
            monitor.subTask("connecting");
            
            CSV csv = resource.resolve(CSV.class, new SubProgressMonitor(monitor, 10) );
            reader = csv.reader();
            reader.readHeaders();
            int nameIndex = reader.getIndex("name");

            IProgressMonitor drawMonitor = new SubProgressMonitor(monitor, 90);
            Coordinate worldLocation = new Coordinate();
            
            drawMonitor.beginTask("draw "+csv.toString(), csv.getSize());            
            while( reader.readRecord() ) {
                Point point = CSV.getPoint(reader);
                Coordinate dataLocation = point.getCoordinate();
                try {
                    JTS.transform(dataLocation, worldLocation, dataToWorld);
                } catch (TransformException e) {
                    continue;
                }                
                if (bounds != null && !bounds.contains(worldLocation)) {
                    continue; // optimize!
                }                
                java.awt.Point p = getContext().worldToPixel(worldLocation);
                g.fillOval(p.x, p.y, 10, 10);
                String name = reader.get(nameIndex);
                g.drawString(name, p.x + 15, p.y + 15);
                drawMonitor.worked(1);

                if (drawMonitor.isCanceled())
                    break;
            }
            drawMonitor.done();            
        } catch (IOException e) {
            throw new RenderException(e); // rethrow any exceptions encountered
        } catch (FactoryException e) {
            throw new RenderException(e); // rethrow any exceptions encountered
        } finally {
            if (reader != null)
                reader.close();
            monitor.done();
        }
    }
    /**
     * Replacement for getRenderBounds() that figures out
     * which is smaller.
     * 
     * @return smaller of viewport bounds or getRenderBounds()
     */
    public ReferencedEnvelope getBounds() {
        ReferencedEnvelope renderBounds = getRenderBounds();
        ReferencedEnvelope viewportBounds = context.getViewportModel().getBounds();
        if (renderBounds == null) {
            return viewportBounds;
        }
        if (viewportBounds == null) {
            return renderBounds;
        }
        if (viewportBounds.contains((BoundingBox)renderBounds)) {
            return renderBounds;
        } else if (renderBounds.contains((BoundingBox)viewportBounds)) {
            return viewportBounds;
        }
        return renderBounds;
    }
    /**
     * The following example is simple and is shown to introduce the concept of a renderer. This
     * example assumes the world is in WGS84; and makes use of a single worldToPixel transformation.
     * <p>
     * Please compare with the complete *render* method below
     * 
     * @param g
     * @param monitor
     * @throws RenderException
     */
    public void render_example( Graphics2D g, IProgressMonitor monitor ) throws RenderException {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask("csv render", IProgressMonitor.UNKNOWN);
        CsvReader reader = null;
        try {
            g.setColor(Color.BLUE);

            ILayer layer = getContext().getLayer();
            IGeoResource resource = layer.findGeoResource(CSV.class);
            if (resource == null)
                return;

            ReferencedEnvelope bounds = getRenderBounds();
            monitor.subTask("connecting");

            CSV csv = resource.resolve(CSV.class, new SubProgressMonitor(monitor, 10));
            reader = csv.reader();
            reader.readHeaders();

            monitor.subTask("drawing");
            int nameIndex = reader.getIndex("name");
            Coordinate worldLocation = new Coordinate();
            while( reader.readRecord() ) {
                Point point = CSV.getPoint(reader);
                worldLocation = point.getCoordinate();
                if (bounds != null && !bounds.contains(worldLocation)) {
                    continue; // optimize!
                }
                java.awt.Point p = getContext().worldToPixel(worldLocation);
                g.fillOval(p.x, p.y, 10, 10);
                String name = reader.get(nameIndex);
                g.drawString(name, p.x + 15, p.y + 15);
                monitor.worked(1);

                if (monitor.isCanceled())
                    break;
            }
        } catch (IOException e) {
            throw new RenderException(e); // rethrow any exceptions encountered
        } finally {
            if (reader != null)
                reader.close();
            monitor.done();
        }
    }

}
