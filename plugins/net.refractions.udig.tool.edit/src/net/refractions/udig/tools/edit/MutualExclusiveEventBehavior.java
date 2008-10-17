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
package net.refractions.udig.tools.edit;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;

/**
 * A Composite Mode (See GOF Composite Pattern) where contained modes are ordered by priority and only the
 * mode with the highest priority and is valid for the current context is ran.
 * 
 * @author jones
 * @since 1.1.0
 */
public class MutualExclusiveEventBehavior implements EventBehaviour, LockingBehaviour {

    private List<EventBehaviour> behaviours=new ArrayList<EventBehaviour>();
    private EventBehaviour current;
    
    public MutualExclusiveEventBehavior(List<EventBehaviour> modes) {
        this.behaviours=modes;
    }
    
    /**
     * @param behaviour
     */
    public MutualExclusiveEventBehavior( EventBehaviour behaviour ) {
        behaviours.add(behaviour);
    }

    public boolean isValid( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        for( EventBehaviour mode : behaviours ) {
            if( mode.isValid(handler,e, eventType) ){
                current=mode;
                return true;
            }
        }
        return false;
    }

    public UndoableMapCommand getCommand( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        return current.getCommand(handler,e, eventType);
        
    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        EditPlugin.log("Very Strange I don't know how this happenned...", error); //$NON-NLS-1$
    }
    
    @Override
    public String toString() {
        StringBuffer buffer=new StringBuffer("["); //$NON-NLS-1$
        for( EventBehaviour behaviour : this.behaviours ) {
            buffer.append(behaviour.toString());
            buffer.append("||"); //$NON-NLS-1$
        }
         buffer.reverse();
         buffer.append("  "); //$NON-NLS-1$
         buffer.reverse();
        buffer.append("]"); //$NON-NLS-1$
        return null;
    }

    /**
     * @return Returns the behaviours.
     */
    public List<EventBehaviour> getBehaviours() {
        return this.behaviours;
    }

    public Object getKey(EditToolHandler handler) {
        for( EventBehaviour behaviour : behaviours ) {
            if (behaviour instanceof LockingBehaviour) {
                LockingBehaviour locker = (LockingBehaviour) behaviour;
                if( handler.isLockOwner(locker) )
                    return locker.getKey(handler);
            }
        }
        return NULL_KEY;
    }
    private static final Object NULL_KEY=new Object();
}
