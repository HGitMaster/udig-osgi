package net.refractions.udig.browser;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Describes an a secondary page of the import wizard
 * <p>
 *
 * </p>
 * @author mleslie
 * @since 1.0.0
 */
public class ExternalCatalogueImportPageDescriptor 
        implements ExternalCatalogueImportDescriptor {
    IConfigurationElement element;
    
    /**
     * @param element
     */
    public ExternalCatalogueImportPageDescriptor(IConfigurationElement element) {
        this.element = element;
    }
    
    /**
     *
     * @return selected import page
     * @throws CoreException
     */
    public ExternalCatalogueImportPage createImportPage() throws CoreException {
        IConfigurationElement[] childs = element.getChildren("externalCataloguePage"); //$NON-NLS-1$
        ExternalCatalogueImportPage page = 
            (ExternalCatalogueImportPage)childs[0].createExecutableExtension("class"); //$NON-NLS-1$
        
        page.setTitle(getLabel());
        page.setDescription(getDescription());
        page.setImageDescriptor(getDescriptionImage());
        page.setListener(getListener());
        page.setViewName(getViewName());
        return page;
    }

    public String getLabel() {
        if(element.getAttribute("name") == null) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        return element.getAttribute("name"); //$NON-NLS-1$
    }
    
    public String getViewName() {
        if(element.getAttribute("viewName") == null) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        return element.getAttribute("viewName"); //$NON-NLS-1$
    }
    
    public String getDescription() {
        String desc = element.getAttribute("description"); //$NON-NLS-1$
        if (desc == null)
            return ""; //$NON-NLS-1$
        
        return desc.trim();
    }
    
    public String getID() {
        return element.getAttribute("id"); //$NON-NLS-1$
    }
    
    /**
     *
     * @return descriptor of the banner image
     */
    public ImageDescriptor getImage() {
        String ns = element.getNamespace();
        String banner = element.getAttribute("image"); //$NON-NLS-1$
        
        if (banner == null)
            return null;
        
        return AbstractUIPlugin.imageDescriptorFromPlugin(ns,banner);
    }
    
    public ImageDescriptor getIcon() {
        String ns = element.getNamespace();
        String banner = element.getAttribute("icon"); //$NON-NLS-1$
        
        if (banner == null)
            return null;
        
        return AbstractUIPlugin.imageDescriptorFromPlugin(ns,banner);
    }
    
    public ImageDescriptor getDescriptionImage() {
        String ns = element.getNamespace();
        String banner = element.getAttribute("banner"); //$NON-NLS-1$
        
        if (banner == null)
            return null;
        
        return AbstractUIPlugin.imageDescriptorFromPlugin(ns,banner);
    }
    
    /**
     *
     * @return Service type
     */
    public String getServiceType() {
        return element.getAttribute("type"); //$NON-NLS-1$
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) 
            return true;
        
        if (obj instanceof ExternalCatalogueImportPageDescriptor) {
            ExternalCatalogueImportPageDescriptor descriptor = 
                (ExternalCatalogueImportPageDescriptor)obj;
            
            return getID() != null && 
                getID().equals(descriptor.getID());
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        if (getID() == null) 
            return "".hashCode(); //$NON-NLS-1$
        return getID().hashCode();
    }

    public LocationListener getListener() {
        LocationListener blah = null;
        try {
            blah = (LocationListener)element.createExecutableExtension("listener"); //$NON-NLS-1$
        } catch (CoreException e) {
            //
        }
        return blah;
    }
}
