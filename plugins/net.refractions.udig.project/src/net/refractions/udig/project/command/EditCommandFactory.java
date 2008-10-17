/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.project.command;

import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.core.StaticBlockingProvider;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.commands.DeleteLayerCommand;
import net.refractions.udig.project.internal.commands.edit.CreateFeatureCommand;
import net.refractions.udig.project.internal.commands.edit.CreateLayerCommand;
import net.refractions.udig.project.internal.commands.edit.DeleteFeatureCommand;
import net.refractions.udig.project.internal.commands.edit.ResetEditFeatureCommand;
import net.refractions.udig.project.internal.commands.edit.RollbackCommand;
import net.refractions.udig.project.internal.commands.edit.SetAttributeCommand;
import net.refractions.udig.project.internal.commands.edit.SetEditFeatureCommand;
import net.refractions.udig.project.internal.commands.edit.SetGeometryCommand;
import net.refractions.udig.project.internal.commands.edit.WriteEditFeatureCommand;
import net.refractions.udig.project.internal.commands.selection.CommitCommand;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates Edit commands which must be used to modify editable feature data. API internal classes
 * are in the returned API
 * 
 * @author jeichar
 * @deprecated
 * @since 0.3
 */
public class EditCommandFactory {
    /**
     * Creates a new EditCommandFactory object
     * 
     * @return a new EditCommandFactory object
     */
    public static EditCommandFactory getInstance() {
        return instance;
    }

    private static final EditCommandFactory instance = new EditCommandFactory();
    protected EditCommandFactory() {
        // no op
    }

    /**
     * Creates a {@linkplain SetAttributeCommand}object.
     * 
     * @param xpath xpath that identifies an attribute in the current editable SimpleFeature
     * @param value the value that the attribute will be set to.
     * @return a new {@linkplain SetAttributeCommand}object
     * @see EditCommand
     */
    public UndoableMapCommand createSetAttributeCommand( String xpath, Object value ) {
        return new SetAttributeCommand(xpath, value);
    }

    /**
     * Creates a {@linkplain SetGeometryCommand}object.
     * 
     * @param xpath xpath that identifies an attribute in the current editable SimpleFeature
     * @param geom the geom (in <b>layer </b> CRS) that the geometry will be set to.
     * @return a new {@linkplain SetGeometryCommand}object
     * @see EditCommand
     * @see Geometry
     */
    public UndoableMapCommand createSetGeometryCommand( String xpath, Geometry geom ) {
        return new SetGeometryCommand(xpath, geom);
    }

    /**
     * Creates a {@linkplain SetGeometryCommand}object that sets the default geometry.
     * 
     * @param geom the geom (in <b>layer </b> CRS) that the geometry will be set to.
     * @return a new {@linkplain SetGeometryCommand}object.
     * @see EditCommand
     * @see Geometry
     */
    public UndoableMapCommand createSetGeometryCommand( Geometry geom ) {
        return new SetGeometryCommand(SetGeometryCommand.DEFAULT, geom);
    }

    /**
     * Creates a {@linkplain SetEditFeatureCommand}object that sets the current editVictim victim.
     * 
     * @param feature the feature that will be the new editable SimpleFeature.
     * @param layer A victim Store that contains the editable SimpleFeature.
     * @return a new {@linkplain SetEditFeatureCommand}object.
     * @see SimpleFeature
     * @see Layer
     * @see UndoableMapCommand
     */
    public UndoableMapCommand createSetEditFeatureCommand( SimpleFeature feature, ILayer layer ) {
        return new SetEditFeatureCommand(feature, layer);
    }
    /**
     * Create a Commit command
     * 
     * @return a new {@linkplain CommitCommand} object that deletes the feature.
     * @see CommitCommand
     */
    public MapCommand createCommitCommand() {
        return new CommitCommand();
    }
    /**
     * Create a Commit command
     * 
     * @return a new {@linkplain RollbackCommand} object that deletes the feature.
     * @see RollbackCommand
     */
    public MapCommand createRollbackCommand() {
        return new RollbackCommand();
    }

    /**
     * Creates a {@linkplain SetEditFeatureCommand}object that sets the current editable SimpleFeature.
     * 
     * @param feature the feature that will be the new editable SimpleFeature.
     * @return a new {@linkplain SetEditFeatureCommand}object.
     * @see UndoableMapCommand
     * @see SimpleFeature
     */
    public UndoableMapCommand createSetEditFeatureCommand( SimpleFeature feature ) {
        return new SetEditFeatureCommand(feature);
    }

    /**
     * Creates a {@linkplain SetEditFeatureCommand}object that sets the current editable SimpleFeature
     * to null.
     * 
     * @return a new {@linkplain SetEditFeatureCommand}object that sets the current editable
     *         SimpleFeature to null..
     * @see UndoableMapCommand
     */
    public UndoableMapCommand createNullEditFeatureCommand() {
        return new SetEditFeatureCommand((SimpleFeature) null, (ILayer) null);
    }

    /**
     * @param coordinates the coordinates of the new feature in <b>Map </b> CRS.
     * @return a new {@linkplain CreateFeatureCommand}object creates the feature.
     * @see CreateFeatureCommand
     */
    public UndoableMapCommand createFeature( Coordinate[] coordinates ) {
        return new CreateFeatureCommand(coordinates);
    }

    /**
     * Create a delete layer command
     * 
     * @param map the map containing the layer
     * @param layer the layer to delete
     * @return a new {@linkplain DeleteLayerCommand}object that deletes the layer.
     * @see DeleteLayerCommand
     */
    public UndoableMapCommand createDeleteLayer( Layer layer ) {
        return new DeleteLayerCommand(layer);
    }

    /**
     * Create a Delete SimpleFeature command
     * 
     * @param layer the layer containing the feature
     * @param feature the feature to delete
     * @return a new {@linkplain DeleteFeatureCommand}object that deletes the feature.
     * @see DeleteFeatureCommand
     */
    public UndoableMapCommand createDeleteFeature( SimpleFeature feature, ILayer layer ) {
        return new DeleteFeatureCommand(new StaticBlockingProvider<SimpleFeature>(feature), new StaticBlockingProvider<ILayer>(layer));
    }

    /**
     * Create a {@linkplain WriteEditFeatureCommand} command
     * 
     * @return a new {@linkplain WriteEditFeatureCommand} object that deletes the feature.
     * @see WriteEditFeatureCommand
     */
    public UndoableMapCommand createWriteEditFeatureCommand() {
        return new WriteEditFeatureCommand();
    }
    
    /**
     * Create a {@linkplain CreateLayerCommand}
     * 
     * @see CreateLayerCommand
     */
    public UndoableMapCommand createCreateLayerCommand(URL resourceId){
    	return new CreateLayerCommand(resourceId);
    }
    
    /**
     * Create a {@linkplain CreateLayerCommand}
     * 
     * @see CreateLayerCommand
     */
    public UndoableMapCommand createCreateLayerCommand(IGeoResource resource){
    	return new CreateLayerCommand(resource);
    }
    
    /**
     * Create a {@linkplain ResetEditFeatureCommand} command
     * 
     * @return a new {@linkplain ResetEditFeatureCommand} object that deletes the feature.
     * @see ResetEditFeatureCommand
     */
    public UndoableMapCommand createResetEditFeatureCommand() {
        return new ResetEditFeatureCommand();
    }

}
