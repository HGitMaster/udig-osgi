package net.refractions.udig.project.ui.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.core.internal.ExtensionPointProcessor;
import net.refractions.udig.core.internal.ExtensionPointUtil;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.ui.IFeatureSite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Utility class assisting in processing the feature panel extension point.
 * <p>
 * This class simply processes the extension point; and use used by FeatureView and FeatureWizard
 * who are responsible for actually keeping a hold of the resulting FeaturePanel instances.
 * <p>
 * Internally this class supports lazy creation of actual feature panels.
 * 
 * @author Jody Garnett
 * @since 1.2.0
 */
public class FeaturePanelProcessor {

    private static final String FEATURE_PANEL_ID = "net.refractions.udig.project.ui.featurePanel"; //$NON-NLS-1$
    private static List<FeaturePanelEntry> featurePanelList;

    public FeaturePanelProcessor() {
        featurePanelList = new ArrayList<FeaturePanelEntry>();
        ExtensionPointUtil.process(ProjectPlugin.getPlugin(), FEATURE_PANEL_ID,
                new ExtensionPointProcessor(){
                    public void process( IExtension extension, IConfigurationElement element )
                            throws Exception {
                        FeaturePanelEntry entry = new FeaturePanelEntry(extension, element);
                        featurePanelList.add(entry);
                    }
                });
    }
    /**
     * List of FeaturePanelEntry that can match the provided element (usually a SimpleFeature).
     * <p>
     * You can use these FeaturePanelEntry to make feature panels to edit the indicated element.
     * 
     * @param element
     * @return List matching FeaturePanelEntry
     */
    public List<FeaturePanelEntry> search( Object element ) {
        List<FeaturePanelEntry> search = new ArrayList<FeaturePanelEntry>();
        for( FeaturePanelEntry entry : featurePanelList ) {
            if (entry.isMatch(element)) {
                search.add(entry);
            }
        }
        return search;
    }
    public List<FeaturePanelEntry> check( IFeatureSite site ) {
        List<FeaturePanelEntry> search = new ArrayList<FeaturePanelEntry>();
        for( FeaturePanelEntry entry : featurePanelList ) {
            if (entry.isChecked(site)) {
                search.add(entry);
            }
        }
        return search;
    }

    /**
     * Used to access all known FeaturePanelEntry.
     * 
     * @return List of all known FeaturePanelEntry
     */
    public List<FeaturePanelEntry> entries() {
        return Collections.unmodifiableList(featurePanelList);
    }
}
