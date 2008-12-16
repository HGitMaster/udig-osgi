/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004-2007, Refractions Research Inc.
 *    (C) 2007,      Adrian Custer.
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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.oracle.OraclePlugin;
import net.refractions.udig.catalog.internal.oracle.OracleServiceExtension;
import net.refractions.udig.catalog.oracle.internal.Messages;
import net.refractions.udig.catalog.ui.preferences.AbstractProprietaryDatastoreWizardPage;
import net.refractions.udig.catalog.ui.preferences.AbstractProprietaryJarPreferencePage;
import net.refractions.udig.catalog.ui.wizard.DataBaseConnInfo;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.oracle.OracleConnectionFactory;
import org.geotools.data.oracle.OracleDataStoreFactory;

/**
 * Enter Oracle connection parameters.
 * 
 * @author   David Zwiers,       dzwiers,        for Refractions Research, Inc.
 * @author   Jesse Eichar,       jeichar,        for Refractions Research, Inc.
 * @author   Justin Deoliveira,  jdeolive,       for Refractions Research, Inc.
 * @author   Amr Alam,           aalam,          for Refractions Research, Inc.
 * @author   Richard Gould,      rgould,         for Refractions Research, Inc.
 * @author   Cory Horner,        chorner,        for Refractions Research, Inc.
 * @author   Adrian Custer,      acuster.
 * 
 * @since 0.3
 */
public class OracleSpatialWizardPage extends AbstractProprietaryDatastoreWizardPage {
    
    //TITLE IMAGE
    public  static final String IMAGE_KEY = "";           //$NON-NLS-1$
    //STORED SETTINGS
    private static final String ORACLE_WIZARD = "ORACLE_WIZARD"; //$NON-NLS-1$
    private static final String ORACLE_RECENT = "ORACLE_RECENT"; //$NON-NLS-1$
    //CONNECTION 
    private static final DataBaseConnInfo DEFAULT_ORACLE_CONN_INFO = 
        new DataBaseConnInfo("","1521","","","","");                                //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    private static OracleDataStoreFactory factory = new OracleDataStoreFactory();

    
    public OracleSpatialWizardPage( ) {

        //Call super with dialog title string
        super(Messages.OracleSpatialWizardPage_wizardTitle);

        //Get any stored settings or create a new one
        settings = OraclePlugin.getDefault().getDialogSettings().getSection(ORACLE_WIZARD);
        if (settings == null) {
            settings = OraclePlugin.getDefault().getDialogSettings().addNewSection(ORACLE_WIZARD);
        }
        
        //Add the name so the parent can store back to this same section
        settingsArrayName = ORACLE_RECENT;

        //Populate the Settings: default, current, and past list
        defaultDBCI.setParameters(DEFAULT_ORACLE_CONN_INFO);
        currentDBCI.setParameters(defaultDBCI);
        String[] recent = settings.getArray(ORACLE_RECENT);
        if (null != recent) {
            for( String s : recent ) {
                DataBaseConnInfo dbs = new DataBaseConnInfo(s);
                if(!storedDBCIList.contains(dbs) )
                    storedDBCIList.add(dbs);
            }
        }
        
        //Populate the db and schema exclusion lists
//        dbExclusionList.add("");                                                //$NON-NLS-1$
//        schemaExclusionList.add("");                                            //$NON-NLS-1$
        
        //Populate the Char and CharSeq exclusion lists
        //TODO: when we activate Verification
    }

    @Override
    protected AbstractProprietaryJarPreferencePage getPreferencePage() {
        return new OracleSpatialPreferences();
    }
    
    public String getId() {
        return "net.refractions.udig.catalog.ui.oracle"; //$NON-NLS-1$
    }

    @Override
    protected String getRestartMessage() {
        return Messages.OracleSpatialWizardPage_restart;
    }

    @Override
    protected String getDriversMessage() {
        return Messages.OracleSpatialWizardPage_drivers;
    }
    
    @Override
    protected void doCreateWizardPage(Composite parent) {

        // For Drag 'n Drop as well as for general selections
        // look for a url as part of the selction
        //TODO: sync with Postgis plugin
        ISelection tmpSelection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
        IStructuredSelection selection=null;
        if (tmpSelection == null || !(tmpSelection instanceof IStructuredSelection)) {
            selection=new StructuredSelection();
        } else {
            selection=(IStructuredSelection) tmpSelection;
        }
        
        String selectedText = null;
        for (Iterator itr = selection.iterator(); itr.hasNext();) {
            Object o = itr.next();
            if (o instanceof URL || o instanceof String) {
                selectedText = (String) o;
                // jdbc:postgresql://host:port/database
                // jdbc:oracle:thin:@host:port:instance
                if (selectedText.contains("jdbc:oracle:thin:@")) { //$NON-NLS-1$
                    break;
                }
                selectedText = null;
            }
        }
        if (selectedText != null) {
            int startindex = selectedText.indexOf("@"); //$NON-NLS-1$
            int hostEnd = selectedText.indexOf(":", startindex); //$NON-NLS-1$
            int portEnd = selectedText.indexOf(":", hostEnd); //$NON-NLS-1$
            int databaseEnd = selectedText.indexOf(":", portEnd); //$NON-NLS-1$
            
            String the_host = selectedText.substring(startindex, hostEnd);
            String the_port = selectedText.substring(hostEnd, portEnd);
            String the_database = selectedText.substring(portEnd,databaseEnd);
            
            currentDBCI.setHost(the_host);
            if (!the_port.equalsIgnoreCase("")) { //$NON-NLS-1$
                currentDBCI.setPort(the_port);
            }
            currentDBCI.setDb(the_database);
        }
    }

    public boolean canProcess(Object object) {
        return getOracleURL(object) != null;
    }

    protected String getOracleURL(Object data) {
        String url = null;
        if (data instanceof String) {
            String[] strings = ((String) data).split("\n"); //$NON-NLS-1$
            url = strings[0];
            if (!url.toLowerCase().contains("jdbc:oracle")) { //$NON-NLS-1$
                url = null;
            }
        }

        return url;
    }

    protected Group createAdvancedControl(Composite arg0) {
        return null;
    }
    
    protected OracleDataStoreFactory getDataStoreFactorySpi() {
        return factory;
    }

    public Map<String, Serializable> getParams() {
        if (!OracleSpatialPreferences.isInstalled())
            return null;

        if ( !couldConnect() )
            return null;
        
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        
        Param[] dbParams = factory.getParametersInfo();

        params.put(dbParams[0].key, "oracle"); //$NON-NLS-1$
        
        params.put(dbParams[1].key, currentDBCI.getHostString() );
        params.put(dbParams[2].key, new Integer(currentDBCI.getPortString()) );
        params.put(dbParams[3].key, currentDBCI.getUserString() );
        params.put(dbParams[4].key, currentDBCI.getPassString() );
        params.put(dbParams[5].key, currentDBCI.getDbString() );
        params.put(dbParams[9].key, currentDBCI.getSchemaString() );

        // not sureabout this line
        // params.put(dbParams[7].key,"MAPINFO"); //$NON-NLS-1$

        return params;
    }

    

    /**
     * TODO summary sentence for getConnection ...
     * 
     * @see net.refractions.udig.catalog.internal.ui.datastore.DataBaseRegistryWizardPage#getConnection()
     * @return
     */
    protected Connection getConnection() {

        final String hostText = currentDBCI.getHostString();
        final String portText = currentDBCI.getPortString();
        final String userText = currentDBCI.getUserString();
        final String passText = currentDBCI.getPassString();
        final String db       = currentDBCI.getDbString();

        //Double check, should never trigger
        if (!couldConnect() )
            return null;
        
            if (null == realConnection) {
                
            try {
                getContainer().run(false, true, 
                    new IRunnableWithProgress(){
    
                    public void run( IProgressMonitor monitor ) 
                                   throws InvocationTargetException,
                                          InterruptedException {
                        //TODO: why the extra layer compared to PostGIS?
                        PlatformGIS.runBlockingOperation(new IRunnableWithProgress(){
                            
                            public void run( IProgressMonitor monitor ) 
                                            throws InvocationTargetException, 
                                            InterruptedException {
                                
                                monitor.beginTask(Messages.OracleSpatialWizardPage_connectionTask, 
                                                  IProgressMonitor.UNKNOWN);
                                
                                if (realConnection != null)
                                    try {
                                        realConnection.close();
                                    } catch (SQLException e1) {
                                        // it's dead anyhow
                                    }
                                
                                OracleConnectionFactory connFac = 
                                    new OracleConnectionFactory(hostText,
                                                                portText,
                                                                db);
                                connFac.setLogin(userText, passText);
                                DriverManager.setLoginTimeout(3);
                                
                                
                            try {
                                realConnection = connFac.getConnectionPool().getConnection();


                                //TODO:Not sure what this was for but we can probably blow it away
//                              if (realConnection != null) {
//                                    OracleSpatialWizardPage.this.dbWgt.getDisplay().asyncExec(new Runnable(){
//                                        public void run() {
//                                            OracleSpatialWizardPage.this.dbWgt.notifyListeners(SWT.FocusIn,
//                                                    new Event());
//    
//                                        }
//                                    });
//                                }
                            } catch (SQLException e) {
                                throw new InvocationTargetException(e, e.getLocalizedMessage());
                            }
                            //TODO: Here the Postgis db has:
//                          if( monitor.isCanceled() )
//                              realConnection=null;
//                          monitor.done();
                          //should that be added?
                        }
                           
                       }, monitor);
                    }
                });
            } catch (InvocationTargetException e2) {
                throw new RuntimeException(e2.getLocalizedMessage(), e2);
            } catch (InterruptedException e2) {
                // Don't know why this exception doesn't do anything.
            }
        }
        
        return realConnection;
    }

    /**
     * TODO summary sentence for hasSchema ...
     * 
     * @see net.refractions.udig.catalog.internal.ui.datastore.DataBaseRegistryWizardPage#dbmsUsesSchema()
     * @return
     */
    protected boolean dbmsUsesSchema() {
        return true;
    }


    @Override
    protected boolean doIsPageComplete() {
        Map p = getParams();
        if (p == null)
            return false;
        boolean r = factory.canProcess(p);
        return r;
    }
    

    public List<IResolve> getResources(IProgressMonitor monitor)
            throws Exception {
        if (!isPageComplete())
            return null;

        OracleServiceExtension creator = new OracleServiceExtension();
        IService service = creator.createService(null, getParams());
        service.getInfo(monitor); // load

        List<IResolve> servers = new ArrayList<IResolve>();
        servers.add(service);
        return servers;
    }
}
