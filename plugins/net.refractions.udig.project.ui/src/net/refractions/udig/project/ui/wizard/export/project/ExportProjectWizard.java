/**
 * LGPL
 */
package net.refractions.udig.project.ui.wizard.export.project;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.project.internal.Project;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
    private ExportSelectionPage selectionPage;

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        destinationDirectory = selectionPage.getDestinationDirectory();
        try {
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
        for( Iterator iterator = selection.iterator(); iterator.hasNext(); ) {
            Object object = iterator.next();
            if (!(object instanceof Project)) {
                canFinish = false;
                break;
            }
        }
        return canFinish;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("Project Export Wizard");
        setNeedsProgressMonitor(true);
        selectionPage = new ExportSelectionPage("Export Project", "Select the destination to export to");
        this.selection = selection;
    }

    @Override
    public void addPages() {
        super.addPages();
        addPage(selectionPage);
    }

    public void run( IProgressMonitor monitor ) throws InvocationTargetException,
    InterruptedException {
        monitor.beginTask("Exporting projects", selection.size());
        for( Iterator curSelection = selection.iterator(); curSelection.hasNext(); ) {
            Project project = (Project) curSelection.next();
            Resource resource = project.eResource();
            String destination = generateDestinationProjectFilePath(resource);
            Resource copy = collectAllResources(resource, destination);
            saveResource(copy);
            monitor.worked(1);
        }
    }

    private String generateDestinationProjectFilePath( Resource resource ) {
        URI origURI = resource.getURI();
        File file = new File(origURI.toFileString());
        return destinationDirectory +"/" + file.getParentFile().getName() + "/" + file.getName();
    }

    private void saveResource( Resource copy ) {
        try {
            copy.save(null);
        } catch (IOException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }

    @SuppressWarnings("unchecked")
    private Resource collectAllResources(Resource projectResource, String dest){
        ResourceSet resourceSet = projectResource.getResourceSet();
        List<Resource> resources = new ArrayList<Resource>();
        resources.add(projectResource);
        addReferencedResourcesToList(resources);
        File tempFile = new File(dest);
        Resource completeResource = resourceSet.createResource(URI.createFileURI(tempFile.getAbsolutePath()));
        Collection cl = new ArrayList();
        for(Resource curResource : resources){
            cl.addAll(curResource.getContents());
        }
        completeResource.getContents().addAll(EcoreUtil.copyAll(cl));
        return completeResource;
    }

    private void addReferencedResourcesToList( List<Resource> resources ) {
        for(int i = 0; i < resources.size(); i++){
            Resource r = resources.get(i);
            for(Iterator<EObject> j = r.getAllContents(); j.hasNext(); ){
                for(Object object : j.next().eCrossReferences()){
                    if (object instanceof EObject) {
                        EObject eObject = (EObject)object;
                        Resource otherResource = eObject.eResource();
                        if(otherResource != null && !resources.contains(otherResource)){
                            resources.add(otherResource);
                        }   
                    }
                }
            }
        }
    }
}
