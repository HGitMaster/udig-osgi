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
package net.refractions.udig.project.ui.internal.actions;

import java.util.Iterator;

import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.command.navigation.SetViewportBBoxCommand;
import net.refractions.udig.project.internal.render.impl.ScaleUtils;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * An action that sets the zoom to include all the data in the layer.
 * 
 * @author jeichar
 * @since 0.6.0
 */
public class ZoomToLayer extends ActionDelegate implements IViewActionDelegate {

    IStructuredSelection selection;

    /**
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged( IAction action, ISelection selection ) {
        try {
            this.selection = (IStructuredSelection) selection;
        } catch (Exception e) { // do nothing
        }
    }

    /**
     * @see org.eclipse.ui.actions.ActionDelegate#runWithEvent(org.eclipse.jface.action.IAction,
     *      org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent( IAction action, Event event ) {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false, true,
                    new ZoomRunnable());
        } catch (Exception e) {
            ProjectUIPlugin.log(null, e);
        }
    }

    /**
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init( IViewPart view ) {
        // do nothing
    }
    private final class ZoomRunnable implements IRunnableWithProgress {
        @SuppressWarnings("unchecked")
        public void run( IProgressMonitor monitor ) {
            Map map = ((Layer) selection.getFirstElement()).getMapInternal();
            
            ReferencedEnvelope bounds = new ReferencedEnvelope( map.getViewportModel().getCRS() );

            Coordinate mapCenter = map.getViewportModel().getCenter();
            CoordinateReferenceSystem mapCRS = map.getViewportModel().getCRS();

            for( Iterator iter = (selection).iterator(); iter.hasNext(); ) {
                try {
                    Layer layer = (Layer) iter.next();

                    if (layer.getMap() != map)
                        continue;
                    ReferencedEnvelope bbox = null;

                    bbox = layer.getBounds(monitor, mapCRS);
                    if (bbox == null || bbox.isNull()) {
                        continue;
                    }

                    ReferencedEnvelope fitToScale = ScaleUtils.fitToMinAndMax(bbox, layer);

                    bounds.expandToInclude(translateToCenter(fitToScale, bbox, mapCenter));
                } catch (Exception e) {
                    ProjectUIPlugin.log("exception getting bounds", e); //$NON-NLS-1$
                }
            }

            if (!bounds.isNull()) {
                map.sendCommandASync(new SetViewportBBoxCommand(bounds));
            }
        }

        /**
         * Attempts to translate the fitted envelope so that the center of the previous view is
         * aligned with the new bounds.
         * <p>
         * This only makes sense if the fitToScale is fully contained within the layerBounds and the
         * layerBounds contains the center.
         * <p>
         * 
         * @param fitToScale the scaled down envelope (or maybe not)
         * @param layerBounds the unrestricted bounds of the layer
         * @param mapCenter the current center of the displayed map.
         */
        private ReferencedEnvelope translateToCenter( ReferencedEnvelope fitToScale,
                ReferencedEnvelope layerBounds, Coordinate mapCenter ) {
            if( fitToScale.equals(layerBounds) ||
                    !layerBounds.covers(mapCenter)){
                return layerBounds;
            }

            Coordinate layerCenter = fitToScale.centre();
            
            double deltaX = mapCenter.x-layerCenter.x;
            double deltaY = mapCenter.y-layerCenter.y;
            
            // the maximum that can be translated in the negative X direction
            double maxNegX = layerBounds.getMinX()-fitToScale.getMinX();
            // the maximum that can be translated in the negative Y direction
            double maxNegY = layerBounds.getMinY()-fitToScale.getMinY();
            // the maximum that can be translated in the positive X direction
            double maxPosX = layerBounds.getMaxX()-fitToScale.getMaxX();
            // the maximum that can be translated in the postive Y direction
            double maxPosY = layerBounds.getMaxY()-fitToScale.getMaxY();
            
            if( deltaX<maxNegX ){
                deltaX = maxNegX;
            } else if( deltaX>maxPosX ){
                deltaX = maxPosX;
            }
            if( deltaY<maxNegY ){
                deltaY = maxNegY;
            } else if( deltaY>maxPosY ){
                deltaY = maxPosY;
            }
            
            fitToScale.translate(deltaX, deltaY);
            return fitToScale;
        }

    }
}
