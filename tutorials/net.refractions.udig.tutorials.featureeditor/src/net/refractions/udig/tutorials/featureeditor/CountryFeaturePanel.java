package net.refractions.udig.tutorials.featureeditor;

import java.io.IOException;
import java.util.Properties;

import net.miginfocom.swt.MigLayout;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.IEditManager;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.CompositeCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.ui.IFeaturePanel;
import net.refractions.udig.project.ui.IFeatureSite;
import net.refractions.udig.project.ui.feature.EditFeature;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

public class CountryFeaturePanel extends IFeaturePanel {
    private static final String DIRTY = "DIRTY";

    /** Attribute name for attribute GMI_CNTRY */
    public final static String GMI_CNTRY = "GMI_CNTRY";

    /** Attribute name for attribute REGION */
    public final static String COLOR_MAP = "COLOR_MAP";

    /** Attribute name for attribute NAME */
    public final static String NAME = "CNTRY_NAME";

    public final static Object[] COLOR_MAP_OPTS = new Object[]{1, 2, 3, 4, 5, 6, 7, 8};

    Text gmiCntry;
    Text name;
    ComboViewer colorMap;

    @Override
    public void aboutToBeShown() {
        listen(true);
    }

    @Override
    public void aboutToBeHidden() {
        listen(false);
    }

    private void listen( boolean listen ) {
        if (listen) {
            gmiCntry.addKeyListener(keyListener);
            name.addKeyListener(keyListener);
            colorMap.addSelectionChangedListener(selectionListener);
        } else {
            gmiCntry.removeKeyListener(keyListener);
            name.removeKeyListener(keyListener);
            colorMap.removeSelectionChangedListener(selectionListener);
        }
    }
    /**
     * Listen to the selection change.
     * <p>
     * It is poliet to keep your listeners internal and not pollute your class definition with extra
     * interfaces that have nothing to do with your interaction with the outside world (and are
     * basically an internal detail).
     */
    private ISelectionChangedListener selectionListener = new ISelectionChangedListener(){
        public void selectionChanged( SelectionChangedEvent event ) {
            Viewer viewer = (Viewer) event.getSource();
            viewer.setData(DIRTY, Boolean.TRUE);
            persistChanges();
        }
    };
    /**
     * List to the fields change as keys are pressed.
     */
    private KeyListener keyListener = new KeyListener(){
        public void keyPressed( KeyEvent e ) {
            // do nothing
        }
        public void keyReleased( KeyEvent e ) {
            Widget widget = (Widget) e.getSource();
            widget.setData(DIRTY, Boolean.TRUE); // dirty!
        }
    };

    private FocusListener focusListener = new FocusListener(){
        public void focusLost( FocusEvent e ) {
            persistChanges();
        }
        public void focusGained( FocusEvent e ) {
        }
    };

    /**
     * Step 0 - Default constructor.
     */
    public CountryFeaturePanel() {
    }

    protected void persistChanges() {
        IFeatureSite site = getSite();
        if( site == null ){
            return;
        }
        EditFeature feature = site.getEditFeature();
        
        if (name != null && !name.isDisposed() && Boolean.TRUE.equals(name.getData(DIRTY))) {
            feature.setAttribute(this.NAME, name.getText());
        }
        if (gmiCntry != null && !gmiCntry.isDisposed()
                && Boolean.TRUE.equals(gmiCntry.getData(DIRTY))) {
            feature.setAttribute(this.GMI_CNTRY, gmiCntry.getText());
        }
        if (colorMap != null && !colorMap.getCombo().isDisposed()
                && Boolean.TRUE.equals(colorMap.getData(DIRTY))) {
            IStructuredSelection selection = (IStructuredSelection) colorMap.getSelection();
            Integer color = (Integer) selection.getFirstElement();
            feature.setAttribute(COLOR_MAP, color.toString());
        }
    }

    /**
     * Step 1 - init using the editor site and memento holding any information from last time
     */
    @Override
    public void init( IFeatureSite site, IMemento memento ) throws PartInitException {
        super.init(site, memento);
    }

    @Override
    public void createPartControl( Composite parent ) {
        parent.setLayout(new MigLayout("", "[right]10[left, grow][min!][min!]", "30"));

        // SWT Widgets
        Label label = new Label(parent, SWT.NONE);
        label.setText("Country:");

        name = new Text(parent, SWT.SHADOW_IN | SWT.BORDER);
        name.setLayoutData("span 3, growx, wrap");
        name.addKeyListener(keyListener);
        name.addFocusListener(focusListener);

        label = new Label(parent, SWT.SHADOW_IN);
        label.setText("Code:");

        gmiCntry = new Text(parent, SWT.SHADOW_IN | SWT.BORDER);
        gmiCntry.setLayoutData("span 3, growx, wrap");
        gmiCntry.addKeyListener(keyListener);
        gmiCntry.addFocusListener(focusListener);

        // JFace Viewer
        label = new Label(parent, SWT.SHADOW_IN);
        label.setText("Color Map:");

        colorMap = new ComboViewer(parent, SWT.SHADOW_IN);
        colorMap.getControl().setLayoutData("wrap");
        colorMap.addSelectionChangedListener(selectionListener);

        // hook up to data
        colorMap.setContentProvider(ArrayContentProvider.getInstance());
        colorMap.setLabelProvider(new LabelProvider(){
            public String getText( Object element ) {
                return " " + element + " color";
            }
        });
        colorMap.setInput(COLOR_MAP_OPTS);
    }
    
    @Override
    public void refresh() {
        SimpleFeature feature = null;
        IFeatureSite site = getSite();        
        if( site != null ){
            IEditManager editManager = site.getEditManager();
            if( editManager != null ){
                feature = editManager.getEditFeature();
            }
        }
        
        listen( false );
        String nameText = "";
        if( feature != null ){
            nameText = (String) feature.getAttribute(NAME);
            if (nameText == null)
                nameText = "";
            name.setText(nameText);
            name.setEnabled(true);
    
            String gmiText = (String) feature.getAttribute(GMI_CNTRY);
            if (gmiText == null)
                gmiText = "";
            gmiCntry.setText(gmiText);
            gmiCntry.setEnabled(true);
            
            String colorText = (String) feature.getAttribute(COLOR_MAP);
            if (colorText != null) {
                StructuredSelection selection = new StructuredSelection(new Integer(colorText));
                colorMap.setSelection(selection);
            } else {
                colorMap.setSelection(new StructuredSelection());
            }
            listen(true);
        }
        else {
            name.setText("--");
            name.setEnabled(false);
            gmiCntry.setText("--");
            gmiCntry.setEnabled(false);
            colorMap.setSelection(new StructuredSelection());
            colorMap.getControl().setEnabled(false);            
        }
    }

    @Override
    public String getDescription() {
        return "Details on the selected country.";
    }

    @Override
    public String getName() {
        return "Country";
    }

    @Override
    public String getTitle() {
        return "Country Details";
    }

}
