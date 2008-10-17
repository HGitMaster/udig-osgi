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
package net.refractions.udig.ui.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.ui.internal.Messages;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Abstract class can be used to implement your ow "export to" opperations.
 * <p>
 * Based on ExportSLD by Jesse, contains ideas inspired by CIP.
 * </p>
 * <p>
 * I hope this can involve into a general export tool,
 * where the prompt
 * @author Jesse
 * @since 1.0.0
 */
public class FileExportOperation implements IOp {
    
    /**
     * Let the extention point perform any additional checks
     * before bothering the users with a prompt.
     * <p>
     * The extention point lets you specify the exact interface (or class)
     * required. This method returns <code>true</code>, but could be used
     * to perform a more indepth check of say a Layer's Schema to pervent
     * the export of a SimpleFeatureType with multiple Geometry attributes
     * being exported as a Shapefile.
     * </p>
     * @param target Target to be considered for export
     * @return <code>true</code> if non <code>null</code>, subclass can overrride with additional tests
     */
    public boolean canExport( Object target ){
        return target != null;
    }
    
    /**
     * Subclass should override to actually do something.
     * 
     * @param target 
     * @param file
     * @param monitor
     * @throws Exception 
     */
    public void exportTo( Object target, File file, IProgressMonitor monitor ) throws Exception {
        return; 
    }

    /**
     * Prompt to use for title (example: "Export to")
     *
     * @param target 
     * @return Prompt (may be based on target), should be internationalized
     */
    public String prompt( Object target ){
        return Messages.FileExportOperation_prompt;
    }
    
    /**
     * Defalt name for provided target.
     * <p>
     * Should make use of provided target's title if available.
     * This will be combined with the first filter extention
     * to form a valid filename.
     * </p>
     * @param target
     * @return Default filename based on target, default is "new"
     */
    public String defaultName( Object target ){
        return Messages.FileExportOperation_defaultName;
    }
    
    /**
     * Override as required (example "*.sld").
     * 
     * @return filter, default "*.*"
     */
    public String[] getFilterExtentions(){
       List<String> filters = new ArrayList<String>();
       for( String extention : getExtentions() ){
           filters.add( "*."+extention ); //$NON-NLS-1$
       }
       return filters.toArray( new String[ filters.size() ]);
    }
    /**
     * Called by getFilterExtentions (example "sld").
     * 
     * @return filter, default "*"
     */
    public String[] getExtentions(){
        return new String[]{"*"}; //$NON-NLS-1$
    }
    /**
     * Override as required (example "Style Layer Descriptor document").
     * <p>
     * Care should be taken to internationalization these.
     * </p>
     * @return Filter names, default "All Files"
     */
    public String[] getFilterNames(){
        return new String[]{ Messages.FileExportOperation_allFiles };
    }
    
    /**
     * Responsible for asking the user for a filename and calleding exporTo.
     * <p>
     * The following methods are used to control the process:
     * <ul>
     * <li>getFilterExtentions()
     * <li>getFilterNames()
     * <li>getPrompt(
     */
    protected class PromptAndExport implements Runnable {
        Display display;
        Object target;
        IProgressMonitor monitor;
        
        // results of prompt
        File file;
        
        /**
         * @param display
         * @param target
         * @param monitor
         */
        public PromptAndExport( Display display, Object target, IProgressMonitor monitor ){
            this.display = display;
            this.target = target;
            this.monitor = monitor;
        }
        
        /** Run with Display.asyncExec */
        public void run() {
            file = promptFile( display, target );
        }
        /** @return File to export to or <code>null</code> to cancel */
        File getFile(){
            return file;
        }
    }
    
    /**
     * Asks the users for a filename to export to ...
     * <p>
     * If the user asks for an existing filename they will be prompted to confirm that they do
     * indeed wish to replace. If they press Yes the existing file will be removed so the
     * opperation can repalce it, if they select false the they will be reprompted.
     * </p>
     * @param display Display used to prompt with
     * @param target Target (may be used to figure out prompt)
     * @return File or <code>null</code> to be used to export
     */
    public File promptFile( Display display, Object target ) {
        String name = defaultName( target );
        String prompt = prompt( target );
        FileDialog fileDialog=new FileDialog( display.getActiveShell(),SWT.SAVE );
        
        fileDialog.setFilterExtensions( getFilterExtentions() );
        fileDialog.setFilterNames( getFilterNames() );            
        fileDialog.setFileName( name+"."+getExtentions()[0] ); //$NON-NLS-1$
        fileDialog.setText( prompt );
                    
        String path=fileDialog.open();
        if( path==null){
            return null; // user canceled
        }
        File file=new File(path);
        if( file.exists() ){
            boolean replace =
                MessageDialog.openConfirm(display.getActiveShell(), prompt,
                    file.getAbsolutePath()+" exists, do you want to replace?" ); //$NON-NLS-1$
            if( !replace ){
                return null; // user canceled (we could reprompt?)
            }
            file.delete();
        }
        return file;
    }

    /**
     * This method is called in a non ui thread...
     * 
     */
    public void op( Display display, Object target, IProgressMonitor monitor ) throws Exception {
        if( !canExport( target )){            
            return; // should we log this? Or disable the op...
        }
        PromptAndExport prompt = new PromptAndExport( display, target, monitor ); 
        display.syncExec( prompt );
        File file = prompt.getFile();
        if(file == null){
        	/*
        	 * Means the user has canceled FileDialog
        	 */
        	return;
        }
        
        status( Messages.FileExportOperation_writingStatus+file );
        exportTo( target, file, monitor );
        status( Messages.FileExportOperation_finishStatus+file );
    }

    /**
     *
     * @param msg Display msg on status bar (if possible)
     */
    public void status(final String msg ) {
        final IStatusLineManager statusBar = getStatusBar();
        final Display display = Display.getCurrent();
        
        if( statusBar == null || display == null ) return;
        
        display.syncExec(new Runnable(){
            public void run(){
                statusBar.setMessage( msg);
            }
        });
    }
    
    private IStatusLineManager getStatusBar(){
        IEditorSite site=getEditorSite();
        if( site==null )
            return null;
        return site.getActionBars().getStatusLineManager();
    }
    
    private IEditorSite getEditorSite() {
        IWorkbenchWindow window = getWindow();
        if( window == null )
            return null;
        IWorkbenchPage page=window.getActivePage();
        if( page==null )
            return null;
        IEditorPart part = page.getActiveEditor();
        if (part == null)
            return null;
        return (IEditorSite) part.getSite();
    }
    private IWorkbenchWindow getWindow() {
        IWorkbench bench=PlatformUI.getWorkbench();
        if( bench==null)
            return null;
        IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
        if( window==null ){
            if( bench.getWorkbenchWindowCount()>0 )
                window=bench.getWorkbenchWindows()[0];
        }
        return window;
    }
}