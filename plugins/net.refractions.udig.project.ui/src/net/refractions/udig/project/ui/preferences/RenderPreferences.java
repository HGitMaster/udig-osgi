package net.refractions.udig.project.ui.preferences;

import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.preferences.PreferenceConstants;
import net.refractions.udig.project.ui.internal.Messages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class RenderPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public RenderPreferences() {
		super(GRID);
		setPreferenceStore(ProjectPlugin.getPlugin().getPreferenceStore());
		setDescription(Messages.RenderPreferences_pageDescription); 
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.P_ANTI_ALIASING,
				Messages.RenderPreferences_antialiasing, 
				getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_TRANSPARENCY,
                Messages.RenderPreferences_transparencies, 
                getFieldEditorParent()));
        addField( new BooleanFieldEditor(net.refractions.udig.project.preferences.PreferenceConstants.P_SHOW_ANIMATIONS, 
                Messages.RenderPreferences_animations,
                getFieldEditorParent()));
        addField( new BooleanFieldEditor(net.refractions.udig.project.preferences.PreferenceConstants.P_IGNORE_LABELS_OVERLAPPING, 
                Messages.RenderPreferences_labelOverlappings,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(net.refractions.udig.project.preferences.PreferenceConstants.P_TILED_RENDERING, 
                Messages.RenderPreferences_tiledRendering,
                getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}