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
package net.refractions.udig.printing.ui.internal;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import net.refractions.udig.printing.model.Box;
import net.refractions.udig.printing.model.BoxPrinter;
import net.refractions.udig.printing.model.Page;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The engine that prints to pdf.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PdfPrintingEngine {

    private Page page;
    private IProgressMonitor monitor;
    private final File outputPdfFile;

    /**
     * Constructs a PdfPrintingEngine using the given Page and the file to which to dump to.
     * 
     * @param page the Page to be printed.
     * @param outputPdfFile the file to which to dump to.
     */
    public PdfPrintingEngine( Page page, File outputPdfFile ) {
        this.page = page;
        this.outputPdfFile = outputPdfFile;
    }

    /**
     * @param monitor
     */
    public void setMonitor( IProgressMonitor monitor ) {
        this.monitor = monitor;
    }

    public boolean printToPdf() {

        Dimension paperSize = page.getPaperSize();
        Dimension pageSize = page.getSize();

        float xScale = (float) paperSize.width / (float) pageSize.width;
        float yScale = (float) paperSize.height / (float) pageSize.height;

        Rectangle paperRectangle = new Rectangle(paperSize.width, paperSize.height);
        Document document = new Document(paperRectangle, 0f, 0f, 0f, 0f);

        try {

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPdfFile));
            document.open();

            PdfContentByte cb = writer.getDirectContent();
            Graphics2D graphics = cb
                    .createGraphics(paperRectangle.getWidth(), paperRectangle.getHeight());

            // BufferedImage bI = new BufferedImage((int) paperRectangle.width(), (int)
            // paperRectangle
            // .height(), BufferedImage.TYPE_INT_ARGB);
            // Graphics graphics2 = bI.getGraphics();

            List<Box> boxes = page.getBoxes();
            for( Box box : boxes ) {
                String id = box.getID();
                System.out.println(id);
                Point boxLocation = box.getLocation();
                int x = boxLocation.x;
                int y = boxLocation.y;
                Dimension size = box.getSize();
                int w = size.width;
                int h = size.height;

                float newX = xScale * (float) x;
                float newY = yScale * (float) y;
                float newW = xScale * (float) w;
                float newH = yScale * (float) h;

                box.setSize(new Dimension((int) newW, (int) newH));
                box.setLocation(new Point((int) newX, (int) newY));

                Graphics2D boxGraphics = (Graphics2D) graphics.create((int) newX, (int) newY,
                        (int) newW, (int) newH);
                BoxPrinter boxPrinter = box.getBoxPrinter();
                boxPrinter.draw(boxGraphics, monitor);
            }

            graphics.dispose();
            // ImageIO.write(bI, "png", new File("c:\\Users\\moovida\\Desktop\\test.png"));
            // graphics.drawImage(bI, null, 0, 0);

            document.newPage();
            document.close();
            writer.close();
        } catch (DocumentException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
