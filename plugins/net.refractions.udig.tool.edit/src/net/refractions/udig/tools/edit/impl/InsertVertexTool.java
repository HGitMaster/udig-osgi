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
package net.refractions.udig.tools.edit.impl;

import java.util.List;
import java.util.Set;

import net.refractions.udig.core.StaticProvider;
import net.refractions.udig.tool.edit.internal.Messages;
import net.refractions.udig.tools.edit.AbstractEditTool;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.Behaviour;
import net.refractions.udig.tools.edit.DefaultEditToolBehaviour;
import net.refractions.udig.tools.edit.EditToolConfigurationHelper;
import net.refractions.udig.tools.edit.EnablementBehaviour;
import net.refractions.udig.tools.edit.activator.DrawGeomsActivator;
import net.refractions.udig.tools.edit.activator.DrawGeomsActivator.DrawType;
import net.refractions.udig.tools.edit.behaviour.AcceptOnDoubleClickBehaviour;
import net.refractions.udig.tools.edit.behaviour.CursorControlBehaviour;
import net.refractions.udig.tools.edit.behaviour.InsertVertexOnEdgeBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectFeatureBehaviour;

import org.eclipse.swt.SWT;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A Tool that adds vertices to EditGeoms and selects features.
 * 
 * @author jones
 * @since 1.1.0
 */
public class InsertVertexTool extends AbstractEditTool {

    @Override
    protected void initActivators( Set<Activator> activators ) {
        DrawType type = DrawGeomsActivator.DrawType.POLYGON;
        Set<Activator> defaults = DefaultEditToolBehaviour.createDefaultEditActivators(type);
        activators.addAll(defaults);
    }

    @Override
    protected void initAcceptBehaviours( List<Behaviour> acceptBehaviours ) {
        List<Behaviour> defaults = DefaultEditToolBehaviour.createAcceptAllChanges();
        acceptBehaviours.addAll(defaults);
    }

    @Override
    protected void initCancelBehaviours( List<Behaviour> cancelBehaviours ) {
        List<Behaviour> defaults = DefaultEditToolBehaviour.createDefaultCancelBehaviours();
        cancelBehaviours.addAll(defaults);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initEventBehaviours( EditToolConfigurationHelper helper ) {
        //helper.add( new DrawCreateVertexSnapAreaBehaviour());
        helper.add( new CursorControlBehaviour(handler, new StaticProvider<String>(Messages.AddVertexTool_select_feature),
                null,null, 
                new CursorControlBehaviour.SystemCursorProvider(SWT.CURSOR_CROSS), new StaticProvider<String>(Messages.AddVertexTool_add_vertex)));

//      vertex selection OR geometry selection should not both happen so make them a mutual exclusion behaviour
        helper.startMutualExclusiveList();
        helper.add(new SelectFeatureBehaviour(new Class[]{Geometry.class}, Intersects.class));
        helper.add(new InsertVertexOnEdgeBehaviour());
        helper.stopMutualExclusiveList();
        
        helper.add( new AcceptOnDoubleClickBehaviour() );
        helper.done();
    }

    @Override
    protected void initEnablementBehaviours( List<EnablementBehaviour> enablementBehaviours ) {
        List<EnablementBehaviour> defaults = DefaultEditToolBehaviour.createEnabledWithAllGeometryLayerBehaviour();
        enablementBehaviours.addAll(defaults);
    }


}
