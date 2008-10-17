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
package net.refractions.udig.tools.edit.activator;

import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.BehaviourCommand;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditToolHandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * Enables the {@link net.refractions.udig.tools.edit.handler.SnapBehaviourCommandHandler} on the CycleSnapBehaviour command.
 * @author Jesse
 * @since 1.1.0
 */
public class EnableAcceptEditCommandHandlerActivator implements Activator {
    /**
     * Runs the tool's accept behaviours when the command comes in.
     * 
     * @author Jesse
     * @since 1.1.0
     */
    private static final class AcceptEditCommandHandler extends AbstractHandler {

        EditToolHandler handler;
        
        public Object execute( ExecutionEvent arg0 ) throws ExecutionException {
            if( handler!=null ){
                BehaviourCommand acceptCommands = handler.getCommand(handler.getAcceptBehaviours());
                handler.getContext().sendASyncCommand(acceptCommands);
            }
            return null;
        }
    }

    private static final String COMMAND_ID = "net.refractions.udig.tool.edit.acceptAction"; //$NON-NLS-1$
    private final ICommandService service = (ICommandService) PlatformUI.getWorkbench().getAdapter(
            ICommandService.class);
    private final AcceptEditCommandHandler commandHandler = new AcceptEditCommandHandler();

    public void activate( EditToolHandler handler ) {
        Command command = service.getCommand(COMMAND_ID);

        commandHandler.handler = handler;
        
        if (command != null){
            command.setHandler(commandHandler);
        }
    }

    public void deactivate( EditToolHandler handler ) {
        commandHandler.handler=null;
        Command command = service.getCommand(COMMAND_ID);
        if (command != null)
            command.setHandler(null);
    }

    public void handleActivateError( EditToolHandler handler, Throwable error ) {
        EditPlugin.log("", error); //$NON-NLS-1$
    }

    public void handleDeactivateError( EditToolHandler handler, Throwable error ) {
        EditPlugin.log("", error); //$NON-NLS-1$
    }

}
