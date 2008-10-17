/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
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
package net.refractions.udig.tools.edit.commands;

import java.awt.Rectangle;

import net.refractions.udig.core.IProvider;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.commands.IDrawCommand;
import net.refractions.udig.tools.edit.MouseTracker;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.PrimitiveShape;
import net.refractions.udig.tools.edit.support.EditUtils.MinFinder;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Draws the two end points of the Shape provided by the Provider<PrimitiveShape>
 * 
 * @author jones
 * @since 1.1.0
 */
public class DrawEndPointsCommand extends AbstractDrawCommand implements IDrawCommand {

    private IProvider<PrimitiveShape> provider;
    private MouseTracker tracker;
    boolean showMouseOver=true;


    /**
     * @param IBlockingProvider
     */
    public DrawEndPointsCommand( MouseTracker tracker, IProvider<PrimitiveShape> provider ) {
        this.provider=provider;
        this.tracker=tracker;
    }

    public void run( IProgressMonitor monitor ) throws Exception {
        PrimitiveShape shape = provider.get();
        if( shape==null || shape.getNumPoints()==0 )
            return;
        Point start = shape.getPoint(0);
        Point end= shape.getPoint(shape.getNumPoints()-1);
        int radius=PreferenceUtil.instance().getVertexRadius();

        if( start==null || end==null )
            return;
        
        if( showMouseOver && tracker.getCurrentPoint()!=null ){
            MinFinder finder=new MinFinder(tracker.getCurrentPoint());
            graphics.setColor(PreferenceUtil.instance().getDrawVertexFillColor());
            if( start!=null && end!=null && finder.dist(start)<radius ){
                graphics.fill(new Rectangle(start.getX()-radius, start.getY()-radius, radius*2, radius*2 ));
                graphics.fill(new Rectangle(end.getX()-radius, end.getY()-radius, radius*2, radius*2 ));
            }
            if( start!=null && end !=null && finder.dist(end)<radius ){
                graphics.fill(new Rectangle(start.getX()-radius, start.getY()-radius, radius*2, radius*2 ));
                graphics.fill(new Rectangle(end.getX()-radius, end.getY()-radius, radius*2, radius*2 ));
            }
        }

        graphics.setColor(PreferenceUtil.instance().getDrawVertexLineColor());
        graphics.draw(new Rectangle(start.getX()-radius, start.getY()-radius, radius*2, radius*2 ));
        graphics.draw(new Rectangle(end.getX()-radius, end.getY()-radius, radius*2, radius*2 ));
        
            
    }

    /**
     * @return Returns the IBlockingProvider.
     */
    public IProvider<PrimitiveShape> getProvider() {
        return this.provider;
    }

    /**
     * @param IBlockingProvider The IBlockingProvider to set.
     */
    public void setProvider( IProvider<PrimitiveShape> provider ) {
        this.provider = provider;
    }

    /**
     * Returns true if the vertex should be filled when the mouse is over it.
     * @return Returns true if the vertex should be filled when the mouse is over it.
     */
    public boolean isShowMouseOver() {
        return this.showMouseOver;
    }

    /**
     * @param showMouseOver true if vertex should be filled when mouse is over.
     */
    public void setShowMouseOver( boolean showMouseOver ) {
        this.showMouseOver = showMouseOver;
    }

    public Rectangle getValidArea() {
        if( provider==null )
            return null;
        PrimitiveShape obj = provider.get();
        if( obj==null )
            return null;
        return provider.get().getBounds();
    }
}
