package net.refractions.udig.catalog.service.database;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.PostgisServiceExtension2;
import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.geotools.data.DataAccessFactory.Param;

/**
 * This class abstracts out all of the service and database specific code for a
 * Geotools Database-based DatastoreIService extension.
 * 
 * @author jeichar
 */
public abstract class DatabaseServiceDialect {

	// The parameter information required for creating a Geotools Datastore.
	// Postgis was used as the template
	/**
	 * The key of the parameter that (at least in Postgis) identifies the schema
	 * that the table resides in.
	 */
	public final Param schemaParam;

	/**
	 * The key of the parameter that identifies the database (within the
	 * database, this is concept is inherited from Postgis)
	 */
	public final Param databaseParam;

	/**
	 * The key that identifies the host server of the database
	 */
	public final Param hostParam;

	/**
	 * The key that identifies the server port for connecting to the database
	 */
	public final Param portParam;

	/**
	 * The key that identifies connecting user's username
	 */
	public final Param usernameParam;

	/**
	 * The key that identifies connecting user's password
	 */
	public final Param passwordParam;

	/**
	 * The key that indicates the type of Datastore to create.  For example PostgisDataStoreFactory#DBTYPE
	 */
    public final Param typeParam;

    public DatabaseServiceDialect(Param schemaParam, Param databaseParam,
            Param hostParam, Param portParam, Param usernameParam,
            Param passwordParam, Param typeParam) {
        this.schemaParam = schemaParam;
        this.databaseParam = databaseParam;
        this.hostParam = hostParam;
        this.portParam = portParam;
        this.usernameParam = usernameParam;
        this.passwordParam = passwordParam;
        this.typeParam = typeParam;
    }

	public Collection<URL> constructResourceIDs(TableDescriptor[] descriptors, Map<String, Serializable> params) {
        try {
            URL url = PostgisServiceExtension2.toURL(params);
            String serviceURL = url.toExternalForm();
            List<URL> urls = new ArrayList<URL>();
            for( int i = 0; i < descriptors.length; i++ ) {
                TableDescriptor descriptor = descriptors[i];
                urls.add(new URL(url, serviceURL+"#"+descriptor.name)); //$NON-NLS-1$
            }
            return urls;
        } catch (MalformedURLException e) {
            // really shouldn't happen
            PostgisPlugin.log("Can't make URL", e); //$NON-NLS-1$
            return Collections.emptySet();
        }
	}

    public abstract IDialogSettings getDialogSetting();

    public abstract void log( String message, InvocationTargetException e );

    /**
     * Creates a {@link DatabaseConnectionRunnable}.
     *
     * @param host the url of the host to connect to
     * @param port the port on which to connect
     * @param username the username for connections
     * @param password the password for connection
     * @param database In postgis there are databases within a database.  This is a common construct but often
     *                 named differently.  Please try to make the mapping.  
     * @return {@link DatabaseConnectionRunnable}
     */
    public abstract DatabaseConnectionRunnable createDatabaseConnectionRunnable( String host, int port,
            String username, String password );

    /**
     * The TabSelectionTab is always created, this method allows the specific implementation to add
     * custom tabs. By default this method returns an empty HashMap
     *
     * @param tabFolder the parent folder of the tabs
     */
    public Map<Control, Tab> createOptionConnectionPageTabs( TabFolder tabFolder, DataConnectionPage containingPage ){
        return new HashMap<Control, Tab>();
    }

    /**
     * Creates a {@link LookUpSchemaRunnable}.
     *
     * @param host the url of the host to connect to
     * @param port the port on which to connect
     * @param username the username for connections
     * @param password the password for connection
     * @param database In postgis there are databases within a database.  This is a common construct but often
     *                 named differently.  Please try to make the mapping.  
     * @return {@link LookUpSchemaRunnable}
     */
    public abstract LookUpSchemaRunnable createLookupSchemaRunnable( String host, int port, String username,
            String password, String database );
}
