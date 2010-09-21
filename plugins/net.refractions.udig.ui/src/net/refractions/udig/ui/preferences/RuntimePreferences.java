package net.refractions.udig.ui.preferences;

import net.refractions.udig.internal.ui.UiPlugin;
import net.refractions.udig.ui.internal.Messages;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RuntimePreferences extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage {

    private RuntimeFieldEditor fieldEditor;

    public RuntimePreferences() {
        super(GRID);
        setPreferenceStore(UiPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.RuntimePreferences_desc);
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    public void createFieldEditors() {
        fieldEditor = new RuntimeFieldEditor("RUNTIMEPREFERENCES", "Runtime preferences",
                getFieldEditorParent());
        addField(fieldEditor);

    }
    
    @Override
    protected void performApply() {
        super.performApply();
        apply();
    }
    
    public boolean performOk() {
        apply();
        return super.performOk();
    }
    
    private void apply() {
        fieldEditor.doStore();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init( IWorkbench workbench ) {
    }

}