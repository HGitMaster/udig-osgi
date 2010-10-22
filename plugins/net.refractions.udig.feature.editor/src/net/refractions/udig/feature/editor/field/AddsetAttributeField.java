package net.refractions.udig.feature.editor.field;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

/* NOT to be fully implmented to the extent it will add stuff to EditManager just
 * showing the business users what it will look like
 */

public class AddsetAttributeField extends ListAttributeField {

    private String lastPath;

    /**
     * The special label text for directory chooser, 
     * or <code>null</code> if none.
     */
    private String prompt;

    /**
     * Creates a new add set attribute field 
     */
    protected AddsetAttributeField() {
    }

    /**
     * Creates a add set attribute field.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param prompt the label text displayed for the directory chooser
     * @param parent the parent of the field editor's control
     */
    public AddsetAttributeField(String name, String labelText,
            String prompt, Composite parent) {
    //    System.out.println("Inside Addset Attribute");
        init(name, labelText);
        this.prompt = prompt;
        createControl(parent);
    }

    final static String SEPARATOR = "\n";
    
    /* (non-Javadoc)
     * Method declared on ListAttributeField.
     * Creates a single string from the given array by separating each
     * string with the appropriate OS-specific path separator.
     */
    protected String createList(String[] items) {
        StringBuffer path = new StringBuffer("");//$NON-NLS-1$

        for (int i = 0; i < items.length; i++) {
            path.append(items[i]);
            path.append(SEPARATOR);
        }
        return path.toString();
    }

    /* (non-Javadoc)
     * Method declared on ListAttributeField.
     * Creates a new path element by means of a directory dialog.
     */
    protected String getNewInputObject() {
        if( prompt == null ){
            prompt = "Please enter:";
        }
        InputDialog dialog = new InputDialog( getShell(), "New "+getLabelText(), prompt, "", new IInputValidator(){
            public String isValid( String newText ) {
                if( newText == null || newText.length() == 0 ){
                    return "Action is required";
                }
                return null;
            }
        });
        
        int sucess = dialog.open();
        if( sucess == InputDialog.CANCEL ){
            return null; // we may have to produce a default value here
        }
        return dialog.getValue();
    }

    /* (non-Javadoc)
     * Method declared on ListAttributeField.
     */
    protected String[] parseString(String stringList) {
        if( stringList == null || stringList.length() == 0 ){
            return new String[0];
        }
        String split[] = stringList.split(SEPARATOR);
        return split;
    }
}
