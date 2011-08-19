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

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;

import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.internal.ProjectRegistry;
import net.refractions.udig.project.ui.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action to Open a project called from the projects view (and file menu?)
 * <p>
 * This action prompts the user for a project file (or directory?) and uses the
 * EMF Resource code to parse it in and recreate the onbjects. The resulting project
 * will be added to the project registery (so it can show up in the projects view).
 * 
 * @author jeichar
 * @since 0.3
 */
public class OpenProject implements IViewActionDelegate, IWorkbenchWindowActionDelegate {
    /** We maintain a single job used to open the project in the background*/
    private volatile Job job;
    
    /** The path of the project file (or folder?) to load */
    private String path;

    /**
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init( IViewPart view ) {
        // do nothing
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run( IAction action ) {
        path=null;
        
    	Shell activeShell = Display.getDefault().getActiveShell();

    	while(path==null){
    	    // prompt the user for the path
    	    
			DirectoryDialog dialog = new DirectoryDialog(activeShell);
	        dialog.setFilterPath(Messages.OpenProject_newProject_filename); 
	        dialog.setMessage(Messages.OpenProject_selectProject); 
	        dialog.setText(Messages.OpenProject_openProject); 
	        path = dialog.open();
	        if (path == null){
	        	return; // user canceled
	        }
	        
	        File projFile = new File(path+File.separator+ProjectRegistry.PROJECT_FILE);
	        if( !projFile.exists() ){
	        	String message = Messages.OpenProject_ErrorMessage;
	        	message = MessageFormat.format(message, ProjectRegistry.PROJECT_FILE);
                MessageDialog.openInformation(activeShell, Messages.OpenProject_ErrorTitle, 
	        			message);
	        	path = null;
	        }
    	}
        
        if (job == null) {
            synchronized (this) {
                if (job == null) {
                    job = new Job(Messages.OpenProject_openProject_title){ 

                        protected IStatus run( IProgressMonitor monitor ) {
                            ProjectPlugin.getPlugin().getProjectRegistry().getProject(path);
                            return Status.OK_STATUS;
                        }

                    };

                }
            }
        }
        job.schedule();

    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged( IAction action, ISelection selection ) {
        // do nothing
    }

    /*
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // okay then
    }

    /*
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init( IWorkbenchWindow window ) {
        //
    }

}
