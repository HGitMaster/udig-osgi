/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, Refractions Research Inc.
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
package net.refractions.udig.catalog.wmsc.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import net.refractions.udig.catalog.internal.wms.WmsPlugin;

import org.geotools.data.ows.AbstractGetCapabilitiesRequest;
import org.geotools.data.ows.Request;
import org.geotools.data.ows.Response;
import org.geotools.ows.ServiceException;


/**
 * TiledWebMapServer is a class representing a WMSC. It is used to access the 
 * Capabilities document and perform requests.
 * 
 * See http://wiki.osgeo.org/wiki/WMS_Tiling_Client_Recommendation
 * 
 * @author Emily Gouge, Graham Davis (Refractions Research, Inc)
 * @since 1.1.0
 */
public class TiledWebMapServer {

    /** Capabilities document */
    private WMSCCapabilities capabilities = null;
    private String getCaps_xml;

    /** URL of WMSC Service */
    private URL service;

    /**
     * Creates a new service with the given url
     * @param serverURL
     */
    public TiledWebMapServer( URL serverURL ) {
        this.service = serverURL;
    }
    
    /**
     * Creates a new service with the given capabilities xml.  If checkForUpdate is true,
     * then it also tries to request a new capabilities document to see if there is 
     * any update it needs (if it can't connect it just continues with the given caps).
     * 
     * @param serverURL
     * @param caps_xml
     * @param checkForUpdate
     */
    public TiledWebMapServer( URL serverURL, String caps_xml, boolean checkForUpdate) {        
    	this.service = serverURL;
    	this.getCaps_xml = caps_xml;
    	
    	// build a capabilities object from the given xml
    	WMSCCapabilities capabilities = null;
    	try {
	        InputStream is = new ByteArrayInputStream(caps_xml.getBytes());
	        WMSCCapabilitiesResponse response;
	
	        response = new WMSCCapabilitiesResponse("txt/xml", is);  //$NON-NLS-1$
	        capabilities = (WMSCCapabilities) response.getCapabilities(); 
    	} catch (Exception e) {
    		WmsPlugin.log("Restore from cached capabilities failed", e);  //$NON-NLS-1$
    	}
    	
    	// try getting a new capabilities and see if its updatesequence is higher
        if (checkForUpdate) {
        	WMSCCapabilities newCaps;
            try {
            	newCaps = readCapabilities();
            	double newUpdateSeq = Double.parseDouble(newCaps.getUpdateSequence());
            	if (capabilities == null || newUpdateSeq > Double.parseDouble(capabilities.getUpdateSequence()) ) {
            		capabilities = newCaps;
            	} 
            	else {
            		// xml would have been reset when reading caps, so set them back
            		this.getCaps_xml = caps_xml;
            	}
            } catch (Exception ex) {
                // TODO: Do something with this error
                ex.printStackTrace();
            }        	
        }
        
        this.capabilities = capabilities;
    }

    /**
     * Get the getCapabilities document. If there was an error parsing it
     * during creation, it will return null (and it should have thrown an
     * exception during creation).
     * 
     * @return a WMSCCapabilities object, representing the Capabilities of the server
     */
    public WMSCCapabilities getCapabilities() {
        if (capabilities == null) {
            try {
                capabilities = readCapabilities();
            } catch (Exception ex) {
                // TODO: Do something with this error
                ex.printStackTrace();
            }
        }
        return capabilities;
    }

    /**
     * Makes a getCapabilities request and parses the response into a WMSCCapabilities 
     * object.  Also stores the resulting getcaps xml.
     *
     * @return a WMSCCapabilities object
     * @throws ServiceException
     * @throws IOException
     */
    private WMSCCapabilities readCapabilities() throws ServiceException, IOException {
        String me = service.getProtocol() + "://" + service.getHost() + ":" + service.getPort() //$NON-NLS-1$ //$NON-NLS-2$
                + "" + service.getPath(); //$NON-NLS-1$

        URL serverURL = new URL(me);
        
        //create a request
        CapabilitiesRequest r = new CapabilitiesRequest(serverURL);
        WmsPlugin.log("WMSC GetCapabilities: " + r.getFinalURL(), null);  //$NON-NLS-1$
        //issue the request
        WMSCCapabilitiesResponse cr;
        try {
            cr = (WMSCCapabilitiesResponse) issueRequest(r);
            // store the getcaps response xml
            if (cr != null) {
            	getCaps_xml = cr.getCapabilitiesXml();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }        

        //return the parsed document
        return (WMSCCapabilities) cr.getCapabilities();
    }   
    
    /**
     * Get the getCapabilities xml string. If there was an error parsing it
     * during creation, it will return null (and it should have thrown an
     * exception during creation).
     * 
     * @return a String of xml, representing the Capabilities of the server
     */
    public String getCapabilitiesXml() {
        if (getCaps_xml == null) {
            getCapabilities();
        }
        return getCaps_xml;
    }    

    /**
     * 
     * A capabilities request for a WMSC getCapabilities Request
     *
     * @author Emily Gouge (Refractions Research, Inc)
     * @since 1.1.0
     */
    static class CapabilitiesRequest extends AbstractGetCapabilitiesRequest {

        public CapabilitiesRequest( URL serverURL ) {
            super(serverURL);
        }

        @Override
        protected void initService() {
            setProperty(REQUEST, "GetCapabilities"); //$NON-NLS-1$
            setProperty(SERVICE, "WMS"); //$NON-NLS-1$;
        }

        @Override
        protected void initVersion() {
            // not used?
        }

        public Response createResponse( String contentType, InputStream inputStream )
                throws ServiceException, IOException {
            return new WMSCCapabilitiesResponse(contentType, inputStream);
        }
    }

    /**
     * Issues a request to the server and returns that server's response. It asks the server to send
     * the response gzipped to provide a faster transfer time.
     * 
     * @param request the request to be issued
     * @return a response from the server, which is created according to the specific Request
     * @throws IOException if there was a problem communicating with the server
     * @throws ServiceException if the server responds with an exception or returns bad content
     */
    public Response issueRequest( Request request ) throws IOException, ServiceException {
        URL finalURL = request.getFinalURL();

        HttpURLConnection connection = (HttpURLConnection) finalURL.openConnection();

        connection.addRequestProperty("Accept-Encoding", "gzip"); //$NON-NLS-1$ //$NON-NLS-2$

        if (request.requiresPost()) {
            connection.setRequestMethod("POST"); //$NON-NLS-1$
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", request.getPostContentType()); //$NON-NLS-1$

            OutputStream outputStream = connection.getOutputStream();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            request.performPostOutput(out);

            InputStream in = new ByteArrayInputStream(out.toByteArray());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            PrintStream stream = new PrintStream(outputStream);

            String postText = ""; //$NON-NLS-1$

            while( reader.ready() ) {
                String input = reader.readLine();
                postText = postText + input;
                stream.println(input);
            }

            System.out.println(postText);

            out.close();
            in.close();

            outputStream.flush();
            outputStream.close();
            stream.flush();
            stream.close();
        } else {
            connection.setRequestMethod("GET"); //$NON-NLS-1$
        }

        InputStream inputStream = connection.getInputStream();

        if (connection.getContentEncoding() != null
                && connection.getContentEncoding().indexOf("gzip") != -1) { //$NON-NLS-1$
            inputStream = new GZIPInputStream(inputStream);
        }

        String contentType = connection.getContentType();

        return request.createResponse(contentType, inputStream);
        
        
    }

    /**
     * Build the base request URL for this server
     *
     * @return
     */
    public String buildBaseTileRequestURL() {
        return service.getProtocol() + "://" + service.getHost() + ":" + service.getPort() + service.getPath() + "?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } 
    
    public URL getService() {
    	return service;
    }

}
