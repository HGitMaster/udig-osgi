package net.refractions.udig.style.sld.editor;

import net.refractions.udig.filter.ComboExpressionViewer;
import net.refractions.udig.filter.ExpressionViewer;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.forms.widgets.Twistie;
import org.eclipse.ui.part.Page;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.filter.expression.Expression;

/**
 * 
 * This class handles the specifics of a 
 * <p>
 *
 * </p>
 * @author mleslie
 * @since 1.1.0
 */
public class MarkEditorPage extends Page {
    private static FilterFactoryImpl factory = new FilterFactoryImpl();
    private static Expression[] WELL_KNOWN_NAMES = {
            factory.createLiteralExpression("square"), //$NON-NLS-1$
            factory.createLiteralExpression("circle"), //$NON-NLS-1$
            factory.createLiteralExpression("triangle"), //$NON-NLS-1$
            factory.createLiteralExpression("star"), //$NON-NLS-1$
            factory.createLiteralExpression("cross"), //$NON-NLS-1$
            factory.createLiteralExpression("X") }; //$NON-NLS-1$
    
    Composite markComposite;
    private Label titleLabel;
    private ComboExpressionViewer markerTypeCombo;
    private ControlDecoration markerTypeDecoration;
    private FillEditorComponent fillComponent;
    private StrokeEditorComponent strokeComponent;
    
    /**
     * This is fairly straight forward, as we only really accept the name of a marker and
     * then defer two sections to Fill and Stroke Editors.  It will not set layout data on the
     * returned composite, leaving that for the calling method.
     *
     * @param parent
     * @return
     */
    public void createControl( Composite parent ) {
        markComposite = new Composite(parent, SWT.NONE);
        markComposite.setLayout(new GridLayout(2, false));
        titleLabel = new Label(markComposite, SWT.NONE);
        titleLabel.setText("Mark");
        titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));
        
        Label label = new Label(markComposite, SWT.NONE);
        label.setText("Marker Type");
        
        markerTypeCombo = new ComboExpressionViewer(markComposite, SWT.SINGLE);
        markerTypeCombo.getControl().setLayoutData(new TableWrapData(TableWrapData.LEFT));
        markerTypeDecoration = new ControlDecoration(label, SWT.LEFT | SWT.TOP);
        markerTypeDecoration.hide();
        markerTypeCombo.getControl().setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
        
        Composite additionalComposite = new Composite(markComposite, SWT.NONE);
        additionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        additionalComposite.setLayout(new TableWrapLayout());
        
        Composite fillComp = new Composite(additionalComposite, SWT.NONE);
        fillComp.setLayoutData(new TableWrapData(TableWrapData.FILL));
        fillComponent = new FillEditorComponent();
        fillComponent.createControl(fillComp);
        
        Composite strokeComp = new Composite(additionalComposite, SWT.NONE);
        strokeComp.setLayoutData(new TableWrapData(TableWrapData.FILL));
        strokeComponent = new StrokeEditorComponent();
        strokeComponent.createControl(strokeComp);
        
    }

    @Override
    public Control getControl() {
        return markComposite;
    }

    @Override
    public void setFocus() {
    }
    
}
