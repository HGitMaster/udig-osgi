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
package net.refractions.udig.project.ui.internal.commands.draw;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import net.refractions.udig.project.internal.EditManager;
import net.refractions.udig.project.internal.ProjectPackage;
import net.refractions.udig.project.internal.render.ViewportModel;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.ui.Drawing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * Draws the currently edited feature on the screen.
 * 
 * @author jeichar
 * @since 0.3
 */
public class DrawEditFeatureCommand extends AbstractDrawCommand {
    // private static Symbolizer[] symbs = getSymbolizers();

    ViewportModel model;

    Drawing drawing = Drawing.create();
    Adapter editListener = new AdapterImpl(){
        /**
         * @see org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.common.notify.Notification)
         */
        public void notifyChanged( Notification msg ) {
            if (msg.getFeatureID(EditManager.class) == ProjectPackage.EDIT_MANAGER__EDIT_FEATURE) {
                ((ViewportPane) model.getRenderManagerInternal().getMapDisplay()).repaint();
            }

        }
    };

    private boolean doKnots = false;

    private MathTransform mt;

    private boolean errorReported;

    /**
     * Creates a new instance of DrawFeatureCommand
     * 
     * @param model The viewportmodel that the command uses to determine how the victim should be
     *        drawn.
     */
    public DrawEditFeatureCommand( IViewportModel model ) {
        this.model = (ViewportModel) model;
    }

    /**
     * @see net.refractions.udig.project.internal.command.MapCommand#open()
     */
    public void run( IProgressMonitor monitor ) {
        SimpleFeature feature = model.getMapInternal().getEditManager().getEditFeature();
        if (feature == null)
            return;

        @SuppressWarnings("unchecked") List<Adapter> list = model.getMapInternal().getEditManagerInternal().eAdapters(); //$NON-NLS-1$
        if (!list.contains(editListener))
            list.add(editListener);
        MathTransform mt = null;
        mt = getMathTransform();

        Symbolizer[] symbs = null;
        if (feature.getDefaultGeometry() instanceof Point
                || feature.getDefaultGeometry() instanceof MultiPoint)
            symbs = Drawing.getSymbolizers(Point.class, Color.RED);
        else
            symbs = Drawing.getSymbolizers(LineString.class, Color.RED);
        drawing.drawFeature(graphics, feature, model.worldToScreenTransform(), doKnots, symbs, mt);
    }

    /**
     *
     * @return
     */
    private MathTransform getMathTransform() {
        if( mt==null)
        try {
            mt = model.getMapInternal().getEditManagerInternal().getEditLayerInternal()
                    .layerToMapTransform();
        } catch (Exception e) {
            mt = null;
        }
        return mt;
    }

    /**
     * If doKnots is set to true the edit features will be drawn with vertex knots.
     */
    public void setDrawKnots( boolean doKnots ) {
        this.doKnots = doKnots;
    }

    /**
     * @see net.refractions.udig.project.ui.commands.AbstractDrawCommand#setValid(boolean)
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void setValid( boolean valid ) {
        super.setValid(valid);
        if (!valid) {
            List<Adapter> adapters = model.getMapInternal().getEditManagerInternal().eAdapters();
            adapters.remove(editListener);
        }
    }

    public Rectangle getValidArea() {
        SimpleFeature feature=getMap().getEditManager().getEditFeature();
        if( feature!=null ){
            try {
                Envelope bounds = new ReferencedEnvelope(feature.getBounds())
                        .transform(getMap().getViewportModel().getCRS(), true);
                double[] points = new double[] { bounds.getMinX(),
                        bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() };
                getMap().getViewportModel().worldToScreenTransform().transform(points, 0, points, 0, 2);
                return new Rectangle((int)points[0], (int)points[1], (int)Math.abs(points[2]-points[0]), (int)Math.abs(points[3]-points[1]));
            } catch (TransformException e) {
                if( !errorReported ){
                    errorReported = true;
                    ProjectUIPlugin.log("error calculating valid area, this will not be reported again", e);
                }
                return null;
            } catch (MismatchedDimensionException e) {
                if( !errorReported ){
                    errorReported = true;
                    ProjectUIPlugin.log("error calculating valid area, this will not be reported again", e);
                }
                return null;
            } catch (FactoryException e) {
                if( !errorReported ){
                    errorReported = true;
                    ProjectUIPlugin.log("error calculating valid area, this will not be reported again", e);
                }
                return null;
            }
        }
        return null;
    }

}