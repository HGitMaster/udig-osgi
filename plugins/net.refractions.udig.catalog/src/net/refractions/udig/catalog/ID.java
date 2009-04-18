package net.refractions.udig.catalog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.refractions.udig.core.internal.CorePlugin;

/**
 * Identifier used to lookup entries in an local IRespository or remote ISearch.
 * <p>
 * While an identifier is often defined by URL or URI this class has constructors to 
 * help remove any possibility ambiguity.
 * </p>
 * @author Jody Garnett
 * @since pending
 */
public class ID implements Serializable {
    /** long serialVersionUID field */
    private static final long serialVersionUID = 5858146620416500314L;
    
    private String id;
    private File file;
    private URL url;
    private URI uri;
    
    public ID( File file ) {
        this.file = file;        
        try {
            this.id = file.getCanonicalPath();
        } catch (IOException e) {
        }
        this.uri = file.toURI();
        if( id == null ){
            id = uri.toString();
        }
        try {
            this.url = uri.toURL();
            if( id == null ){
                id = url.toString();
            }
        } catch (MalformedURLException e) {
        }
    }
    
    public ID( URL url ){
        this.url = url;
        try {
            this.uri = url.toURI();
        } catch (URISyntaxException e) {            
        }
        if( uri != null && uri.isAbsolute() && "file".equals( uri.getScheme())){ //$NON-NLS-1$
            try {
                file = new File(uri);
            }
            catch( Throwable t ){
                file = null;
                if( CatalogPlugin.getDefault().isDebugging()){
                    t.printStackTrace();
                }
            }
        }
        if( file != null ){
            try {
                id = file.getCanonicalPath();
            } catch (IOException e) {
            }
        }
        if( id == null ){
            if( uri != null ){
                id = uri.toString();
            }
            else {
                id = url.toString();
            }
        }        
    }
    
    public ID( URI uri ) throws IOException {
        this.uri = uri;
        if( uri.isAbsolute() ){
            try {
                url = uri.toURL();
            }
            catch ( MalformedURLException noProtocol){
                url = new URL( null, url.toExternalForm(), CorePlugin.RELAXED_HANDLER );
            }
        }
        if( uri.isAbsolute() && "file".equals( uri.getScheme())){ //$NON-NLS-1$
            file = new File(uri);
        }
        else {
            file = null; // not a file?
        }
        if( file != null ){
            id = file.getCanonicalPath();
        }
        else if( uri != null ){
            id = uri.toString();
        }
    }
    public ID( ID parent, String child ) {
        this.id = parent.id+"#"+child; //$NON-NLS-1$
        try {
            this.url = new URL( null, parent.id.toString()+"#"+child, CorePlugin.RELAXED_HANDLER ); //$NON-NLS-1$
        } catch (MalformedURLException e1) {
        }
        try {
            this.uri = new URI( parent.uri.toString()+"#"+child ); //$NON-NLS-1$
        } catch (URISyntaxException e) {
        }
        this.file = parent.file;              
    }
    
    public File toFile(){
        return file;
    }
    public URL toURL(){
        return url;
    }
    
    public URI toURI(){
        return uri;
    }
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : id.hashCode();
        /*
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
        */
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ID other = (ID) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }    
    
    //
    // URL Handling
    //
    /**
     * Produce a relative URL for the provided file baseDirectory; if the 
     * ID is not a file URL (and not contained by the baseDirectory) it
     * will be returned as provided by toURL().
     *
     * @see URLUtils.toRelativePath
     * @param baseDirectory
     * @return relative file url if possible; or the same as toURL()
     */
    public URL toURL( File baseDirectory ){
        return URLUtils.toRelativePath( baseDirectory, toURL() );
    }
    
    /**
     * @return true if ID represents a File
     */
    public boolean isFile() {
        return file != null;
    }
    /**
     *  @return true if ID represents a decorator
     */
    public boolean isDecorator() {
        if( url == null) return false;
    
        String HOST = url.getHost();
        String PROTOCOL = url.getProtocol();
        String PATH = url.getPath();
        if (!"http".equals(PROTOCOL))return false; //$NON-NLS-1$
        if (!"localhost".equals(HOST))return false; //$NON-NLS-1$

        if (!"/mapgraphic".equals(PATH))return false; //$NON-NLS-1$
        return true;
    }
    /**
     * @return true if ID represents a temporary (or memory) resource
     */
    public boolean isTemporary() {
        if( url == null ) return false;
        String HOST = url.getHost();
        String PROTOCOL = url.getProtocol();
        String PATH = url.getPath();
        if (!"http".equals(PROTOCOL))return false; //$NON-NLS-1$
        if (!"localhost".equals(HOST))return false; //$NON-NLS-1$

        if (!"/scratch".equals(PATH))return false; //$NON-NLS-1$
        return true;
    }
    public boolean isWMS(){
        if( url == null ) return false;
        String PATH = url.getPath();
        String QUERY = url.getQuery();
        String PROTOCOL = url.getProtocol();
        if (!"http".equals(PROTOCOL)) { //$NON-NLS-1$
            return false;
        }
        if (QUERY != null && QUERY.toUpperCase().indexOf("SERVICE=WMS") != -1) { //$NON-NLS-1$
            return true;
        }
        else if (PATH != null && PATH.toUpperCase().indexOf("GEOSERVER/WMS") != -1) { //$NON-NLS-1$
            return true;
        }
        return false;
    }
    public boolean isWFS( URL url ) {
        if( url == null ) return false;
        String PATH = url.getPath();
        String QUERY = url.getQuery();
        String PROTOCOL = url.getProtocol();

        if (!"http".equals(PROTOCOL)) { //$NON-NLS-1$
            return false;
        }
        if (QUERY != null && QUERY.toUpperCase().indexOf("SERVICE=WFS") != -1) { //$NON-NLS-1$
            return true;
        } else if (PATH != null && PATH.toUpperCase().indexOf("GEOSERVER/WFS") != -1) { //$NON-NLS-1$
            return true;
        }
        return false;
    }
    public boolean isJDBC( URL url ) {
        return id.startsWith("jdbc:"); //$NON-NLS-1$
//        if( url != null){
//            String PROTOCOL = url.getProtocol();
//            String HOST = url.getHost();
//            return "http".equals(PROTOCOL) && HOST != null && HOST.indexOf(".jdbc") != -1; //$NON-NLS-1$ //$NON-NLS-2$
//        }
    }
    public String labelResource(){
        if (url == null){
            return id; 
        }
        String HOST = url.getHost();
        String QUERY = url.getQuery();
        String PATH = url.getPath();
        String PROTOCOL = url.getProtocol();
        String REF = url.getRef();

        if (REF != null) {
            return REF;
        }
        if (PROTOCOL == null) {
            return ""; // we do not represent a server (local host does not cut it) //$NON-NLS-1$
        }
        StringBuffer label = new StringBuffer();
        if ("file".equals(PROTOCOL)) { //$NON-NLS-1$
            int split = PATH.lastIndexOf('/');
            if (split == -1) {
                label.append(PATH);
            } else {
                String file = PATH.substring(split + 1);
                int dot = file.lastIndexOf('.');
                if (dot != -1) {
                    file = file.substring(0, dot);
                }
                file = file.replace("%20"," "); //$NON-NLS-1$ //$NON-NLS-2$
                label.append(file);
            }
        } else if ("http".equals(PROTOCOL) && HOST.indexOf(".jdbc") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            if (QUERY != null) {
                label.append(QUERY);
            } else {
                label.append(PATH);
            }
        } else if ("http".equals(PROTOCOL)) { //$NON-NLS-1$
            if (QUERY != null && QUERY.toUpperCase().indexOf("SERVICE=WFS") != -1) { //$NON-NLS-1$
                for( String split : QUERY.split("&") ) { //$NON-NLS-1$
                    if (split.toLowerCase().startsWith("type=")) { //$NON-NLS-1$
                        label.append(split.substring(5));
                    }
                }
            } else if (QUERY != null && QUERY.toUpperCase().indexOf("SERVICE=WMS") != -1) { //$NON-NLS-1$
                for( String split : QUERY.split("&") ) { //$NON-NLS-1$
                    if (split.startsWith("LAYER=")) { //$NON-NLS-1$
                        label.append(split.substring(6));
                    }
                }
            } else {
                int split = PATH.lastIndexOf('/');
                if (split == -1) {
                    label.append(PATH);
                } else {
                    label.append(PATH.substring(split + 1));
                }
            }
        } else {
            int split = PATH.lastIndexOf('/');
            if (split == -1) {
                label.append(PATH);
            } else {
                label.append(PATH.substring(split + 1));
            }
        }
        return label.toString();
    }
    
    public String labelServer() {
        if (url == null){
            return id; 
        }
        String HOST = url.getHost();
        int PORT = url.getPort();
        String PATH = url.getPath();
        String PROTOCOL = url.getProtocol();

        if (PROTOCOL == null) {
            return ""; // we do not represent a server (local host does not cut it) //$NON-NLS-1$
        }
        StringBuffer label = new StringBuffer();
        if (isFile()) {
            String split[] = PATH.split("\\/"); //$NON-NLS-1$

            if (split.length == 0) {
                label.append(File.separatorChar);
            } else {
                if (split.length < 2) {
                    label.append(File.separatorChar);
                    label.append(split[0]);
                    label.append(File.separatorChar);
                } else {
                    label.append(split[split.length - 2]);
                    label.append(File.separatorChar);
                }
                label.append(split[split.length - 1]);
            }
        } else if (isJDBC(url)) {
            int split2 = HOST.lastIndexOf('.');
            int split1 = HOST.lastIndexOf('.', split2 - 1);
            label.append(HOST.substring(split1 + 1, split2));
            label.append("://"); //$NON-NLS-1$
            label.append(HOST.subSequence(0, split1));
        } else if ("http".equals(PROTOCOL) || "https".equals(PROTOCOL)) { //$NON-NLS-1$ //$NON-NLS-2$
            if (isWMS()) {
                label.append("wms://"); //$NON-NLS-1$
            } else if (isWFS(url)) {
                label.append("wfs://"); //$NON-NLS-1$
            }
            label.append(HOST);
        } else {
            label.append(PROTOCOL);
            label.append("://"); //$NON-NLS-1$
            label.append(HOST);
        }
        if (PORT != -1) {
            label.append(":"); //$NON-NLS-1$
            label.append(PORT);
        }
        return label.toString();
    }
    
}