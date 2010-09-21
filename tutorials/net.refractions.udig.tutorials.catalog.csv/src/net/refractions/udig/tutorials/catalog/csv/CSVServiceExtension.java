package net.refractions.udig.tutorials.catalog.csv;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.URLUtils;

public class CSVServiceExtension implements ServiceExtension {
    /* CSV service key, url to the CSV file */
    public static final String KEY 
        = "net.refractions.udig.tutorials.catalog.csv.url";
    
public Map<String, Serializable> createParams( URL url ) {
    try {
        //does the URL represent a file        
        File file = URLUtils.urlToFile( url );
        if (file.exists()) {
            //check the filename, is it a CSV file?
            if( file.getName().endsWith(".csv") || file.getName().endsWith(".CSV")){
                Map<String, Serializable> params = new HashMap<String, Serializable>();
                params.put(KEY, url);
                return params;
            }
        }
    }
    catch(Throwable t) {
        //something went wrong, url must be for another service
    }
    
    //unable to create the parameters, url must be for another service
    return null;
}

    public IService createService( URL id, Map<String, Serializable> params ) {
        //good defensive programming
        if (params == null)
            return null;
            
        //check for the property service key
        if (params.containsKey(KEY)) {
            //found it, create the service handle
            return new CSVService(params);
        }
            
        //key not found
        return null;
    }

}
