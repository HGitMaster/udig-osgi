package net.refractions.udig.project.internal.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.IProject;
import net.refractions.udig.project.IProjectElement;
import net.refractions.udig.project.ProjectBlackboardConstants;
import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.LayerFactory;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.Messages;
import net.refractions.udig.project.internal.Project;
import net.refractions.udig.project.internal.ProjectFactory;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.internal.Trace;
import net.refractions.udig.project.preferences.PreferenceConstants;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

public class CreateMapCommand extends AbstractCommand implements UndoableMapCommand {

    /** name of the new map * */
    String name;

    /** resources / layers * */
    List<IGeoResource> resources;

    /** owning project * */
    Project owner;

    /** created map * */
    Map map;

    public CreateMapCommand( String name, List<IGeoResource> resources, IProject owner ) {
        this.name = name;
        this.resources = resources;
        this.owner = (Project) owner;
    }

    public void run( IProgressMonitor monitor ) throws Exception {
        if (owner == null) {
            // default to current project
            owner = ProjectPlugin.getPlugin().getProjectRegistry().getCurrentProject();
        }
        if (name == null) {
            if (resources.size() > 0) {
                IGeoResource resource = resources.get(0);

                IGeoResourceInfo info = resource.getInfo(monitor);
                if (info != null) {
                    name = info.getTitle();
                    if (name == null)
                        name = info.getName();
                }
            }
            if (name == null) {
                name = Messages.CreateMapCommand_defaultname; 
            }

            int i=1;
            String newName=name;
            while ( nameTaken(newName) ){
                i++;
                newName=name+" "+i; //$NON-NLS-1$
            }
            name=newName;
        }
        // create the map
        map = ProjectFactory.eINSTANCE.createMap(owner, name, new ArrayList());

        IPreferenceStore store = ProjectPlugin.getPlugin().getPreferenceStore();
        RGB background = PreferenceConverter.getColor(store, PreferenceConstants.P_BACKGROUND); 
        map.getBlackboard().put(ProjectBlackboardConstants.MAP__BACKGROUND_COLOR, new Color(background.red, background.green, background.blue ));
        
        LayerFactory layerFactory = map.getLayerFactory();
        List<Layer> toAdd=new ArrayList<Layer>(resources.size());
        for( IGeoResource resource : resources ) {
            Layer layer = layerFactory.createLayer(resource);
            toAdd.add(layer);
        }

        map.getLayersInternal().addAll(toAdd);
        
        trace(toAdd);
        
    }

    private void trace( List<Layer> toAdd ) {
        if( ProjectPlugin.isDebugging(Trace.COMMANDS) ){
            List<String> ids=new ArrayList<String>();
            for( Layer layer : toAdd ) {
                ids.add(layer.getID().toString());
            }
            ProjectPlugin.trace(getClass(), "Created Map: "+map.getName()+" and added Layers: "+ids, null); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    private boolean nameTaken( String newName ) {
        for( IProjectElement element : owner.getElements() ) {
            if( newName.equals(element.getName()) )
                return true;
        }
        return false;
    }

    public String getName() {
        return Messages.CreateMapCommand_commandname; 
    }

    public IMap getCreatedMap() {
        return map;
    }

    public void rollback( IProgressMonitor monitor ) throws Exception {
        owner.getElementsInternal().remove(map);
        map.eResource().unload();
    }

}