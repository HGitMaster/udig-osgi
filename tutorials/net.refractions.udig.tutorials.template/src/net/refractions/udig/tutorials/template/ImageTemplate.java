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
package net.refractions.udig.tutorials.template;

import net.refractions.udig.printing.model.Box;
import net.refractions.udig.printing.model.ModelFactory;
import net.refractions.udig.printing.model.Page;
import net.refractions.udig.printing.model.impl.LabelBoxPrinter;
import net.refractions.udig.printing.model.impl.MapBoxPrinter;
import net.refractions.udig.printing.ui.internal.AbstractTemplate;
import net.refractions.udig.project.internal.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ImageTemplate extends AbstractTemplate {

    public void init( Page page, Map map ) {
        Box labelBox = ModelFactory.eINSTANCE.createBox();
        Box mapBox = ModelFactory.eINSTANCE.createBox();
        Box imageBox = ModelFactory.eINSTANCE.createBox();
        
        boxes.add(labelBox);
        boxes.add(mapBox);
        boxes.add(imageBox);
        
        mapBox.setSize(new Dimension(400, 400));
        imageBox.setSize(new Dimension(200, 162));
        labelBox.setSize(new Dimension(150, 30));
        
        imageBox.setLocation(new Point(43, 10));
        mapBox.setLocation(new Point(143, 210));
        labelBox.setLocation(new Point(100, 612));
        
        LabelBoxPrinter lbPrinter = new LabelBoxPrinter();
        MapBoxPrinter mbPrinter = new MapBoxPrinter();
        ImageBoxPrinter ibPrinter = new ImageBoxPrinter();
        
        mbPrinter.setMap(map);
        lbPrinter.setText("Image Example");
        
        mapBox.setBoxPrinter(mbPrinter);
        labelBox.setBoxPrinter(lbPrinter);
        imageBox.setBoxPrinter(ibPrinter);
        
        page.setName(map.getName());
    }

    public String getName() {
        return "Image Template"; //Should be internationalized!
    }

}
