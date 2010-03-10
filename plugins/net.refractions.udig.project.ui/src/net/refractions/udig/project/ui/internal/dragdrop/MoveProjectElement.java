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
package net.refractions.udig.project.ui.internal.dragdrop;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.project.internal.Project;
import net.refractions.udig.project.internal.ProjectElement;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.ui.IDropAction;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Move Project elements between projects
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class MoveProjectElement extends IDropAction {

    @Override
    public boolean accept() {
        
        Collection<ProjectElement> elements = new HashSet<ProjectElement>(toProjectElements());
        
        for( Iterator<ProjectElement> iterator = elements.iterator(); iterator.hasNext(); ) {
            ProjectElement projectElement = iterator.next();
            if( getDestination().equals(projectElement.getProject())){
                iterator.remove();
            }
        }
        
        if (elements == null || elements.isEmpty())
            return false;

        return true;
    }

    private Collection<ProjectElement> toProjectElements() {
        if (getData() instanceof ProjectElement) {
            ProjectElement element = (ProjectElement) getData();
            return Collections.singleton(element);
        }

        if (getData() instanceof EObject) {
            EObject eobj = (EObject) getData();
            while( eobj != null && !(eobj instanceof ProjectElement) )
                eobj = eobj.eContainer();

            if (eobj instanceof ProjectElement)
                return Collections.singleton((ProjectElement) eobj);

            return null;
        }

        Object[] array = null;
        if (getData() instanceof Collection< ? >) {
            Collection< ? > data = (Collection< ? >) getData();
            array=data.toArray();
        }
        if(getData().getClass().isArray()){
            array=(Object[]) getData();
        }
        if (array != null) {
            Collection<ProjectElement> elements = new HashSet<ProjectElement>();

            for( Object object : array ) {
                if (object instanceof ProjectElement) {
                    ProjectElement element = (ProjectElement) object;
                    elements.add(element);
                }

                if (object instanceof EObject) {
                    EObject eobj = (EObject) object;
                    while( eobj != null && !(eobj instanceof ProjectElement) )
                        eobj = eobj.eContainer();

                    if (eobj instanceof ProjectElement)
                        elements.add((ProjectElement) eobj);
                }
            }
            if (!elements.isEmpty())
                return elements;
        }        
        return null;
    }
    @Override
    public void perform( IProgressMonitor monitor ) {
        Collection<ProjectElement> elements = toProjectElements();

        Project destination = (Project) getDestination();

        Collection<Project> projects = collectAffectedProjects(elements, destination);

        Collection<String> messages = ProjectPlugin.saveProjects(projects);
        if (!messages.isEmpty()) {
            MessageDialog
                    .openError(
                            Display.getDefault().getActiveShell(),
                            "Error saving projects",
                            "An error occurred while attempting to save projects.  Please verify you have write access to the project files and no other applications have locked the files.");
            return;
        }

        Collection<ProjectElement> allElements = removeFromOldProjects(elements);

        destination.getElementsInternal().addAll(allElements);

        Collection<String> errors = ProjectPlugin.saveProjects(projects);

        if (!errors.isEmpty()) {
            MessageDialog
                    .openError(
                            Display.getDefault().getActiveShell(),
                            "Error saving projects",
                            "An error occurred while attempting to save projects.  Please verify you have write access to the project files and no other applications have locked the files.");
            return;
        }
    }

    private Collection<ProjectElement> removeFromOldProjects( Collection<ProjectElement> elements ) {
        HashSet<ProjectElement> all = new HashSet<ProjectElement>();
        
        for( ProjectElement projectElement : elements ) {
            Project projectInternal = projectElement.getProjectInternal();
            all.add(projectElement);
            List<ProjectElement> children = projectElement.getElements(ProjectElement.class);
            all.addAll(removeFromOldProjects(children));
            if (projectInternal != null) {
                projectInternal.getElementsInternal().remove(projectElement);
            }
        }
        
        return all;
    }

    private Collection<Project> collectAffectedProjects( Collection<ProjectElement> elements,
            Project destination ) {

        Collection<Project> projects = new HashSet<Project>();
        projects.add(destination);

        for( ProjectElement projectElement : elements ) {
            Project projectInternal = projectElement.getProjectInternal();
            if (projectInternal != null) {
                projects.add(projectInternal);
            }
            Collection<Project> affectedProjects = collectAffectedProjects(projectElement
                    .getElements(ProjectElement.class), destination);
            projects.addAll(affectedProjects);
        }

        return projects;

    }

}
