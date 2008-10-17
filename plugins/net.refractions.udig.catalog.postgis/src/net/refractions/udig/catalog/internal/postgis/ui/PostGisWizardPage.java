/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004-2007, Refractions Research, Inc.
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
package net.refractions.udig.catalog.internal.postgis.ui;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;
import net.refractions.udig.catalog.postgis.internal.Messages;
import net.refractions.udig.catalog.ui.wizard.DataBaseConnInfo;
import net.refractions.udig.catalog.ui.wizard.DataBaseRegistryWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.postgis.PostgisConnectionFactory;
import org.geotools.data.postgis.PostgisDataStoreFactory;

/**
 * The class which provides the wizard 'Page' which the eclipse 'Import...' 
 * system uses first to get connection parameters and then to actually create a 
 * JDBC 'Connection' to a Postgis database.
 * 
 * 
 * The class provides the GUI 'page' required by the 'wizardPage' part of the 
 *     <code>net.refractions.udig.catalog.ui.connectionFactory</code>
 * extension point contract between this plugin and the uDig catalog system.
 * 
 * 
 * The class extends the abstract DataBaseRegistryWizardPage which has most of 
 * the GUI and logic. This class implements abstract methods and overwrites some 
 * methods of the parent class. 
 * 
 * The only GUI added by this class is in this implementation of the 
 *   createAdvancedControl(..)
 * method which creates the 'Advanced' section of the wizard Page. 
 * 
 * The key methods added by this class include the 
 *   getConnection(..) 
 * method which actually connects to the database and the various event 
 * listeners added by this class to handle Postgis specific issues.
 * 
 * 
 * Following the contract specified in the parent DataBaseRegistryWizardPage, 
 * the class provides the following functionality:
 * 
 *   * the constructor provides a connection to the Eclipse settings storage 
 *     system, possibly obtaining stored connection parameters if they exist.
 *         
 * 
 * @author   David Zwiers,       dzwiers,        for Refractions Research, Inc.
 * @author   Jody Garnett,       jody,           for Refractions Research, Inc.
 * @author   Jesse Eichar,       jeichar,        for Refractions Research, Inc.
 * @author   Richard Gould,      rgould,         for Refractions Research, Inc.
 * @author   Justin Deoliveira,  jdeolive,       for Refractions Research, Inc.
 * @author   Amr Alam,           aalam,          for Refractions Research, Inc.
 * @author   Cory Horner,        chorner,        for Refractions Research, Inc.
 * @author   Adrian Custer,      acuster.
 * 
 * 
 * @since 0.3
 */
public class PostGisWizardPage extends DataBaseRegistryWizardPage {

    //TITLE IMAGE
    public static final String IMAGE_KEY = "PostGisWizardPageImage"; //$NON-NLS-1$
    //STORED SETTINGS
    private static final String POSTGIS_WIZARD = "POSTGIS_WIZARD"; //$NON-NLS-1$
    private static final String POSTGIS_RECENT = "POSTGIS_RECENT"; //$NON-NLS-1$
    //CONNECTION 
    private static final DataBaseConnInfo DEFAULT_POSTGIS_CONN_INFO = new DataBaseConnInfo(
            "", "5432", "", "", "template1", "public"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    private static PostgisDataStoreFactory factory = new PostgisDataStoreFactory();
    private PostGisConnectionFactory pgcFactory = new PostGisConnectionFactory();

    //WIDGETS for the Advanced GUI section
    private Button wkbBtnWgt = null;
    private Button looseBBoxBtnWgt = null;

    /**
     * The constructor gets an IDialogSettings instance linked to the 
     * 'POSTGIS_WIZARD' section of the preferences either by getting the stored
     * parameters or by making the section.
     * 
     * The constructor is called on plugin activation immediately prior to the 
     * createControl(..) method below.
     * 
     * CONTRACT:
     *   O. TODO: Check for the network (for all factories that rely on TCP/IP)
     *   1. call the super("Title")
     *   2. get the settings (or create one)
     *   3. populate the past connection list
     *   4. populate the db and schema exclusion lists
     *   ?. extend the char and charsequence exclusion lists TODO
     * 
     */
    public PostGisWizardPage() {

        //Call super with dialog title string
        super(Messages.PostGisWizardPage_title);

        //Get any stored settings or create a new one
        settings = PostgisPlugin.getDefault().getDialogSettings().getSection(POSTGIS_WIZARD);
        if (null == settings) {
            settings = PostgisPlugin.getDefault().getDialogSettings().addNewSection(POSTGIS_WIZARD);
        }

        //Add the name so the parent can store back to this same section
        settingsArrayName = POSTGIS_RECENT;

        //Populate the Settings: default, current, and past list
        defaultDBCI.setParameters(DEFAULT_POSTGIS_CONN_INFO);
        currentDBCI.setParameters(defaultDBCI);
        String[] recent = settings.getArray(POSTGIS_RECENT);
        if (null != recent) {
            for( String s : recent ) {
                DataBaseConnInfo dbs = new DataBaseConnInfo(s);
                if (!storedDBCIList.contains(dbs))
                    storedDBCIList.add(dbs);
            }
        }

        //Populate the db and schema exclusion lists
        dbExclusionList.add("template0"); //$NON-NLS-1$
        dbExclusionList.add("template1"); //$NON-NLS-1$
        schemaExclusionList.add("information_schema"); //$NON-NLS-1$
        schemaExclusionList.add("pg_catalog"); //$NON-NLS-1$

        //Populate the Char and CharSeq exclusion lists
        //TODO: when we activate Verification

    }

    //UTILITY METHODS
    /** 
     * Called during createControl to handle Drag-n-drop of a selected object.
     * 
     * TODO: move to createControl?
     * TODO: flesh out, what can selection be?
     */
    protected Map<String, Serializable> getParamsFromWorkbenchSelection() {
        IStructuredSelection selection = (IStructuredSelection) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (null == selection)
            return Collections.emptyMap();
        for( Iterator< ? > itr = selection.iterator(); itr.hasNext(); ) {
            Map<String, Serializable> params = pgcFactory.createConnectionParameters(itr.next());
            if (params != null && !params.isEmpty())
                return params;
        }
        //TODO: Why is this here? Is this to handle the existence of a selection
        //      but one for which the for loop is going to create garbage? If so,
        //      let's not make the garbage.
        return Collections.emptyMap();

    }
    /**
     * Does the Database Management System (DBMS) use schemata for connections.
     * 
     * @return true because PostgreSQL uses schemata
     */
    @Override
    protected boolean dbmsUsesSchema() {
        return true;
    }
    /**
     * Returns the connection parameters as a DBMS specific map of key:value, 
     * with the keys coming from the Geotools DataStore Factory class.
     * 
     * @return the map of factoryKey:Value pairs used for the connection
     */
    @Override
    public Map<String, Serializable> getParams() {

        Map<String, Serializable> params = new HashMap<String, Serializable>();

        params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis"); //$NON-NLS-1$

        currentDBCI.treatEmptyStringAsNull(true);
        params.put(PostgisDataStoreFactory.HOST.key, currentDBCI.getHostString());
        params.put(PostgisDataStoreFactory.PORT.key, new Integer(currentDBCI.getPortString()));
        params.put(PostgisDataStoreFactory.USER.key, currentDBCI.getUserString());
        params.put(PostgisDataStoreFactory.PASSWD.key, currentDBCI.getPassString());
        params.put(PostgisDataStoreFactory.DATABASE.key, currentDBCI.getDbString());
        params.put(PostgisDataStoreFactory.SCHEMA.key, currentDBCI.getSchemaString());

        currentDBCI.treatEmptyStringAsNull(false);

        if (wkbBtnWgt.getSelection())
            params.put(PostgisDataStoreFactory.WKBENABLED.key, Boolean.TRUE);
        if (looseBBoxBtnWgt.getSelection())
            params.put(PostgisDataStoreFactory.LOOSEBBOX.key, Boolean.TRUE);

        params.put(PostgisDataStoreFactory.NAMESPACE.key, ""); //$NON-NLS-1$

        return params;
    }
    /**
     * Will be called automatically by the RCP 'Import...' mechanism when we
     * call updateButtons() in the widgetSelected handler section for the 
     * "Connect" button.
     */
    @Override
    public boolean isPageComplete() {

        return (null != realConnection) && factory.canProcess(getParams());

    }

    //TODO: what are these for and about?
    @Override
    protected DataStoreFactorySpi getDataStoreFactorySpi() {
        return factory;
    }
    public String getId() {
        return "net.refractions.udig.catalog.ui.postgis"; //$NON-NLS-1$
    }
    //TODO: move me up to DBRegWizPage
    @Override
    public void dispose() {

        if (realConnection != null) {
            try {
                if (!realConnection.isClosed()) {
                    realConnection.close();
                }
            } catch (SQLException e) {
                //couldn't close connection, no matter -- we are exiting
            }
        }
        super.dispose();
    }

    //CONTRACT METHODS
    /**
     * The method createControl(..) is the method initially called by the plugin
     * system when the plugin is activated and asked to display its GUI 'group' 
     * in the 'Import...' wizard.
     * 
     * The method first calls the overridden method in the parent class which 
     * does most of the work creating the GUI. 
     * 
     * The method then handles drag-n-drop by setting the text values.
     * TODO: Flesh out this documentation since I don't yet understand the dnd
     *       system.
     * 
     * @param arg0 the Composite wiget into which the top level group will be 
     *             added.
     */
    @Override
    public void createControl( Composite arg0 ) {

        super.createControl(arg0);

        // Handle Drag-n-drop by looking at the Map of parameters
        Map<String, Serializable> params = getParamsFromWorkbenchSelection(); // based on selection
        String selectedHost = (String) params.get(PostgisDataStoreFactory.HOST.key);
        if (selectedHost != null) {
            //TODO: make sure this triggers a modifyEvent which then puts the value in 
            //      currentDBCI
            hostTextWgt.setText(params.get(PostgisDataStoreFactory.HOST.key).toString());
            portTextWgt.setText(params.get(PostgisDataStoreFactory.PORT.key).toString());
            userTextWgt.setText(params.get(PostgisDataStoreFactory.USER.key).toString());
            passTextWgt.setText(params.get(PostgisDataStoreFactory.PASSWD.key).toString());
            dbComboWgt.setText(params.get(PostgisDataStoreFactory.DATABASE.key).toString());
            schemaComboWgt.setText(params.get(PostgisDataStoreFactory.SCHEMA.key).toString());
        }

    }
    /**
     * Creates the GUI that will be shown when the advanced button is clicked
     * in the dialog.
     * 
     * TODO: Document what these settings do.
     * 
     * For a complex set of Database specific parameters, we could override the
     * selectionEvent(.) handler method, starting with a call to the method in 
     * the super class and extending the 'switch on widget' mechanism of the 
     * super class method to handle each of the widgets in our advanced group. 
     * In our case, this is not necessary since the widgets are simple check 
     * buttons and the state of these buttons will be read directly during the 
     * creation of the actual connection when the 'Finished' button is pressed.
     * TODO: verify and explain where the call is made.
     * 
     * 
     * @param arg0 The Composite into which the GUI must be added.
     * 
     * @return the GUI Group to be added to the wizard page.
     */
    @Override
    protected Group createAdvancedControl( Composite arg0 ) {

        Group advGrp = new Group(arg0, SWT.SHADOW_NONE);
        advGrp.setLayout(new GridLayout(1, false));

        wkbBtnWgt = new Button(advGrp, SWT.CHECK);
        wkbBtnWgt.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));
        wkbBtnWgt.setText(Messages.PostGisWizardPage_button_wkb_text);
        wkbBtnWgt.setToolTipText(Messages.PostGisWizardPage_button_wkb_tooltip);
        wkbBtnWgt.setSelection(true);

        looseBBoxBtnWgt = new Button(advGrp, SWT.CHECK);
        looseBBoxBtnWgt.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));
        looseBBoxBtnWgt.setText(Messages.PostGisWizardPage_button_looseBBox_text);
        looseBBoxBtnWgt.setToolTipText(Messages.PostGisWizardPage_button_looseBBox_tooltip);
        looseBBoxBtnWgt.setSelection(true);

        return advGrp;
    }
    /**
     * The key method of the whole exercise, it will get a java.sql.Connection 
     * object with which we can move on to work with data.
     * 
     * Note this will be called both by the selection handler for both the 
     * lookup and the connect buttons so don't make assumptions about the 
     * connection.
     * 
     * @return The java.sql.Connection we will use to get and store data
     * 
     */
    @Override
    protected Connection getConnection() {

        try {
            getContainer().run(false, true, new IRunnableWithProgress(){

                public void run( IProgressMonitor monitor ) throws InvocationTargetException,
                        InterruptedException {

                    monitor.beginTask(Messages.PostGisWizardPage_0, IProgressMonitor.UNKNOWN);

                    if (realConnection != null)
                        try {
                            realConnection.close();
                        } catch (SQLException e1) {
                            // it's dead anyhow
                        }


                    PostgisConnectionFactory conFac = new PostgisConnectionFactory(currentDBCI.getHostString(),
                            currentDBCI.getPortString(), currentDBCI.getDbString());
                    conFac.setLogin(currentDBCI.getUserString(), currentDBCI.getPassString());
                    DriverManager.setLoginTimeout(3);
                    try {
                        if (monitor.isCanceled())
                            return;
                        realConnection = conFac.getConnection();
                    } catch (SQLException e) {
                        throw new InvocationTargetException(e, e.getLocalizedMessage());
                    }
                    if (monitor.isCanceled())
                        realConnection = null;
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e2) {
            throw new RuntimeException(e2.getLocalizedMessage(), e2);
        } catch (InterruptedException e2) {

        }

        return realConnection;
    }


    /**
     * Populates the database drop-down list. Implementation is identical to 
     * base class implementation in all regards except one: we can't use getCatalogs()
     * because the JDBC driver for PostgreSQL only returns one catalog (see
     * http://archives.postgresql.org/pgsql-jdbc/2005-11/msg00224.php for discussion on this change).
     */
    protected ResultSet getDatabaseResultSet(Connection c) throws SQLException
    {
    Statement statement = c.createStatement();
    return statement.executeQuery(
    		"SELECT datname from pg_database ORDER BY datname"); //$NON-NLS-1$
    // Ideally we should be closing the statement but we cannot
    // and the connection should be closed soon anyways.
    }
}
