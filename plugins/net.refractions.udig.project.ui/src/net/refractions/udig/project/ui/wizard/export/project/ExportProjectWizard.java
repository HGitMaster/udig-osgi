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
package net.refractions.udig.project.ui.wizard.export.project;

import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.project.IProject;
import net.refractions.udig.project.internal.Messages;
import net.refractions.udig.project.internal.Project;
import net.refractions.udig.project.internal.impl.MapImpl;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for exporting a project, its maps and all associated data to a project.
 * 
 * @author Jesse
 */
public class ExportProjectWizard extends Wizard implements IExportWizard, IRunnableWithProgress {

    private String destinationDirectory = "";
    private IStructuredSelection selection;
    
    /** prompts the user about what to export; select projects to export, and directory to export */
    private ExportSelectionPage selectionPage;
    
    ImageDescriptor wizardPageIconDescriptor = 
        ProjectUIPlugin.imageDescriptorFromPlugin(ProjectUIPlugin.ID, "icons/wizban/exportproject_wiz.png");

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        destinationDirectory = selectionPage.getDestinationDirectory();
        try {
            // run in a background thread using this wizards progress bar
            getContainer().run(true, true, this);
        } catch (Exception e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
        return true;
    }

    @Override
    public boolean canFinish() {
        boolean canFinish = true;
        if(!selectionPage.isPageComplete()){
            canFinish = false;
        } 
        return canFinish;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle(Messages.ExportProjectWizard_Title);
        setNeedsProgressMonitor(true);
        selectionPage = new ExportSelectionPage(Messages.ExportSelectionPage_Destination,
                Messages.ExportProjectWizard_Destination2, wizardPageIconDescriptor);
        Object selectionObj = selection.getFirstElement();
        IProject project = null;
        if (selectionObj instanceof MapImpl) {
            MapImpl map = (MapImpl) selectionObj;
            project = map.getProject();
        }
        if (selectionObj instanceof IProject) {
            project = (IProject) selectionObj;
        }
        if (project != null) {
            selectionPage.selectProject(project.getID().toString());
        }
        this.selection = selection;
    }

    @Override
    public void addPages() {
        super.addPages();
        addPage(selectionPage);
    }
    /**
     * This is the method that actually does the export (called by performFinish)
     */
    public void run( IProgressMonitor monitor ) throws InvocationTargetException,
    InterruptedException {
        Project project = selectionPage.getProject();
        ExportProjectUtils.exportProject(project, destinationDirectory, monitor);
    }

}
