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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.refractions.udig.catalog.PostgisServiceExtension2;
import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.geotools.data.postgis.PostgisDataStoreFactory;

/**
 * A composite that helps a user select a set of tables in a database. It provides various methods
 * to for filtering the tables.
 * 
 * @author jesse
 * @since 1.1.0
 */
public class PostgisTableSelectionComposite implements Tab {

    private static final int LAYOUT_COLUMNS = 3;

    private CheckboxTableViewer tableViewer;
    private Button publicSchema;
    private Text filterBox;
    private Set<Listener> listeners=new HashSet<Listener>();

    /**
     * Creates the control. The style is the style passed to the main composite.
     * 
     * @param parent the parent composite
     * @param style the style to pass to the top level composite.
     * @return the top level control
     */
    public Control createControl( Composite parent, int style ) {
        Composite top = new Composite(parent, style);
        top.setLayout(new GridLayout(LAYOUT_COLUMNS, false));

        createFilterText(top);
        createFilterButtons(top);
        createTable(top);
        return top;
    }

    /**
     * Sets the input in the table viewer so it will display the collection
     * 
     * @param tables the table information to display.
     */
    public void setTableInput( Collection<PostgisTableDescriptor> tables ) {
        tableViewer.setInput(tables);
    }

    public Map<String, Serializable> getParams( Map<String, Serializable> params ) {
        Object[] elements = tableViewer.getCheckedElements();
        
        if( elements.length==0){
            return null;
        }
        
        StringBuilder builder = new StringBuilder();
        for( int i = 0; i < elements.length; i++ ) {
            PostgisTableDescriptor descriptor = (PostgisTableDescriptor) elements[i];
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(descriptor.schema);
    
        }
        params.put(PostgisDataStoreFactory.SCHEMA.key, builder.toString());
        return params;
    }

    public boolean leavingPage() {
        return true;
    }

    public Collection<URL> getResourceIDs( Map<String, Serializable> params ) {
        Object[] elements = tableViewer.getCheckedElements();
        try {
            URL url = PostgisServiceExtension2.toURL(params);
            String serviceURL = url.toExternalForm();
            List<URL> urls = new ArrayList<URL>();
            for( int i = 0; i < elements.length; i++ ) {
                PostgisTableDescriptor descriptor = (PostgisTableDescriptor) elements[i];
                urls.add(new URL(url, serviceURL+"#"+descriptor.name)); //$NON-NLS-1$
            }
            return urls;
        } catch (MalformedURLException e) {
            // really shouldn't happen
            PostgisPlugin.log("Can't make URL", e); //$NON-NLS-1$
            return Collections.emptySet();
        }
    }

    public void addListener( Listener modifyListener ) {
        listeners.add(modifyListener);
    }

    private void createTable( Composite top ) {
        Table table = new Table(top, SWT.FULL_SELECTION | SWT.CHECK);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = LAYOUT_COLUMNS;
        table.setLayoutData(gridData);
        table.setHeaderVisible(true);
        TableLayout layout = new TableLayout();
        table.setLayout(layout);

        layout.addColumnData(new ColumnWeightData(1));
        layout.addColumnData(new ColumnWeightData(1));
        layout.addColumnData(new ColumnWeightData(1));

        TableColumn nameColumn = new TableColumn(table, SWT.LEFT);
        nameColumn.setText("Table");

        TableColumn schemaColumn = new TableColumn(table, SWT.LEFT);
        schemaColumn.setText("Schema");

        TableColumn geometryColumn = new TableColumn(table, SWT.LEFT);
        geometryColumn.setText("Geometry");

        tableViewer = new CheckboxTableViewer(table);
        tableViewer.setContentProvider(new FilteringContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());

        tableViewer.addDoubleClickListener(new CheckOnDoubleClickListener());
        tableViewer.addCheckStateListener(new ICheckStateListener(){

            public void checkStateChanged( CheckStateChangedEvent event ) {
                for( Listener l : listeners ) {
                    Event event2 = new Event();
                    event2.type=SWT.Modify;
                    Control control = tableViewer.getControl();
                    event2.display = control.getDisplay();
                    event2.widget=control;
                    l.handleEvent(event2);
                }
            }
            
        });
    }

    private void createFilterButtons( Composite top ) {
        publicSchema = new Button(top, SWT.CHECK);
        publicSchema.setText("Public Schema");
        publicSchema.setToolTipText("Shows only data that is part of the public postgis schema");

        Listener listener = new Listener(){

            public void handleEvent( Event event ) {
                tableViewer.refresh(false);
            }

        };
        publicSchema.addListener(SWT.Selection, listener);

        // other = new Button(top, SWT.CHECK);
        // other.setText("Viewable");
    }

    private void createFilterText( Composite top ) {
        Label label = new Label(top, SWT.NONE);
        label.setText("Filter");

        filterBox = new Text(top, SWT.BORDER);
        filterBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filterBox
                .setToolTipText("Shows data with a srid, name, geometry or schema that contains the filter text");

        filterBox.addListener(SWT.Modify, new Listener(){

            public void handleEvent( Event event ) {
                tableViewer.refresh(false);
            }

        });
    }

    private final class CheckOnDoubleClickListener implements IDoubleClickListener {
        public void doubleClick( DoubleClickEvent event ) {
            if (event.getSelection().isEmpty()
                    || !(event.getSelection() instanceof IStructuredSelection)) {
                return;
            }

            IStructuredSelection structured = (IStructuredSelection) event.getSelection();

            Object[] elements = structured.toArray();
            for( Object element : elements ) {
                boolean state = tableViewer.getChecked(element);
                tableViewer.setChecked(element, !state);
            }
        }
    }

    /**
     * Displays the postgis table name in the first column and the geometry type in the second.
     * 
     * @author jesse
     * @since 1.1.0
     */
    public class TableLabelProvider extends LabelProvider
            implements
                IBaseLabelProvider,
                ITableLabelProvider {

        public Image getColumnImage( Object element, int columnIndex ) {
            return null;
        }

        public String getColumnText( Object element, int columnIndex ) {
            PostgisTableDescriptor table = (PostgisTableDescriptor) element;
            switch( columnIndex ) {
            case 0:
                return table.name;
            case 1:
                return table.schema;
            case 2:
                return table.geometryType;
            default:
                throw new IllegalArgumentException(columnIndex
                        + " is not a valid index for this table"); //$NON-NLS-1$
            }
        }

    }

    /**
     * Filters the input based on the textbox and the button states in
     * {@link PostgisTableSelectionComposite}
     * 
     * @author jesse
     * @since 1.1.0
     */
    public class FilteringContentProvider extends ArrayContentProvider implements IContentProvider {
    
        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements( Object inputElement ) {
            Collection<PostgisTableDescriptor> tables = (Collection<PostgisTableDescriptor>) inputElement;
            Set<PostgisTableDescriptor> filtered = new HashSet<PostgisTableDescriptor>();
            for( PostgisTableDescriptor table : tables ) {
                if (publicSchema.getSelection()
                        && !table.schema.equals(PostgisDataStoreFactory.SCHEMA.sample)) {
                    continue;
                } else {
                    Pattern filter = Pattern.compile("\\w*" + filterBox.getText().toLowerCase() //$NON-NLS-1$
                            + "\\w*"); //$NON-NLS-1$
                    boolean geometryTypeMatch = filter.matcher(table.geometryType.toLowerCase())
                            .matches();
                    boolean nameMatch = filter.matcher(table.name.toLowerCase()).matches();
                    boolean sridMatch = filter.matcher(table.srid.toLowerCase()).matches();
                    boolean schemaMatch = filter.matcher(table.schema.toLowerCase()).matches();
    
                    if (geometryTypeMatch || nameMatch || sridMatch || schemaMatch) {
                        filtered.add(table);
                    }
                }
            }
    
            return filtered.toArray();
    
        }
    }

}
