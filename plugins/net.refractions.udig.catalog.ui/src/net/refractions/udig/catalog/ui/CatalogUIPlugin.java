package net.refractions.udig.catalog.ui;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ICatalogInfo;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveChangeListener;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.IResolveChangeEvent.Type;
import net.refractions.udig.catalog.IResolveDelta.Kind;
import net.refractions.udig.catalog.internal.ui.Images;
import net.refractions.udig.catalog.ui.internal.Messages;
import net.refractions.udig.catalog.util.HandleListener;
import net.refractions.udig.core.internal.CorePlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.osgi.framework.BundleContext;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Startable;

/**
 * Lifecycle & Resource management for RegistryUI.
 * <p>
 * The CatalogUIPlugin provides access for shared images descriptors.
 * </p>
 * Example use of a shared image descriptor:
 * 
 * <pre><code>
 * ImageRegistry images = CatalogUIPlugin.getDefault().getImageRegistry();
 * ImageDescriptor image = images.getDescriptor(ISharedImages.IMG_DATASTORE_OBJ);
 * </code></pre>
 * 
 * </p>
 * <h3>Implementation Note</h3>
 * </p>
 * The CatalogUIPlugin delegates the following resource management tasks:
 * <ul>
 * <li>ResourceBundle: Policy</li>
 * <li>ImageDescriptors: Images</li>
 * </ul>
 * These resources are intended for use by classes within this plugin.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc
 */
public class CatalogUIPlugin extends AbstractUIPlugin {

    /**
     * The id of the plug-in
     */
    public static final String ID = "net.refractions.udig.catalog.ui"; //$NON-NLS-1$

    // public static final String DECORATOR_ID = "net.refractions.udig.registry.ui.decorator";
    // //$NON-NLS-1$
    /** Preference store for the last directory open by the file selection dialog */
    public static final String PREF_OPEN_DIALOG_DIRECTORY = "udig.preferences.openDialog.lastDirectory"; //$NON-NLS-1$

    /** Icons path (value "icons/") */
    public final static String ICONS_PATH = "icons/";//$NON-NLS-1$

    private static final String LABELS_PREFERENCE_STORE = "CATALOG_LABELS_PREFERENCE_STORAGE"; //$NON-NLS-1$

    private boolean loaded = false;

    // The shared instance.
    private static CatalogUIPlugin plugin;

    /** Managed Images instance */
    private Images images = new Images();
    private volatile static MutablePicoContainer pluginContainer;

    /**
     * The constructor.
     */
    public CatalogUIPlugin() {
        super();
        plugin = this;
    }

    /**
     * Set up shared images.
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     * @param context
     * @throws Exception
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        final URL iconsUrl = context.getBundle().getEntry(ICONS_PATH);

        images.initializeImages(iconsUrl, getImageRegistry());
        registerChangeListener();
    }
    /**
     * Registers a listener with the local catalog for reset events so that there is a mechanism to 
     * reset the label cache for an IResolve.
     */
    private void registerChangeListener() {
        CatalogPlugin.addListener(new IResolveChangeListener(){

            public void changed( IResolveChangeEvent event ) {
                if( PlatformUI.getWorkbench().isClosing() )
                    return;
//                IPreferenceStore p = getPreferenceStore();
                if( event.getType()==Type.POST_CHANGE 
                        && event.getDelta()!=null ){
                    //  TODO enable when resolve information is available
//                     updateCache(event.getDelta(), p);
                }

            }

            private void updateCache( IResolveDelta delta, IPreferenceStore p ) {

                if (delta.getKind() == Kind.REPLACED || delta.getKind() == Kind.REMOVED
                        || delta.getKind()==Kind.CHANGED) {
                    if (delta.getResolve() != null && delta.getResolve().getIdentifier() != null) {
                        String string = LABELS_PREFERENCE_STORE+delta.getResolve().getIdentifier().toString();
                        p.setToDefault(string);
                    }
                }
                List<IResolveDelta> children = delta.getChildren();
                for( IResolveDelta delta2 : children ) {
                    updateCache(delta2, p);
                }
            }
            
        });
    }

    /**
     * Cleanup after shared images.
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     * @param context
     * @throws Exception
     */
    public void stop( BundleContext context ) throws Exception {
        try{
//            storeLabels();
            images.cleanUp();
        }catch(Exception e){
           log("", e); //$NON-NLS-1$
        }finally{
            super.stop(context);
        }
    }

    /**
     * Returns the shared instance.
     * 
     * @return CatalogUIPlugin singleton
     */
    public static CatalogUIPlugin getDefault() {
        if (plugin == null || !plugin.loaded) {
            synchronized (CatalogUIPlugin.class) {

                if (plugin == null) {
                    plugin = new CatalogUIPlugin();
                }

            }
        }
        return plugin;
    }
    
    /**
     * Images instance for use with ImageConstants.
     * 
     * @return Images for use with ImageConstants.
     */
    public ISharedImages getImages() {
        return images;
    }

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR iff:
     * <ul>
     * <li>t is an Exception we are assuming it is human readable or if a message is provided
     * </ul>
     * </p>
     * 
     * @param message
     * @param t
     */
    public static void log( String message2, Throwable t ) {
        String message=message2;
        if (message == null)
            message = ""; //$NON-NLS-1$
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        getDefault().getLog().log(new Status(status, ID, IStatus.OK, message, t));
    }
    /**
     * Messages that only engage if getDefault().isDebugging()
     * <p>
     * It is much prefered to do this:
     * 
     * <pre><code>
     * private static final String RENDERING = &quot;net.refractions.udig.project/render/trace&quot;;
     * if (ProjectUIPlugin.getDefault().isDebugging() &amp;&amp; &quot;true&quot;.equalsIgnoreCase(RENDERING)) {
     *     System.out.println(&quot;your message here&quot;);
     * }
     * </code></pre>
     * 
     * </p>
     * 
     * @param message
     * @param e
     */
    public static void trace( String message, Throwable e ) {
        if (getDefault().isDebugging()) {
            if (message != null)
                System.out.println(message);
            if (e != null)
                e.printStackTrace();
        }
    }
    public static void trace( String message ) {
        if (getDefault().isDebugging()) {
            if (message != null)
                System.out.println(message);
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
     * 
     * @param trace currently only RENDER is defined
     * @return true if -debug is on for this plugin
     */
    public static boolean isDebugging( final String trace ) {
        return getDefault().isDebugging()
                && "true".equalsIgnoreCase(Platform.getDebugOption(trace)); //$NON-NLS-1$    
    }

    /**
     * Gets the container for catalog ui.
     * <p>
     * This is used by the IResourceLabel decorator to pass titles, and images between threads.
     * </p>
     * 
     * @return Container assocaited with the catalog ui
     */
    public static MutablePicoContainer getContainer() {
        if (pluginContainer == null) {
            synchronized (CatalogUIPlugin.class) {
                // check so see that you were not queued for double creation
                if (pluginContainer == null) {
                    // This line does it ... careful to only call it once!
                    pluginContainer = CorePlugin.getBlackBoard().makeChildContainer();
                }
            }
        }
        return pluginContainer;
    }
    /**
     * Gets a container associated with this handle.
     * <p>
     * As with any container, the contents should not be assumned, etc...
     * </p>
     * 
     * @param handle
     * @return Container associated with display of resolve
     */
    public static MutablePicoContainer getContainer( IResolve handle ) {
        Object instance = getContainer().getComponentInstance(handle);
        if (instance != null) {
            HandleLifecycle holder = (HandleLifecycle) instance;
            return holder.getContainer();
        }
        synchronized (handle) {
            instance = getContainer().getComponentInstance(handle);
            if (instance != null) {
                HandleLifecycle holder = (HandleLifecycle) instance;
                return holder.getContainer();
            }
            HandleLifecycle holder = new HandleLifecycle(handle);
            getContainer().registerComponentInstance(handle, holder);
            return holder.getContainer();
        }
    }

    /**
     * Quick and dirty image generated based on ID, this image is shared and should not be disposed.
     * <p>
     * This method does not block and can be safely used to by a LabelProvider. This method does not
     * make use of any information available via an info object (because that would require blocking
     * and be unsafe).
     * </p>
     * 
     * @see glyph
     * @param resource
     * @return Image representing provided resource
     */
    public static Image image( IResolve resolve ) {
        ISharedImages images = CatalogUIPlugin.getDefault().getImages();

        if (resolve == null) {
            return null;
        }
        if (resolve instanceof IResolveFolder ) {
        	return images.get(ISharedImages.FOLDER_OBJ);
        }else if (resolve instanceof IGeoResource) {
            IGeoResource resource = (IGeoResource) resolve;
            boolean isFeature = resource.canResolve(FeatureSource.class);
            URL url = resource.getIdentifier();
            if (Identifier.isGraphic(url)) {
                return images.get(ISharedImages.GRAPHIC_OBJ);
            }
            if (Identifier.isWMS(url)) {
                return images.get(ISharedImages.GRID_OBJ);
            }
            if (Identifier.isGraphic(url)) {
                return images.get(ISharedImages.GRAPHIC_OBJ);
            }
            if (Identifier.isMemory(url)) {
                return images.get(ISharedImages.MEMORY_OBJ);
            }
            Image image = isFeature ? images.get(ISharedImages.FEATURE_OBJ) : images
                    .get(ISharedImages.GRID_OBJ);
            return image;
        } else if (resolve instanceof IService) {
            IService service = (IService) resolve;
            boolean isFeature = service.canResolve(DataStore.class);
            URL url = service.getIdentifier();

            if (Identifier.isFile(url)) {
                Image image = isFeature ? images.get(ISharedImages.FEATURE_FILE_OBJ) : images
                        .get(ISharedImages.GRID_FILE_OBJ);
                return image;
            }
            if (Identifier.isGraphic(url)) {
                return images.get(ISharedImages.MAP_GRAPHICS_OBJ);
            }
            if (Identifier.isWMS(url)) {
                return images.get(ISharedImages.WMS_OBJ);
            }
            if (Identifier.isWFS(url)) {
                return images.get(ISharedImages.WFS_OBJ);
            }
            if (Identifier.isJDBC(url)) {
                return images.get(ISharedImages.DATABASE_OBJ);
            }
            if (Identifier.isGraphic(url)) {
                return images.get(ISharedImages.MAP_GRAPHICS_OBJ);
            }
            if (isFeature) {
                return images.get(ISharedImages.DATASTORE_OBJ);
            }
            return images.get(ISharedImages.SERVER_OBJ);
        } else if (resolve instanceof ICatalog) {
            return images.get(ISharedImages.CATALOG_OBJ);
        }
        return images.get(ISharedImages.RESOURCE_OBJ);
    }

    public static ImageDescriptor icon( IResolve resolve ) throws IOException {

        return icon(resolve, new NullProgressMonitor());
    }

    /**
     * Create icon for provided resource, this will block!
     * 
     * @param resource
     * @return ImageDescriptor for resource.
     * @throws IOException
     */
    public static ImageDescriptor icon( final IResolve resolve, IProgressMonitor monitor )
            throws IOException {

        if( resolve.canResolve(ImageDescriptor.class) ){
            ImageDescriptor descriptor = resolve.resolve(ImageDescriptor.class, monitor);
            if( descriptor!=null)
                return descriptor;
        }
        if (resolve instanceof IGeoResource) {
            ImageDescriptor icon = icon((IGeoResource) resolve, monitor);
            return icon != null ? icon : new ImageDescriptor(){

                @Override
                public ImageData getImageData() {
                    return image(resolve).getImageData();
                }
                
            };
        }

        if (resolve instanceof IService) {
            ImageDescriptor icon = icon((IService) resolve, monitor);
            return icon != null ? icon : Images.getDescriptor(ISharedImages.SERVER_OBJ);
        }

        if (resolve instanceof IResolveFolder) {
            ImageDescriptor icon = icon((IResolveFolder) resolve, monitor);
            return icon != null ? icon : Images.getDescriptor(ISharedImages.FOLDER_OBJ);
        }

        if (resolve instanceof ICatalog)
            return CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
                    ISharedImages.CATALOG_OBJ);

        return CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
                ISharedImages.RESOURCE_OBJ);
    }

    /**
     * Create icon for provided resource, this will block!
     * 
     * @param resource
     * @return ImageDescriptor for resource.
     * @throws IOException
     */
    private static ImageDescriptor icon( IGeoResource resource, IProgressMonitor monitor )
            throws IOException {

        IGeoResourceInfo info;
        try{
            info = resource.resolve(IGeoResourceInfo.class, monitor);            
        }catch(Throwable t){
            log("Error obtaining info", t); //$NON-NLS-1$
            return null;
        }   
        
        if (info == null)
            return null;

        try{
            return info.getImageDescriptor();       
        }catch(Throwable t){
            log("Error obtaining icon for IGeoResource", t); //$NON-NLS-1$
            return null;
        }   
    }

    /**
     * Create icon for provided folder, this will block!
     * 
     * @return ImageDescriptor for folder.
     * @throws IOException
     */
    private static ImageDescriptor icon( IResolveFolder folder, IProgressMonitor monitor )
            throws IOException {

        try{
            return folder.getIcon(monitor);       
        }catch(Throwable t){
            log("Error obtaining icon for IResolveFolder", t); //$NON-NLS-1$
            return null;
        }   
    }

    
    /**
     * Create icon for provided service, this will block!
     * 
     * @param resource
     * @return ImageDescriptor for resource.
     * @throws IOException
     */
    private static ImageDescriptor icon( IService service, IProgressMonitor monitor )
            throws IOException {

        IServiceInfo info = service.getInfo(monitor);
        if (info == null)
            return null;

        return info.getImageDescriptor();
    }
}

class HandleLifecycle implements Startable, Disposable {
    HandleListener listener;
    volatile MutablePicoContainer container;

    HandleLifecycle( IResolve resolveHandle ) {
        container = null; // until used by getContainer();
        listener = new HandleListener(resolveHandle){
            public void stop( IResolve handle ) {
                HandleLifecycle.this.stop();
            }
            public void dispose() {
                CatalogPlugin.removeListener(this);
                HandleLifecycle.this.dispose();
            }

            public void start( IResolve handle ) {
                HandleLifecycle.this.start();
            }
            public void refresh( IResolve handle ) {
                // nop
            }
            /** Replace if easy */
            public void replace( IResolve handle, IResolve newHandle ) {
                if (newHandle != null) {
                    setHandle(newHandle);
                }
                reset(handle, null);
            }
            /** Clear out if hard */
            public void reset( IResolve handle, IResolveChangeEvent event ) {
                HandleLifecycle.this.stop();
                CatalogPlugin.removeListener(this);
                HandleLifecycle.this.dispose();
            }
        };
        CatalogPlugin.addListener(listener);
    }

    /**
     * A container for collaboration, or null if handle is out of scope.
     * 
     * @return The container associated with handle
     */
    public MutablePicoContainer getContainer() {
        IResolve resolve = listener.getHandle();
        if (resolve == null)
            return null;

        if (container == null) {
            synchronized (resolve) {
                // check so see that you were not queued for double creation
                if (container == null) {
                    // This line does it ... careful to only call it once!
                    container = makeChildContainer();
                }
            }
        }
        return container;
    }
    /**
     * Create container, must only be called once
     * 
     * @return created container
     */
    protected MutablePicoContainer makeChildContainer() {
        if (container != null) {
            throw new IllegalStateException(Messages.CatalogUIPlugin_childContainerException); 
        }
        return CatalogUIPlugin.getContainer().makeChildContainer();
    }
    /** We have just been created, lets listen to the catalog */
    public void start() {
        CatalogPlugin.addListener(listener);
    }
    /** We are no longer in use */
    public void stop() {
        if (listener != null) {
            CatalogPlugin.removeListener(listener);
            listener.dispose();
            listener = null;
        }
        if (container != null) {
            container.dispose();
            container = null;
        }
    }

    /** Stop listening, fee references, and turn off the lights */
    public void dispose() {
        if (listener != null) {
            CatalogPlugin.removeListener(listener);
            listener.dispose();
            listener = null;
        }
        if (container != null) {
            container.dispose();
            container = null;
        }
    }
}