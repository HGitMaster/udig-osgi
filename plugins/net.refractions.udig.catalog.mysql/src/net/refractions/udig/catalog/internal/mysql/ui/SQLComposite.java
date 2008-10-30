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
package net.refractions.udig.catalog.internal.mysql.ui;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

/**
 * Provides a Text area for entering SQL and keeps a history of the SQL in the dialog constants provided.
 * 
 * @author jesse
 * @author Harry Bullen, Intelligent Automation
 * @since 1.1.0
 */
public class SQLComposite implements Listener, Tab {

    private Text sqlText;
    @SuppressWarnings("unused")
	private IDialogSettings settings;
    
    public SQLComposite(IDialogSettings settings) {
        this.settings = settings;
    }

    /**
     * Creates the control.  The style is the style passed to the main composite.
     *
     * @param parent the parent composite
     * @param style the style to pass to the top level composite.
     * 
     * @return the top level control
     */
    public Control createControl( TabFolder tabFolder, int style ) {
        Composite top = new Composite(tabFolder, style);
        top.setLayout(new GridLayout(1,false));
        createLabel(top);
        createTextArea(top);
        createHistoryButtons(top);
        createTestButton(top);
        return top;
    }

    private void createHistoryButtons( Composite parent ) {
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false,false));
        top.setLayout(new GridLayout(4,false));
        
        Button startButton = new Button(top, SWT.PUSH);
        startButton.setText("<<");
        startButton.addListener(SWT.Selection, this);

        Button backButton = new Button(top, SWT.PUSH);
        backButton.setText("<");
        backButton.addListener(SWT.Selection, this);

        Button forwardButton = new Button(top, SWT.PUSH);
        forwardButton.setText(">");
        forwardButton.addListener(SWT.Selection, this);

        Button endButton = new Button(top, SWT.PUSH);
        endButton.setText(">>");
        endButton.addListener(SWT.Selection, this);
}

    private void createLabel( Composite top ) {
        Label label = new Label(top, SWT.NONE);
        label.setText("SQL");
    }

    private void createTestButton(Composite top) {
        Button testButton = new Button(top, SWT.PUSH);
        testButton.setText("Test");
        testButton.addListener(SWT.Selection, new Listener(){

            public void handleEvent( Event event ) {
                // TODO implement test button
            }
            
        });
        
    }

    private void createTextArea(Composite top) {
        sqlText = new Text(top, SWT.MULTI|SWT.BORDER);
        sqlText.setLayoutData(new GridData(GridData.FILL_BOTH));
        
    }

    public void handleEvent( Event event ) {
        // implement for the pressing of the history buttons.
    }

    public Map<String, Serializable> getParams() {
        return null;
    }

    public boolean leavingPage() {
        return false;
    }

    public void setWizard( IWizard wizard ) {
    }

}
