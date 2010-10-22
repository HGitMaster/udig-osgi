package net.refractions.udig.feature.panel;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;


public class TabLabelProvider extends LabelProvider {
    ILabelProvider delegate;
    
    public TabLabelProvider(){
        this(null);
    }
    public TabLabelProvider( ILabelProvider delegate ){
        this.delegate = delegate;
    }
    
    @Override
    public String getText( Object element ) {
        if( element instanceof StructuredSelection ){
            StructuredSelection sel = (StructuredSelection) element;
            if (sel.isEmpty()){
                return "Please select a feature";
            }
            element = sel.getFirstElement();
        }
        if( element == null ){
            return null;
        }
        if( element instanceof SimpleFeature ){
            SimpleFeature feature = (SimpleFeature) element;
            if( delegate != null ){
                String text = delegate.getText( feature );
                if( text != null ){
                    return text;
                }
            }
            return feature.getID();
        }
        if( element instanceof FeaturePanelTabDescriptor ){
            FeaturePanelTabDescriptor tabDescriptor = (FeaturePanelTabDescriptor) element;
            String title = tabDescriptor.getEntry().getTitle();
            if( title == null || title.length() == 0 ){
                title = tabDescriptor.getLabel();
            }
            return title;
        }
        else {
            String text = element.toString();
            return text;
        }
    }
}
