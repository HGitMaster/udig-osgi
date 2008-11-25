/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2005-2007, Refractions Research Inc.
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
package net.refractions.udig.catalog.internal.db2.ui;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.db2.DB2Plugin;
import net.refractions.udig.catalog.db2.internal.Messages;
import net.refractions.udig.catalog.internal.db2.DB2ServiceExtension;
import net.refractions.udig.catalog.ui.preferences.AbstractProprietaryDatastoreWizardPage;
import net.refractions.udig.catalog.ui.preferences.AbstractProprietaryJarPreferencePage;
import net.refractions.udig.catalog.ui.wizard.DataBaseConnInfo;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.db2.DB2ConnectionFactory;
import org.geotools.data.db2.DB2DataStoreFactory;

/**
 * Specify DB2 database connection parameters.
 * <p>
 * </p>
 * 
 * @author   Justin Deoliveira,  jdeolive,       for Refractions Research, Inc.
 * @author   dadler
 * @author   Jesse Eichar,       jeichar,        for Refractions Research, Inc.
 * @author   Richard Gould,      rgould,         for Refractions Research, Inc.
 * @author   Adrian Custer,      acuster.
 * 
 * @since 1.0.1
 */
public class DB2WizardPage extends AbstractProprietaryDatastoreWizardPage {

    //TITLE IMAGE
    public final String IMAGE_KEY = "DB2PageImage"; //$NON-NLS-1$   
    //STORED SETTINGS
    private static final String DB2_RECENT = "DB2_RECENT"; //$NON-NLS-1$
    private static final String DB2_WIZARD = "DB2_WIZARD"; //$NON-NLS-1$
    //CONNECTION 
    // TODO: doesn't db2 use 446 as in "http://publib.boulder.ibm.com/infocenter/dzichelp/v2r2/index.jsp?topic=/com.ibm.db29.doc.inst/tcpip.htm" ?
    private static final DataBaseConnInfo DEFAULT_DB2_CONN_INFO = 
        new DataBaseConnInfo("","50000","","","","");                                //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    private static DB2DataStoreFactory factory = new DB2DataStoreFactory();

    //TO UNDERSTAND
    ArrayList<DataBaseConnInfo> dbData;
    private DB2Preferences preferences;

    /**
     * Constructs a DB2 database connection wizard page. Reads any settings that may have been saved
     * from a previous session.
     */
    public DB2WizardPage() {
        
        //Call super with dialog title string
        super(Messages.DB2WizardPage_title); 
        
        //Get any stored settings or create a new one
        settings = DB2Plugin.getDefault().getDialogSettings().getSection(DB2_WIZARD);
        if (settings == null) {
            settings = DB2Plugin.getDefault().getDialogSettings().addNewSection(DB2_WIZARD);
        }
        
        //Add the name so the parent can store back to this same section
        settingsArrayName = DB2_RECENT;

        //Populate the Settings: default, current, and past list
        defaultDBCI.setParameters(DEFAULT_DB2_CONN_INFO);
        currentDBCI.setParameters(defaultDBCI);
        String[] recent = settings.getArray(DB2_RECENT);
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
    /**
     * Checks if all user input fields are non-empty.
     * 
     * @return true if all needed fields are non-empty.
     */

    protected boolean areAllFieldsFilled() {
        
        //What does this do?
        if (!DB2Preferences.isInstalled())
            return false;
        
        return couldConnect();
        
    }

    private String emptyAsNull( String value ) {
        if (value.length() == 0)
            return null;
        return value;
    }
    /**
     * Always returns false as we want to keep all schema candidates.
     * 
     * @param schemaName
     * @return false
     */
    @Override
    protected boolean excludeSchemaFromUserChoices( String schemaName ) {
        return false;
    }
    /**
     * Gets a connection to the DB2 database. The port, host, userid, password and database name
     * must have been specified in order for the connection to succeed.
     * 
     * @return a database Connection
     * @throws Exception
     */
    @Override
    protected Connection getConnection() {

        final String hostText = currentDBCI.getHostString();
        final String portText = currentDBCI.getPortString();
        final String userText = currentDBCI.getUserString();
        final String passText = currentDBCI.getPassString();
        final String db       = currentDBCI.getDbString();

        //TODO: this is what the parent couldConnect() does.
        if (!areAllFieldsFilled()) {
            return null;
        }

        if (realConnection == null) {

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
                                    
                                        monitor.beginTask(Messages.DB2WizardPage_connectionTask, 
                                                          IProgressMonitor.UNKNOWN); 
                                        
                                        if (realConnection != null)
                                            try {
                                                realConnection.close();
                                            } catch (SQLException e1) {
                                                // it's dead anyhow
                                            }
        
                                        DB2ConnectionFactory connFac = 
                                            new DB2ConnectionFactory(hostText, 
                                                                     portText,
                                                                     db);
                                        connFac.setLogin(userText, passText);
                                        DriverManager.setLoginTimeout(3);
                                        try {
                                            realConnection = connFac.getConnectionPool().getConnection();
                                            
                                            //TODO:Not sure what this was for but we can probably blow it away
//                                            if (realConnection != null) {
//                                                dbComboWgt.getDisplay().asyncExec(new Runnable(){
//                                                    public void run() {
//                                                        dbComboWgt.notifyListeners(SWT.FocusIn,
//                                                                new Event());
//        
//                                                    }
//                                                });
//                                            }
                                        } catch (SQLException e) {
                                            throw new InvocationTargetException(e, e.getLocalizedMessage());
                                        }
                                        //TODO: Here the Postgis db has:
//                                        if( monitor.isCanceled() )
//                                            realConnection=null;
//                                        monitor.done();
                                        //should that be added?
                                    }
                                
                            }, monitor);
                        }
                    });
            } catch (InvocationTargetException e2) {
                preferences.performDefaults();
                throw new RuntimeException(e2.getLocalizedMessage(), e2);
            } catch (InterruptedException e2) {
                // Don't know why this exception doesn't do anything.
            }
        }

        return realConnection;
    }
    /**
     * Returns the DB2DataStoreFactory.
     * 
     * @return the DB2DataStoreFactory
     */
    @Override
    protected DataStoreFactorySpi getDataStoreFactorySpi() {
        return factory;
    }
    /**
     * Returns a string with the name of the DB2 plugin
     * 
     * @return the DB2 plugin name
     */
    public String getId() {
        return "net.refractions.udig.catalog.ui.db2"; //$NON-NLS-1$
    }
    /**
     * Returns the parameters Empty strings are converted to null to work correctly with
     * factory.canProcess.
     * 
     * @return a map of parameter values
     */
    @Override
    public Map<String, Serializable> getParams() {
        
        if( !areAllFieldsFilled() || getConnection()==null ){
            return null;
        }
        
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        Param[] dbParams = factory.getParametersInfo();
        params.put(dbParams[0].key, "db2"); //$NON-NLS-1$
        params.put(dbParams[1].key, emptyAsNull(currentDBCI.getHostString()));
        String dbport = emptyAsNull(currentDBCI.getPortString());
        try {
            params.put(dbParams[2].key, emptyAsNull(dbport));
        } catch (NumberFormatException e) {
            params.put(dbParams[2].key, "50000"); //$NON-NLS-1$
        }
        String db = currentDBCI.getDbString();
        params.put(dbParams[3].key, emptyAsNull(db));

        String userName = currentDBCI.getUserString();
        params.put(dbParams[4].key, emptyAsNull(userName));
        String password = currentDBCI.getPassString();
        params.put(dbParams[5].key, emptyAsNull(password));

        params.put(dbParams[6].key, emptyAsNull(currentDBCI.getSchemaString())); 

        return params;
    }
    
    /**
     * Creates the DB2 service so we can do real work. Saves the values of text fields from the GUI
     * so that they can be used the next time this GUI page is displayed.
     * 
     * @param monitor
     * @return a List with the DB2 service
     * @throws Exception
     */
    public List<IService> getResources( IProgressMonitor monitor ) throws Exception {
        if (!isPageComplete())
            return null;

        DB2ServiceExtension creator = new DB2ServiceExtension();
        IService service = creator.createService(null, getParams());
        service.getInfo(monitor); // load

        List<IService> servers = new ArrayList<IService>();
        servers.add(service);

        /*
         * Success! Store the connection settings in history.
         */
//        saveWidgetValues();
        //TODO: Review: This no longer exists so was removed-AVC

        return servers;
    }
    
    /**
     * DB2 always requires the schema.
     * 
     * @return true
     */
    @Override
    protected boolean dbmsUsesSchema() {
        return true;
    }

    @Override
    public boolean doIsPageComplete() {
        boolean isComplete = false;
        
        if (areAllFieldsFilled())
            isComplete = factory.canProcess(getParams());
        
        return isComplete;
    }
    /**
     * TODO: VERIFY
     * According the Javadocs in the previous version of this class, DB2 does
     * not support getting database names. If that's true, then we will have
     * to activate the method below.
     */
//    @Override
//	  protected ResultSet getDatabaseResultSet(Connection c) throws SQLException
//    protected String [] lookupDbNamesForDisplay(Connection con) 
//    {
//        return null;
//    }
    
    /**
     * Gets the names of all the schema available for the specified database. 
     * 
     * The DB2 catalog table db2gse.st_geometry_columns is used to get a list of 
     * all the schema values associated with tables that have spatial columns.
     */
    @Override
    protected ResultSet getSchemasResultSet(Connection c) throws SQLException
    {
    	Statement statement = c.createStatement();
    	return statement.executeQuery(
    		"SELECT DISTINCT table_schema FROM db2gse.st_geometry_columns"); //$NON-NLS-1$
    	// Ideally we should be closing the statement but we cannot
    	// and the connection should be closed soon anyways.
    }
    
    @Override
    protected void doCreateWizardPage( Composite parent ) {
        
        //TODO: Review and remove. The string is now set in the default 
//        this.portWgt.setTextLimit(5);
//        this.portWgt.setText("50000"); //$NON-NLS-1$
//        this.schemaWgt.setEnabled(false);
        
        //TODO: Remove. This is now done in the DataBaseRegistryWizardPage
//        String[] recentDB2s = this.settings.getArray(DB2_RECENT);
//        ArrayList<String> hosts = new ArrayList<String>();
//        this.dbData = new ArrayList<DataBaseConnInfo>();
//        if (recentDB2s != null) {
//            for( String recent : recentDB2s ) {
//                DataBaseConnInfo dbs = new DataBaseConnInfo(recent);
//                this.dbData.add(dbs);
//                hosts.add(dbs.getHostString());
//            }
//        }
//        if (hosts.size() > 0) {
//            ((CCombo) this.hostWgt).setItems(hosts.toArray(new String[0]));
//            ((CCombo) this.hostWgt).addModifyListener(new ModifyListener(){
//                public void modifyText( ModifyEvent e ) {
//                    if (e.widget != null) {
//                        for( DataBaseConnInfo db : DB2WizardPage.this.dbData ) {
//                            if (db.getHostString().equalsIgnoreCase(getHostText())) {
//                                setPortText(db.getPortString());
//                                setUserText(db.getUserString());
//                                setPassText(db.getPassString());
//                                setPassText(db.getPassString());
//                                setDBText(db.getDbString());
//                                 DB2WizardPage.this.schemaWgt.setText(db.getSchemaString());
//                                break;
//                            }
//                        }
//                    }
//                }
//            });
//        }
    }
    @Override
    protected String getDriversMessage() {
        return Messages.DB2WizardPage_installDrivers;
    }
    @Override
    protected AbstractProprietaryJarPreferencePage getPreferencePage() {
        return new DB2Preferences();
    }
    @Override
    protected String getRestartMessage() {
        return Messages.DB2WizardPage_warning;
    }

}