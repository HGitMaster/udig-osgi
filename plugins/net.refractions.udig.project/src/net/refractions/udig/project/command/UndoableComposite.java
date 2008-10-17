/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.project.command;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * A UndoableCommand composed of multiple UndoableCommands. Executes and rollsback as a atomic
 * command. See Composite Pattern.
 * 
 * @author jeichar
 * @since 0.3
 * @see CompositeCommand
 * @see UndoableCommand
 */
public class UndoableComposite extends CompositeCommand implements
UndoableMapCommand, PostDeterminedEffectCommand {

    /**
     * Creates a new instance of UndoableComposite
     * 
     * @param undoableCommands an ordered list of UndoableCommands
     */
    public UndoableComposite() {
        super();
    }
    /**
     * Creates a new instance of UndoableComposite
     * 
     * API List<UndoableCommand>
     * 
     * @param undoableCommands an ordered list of UndoableCommands
     */
    public UndoableComposite(List undoableCommands) {
        super(undoableCommands);
    }

    
    @Override
    public void run( IProgressMonitor monitor ) throws Exception {
        execute(monitor);
    }
    
	/**
	 * @see net.refractions.udig.project.internal.command.UndoableCommand#rollback()
	 */
	public void rollback(IProgressMonitor monitor) throws Exception {

        for (int i = finalizerCommands.size() - 1; i > -1; i--) {
            UndoableCommand command = (UndoableCommand) finalizerCommands.get(i);
            command.rollback(monitor);
        }

        for (int i = commands.size() - 1; i > -1; i--) {
            UndoableCommand command = (UndoableCommand) commands.get(i);
            command.rollback(monitor);
        }
	}

    public boolean execute( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask(getName(),2 + 10*commands.size() + 10*finalizerCommands.size());
        monitor.worked(2);
        boolean changedState=false;
        try{
        for (MapCommand command : commands) {
            command.setMap(getMap());
            SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 10);
            if( command instanceof PostDeterminedEffectCommand){
                boolean change=((PostDeterminedEffectCommand) command).execute(subProgressMonitor);
                changedState=changedState||change;
            }else{
                command.run(subProgressMonitor);
                changedState=true;
            }
            subProgressMonitor.done();
        }
        }finally{
            for (MapCommand command : finalizerCommands) {
                command.setMap(getMap());
                SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 10);
                if( command instanceof PostDeterminedEffectCommand){
                    boolean change=((PostDeterminedEffectCommand) command).execute(subProgressMonitor);
                    changedState=changedState||change;
                }else{
                    command.run(subProgressMonitor);
                    changedState=true;
                }
                subProgressMonitor.done();
            }
            
        }
        monitor.done();

        return changedState;
    }
    public void add( MapCommand command ) {
        commands.add( command );
    }

}
