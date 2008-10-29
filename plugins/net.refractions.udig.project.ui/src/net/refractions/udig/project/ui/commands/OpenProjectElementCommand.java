package net.refractions.udig.project.ui.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.project.IProjectElement;
import net.refractions.udig.project.command.Command;
import net.refractions.udig.project.command.UndoableCommand;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.UDIGEditorInput;
import net.refractions.udig.project.ui.internal.MapEditor;
import net.refractions.udig.project.ui.internal.Messages;
import net.refractions.udig.project.ui.internal.ProjectExplorer;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;

import com.vividsolutions.jts.geom.Envelope;

public class OpenProjectElementCommand implements UndoableCommand {

    IProjectElement element;
    private IEditorPart previous;

    public OpenProjectElementCommand( IProjectElement element ) {
        this.element = element;
    }

    public void run( IProgressMonitor monitor ) throws Exception {
        try {
            if (PlatformUI.getWorkbench().isClosing())
                return;
            
            monitor.beginTask(Messages.OpenMapCommand_taskName, IProgressMonitor.UNKNOWN); 
            final UDIGEditorInput input = ApplicationGIS.getInput(element);
                if (element instanceof Map) {
                    Map map = (Map) element;
                    if (map.getViewportModel().getBounds().isNull()){
                        Envelope bounds = map.getBounds(monitor);
                        map.getViewportModelInternal().setBounds(bounds);
                    }
                }
            input.setProjectElement(element);

            PlatformGIS.syncInDisplayThread(new Runnable(){
                public void run() {
                    
                    IWorkbench workbench = PlatformUI.getWorkbench();
                    IIntroManager introManager = workbench.getIntroManager();
                    IIntroPart intro = introManager.getIntro();
                    if( intro!=null) introManager.closeIntro(intro);
                        
                    IWorkbenchWindow activeWorkbenchWindow = workbench
                            .getActiveWorkbenchWindow();
                    IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                    IEditorReference[] editors = activePage.getEditorReferences();
                    for( IEditorReference reference : editors ) {
                        try {
                            if (reference.getEditorInput().equals(input)) {
                                previous=activePage.getActiveEditor();
                                activePage.activate(reference.getPart(true));
                                return;
                            }
                        } catch (PartInitException e) {
                            // ignore
                        }
                    }
                    openMap(input);
                    }

                });
        } finally {
            monitor.done();
        }
    }

    private void openMap( final UDIGEditorInput input ) {
        try {
            IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
                    input.getEditorId());
            
            ProjectExplorer explorer = ProjectExplorer.getProjectExplorer();
            explorer.setSelection(Collections.singleton(input.getProjectElement()), true);

            if( part instanceof MapEditor ){
                MapEditor mapEditor=(MapEditor) part;
                while( !mapEditor.getComposite().isVisible() || !mapEditor.getComposite().isEnabled() ){
                    if( !Display.getCurrent().readAndDispatch() ){
                        Thread.sleep(300);
                    }
                }
            }
        } catch (PartInitException e) {
            ProjectUIPlugin.log(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }

    public String getName() {
        return Messages.OpenMapCommand_commandName; 
    }

    public void rollback( IProgressMonitor monitor ) throws Exception {
        if (previous!=null){
            previous.getEditorSite().getPage().activate(previous);
            return;
        }
        PlatformGIS.syncInDisplayThread(new Runnable(){
            public void run() {
                UDIGEditorInput input = ApplicationGIS.getInput(element);
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage();
                IEditorReference[] editors = activePage.getEditorReferences();
                List<IEditorReference> matches = new ArrayList<IEditorReference>();
                for( IEditorReference reference : editors ) {
                    try {
                        if (reference.getEditorInput().equals(input)) {
                            matches.add(reference);
                        }
                    } catch (PartInitException e) {
                        // do nothing
                    }
                }

                activePage
                        .closeEditors(matches.toArray(new IEditorReference[matches.size()]), true);
            }
        });
    }

    public Command copy() {
        return null;
    }

}