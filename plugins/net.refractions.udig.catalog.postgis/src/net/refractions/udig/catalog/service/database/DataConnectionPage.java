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
package net.refractions.udig.catalog.service.database;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;

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
public class DataConnectionPage extends AbstractUDIGImportPage implements Listener {

    private Combo database;
    private UserHostPage userHostPage;
    Map<Control, Tab> tabs = new HashMap<Control, Tab>();
    private TableSelectionTab tableSelection;
    private TabFolder tabFolder;
    // for checking to see if the connection has changed 
    // if it has then the cached information must be changed.
    private String currentHost, currentUsername;
    private int currentPort;
    
    public DataConnectionPage() {
        super("Databse connection page");
    }

    @Override
    public boolean isPageComplete() {
        return getParams()!=null;
    }
    
    public Map<String, Serializable> getParams() {
        
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(userHostPage.dialect.hostParam.key, userHostPage.getHost());
        params.put(userHostPage.dialect.portParam.key, userHostPage.getPort());
        params.put(userHostPage.dialect.usernameParam.key, userHostPage.getUsername());
        params.put(userHostPage.dialect.passwordParam.key, userHostPage.getPassword());
        params.put(userHostPage.dialect.databaseParam.key, database.getText());
        params.put(userHostPage.dialect.schemaParam.key, (Serializable) userHostPage.dialect.schemaParam.sample);
        params.put(userHostPage.dialect.typeParam.key, (Serializable) userHostPage.dialect.typeParam.sample);
        
        Either<String, Map<String, Serializable>> result = getActiveTab().getParams(params);
        if( result.isLeft() ){
            setErrorMessage(result.getLeft());
            return null;
        }else{
            setErrorMessage(null);
            return result.getRight();
        }
    }
    
    private Tab getActiveTab() {
        int selection = tabFolder.getSelectionIndex();
        Control control2 = tabFolder.getItem(selection).getControl();
        
        return tabs.get(control2);
    }
    
    @Override
    public void shown() {
        boolean sameConnection = userHostPage.getHost().equals(currentHost) &&
                userHostPage.getPort() == currentPort &&
                userHostPage.getUsername().equals(currentUsername);
        
        if( !sameConnection ){
            for( Tab tab : tabs.values() ) {
                tab.init();
            }
            populateDatabaseCombo();
        }
        
        currentHost = userHostPage.getHost();
        currentPort = userHostPage.getPort();
        currentUsername = userHostPage.getUsername();
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
        userHostPage = (UserHostPage) getPreviousPage();

        Composite top = new Composite(parent, SWT.NONE);
        setControl(top);
        top.setLayout(new GridLayout(2, false));

        createDatabaseCombo(top);
        createLookupButton(top);
        tabFolder = createTabFolder(top);

        addTableSelectionTab(tabFolder);
        tabs.putAll(userHostPage.dialect.createOptionConnectionPageTabs(tabFolder, this));
    }

    private void addTableSelectionTab( TabFolder tabFolder ) {
        tableSelection = new TableSelectionTab(userHostPage.dialect);
        
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("Tables");
        item.setControl(tableSelection.createControl(tabFolder, SWT.NONE));
        tabs.put(item.getControl(), tableSelection);
        tableSelection.addListener(this);
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
                String database = DataConnectionPage.this.database.getText();

                LookUpSchemaRunnable runnable = userHostPage.dialect.createLookupSchemaRunnable(host, port, username,
                        password, database);
                try {
                    getContainer().run(false, true, runnable);
                    if (runnable.getError() != null) {
                        setErrorMessage(runnable.getError());
                        tableSelection.setTableInput(Collections.<TableDescriptor>emptySet());
                    } else {
                        setErrorMessage(null);
                        tableSelection.setTableInput(runnable.getSchemas());
                    }
                    getContainer().updateButtons();
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

        populateDatabaseCombo();
        database.addListener(SWT.KeyUp, new Listener(){
            
            public void handleEvent( Event event ) {
                String[] items = database.getItems();
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

    private void populateDatabaseCombo() {
        String[] names = userHostPage.getDatabaseNames();
        database.setText("");
        if( names.length == 0){
            database.setItems(new String[0]);
            setMessage("You do not have permissions to access the list of all databases, Please enter the database to connect to", WARNING);
        } else {
            setMessage(null);
            Arrays.sort(names);

            String[] items = new String[names.length + 1];
            items[0] = ""; //$NON-NLS-1$
            System.arraycopy(names, 0, items, 1, names.length);

            database.setItems(items);
        }
    }

    @Override
    public Collection<URL> getResourceIDs() {
        Map<String, Serializable> params = getParams();
        if( params==null ){
            return Collections.emptySet();
        }
        return getActiveTab().getResourceIDs(params);
    }

    public void handleEvent( Event event ) {
        getContainer().updateButtons();
    }
    
}
