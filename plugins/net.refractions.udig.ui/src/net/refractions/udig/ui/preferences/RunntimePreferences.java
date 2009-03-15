package net.refractions.udig.ui.preferences;

import net.refractions.udig.internal.ui.UiPlugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RunntimePreferences extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage {

    public RunntimePreferences() {
        super(GRID);
        setPreferenceStore(UiPlugin.getDefault().getPreferenceStore());
        setDescription("Gives the possibility to set some of the runtime preferences.");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    public void createFieldEditors() {
        addField(new RunntimeFieldEditor("RUNNTIMEPREFERENCES", "Runntime preferences",
                getFieldEditorParent()));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init( IWorkbench workbench ) {
    }

}