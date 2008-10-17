/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
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
package net.refractions.udig.catalog.internal.postgis.ui;

import static org.geotools.data.postgis.PostgisDataStoreFactory.DATABASE;
import static org.geotools.data.postgis.PostgisDataStoreFactory.DBTYPE;
import static org.geotools.data.postgis.PostgisDataStoreFactory.HOST;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PASSWD;
import static org.geotools.data.postgis.PostgisDataStoreFactory.PORT;
import static org.geotools.data.postgis.PostgisDataStoreFactory.SCHEMA;
import static org.geotools.data.postgis.PostgisDataStoreFactory.USER;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;
import net.refractions.udig.catalog.postgis.internal.Messages;
import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * This page allows the user to select the database and rest of the information for creating a layer
 * from the database. (Schema, table, SQL)
 * 
 * @author jesse
 * @since 1.1.0
 */
public class PostgisConnectionPage extends AbstractUDIGImportPage implements Listener {

    private Combo database;
    private PostgisUserHostPage userHostPage;
    Map<Control, Tab> tabs = new HashMap<Control, Tab>();
    private PostgisTableSelectionComposite tableSelection;
    private TabFolder tabFolder;
    
    public PostgisConnectionPage() {
        super(Messages.PostGisWizardPage_title);
    }

    @Override
    public boolean isPageComplete() {
        return getParams()!=null;
    }
    
    public Map<String, Serializable> getParams() {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(HOST.key, userHostPage.getHost());
        params.put(PORT.key, userHostPage.getPort());
        params.put(USER.key, userHostPage.getUsername());
        params.put(PASSWD.key, userHostPage.getPassword());
        params.put(DATABASE.key, database.getText());
        params.put(SCHEMA.key, (Serializable) SCHEMA.sample);
        params.put(DBTYPE.key, (Serializable) DBTYPE.sample);

        return getActiveTab().getParams(params);
    }
    
    private Tab getActiveTab() {
        int selection = tabFolder.getSelectionIndex();
        Control control2 = tabFolder.getItem(selection).getControl();
        
        return tabs.get(control2);
    }

    @Override
    public boolean leavingPage() {
        return getActiveTab().leavingPage();
    }

    public void createControl( Composite parent ) {
        Point size = getShell().getSize();
        if (size.y < 640) {
            getShell().setSize(size.x, 640);
        }
        userHostPage = (PostgisUserHostPage) getPreviousPage();

        Composite top = new Composite(parent, SWT.NONE);
        setControl(top);
        top.setLayout(new GridLayout(2, false));

        createDatabaseCombo(top);
        createLookupButton(top);
        tabFolder = createTabFolder(top);

        addTableSelectionTab(tabFolder);
        addSQLTab(tabFolder);
    }

    private void addTableSelectionTab( TabFolder tabFolder ) {
        tableSelection = new PostgisTableSelectionComposite();
        
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("Tables");
        item.setControl(tableSelection.createControl(tabFolder, SWT.NONE));
        tabs.put(item.getControl(), tableSelection);
        tableSelection.addListener(this);
    }

    private void addSQLTab( TabFolder tabFolder ) {
        SQLComposite sqlComposite = new SQLComposite(getDialogSettings());
        
        sqlComposite.setWizard(getWizard());
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("SQL");
        item.setControl(sqlComposite.createControl(tabFolder, SWT.NONE));
        tabs.put(item.getControl(), sqlComposite);
    }

    private TabFolder createTabFolder( Composite top ) {
        TabFolder folder = new TabFolder(top, SWT.TOP);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.horizontalSpan = 2;
        folder.setLayoutData(layoutData);
        return folder;
    }

    private void createLookupButton( Composite top ) {
        Button button = new Button(top, SWT.PUSH);
        button.setText("Lookup Tables");

        GridData layoutData = new GridData();
        layoutData.horizontalSpan = 2;
        button.setLayoutData(layoutData);

        button.addListener(SWT.Selection, new Listener(){

            public void handleEvent( Event event ) {
                String host = userHostPage.getHost();
                int port = userHostPage.getPort();
                String password = userHostPage.getPassword();
                String username = userHostPage.getUsername();
                String database = PostgisConnectionPage.this.database.getText();

                LookUpSchemaRunnable runnable = new LookUpSchemaRunnable(host, port, username,
                        password, database);
                try {
                    getContainer().run(false, true, runnable);
                    if (runnable.getError() != null) {
                        setErrorMessage(runnable.getError());
                    } else {
                        tableSelection.setTableInput(runnable.getSchemas());
                    }
                } catch (InvocationTargetException e) {
                    throw (RuntimeException) new RuntimeException().initCause(e);
                } catch (InterruptedException e) {
                    throw (RuntimeException) new RuntimeException().initCause(e);
                }
            }

        });
    }

    private void createDatabaseCombo( Composite top ) {
        Label label = new Label(top, SWT.NONE);
        label.setText("Database");

        database = new Combo(top, SWT.BORDER);
        database.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String[] names = userHostPage.getDatabaseNames();

        if( names.length == 0){
            setMessage("You do not have permissions to access the list of all databases, Please enter the database to connect to", WARNING);
        } else {
            Arrays.sort(names);

            final String[] items = new String[names.length + 1];
            items[0] = ""; //$NON-NLS-1$
            System.arraycopy(names, 0, items, 1, names.length);

            database.setItems(items);
            database.addListener(SWT.KeyUp, new Listener(){

                public void handleEvent( Event event ) {
                    // if the key pressed is a word character
                    if (("" + event.character).matches("\\w") || event.keyCode == SWT.BS  //$NON-NLS-1$//$NON-NLS-2$
                            || event.keyCode == SWT.DEL) {
                        String string = database.getText();
                        if (string.trim().length() == 0) {
                            database.setItems(items);
                        } else {
                            List<String> filtered = new ArrayList<String>();
                            for( String item : items ) {
                                if (item.startsWith(string)) {
                                    filtered.add(item);
                                }
                            }

                            database.setItems(filtered.toArray(new String[0]));
                            database.setText(string);
                        }
                    }
                }

            });
        }
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return PostgisPlugin.getDefault().getDialogSettings();
    }

    @Override
    public Collection<URL> getResourceIDs() {
        return getActiveTab().getResourceIDs(getParams());
    }

    public void handleEvent( Event event ) {
        getContainer().updateButtons();
    }
    
}
