/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.catalog;

import static org.geotools.data.postgis.PostgisDataStoreFactory.DATABASE;
import static org.geotools.data.postgis.PostgisDataStoreFactory.DBTYPE;
import static org.geotools.data.postgis.PostgisDataStoreFactory.HOST;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PASSWD;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PORT;
import static org.geotools.data.postgis.PostgisDataStoreFactory.SCHEMA;
import static org.geotools.data.postgis.PostgisDataStoreFactory.USER;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;
import net.refractions.udig.catalog.postgis.internal.Messages;
import net.refractions.udig.core.Pair;
import net.refractions.udig.core.internal.CorePlugin;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.postgis.PostgisDataStoreFactory;

/**
 * PostGis ServiceExtension that has a hierarchy. It represents a Database and has folders within it.
 * One for each Schema that is known. The params object is the same as a normal Postgis except that
 * the schema parameter can be a list of comma separated string.
 * 
 * @author Jesse Eichar, Refractions Research
 * @since 1.2
 */
public class PostgisServiceExtension2 extends AbstractDataStoreServiceExtension
        implements
            ServiceExtension2 {

    public IService createService( URL id, Map<String, Serializable> params ) {
        if( reasonForFailure(params)!=null ){
            return null;
        }
        Map<String, Serializable> params2 = params;

        ensurePortIsInt(params2);

        try {
            URL finalID = toURL(params2);
            Pair<Map<String, Serializable>, String> split = processParams(params2);
            if (split.getRight() != null) {
                return null;
            }

            return new PostgisService2(finalID, split.getLeft());
        } catch (MalformedURLException e) {
            PostgisPlugin.log("Unable to construct proper service URL.", e); //$NON-NLS-1$
            return null;
        }

    }

    private void ensurePortIsInt( Map<String, Serializable> params ) {
        if (params != null && params.containsKey(PORT.key)
                && params.get(PORT.key) instanceof String) {
            int val = new Integer((String) params.get(PORT.key));
            params.put(PORT.key, val);
        }
    }

    public static URL toURL( Map<String, Serializable> params ) throws MalformedURLException {
        String the_host = (String) params.get(HOST.key);
        Integer intPort = (Integer) params.get(PORT.key);
        String the_database = (String) params.get(DATABASE.key);
        String the_username = (String) params.get(USER.key);
        String the_password = (String) params.get(PASSWD.key);

        URL toURL = toURL(the_username, the_password, the_host, intPort, the_database);
        return toURL;
    }

    /**
     * This is a guess ...
     */
    public Map<String, Serializable> createParams( URL url ) {
        if (!isPostGIS(url)) {
            return null;
        }

        ParamInfo info = parseParamInfo(url);

        Map<String, Serializable> postGISParams = new HashMap<String, Serializable>();
        postGISParams.put(DBTYPE.key, "postgis"); // dbtype //$NON-NLS-1$
        postGISParams.put(USER.key, info.username); // user
        postGISParams.put(PASSWD.key, info.password); // pass
        postGISParams.put(HOST.key, info.host); // host
        postGISParams.put(DATABASE.key, info.the_database); // database
        postGISParams.put(PORT.key, info.the_port); // port
        postGISParams.put(SCHEMA.key, info.the_schema); // database

        return postGISParams;
    }

    private static PostgisDataStoreFactory factory;

    public static PostgisDataStoreFactory getFactory() {
    	if( factory == null ){
    		factory = new PostgisDataStoreFactory();
    	}
        return factory;
    }

    /** A couple quick checks on the url */
    public static final boolean isPostGIS( URL url ) {
        if (url == null)
            return false;
        return url.getProtocol().toLowerCase().equals("postgis") || url.getProtocol().toLowerCase().equals("postgis.jdbc") || //$NON-NLS-1$ //$NON-NLS-2$
                url.getProtocol().toLowerCase().equals("jdbc.postgis"); //$NON-NLS-1$
    }

    public static URL toURL( String the_username, String the_password, String the_host,
            Integer intPort, String the_database ) throws MalformedURLException {
        String the_spec = "postgis.jdbc://" + the_username //$NON-NLS-1$
                + ":" + the_password + "@" + the_host //$NON-NLS-1$ //$NON-NLS-2$
                + ":" + intPort + "/" + the_database; //$NON-NLS-1$  //$NON-NLS-2$
        return toURL(the_spec);
    }

    public static URL toURL( String the_spec ) throws MalformedURLException {
        return new URL(null, the_spec, CorePlugin.RELAXED_HANDLER);
    }

    public String reasonForFailure( URL url ) {
        if (!isPostGIS(url))
            return Messages.PostGISServiceExtension_badURL;
        return reasonForFailure(createParams(url));
    }

    @Override
    protected String doOtherChecks( Map<String, Serializable> params ) {
        if( !DBTYPE.sample.equals(params.get(DBTYPE.key)) ){
            return "Parameter DBTYPE is required to be \"postgis\"";
        }
        Pair<Map<String, Serializable>, String> resultOfSplit = processParams(params);
        if (resultOfSplit.getRight() != null) {
            String reason = resultOfSplit.getRight();
            return reason;
        }
        return null;
    }

    private Pair<Map<String, Serializable>, String> processParams( Map<String, Serializable> params ) {
        String schemasString = (String) params.get(SCHEMA.key);

        Set<String> goodSchemas = new HashSet<String>();

        HashMap<String, Serializable> testedParams = new HashMap<String, Serializable>(params);
        testedParams.put(SCHEMA.key, "public"); //$NON-NLS-1$
        String reason = super.reasonForFailure(params);

        if (reason == null) {
            goodSchemas.add("public"); //$NON-NLS-1$
        }

        String[] schemas = schemasString.split(","); //$NON-NLS-1$

        for( String string : schemas ) {
            testedParams = new HashMap<String, Serializable>(params);
            String trimmedSchema = string.trim();    
            testedParams.put(SCHEMA.key, trimmedSchema);

            String reasonForFailure = super.reasonForFailure(testedParams);
            if (reasonForFailure == null) {
                goodSchemas.add(string);
            } else {
                reason = reasonForFailure;
            }
        }

        if (!goodSchemas.isEmpty()) {
            testedParams.put(SCHEMA.key, combineSchemaStrings(goodSchemas));
        }

        Pair<Map<String, Serializable>, String> result;
        result = new Pair<Map<String, Serializable>, String>(testedParams, reason);
        return result;
    }

    @Override protected DataStoreFactorySpi getDataStoreFactory() {
        return getFactory();
    };
    
    private Serializable combineSchemaStrings( Set<String> goodSchemas ) {
        StringBuilder builder = new StringBuilder();
        for( String string : goodSchemas ) {
            if (builder.length() > 0) {
                builder.append(',');
            }

            builder.append(string);
        }

        return builder.toString();
    }

}
