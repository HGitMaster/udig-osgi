package net.refractions.udig.project.ui.preferences;

import net.refractions.udig.project.ui.internal.MapEditor;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = ProjectUIPlugin.getDefault().getPreferenceStore();
        
        store.setDefault(PreferenceConstants.P_OPEN_MAPS_ON_STARTUP, true);        
        store.setDefault(MapEditor.ID, 0);
        store.setDefault("Test", 0);
    }

}
