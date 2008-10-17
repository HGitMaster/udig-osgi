package net.refractions.udig.project.ui.internal.commands.draw;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.render.displayAdapter.IMapDisplay;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.commands.IDrawCommand;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Composite drawing command.
 * <p>
 * Contains several internal drawing commands. Only the composite command
 * is sent to the ViewportPainter.
 * <p>
 * The composite command is responsible for the running all internal
 * commands.
 * 
 * @author Vitalus
 * @since 1.1.0
 * 
 */
public class CompositeDrawCommand extends AbstractDrawCommand {
	
	private List<IDrawCommand> internalCommands = null;

	public CompositeDrawCommand(IDrawCommand[] commandsArray) {

		this.internalCommands = new ArrayList<IDrawCommand>();
		for (int i = 0; i < commandsArray.length; i++) {
			internalCommands.add(commandsArray[i]);
		}
	}
	
	/**
	 * 
	 * @param commandsList list of <code>IDrawCommand</code>s.
	 */
	public CompositeDrawCommand(List<? extends IDrawCommand> commandsList) {
		this.internalCommands = new ArrayList<IDrawCommand>(commandsList);
	}

	public Rectangle getValidArea() {
		return null;
	}
	
	

	@Override
	public void setGraphics(ViewportGraphics graphics, IMapDisplay display) {

		super.setGraphics(graphics, display);
		if(internalCommands != null){
			for (IDrawCommand command : internalCommands) {
				command.setGraphics(graphics, display);
			}
		}
	}

	@Override
	public void setValid(boolean valid) {
		
		super.setValid(valid);
		if(internalCommands != null){
			for (IDrawCommand command : internalCommands) {
				command.setValid(valid);
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public void setMap(IMap map) {
		super.setMap(map);
		if(internalCommands != null){
			for (IDrawCommand command : internalCommands) {
				command.setMap(map);
			}
		}
	}


	/**
	 * 
	 */
	public void run(IProgressMonitor monitor) throws Exception {
		
		for  (IDrawCommand command : internalCommands) {
			try {
				if(command.isValid())
					command.run(monitor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
