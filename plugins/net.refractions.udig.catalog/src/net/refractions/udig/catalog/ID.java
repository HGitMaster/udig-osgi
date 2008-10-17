package net.refractions.udig.catalog;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.refractions.udig.core.internal.CorePlugin;

/**
 * Identifier used to lookup entries in an local IRespository or remote ISearch.
 * <p>
 * While this identifier is often a URL or URI this class has constructors to 
 * help remove any possibility ambiguity.
 * </p>
 * @author Jody
 * @since pending
 */
public class ID {
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
        if( uri.isAbsolute() && "file".equals( uri.getScheme())){
            file = new File(uri);
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
        if( uri.isAbsolute() && "file".equals( uri.getScheme())){
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
        this.id = parent.id+"#"+child;
        try {
            this.url = new URL( null, parent.id.toString()+"#"+child, CorePlugin.RELAXED_HANDLER );
        } catch (MalformedURLException e1) {
        }
        try {
            this.uri = new URI( parent.uri.toString()+"#"+child );
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
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
}