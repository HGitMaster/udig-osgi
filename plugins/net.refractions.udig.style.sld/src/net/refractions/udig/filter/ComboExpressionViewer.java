package net.refractions.udig.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Text;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

/**
 * Use a Combo to edit CQL Expressions; the options in the Combo can be any Expression.
 * <p>
 * Internally we have a JFace ComboViewer tricked out to provide nice representations
 * of various options (Expression.NIL represented as "(None)" for example).
 * <p>
 * We have helper methods to seed the available options from common cases such
 * as numbers (for size) or colors, or a FeatureType to get all the property names
 * listed.
 * <p>
 * Remember that although Viewers are a wrapper around some SWT Control or Composite you still
 * have direct access using the getControl() method so that you can do your layout data thing.
 * </p>
 * <p>
 * Future directions from Mark:
 * <ul>
 * <li>
 * @author jive
 * @since 1.1.0
 */
public class ComboExpressionViewer extends Viewer {
    /**
     * This is the expression we are working on here.
     * <p>
     * We are never going to be "null"; Expression.NIL is used to indicate
     * an intentionally empty expression.
     */
    protected Expression expr = Expression.NIL;
    
    /** Internal ComboViewer */
    protected ComboViewer viewer;
    
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    
    private ISelectionChangedListener listener = new ISelectionChangedListener(){        
        public void selectionChanged( SelectionChangedEvent event ) {
            changed( event );
        }
    };
    private ControlDecoration feedback;
    
    public ComboExpressionViewer( Composite parent ){
        this( parent, SWT.SINGLE );
    }
    
    /**
     * Creates an ComboExpressionViewer using the provided style.
     * <p>
     * Creates a combo viewer on a newly-created combo control under the given parent.
     * The combo control is created using the given SWT style bits.
     * @param parent
     * @param style
     */
    public ComboExpressionViewer( Composite parent, int style ) {
        viewer = new ComboViewer( parent, style );
        viewer.addPostSelectionChangedListener( listener );
        viewer.setContentProvider( new IStructuredContentProvider(){            
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
                // we are not really listening to our input here ...
            }            
            public void dispose() {
            }
            public Object[] getElements( Object inputElement ) {
                if(inputElement instanceof List) {
                    return ((List)inputElement).toArray();
                }
                return new Object[0];
            }
        });
        viewer.setLabelProvider( new LabelProvider(){
            public String getText( Object element ) {
                if( element instanceof Expression){
                    Expression expression = (Expression) element;
                    return CQL.toCQL( expression );
                }
                return super.getText(element); // ie use toString()
            }
        });
    }
    
    public void changed( SelectionChangedEvent event ){
        // we can try and parse this puppy; and issue a selection changed
        // event when we actually have an expression that works
        ISelection selection = viewer.getSelection();
        if( selection.isEmpty() ) {
            expr = Expression.NIL;
            return;
        }
        if( selection instanceof IStructuredSelection){
            IStructuredSelection selection2 = (IStructuredSelection) selection;
            Object obj = selection2.getFirstElement();
            if( obj instanceof Expression){
                expr = (Expression) obj;
                feedback();
                return;
            }
            if( obj instanceof String ){
                String cql = (String) obj;
                try {
                    expr = CQL.toExpression( cql );
                    feedback();
                    return;
                } catch (CQLException e1) {
                    expr = Expression.NIL; // no valid expression right now
                    // set warning on associated feedback label
                    feedback( "Could not understand expression", e1 );
                }
            }
        }
    }    
    public void setOptions( String[] array ){
        List<Expression> options = new ArrayList<Expression>();
        for( String str : array ){
            options.add( ff.literal( str ));
        }
        viewer.setInput( options );
    }
    public void setOptions( int[] array ){
        List<Expression> options = new ArrayList<Expression>();
        for( int number : array ){
            options.add( ff.literal( number ));
        }
        viewer.setInput( options );
    }
    
    public void setOptions( double[] array ){
        List<Expression> options = new ArrayList<Expression>();
        for( double number : array ){
            options.add( ff.literal( number ));
        }
        viewer.setInput( options );
    }
    
    public void setOptions( SimpleFeatureType featureType ){
        List<Expression> options = new ArrayList<Expression>();
        for( AttributeDescriptor attributeDescriptor : featureType.getAttributeDescriptors() ){
            options.add( ff.property( attributeDescriptor.getName().getLocalPart() ));
        }
        viewer.setInput( options );
    }

    public void setOptions( List<Expression> list ){
        List<Expression> options = new ArrayList<Expression>( list );
        viewer.setInput( options );
    }

    /**
     * Provide the feedback that everything is fine.
     * <p>
     * This method will make use of an associated ControlDecoration if available;
     * if not it will make use of a tooltip or something.
     * </p>
     */
    public void feedback(){
        if( feedback != null){
            feedback.hide();
            return;
        }
        Control control = getControl();
        if( control != null && !control.isDisposed() ){
            control.setToolTipText("");
        }
    }
    /**
     * Provide the feedback that everything is fine.
     * <p>
     * This method will make use of an associated ControlDecoration if available;
     * if not it will make use of a tooltip or something.
     * </p>
     */
    public void feedback( String warning ){
        if( feedback != null ){
            feedback.setDescriptionText( warning );
            feedback.show();
        }
        Control control = getControl();
        if( control != null && !control.isDisposed() ){
            control.setToolTipText( warning );
        }
    }
    /**
     * Provide the feedback that everything is fine.
     * <p>
     * This method will make use of an associated ControlDecoration if available;
     * if not it will make use of a tooltip or something.
     * </p>
     */
    public void feedback( String error, Exception eek ){
        Control control = getControl();
        if( control != null && !control.isDisposed() ){
            control.setToolTipText( error +":"+ eek );
        }
    }
    /**
     * This is the widget used to display the Expression; its parent has been provided
     * in the ExpressionViewer's constructor; but you may need direct access to it
     * in order to set layout data etc.
     *
     * @return
     */
    public Control getControl(){
        return viewer.getControl();
    }
    
    /**
     * Provides access to the Expression being used by this viewer.
     * <p>
     * @return Expression being viewed; may be Expression.NIL if empty (but will not be null)
     */
    @Override
    public Expression getInput() {
        return expr;
    }
    
    @Override
    public ISelection getSelection() {
        IStructuredSelection selection = new StructuredSelection(expr);
        return selection;
    }
    
    @Override
    public void refresh() {
        viewer.setSelection( new StructuredSelection( expr ) );
    }
    
    /**
     * Set the input for this viewer.
     * <p>
     * This viewer accepts several alternative forms of input to get started:
     * <ul>
     * <li>Expression - is used directly
     * <li>String - is parsed by ECQL.toExpression; and if successful it is used
     * </ul>
     * If you have other suggestions (PropertyName could be provided by an AttributeType for example)
     * please ask on the mailing list.
     * @param input Expression or String to use as the input for this viewer
     */
    @Override    
    public void setInput( Object input ) {
        if( input instanceof Expression ){
            expr = (Expression) input;
        }
        else if (input instanceof String){
            String txt = (String) input;
            try {
                expr = ECQL.toExpression( txt );
            } catch (CQLException e) {
            }
        }
    }
    
    @Override
    public void setSelection( ISelection selection, boolean reveal ) {
        // do nothing by default
    }
}
