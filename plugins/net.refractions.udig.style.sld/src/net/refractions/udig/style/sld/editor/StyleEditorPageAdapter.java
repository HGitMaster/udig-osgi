package net.refractions.udig.style.sld.editor;

import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.style.IStyleConfigurator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Adapter used to place a StyleConfigurator into the StyleEditor.
 */
public class StyleEditorPageAdapter extends StyleEditorPage {
    IStyleConfigurator configurator;

    public static final String XPID = "net.refractions.udig.style.styleConfigurator"; //$NON-NLS-1$
    
    public StyleEditorPageAdapter(IStyleConfigurator configurator) {
        this.configurator = configurator;
    }
    
    @Override
    public void createPageContent( Composite parent ) {
        Composite configHolder = new Composite(parent, SWT.NONE);
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.pack = false;
        layout.wrap = true;
        layout.type = SWT.HORIZONTAL;
        layout.fill = true;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.spacing = 0;
        configHolder.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 400;
        configHolder.setLayoutData(gd);

        configurator.createControl(configHolder);
        
        Layer selectedLayer = getContainer().getSelectedLayer();
        if (configurator.canStyle(selectedLayer)) {
            
            configurator.setAction(getContainer().getApplyAction());
            configurator.focus(selectedLayer);
        }
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public boolean performCancel() {
        return false;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    public void styleChanged(Object source ){
        //do nothing
    }
    
    public void gotFocus() {
        refresh();
    };

    public boolean okToLeave() {
        return true;
    }

    public boolean performOk() {
        return false;
    }

    public boolean performApply() {
        configurator.preApply();
        return true;
    }

    public void refresh() {
        configurator.setAction(getContainer().getApplyAction());
        configurator.focus(getSelectedLayer());
    }

}
