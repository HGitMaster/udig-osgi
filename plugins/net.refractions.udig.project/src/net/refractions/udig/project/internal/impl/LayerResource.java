package net.refractions.udig.project.internal.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.core.internal.ExtensionPointList;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IResourceCachingInterceptor;
import net.refractions.udig.project.IResourceInterceptor;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.preferences.PreferenceConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Wraps a IGeoResource for a layer. This is to ensure that each item that is resolved to is the
 * same instance regardless of how the IGeoResource's resolve method is implemented.
 * 
 * @author Jesse
 * @since 1.0.0
 */
public class LayerResource extends IGeoResource {

    /**
     * The layer that owns/created the Resource
     */
    private final LayerImpl layer;

    /**
     * The resource that is wrapped by this object
     */
    IGeoResource            geoResource;

    private volatile InterceptorsBag interceptors;

    private Sorter postSorter;

    private Sorter preSorter;

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals( Object obj ) {
        if (obj instanceof LayerResource) {
            LayerResource other = (LayerResource) obj;
            return geoResource.equals(other.geoResource);
        }
        return false;
    }

    /**
     * @see net.refractions.udig.catalog.IGeoResource#hashCode()
     */
    public int hashCode() {
        return geoResource.hashCode();
    }

    /**
     * Construct <code>LayerImpl.LayerResource</code>.
     */
    public LayerResource( LayerImpl impl, IGeoResource resource ) {
        layer = impl;
        this.geoResource = resource;
    }

    /**
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        if( ILayer.class.isAssignableFrom(adaptee) ){
            return true;
        }

        return geoResource.canResolve(adaptee);
    }

    /**
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URL getIdentifier() {
        return geoResource.getIdentifier();
    }

    /**
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return geoResource.getMessage();
    }

    /**
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        return geoResource.getStatus();
    }

    /**
     * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.IProgressMonitor)
     */
    public List<IResolve> members( IProgressMonitor monitor ) {
        return geoResource.members(monitor);
    }

    /**
     * @see net.refractions.udig.catalog.IGeoResource#resolve(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public synchronized <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if( ILayer.class.isAssignableFrom(adaptee) ){
            return adaptee.cast(layer);
        }
    	if( IGeoResource.class==adaptee )
    		return adaptee.cast(this);
    	if( this.geoResource.getClass().isAssignableFrom(adaptee) )
    		return adaptee.cast(geoResource);
        T resource = processResourceCachingStrategy(monitor, adaptee);
        if (resource == null)
            return null;
        resource = processPostResourceInterceptors(resource, adaptee);
        return resource;
    }
    public IService service( IProgressMonitor monitor ) throws IOException {
        return resolve(IService.class, monitor);
    }
    @Override
    public IResolve parent( IProgressMonitor monitor ) throws IOException {
        return geoResource.parent(monitor);
    }
    private <T> T processPreResourceInterceptors( T resource, Class<T> requestedType ) {
        List<Wrapper<T>> pre = getPreInterceptors(resource);
        return runInterceptors(requestedType, resource, pre);
    }

    private <T> T runInterceptors( Class<T> requestedType, T resource, List<Wrapper<T>> pre ) {
        T resource2=resource;
        for( Wrapper<T> interceptor : pre ) {
            if( resource2 == null )
                return null;
            if( isAssignable(resource2, interceptor.targetType) )
                resource2 = interceptor.run(layer, resource2, requestedType);
        }
        return resource2;
    }

    private <T> List<Wrapper<T>> getPreInterceptors(T resource) {
        loadInterceptors();
        Set<Entry<IConfigurationElement, Wrapper>> entires = interceptors.pre.entrySet();
        List<Wrapper<T>> result = findValidInterceptors(resource, entires);
        if( preSorter!=null ){
            Collections.sort(result, preSorter);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> List<Wrapper<T>> findValidInterceptors( T resource, Set<Entry<IConfigurationElement, Wrapper>> entires ) {
        List<Wrapper<T>> result=new ArrayList<Wrapper<T>>();
        for( Entry<IConfigurationElement, Wrapper> entry : entires ) {
            String attribute = entry.getKey().getAttribute("target"); //$NON-NLS-1$
            if( attribute==null ){
                result.add(entry.getValue());
            }else{
                    boolean assignableFrom = isAssignable(resource, attribute);
                    if( assignableFrom)
                        result.add(entry.getValue());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> boolean isAssignable( T resource, String attribute ) {
        if( attribute==null )
            return true;
        boolean assignableFrom;
        try {
            Class< ? extends Object> class1 = resource.getClass();
            ClassLoader classLoader = class1.getClassLoader();
            if( classLoader == null ){
                // its a library class so use normal class loader
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            Class<T> loadClass = (Class<T>) classLoader.loadClass(attribute);
            assignableFrom = (loadClass).isAssignableFrom(class1);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return assignableFrom;
    }
    private <T> T processResourceCachingStrategy( IProgressMonitor monitor, Class<T> requestedType ) throws IOException {
         IResourceCachingInterceptor cachingStrategy = getCachingInterceptors();
        T resource2;
        if( cachingStrategy==null ){
            resource2=geoResource.resolve(requestedType, monitor);
            if( resource2==null )
                return null;
            return processPreResourceInterceptors(resource2, requestedType);
        }
        
        if( cachingStrategy.isCached(layer, geoResource, requestedType) ){
            return cachingStrategy.get(layer, requestedType);
        }else{
            if( IGeoResourceInfo.class.isAssignableFrom(requestedType) ){
                resource2 = requestedType.cast(geoResource.getInfo(monitor));
            }else{
                resource2=geoResource.resolve(requestedType, monitor);
            }
            if( resource2==null )
                return null;
            resource2=processPreResourceInterceptors(resource2, requestedType);
            cachingStrategy.put(layer, resource2, requestedType);
            return resource2;
        }
    }

    private IResourceCachingInterceptor getCachingInterceptors() {
        loadInterceptors();
        String string = ProjectPlugin.getPlugin().getPreferenceStore().getString(PreferenceConstants.P_LAYER_RESOURCE_CACHING_STRATEGY);
        return interceptors.caching.get(string);
    }
    private <T> T processPostResourceInterceptors( T resource2, Class<T> requestedType ) {
        T resource = resource2;
        List<Wrapper<T>> interceptors = getPostInterceptors(resource);
        return runInterceptors(requestedType, resource, interceptors);
    }

    private <T> List<Wrapper<T>> getPostInterceptors(T resource) {
        loadInterceptors();
        List<Wrapper<T>> findValidInterceptors = findValidInterceptors(resource, interceptors.post.entrySet());
        if( postSorter!=null ){
            Collections.sort(findValidInterceptors, postSorter);
        }
        return findValidInterceptors;
    }

    @SuppressWarnings("unchecked")
    private void loadInterceptors(){
        if( interceptors==null ){
            synchronized (this) {
                if( interceptors==null ){

                    final Map<IConfigurationElement, Wrapper> pre=new HashMap<IConfigurationElement, Wrapper>();
                    final Map<String, IResourceCachingInterceptor> caching=new HashMap<String, IResourceCachingInterceptor>();
                    final Map<IConfigurationElement, Wrapper> post=new HashMap<IConfigurationElement, Wrapper>();
                    
                    List<IConfigurationElement> list = ExtensionPointList.getExtensionPointList("net.refractions.udig.project.resourceInterceptor"); //$NON-NLS-1$
                    for( IConfigurationElement element : list ) {
                        try {
                            if( element.getName().equals("cachingStrategy")){ //$NON-NLS-1$
                                    IResourceCachingInterceptor strategy = (IResourceCachingInterceptor) element.createExecutableExtension("class"); //$NON-NLS-1$
                                    caching.put(element.getNamespaceIdentifier()+"."+element.getAttribute("id"),strategy); //$NON-NLS-1$ //$NON-NLS-2$
                            }else{
                                    IResourceInterceptor tmp=(IResourceInterceptor) element.createExecutableExtension("class"); //$NON-NLS-1$
                                    
                                    Wrapper interceptor = new Wrapper(tmp, element.getAttribute("target")); //$NON-NLS-1$
                                    String order = element.getAttribute("order"); //$NON-NLS-1$
                                    if( "PRE".equals(order)) {  //$NON-NLS-1$
                                        pre.put(element,interceptor);
                                    }else if( "POST".equals(order)) {  //$NON-NLS-1$
                                        post.put(element,interceptor);
                                    }
                            }
                        } catch (CoreException e) {
                            ProjectPlugin.log("Failed to load resource interceptor:"+element.getNamespaceIdentifier()+"."+element.getAttribute("id"), e);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                        }
                    }
                    
                    interceptors=new InterceptorsBag(pre, caching, post);
                }
            }
        }
    }
    @Override
    public IGeoResourceInfo getInfo( IProgressMonitor monitor ) throws IOException {
        return resolve(IGeoResourceInfo.class, monitor);
    }

    public LayerImpl getLayer() {
        return layer;
    }

    /**
     * ONLY FOR TESTING
     *
     * @param <T>
     * @param comparator the comparator for sorting
     * @param sortPre if true the preResourceInterceptors will be sorted other wise the postResourceInterceptors will 
     * be sorted.
     */
    public void testingOnly_sort(Comparator<IResourceInterceptor<? extends Object>> comparator, boolean sortPre){
        if( sortPre )
            preSorter=new Sorter(comparator);
        else
            postSorter=new Sorter(comparator);
            
    }
    private static class InterceptorsBag {
        final Map<IConfigurationElement,Wrapper> pre;
        final Map<String, IResourceCachingInterceptor> caching;
        final Map<IConfigurationElement, Wrapper> post;
        public InterceptorsBag( final Map<IConfigurationElement, Wrapper> pre,
                final Map<String, IResourceCachingInterceptor> caching, 
                final Map<IConfigurationElement, Wrapper> post ) {
            super();
            this.pre = pre;
            this.caching = caching;
            this.post = post;
        }
    }
    
    private static class Sorter implements Comparator<Wrapper<? extends Object>>{

        private Comparator<IResourceInterceptor< ? extends Object>> wrapped;

        public Sorter( Comparator<IResourceInterceptor< ? extends Object>> comparator ) {
            wrapped=comparator;
        }

        public int compare( Wrapper< ? extends Object> o1, Wrapper< ? extends Object> o2 ) {
            return wrapped.compare(o1.interceptor, o2.interceptor);
        }
        
    }

    /**
     * Provides extra info.
     * 
     * @author Jesse
     * @since 1.1.0
     */
    private static class Wrapper<T> implements IResourceInterceptor<T> {

        public String targetType;
        private IResourceInterceptor<T> interceptor;

        public Wrapper( IResourceInterceptor<T> interceptor, String targetType ) {
            this.interceptor=interceptor;
            this.targetType=targetType;
        }

        @SuppressWarnings("unchecked")
        public T run( ILayer layer, T resource, Class<? super T> requestedType ) {
            try{
                return interceptor.run(layer, resource, requestedType);
            }catch( Throwable t){
                ProjectPlugin.log("Exception when running interceptor: "+interceptor, t); //$NON-NLS-1$
                return resource;
            }
        }
        
        @Override
        public String toString() {
            return interceptor.toString();
        }

    }
}