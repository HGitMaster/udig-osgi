package net.refractions.udig.project.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TestsUIPlugin extends AbstractUIPlugin {


    private static TestsUIPlugin plugin;

    public static String ID = "net.refractions.udig.render.feature.basic"; //$NON-NLS-1$
    /**
     * Construct <code>RendererPlugin</code>.
     *
     */
    public TestsUIPlugin() {
        super();
        plugin=this;
    }

    public static Plugin getDefault(){
        return plugin;
    }
    /**
     * Writes an info log in the plugin's log.
     * <p>
     * This should be used for user level messages.
     * </p>
     * @param message Message to tell the user
     * @param e Throwable assocaited with this message 
     */
    public static void log( String message, Throwable e) {
        getDefault().getLog().log(new Status(IStatus.INFO, ID, 0, message, e));
    }
    /**
     * Messages that only engage if getDefault().isDebugging()
     * <p>
     * It is much prefered to do this:<pre><code>
     * private static final String RENDERING = "net.refractions.udig.project/render/trace";
     * if( ProjectUIPlugin.getDefault().isDebugging() && "true".equalsIgnoreCase( RENDERING ) ){
     *      System.out.println( "your message here" );
     * }
     * @param message Message to send to standard out
     * @param e Throwable associated with this trace
     */
    public static void trace( String message, Throwable e) {
        if( getDefault().isDebugging() ) {
            if( message != null ) System.out.println( message );
            if( e != null ) e.printStackTrace();
        }
    }
    /**
     * Performs the Platform.getDebugOption true check on the provided trace
     * <p>
     * Note: ProjectUIPlugin.getDefault().isDebugging() must also be on.
     * <ul>
     * <li>Trace.RENDER - trace rendering progress
     * </ul>
     * </p> 
     * @param trace currently only RENDER is defined
     * @return true if -debug is used with a .options file to enable tracing
     */
    public static boolean isDebugging( final String trace ){
        boolean on = true; //getDefault().isDebugging();
        boolean enable = "true".equalsIgnoreCase(Platform.getDebugOption(trace)); //$NON-NLS-1$ 
        return on && enable;
                
    }    
}
