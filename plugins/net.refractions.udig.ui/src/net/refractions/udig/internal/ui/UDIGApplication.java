/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004-2007, Refractions Research Inc.
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
 *
 * Created on Jun 23, 2004
 */
package net.refractions.udig.internal.ui;

import net.refractions.udig.ui.internal.Messages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * IApplication used by the uDig product - subclass for your own application.
 * <p>
 * The UDIGApplication serves three goals:
 * <ul>
 * <li>It is a show case of what the SDK can do; usable by end-users as a functional GIS
 * Application
 * <li>It is an example for other RCP developers to copy (either cut and paste or subclass)
 * <li>It provides static final helper methods that can be used by other RCP developers
 * </ul>
 * If you wanted to configure how the menus and toolbars are ordered please look into
 * overriding the UDIGWorkbenchAdvisor in addition to this class.

 * <h2>Rolling your Own</h2>
 * Your application will need to be registered with the
 * <code>org.eclipse.equinox.applications</code> extension-point.
 * 
 * @author Jesse Eichar, Refractions Research Inc.
 * @author Jody Garnett, Refractions Research Inc.
 * @since 0.3
 */
public class UDIGApplication implements IApplication {

    /**
     * Starts GIS application with the given context and returns a result. This method must not exit
     * until the application is finished and is ready to exit. The content of the context is
     * unchecked and should conform to the expectations of the application being invoked.
     * <p>
     * Applications can return any object they like. If an <code>Integer</code> is returned it is
     * treated as the program exit code if Eclipse is exiting.
     * <p>
     * Note: This method is called by the platform; it is not intended to be called directly by
     * clients.
     * </p>
     * 
     * @return the return value of the application
     * @see #EXIT_OK
     * @see #EXIT_RESTART
     * @see #EXIT_RELAUNCH
     * @param context the application context to pass to the application
     * @exception Exception if there is a problem running this application.
     */
    public Object start( IApplicationContext context ) throws Exception {
        WorkbenchAdvisor workbenchAdvisor = createWorkbenchAdvisor();
        Display display = PlatformUI.createDisplay();

        if(!login()){
            // user did not login
            return EXIT_OK;
        }
        int returnCode = EXIT_OK;
        try {
            returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
        } catch (Throwable t) {
            UiPlugin.log(Messages.UDIGApplication_error, t);
        } finally {
            Platform.endSplash();
            display.dispose();
        }
        if (returnCode == PlatformUI.RETURN_RESTART) {
            return EXIT_RESTART;
        }
        return EXIT_OK;
    }

    /**
     * You can override this method to do any kind of login
     * routine you may need.
     *
     * @return
     */ 
    protected boolean login() {
        return true;
    }

    /**
     * Returns the WorkbenchAdvisor that will control the setup of the application 
     * <p>
     * It is recommended but not required that the advisor be a subclass of {@link UDIGWorkbenchAdvisor}
     * </p>
     * 
     * @return the WorkbenchAdvisor that will control the setup of the application
     * @see UDIGWorkbenchAdvisor
     */
    protected WorkbenchAdvisor createWorkbenchAdvisor() {
        return new UDIGWorkbenchAdvisor();
    }

    /**
     * Forces this running application to exit. This method should wait until the running
     * application is ready to exit. The {@link #start(IApplicationContext)} should already have
     * exited or should exit very soon after this method exits
     * <p>
     * This method is only called to force an application to exit. This method will not be called if
     * an application exits normally from the {@link #start(IApplicationContext)} method.
     * <p>
     * Note: This method is called by the platform; it is not intended to be called directly by
     * clients.
     * </p>
     */
    public void stop() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null){
            return;
        }
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable(){
            public void run() {
                if (!display.isDisposed()){
                    workbench.close();
                }
            }
        });
    };
}
