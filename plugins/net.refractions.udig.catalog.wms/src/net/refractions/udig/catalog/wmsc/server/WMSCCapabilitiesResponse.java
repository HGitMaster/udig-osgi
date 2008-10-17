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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.ows.Capabilities;
import org.geotools.data.ows.GetCapabilitiesResponse;
import org.geotools.ows.ServiceException;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.xml.sax.SAXException;

/**
 * 
 * This class takes the WMSC getCapabilities request response and parses out
 * the capabilities document.
 *<p>
 * http://wiki.osgeo.org/wiki/WMS_Tiling_Client_Recommendation#GetCapabilities_Responses
 * </p>
 * @author Emily Gouge (Refractions Research, Inc)
 * @since 1.1.0
 */
public class WMSCCapabilitiesResponse extends GetCapabilitiesResponse {

    /**
     * Creates a new response for a given content type and input.
     * @param contentType
     * @param inputStream
     * @throws ServiceException
     * @throws IOException
     */
    public WMSCCapabilitiesResponse( String contentType, InputStream inputStream )
            throws ServiceException, IOException {
        super(contentType, inputStream);

        
        try {
            Map<String, Object> hints = new HashMap<String, Object>();
            hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSCSchema.getInstance());
            hints.put(DocumentFactory.VALIDATION_HINT, Boolean.FALSE);
    
            Object object;
            try {
                object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
            } catch (SAXException e) {
                throw (ServiceException) new ServiceException("Error while parsing XML.").initCause(e); //$NON-NLS-1$
            }
            
            if (object instanceof ServiceException) {
                throw (ServiceException) object;
            }
            
            this.capabilities = (Capabilities)object;
        } finally {
            inputStream.close();
        }
    }
}
