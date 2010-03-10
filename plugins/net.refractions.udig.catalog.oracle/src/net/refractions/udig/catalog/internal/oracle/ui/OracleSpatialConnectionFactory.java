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
package net.refractions.udig.catalog.internal.oracle.ui;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.ServiceExtension2;
import net.refractions.udig.catalog.internal.oracle.OracleServiceExtension;
import net.refractions.udig.catalog.internal.oracle.OracleServiceImpl;
import net.refractions.udig.catalog.ui.AbstractUDIGConnectionFactory;

/**
 * This appears to be glue code added by Jesse.
 * 
 * @since 1.2.0
 */
public class OracleSpatialConnectionFactory extends AbstractUDIGConnectionFactory {

    @Override
    protected Map<String, Serializable> doCreateConnectionParameters( Object context ) {
        if( context instanceof OracleServiceImpl ){
            OracleServiceImpl oracle = (OracleServiceImpl) context;
            return oracle.getConnectionParams();
        }
        // we need to check the provided object (probably a URL)
        // and ensure it is ment for us
        ID id = ID.cast( context );
        if( id.toString().indexOf("oracle") != -1){
            
        }        
        return null;
    }


    @Override
    protected URL doCreateConnectionURL( Object context ) {
        return null;
    }

    @Override
    protected boolean doOtherChecks( Object context ) {
        return false;
    }

    @Override
    protected ServiceExtension2 getServiceExtension() {
        return new OracleServiceExtension();
    }

}
