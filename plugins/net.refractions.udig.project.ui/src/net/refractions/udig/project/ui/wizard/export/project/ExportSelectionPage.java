/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.refractions.udig.project.ui.wizard.export.project;

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ExportSelectionPage extends WizardPage {

    private DirectoryFieldEditor editor;
    
    public ExportSelectionPage(String title, String description) {
        super(title);
        setTitle(title);
        setDescription(description);
    }
    
    public void createControl( Composite parent ) {
         Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL);
        fileSelectionArea.setLayoutData(fileSelectionData);
        fileSelectionArea.setLayout(new GridLayout(3, false));
        createFileEditor(fileSelectionArea);
        fileSelectionArea.moveAbove(null);
        setControl(fileSelectionArea);
        setPageComplete(false);
        setMessage(null);
        setErrorMessage(null);
    }

    private void createFileEditor( Composite parent ) {
        editor = new DirectoryFieldEditor("directorySelect", "Destination: ", parent){
            @Override
            public boolean isValid() {
                File file = new File(editor.getStringValue());
                return file.isDirectory();
            }
        };
        editor.getTextControl(parent).addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent e ) {
                setPageComplete(editor.isValid());
            }
        });
    }
 
    public String getDestinationDirectory(){
        return editor.getStringValue();
    }
}
