package net.refractions.udig.catalog.internal.postgis.ui;
import static org.geotools.data.postgis.PostgisDataStoreFactory.DATABASE;
import static org.geotools.data.postgis.PostgisDataStoreFactory.HOST;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PASSWD;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PORT;
import static org.geotools.data.postgis.PostgisDataStoreFactory.SCHEMA;
import static org.geotools.data.postgis.PostgisDataStoreFactory.USER;

import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;
import net.refractions.udig.catalog.service.database.DatabaseConnectionRunnable;
import net.refractions.udig.catalog.service.database.DatabaseServiceDialect;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * Describes the postgis parameters for creating the Postgis DataStore and ServiceExtension
 * 
 * @author jeichar
 */
public class PostgisServiceDialect extends DatabaseServiceDialect {

	public PostgisServiceDialect() {
		super(SCHEMA, DATABASE, HOST, PORT, USER, PASSWD);
	}

    @Override
    public IDialogSettings getDialogSetting() {
        return PostgisPlugin.getDefault().getDialogSettings();
    }

    @Override
    public void log( String message, InvocationTargetException e ) {
        PostgisPlugin.log(message, e);
    }

    @Override
    public DatabaseConnectionRunnable createDatabaseConnectionRunnable( String host, int port,
            String username, String password ) {
        return new PostgisDatabaseConnectionRunnable(host, port, username, password);
    }

}
