/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This
 * library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.project.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.internal.ui.IDropTargetProvider;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IProject;
import net.refractions.udig.project.IProjectElement;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.Project;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.internal.ProjectRegistry;
import net.refractions.udig.project.internal.provider.LoadingPlaceHolder;
import net.refractions.udig.project.ui.AdapterFactoryLabelProviderDecorator;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.internal.UDIGAdapterFactoryContentProvider.InputChangedListener;
import net.refractions.udig.project.ui.internal.actions.OpenProject;
import net.refractions.udig.project.ui.tool.IToolManager;
import net.refractions.udig.ui.UDIGDragDropUtilities;
import net.refractions.udig.ui.ZoomingDialog;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * Displays the Projects and their contents
 * 
 * @author jeichar
 * @since 0.3
 */
public class ProjectExplorer extends ViewPart implements IMenuListener, ISetSelectionTarget, IDropTargetProvider{

    /** The Extension ID of ProjectExplorer */
    public static final String ID = "net.refractions.udig.project.ui.projectExplorer"; //$NON-NLS-1$

    public static final String PROJECT_EXPLORER_LINKED = "PROJECT_EXPLORER_LINKED"; //$NON-NLS-1$

    ProjectRegistry projectRegistry;

    private Composite container;

    TreeViewer treeViewer;

    private PropertySheetPage propertySheetPage;

    private static ProjectExplorer explorer = new ProjectExplorer();

    private IAction propertiesAction;

    private Action openAction;

    private Action openProjectAction;

    private UDIGAdapterFactoryContentProvider contentProvider;

	private IAction deleteAction;

    private Action linkAction;
	
    /**
     * Construct <code>ProjectExplorer</code>.
     */
    public ProjectExplorer() {
        super();
        CatalogPlugin.getDefault();
        explorer = this;

        projectRegistry = ProjectPlugin.getPlugin().getProjectRegistry();

    }

    /**
     * returns the list of all the selected objects of the given class.
     * 
     * @param clazz The object types to add to the list.
     * @return the list of all the selected objects of the given class
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getSelected( Class<T> clazz ) {
        StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
        List<T> selected = new ArrayList<T>();
        for( Iterator<T> iter = selection.iterator(); iter.hasNext(); ) {
            T obj = iter.next();
            if (clazz != null && clazz.isAssignableFrom(obj.getClass()))
                selected.add(obj);
        }
        return selected;
    }

    /**
     * TODO summary sentence for init ...
     * 
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     * @param site
     * @throws org.eclipse.ui.PartInitException
     */
    public void init( IViewSite site ) throws PartInitException {
        super.init(site);

    }

    /**
     * This creates a context menu for the viewer and adds a listener as well registering the menu
     * for extension. <!--
     * 
     * @param viewer The viewer to create a contect menu for.
     */
    protected void createContextMenuFor( StructuredViewer viewer ) {
        MenuManager contextMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
        contextMenu.setRemoveAllWhenShown(true);
        contextMenu.addMenuListener(this);
        Menu menu = contextMenu.createContextMenu(viewer.getControl());
        
        viewer.getControl().setMenu(menu);
        
        getSite().registerContextMenu(contextMenu, viewer);
    }

    /**
     * Returns the composite object that is the UI of the ProjectExplorer.
     * 
     * @return the composite object that is the UI of the ProjectExplorer.
     */
    public Composite getContainer() {
        return container;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     * @param parent
     */
    public void createPartControl( Composite parent ) {

        container = parent;

        Tree tree = new Tree(getContainer(), SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

        treeViewer = new TreeViewer(tree);

        UDIGDragDropUtilities.addDragDropSupport(treeViewer, this, true, true);

        treeViewer.setAutoExpandLevel(1);
        contentProvider = new UDIGAdapterFactoryContentProvider(getAdapterFactory());
        treeViewer.setContentProvider(contentProvider);

        treeViewer.setLabelProvider(new AdapterFactoryLabelProviderDecorator(getAdapterFactory(),
                treeViewer));
        treeViewer.setInput(projectRegistry);
        // ensure our site knows our selection provider
        // so that global actions can hook into the selection
        //
        getSite().setSelectionProvider(treeViewer);
        
        setTreeSorter();
        createContextMenuFor(treeViewer);
        addMenuActions();
        addToobarActions();
        addSelectionListener();
        addDoubleCickListener();
        setGlobalActions();
    }

    private void setTreeSorter() {
        treeViewer.setSorter(new ViewerSorter(){
            ViewerLayerSorter layerSorter=new ViewerLayerSorter();
            /**
             * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
             *      java.lang.Object, java.lang.Object)
             */
            public int compare( Viewer viewer, Object e1, Object e2 ) {
                if (e1 instanceof Layer && e2 instanceof Layer) {
                    return layerSorter.compare(viewer, e1, e2);
                }
                if( e1 instanceof LoadingPlaceHolder)
                    return 1;
                if( e2 instanceof LoadingPlaceHolder )
                    return -1;
                return super.compare(viewer, e1, e2);
            }
        });
    }

    private void addDoubleCickListener() {
        treeViewer.addDoubleClickListener(new IDoubleClickListener(){

            public void doubleClick( DoubleClickEvent event ) {
                if (PlatformUI.getWorkbench().isClosing())
                    return;

                final Object obj = ((IStructuredSelection) treeViewer.getSelection())
                        .getFirstElement();
                if (!(obj instanceof IProjectElement)) {
                    Display.getDefault().asyncExec(new Runnable(){

                        public void run() {
                            if( obj!=null) 
                                treeViewer.setExpandedState(obj, !treeViewer.getExpandedState(obj));
                        }
                    });
                    return;
                }

                open((IProjectElement) obj, false);
            }

        });
    }

    private void addSelectionListener() {
        final IPreferenceStore preferenceStore = ProjectUIPlugin.getDefault().getPreferenceStore();
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged( SelectionChangedEvent event ) {
                if (event.getSelection().isEmpty())
                    return;
                final Object obj = ((IStructuredSelection) treeViewer.getSelection())
                        .getFirstElement();
                if (obj instanceof IProject)
                    projectRegistry.setCurrentProject((Project) obj);
                if (obj instanceof ILayer)
                    getViewSite().getActionBars().getStatusLineManager().setMessage(
                            ((ILayer) obj).getStatusMessage());
                if( preferenceStore.getBoolean(PROJECT_EXPLORER_LINKED) ){
                    if( obj instanceof IProjectElement ){
                        IWorkbenchPage page = getSite().getPage();
                        IEditorPart part = page.findEditor(ApplicationGIS.getInput((IProjectElement) obj));
                        page.bringToTop(part);
                    }
                }
            }

        });
    }

    private void setGlobalActions() {
        IToolManager toolManager=ApplicationGIS.getToolManager();
        IActionBars actionBars = getViewSite().getActionBars();
        toolManager.contributeGlobalActions(this, actionBars);
        toolManager.registerActionsWithPart(this);
        actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), getDeleteAction(actionBars));

    }

	private IAction getDeleteAction(IActionBars actionBars) {
		if( deleteAction==null ){
            deleteAction=ApplicationGIS.getToolManager().getDELETEAction();
		}
		return deleteAction;
	}

    /**
     * @return a ComposeableAdapterFactory for all the Udig's EMF objects.
     */
    public AdapterFactory getAdapterFactory() {
        return ProjectUIPlugin.getDefault().getAdapterFactory();
    }

    private void addToobarActions() {
        
        getActionBars().getToolBarManager().add(createLinkAction());
        getActionBars().getToolBarManager().add(
                new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private IAction createLinkAction() {
        final IPreferenceStore preferenceStore = ProjectUIPlugin.getDefault().getPreferenceStore();
        linkAction=new Action(Messages.ProjectExplorer_link_name, SWT.CHECK){ 
        	
        	@Override
			public void runWithEvent(Event event) {
                boolean linked = isChecked();
                preferenceStore.setValue(PROJECT_EXPLORER_LINKED, linked);
                if( linked ){
                    addEditorListener();
                }else{
                    removeEditorListener();
                }
			}
        };
        boolean linked = preferenceStore.getBoolean(PROJECT_EXPLORER_LINKED);
        linkAction.setChecked(linked);
        linkAction.setImageDescriptor(Images.getDescriptor(ImageConstants.LINKED_ENABLED_CO));
        linkAction.setDisabledImageDescriptor(Images.getDescriptor(ImageConstants.LINKED_DISABLED_CO));
        linkAction.setToolTipText(Messages.ProjectExplorer_link_tooltip); 
        if( linked )
            addEditorListener();
        return linkAction;
    }
    
    IPartListener2 editorListener=new IPartListener2(){

        public void partActivated( IWorkbenchPartReference partRef ) {
            if ( isLinkedWithEditor() && partRef.getPart(false) instanceof MapEditor ){
                MapPart editor=(MapPart) partRef.getPart(false);
                setSelection(Collections.singleton(editor.getMap()), true);
            }
        }

        public void partBroughtToTop( IWorkbenchPartReference partRef ) {
        }

        public void partClosed( IWorkbenchPartReference partRef ) {
        }

        public void partDeactivated( IWorkbenchPartReference partRef ) {
        }

        public void partHidden( IWorkbenchPartReference partRef ) {
        }

        public void partInputChanged( IWorkbenchPartReference partRef ) {
        }

        public void partOpened( IWorkbenchPartReference partRef ) {
        }

        public void partVisible( IWorkbenchPartReference partRef ) {
        }
        
    };
    
    protected void removeEditorListener() {
        getSite().getPage().removePartListener(editorListener);
    }

    protected void addEditorListener() {
        getSite().getPage().addPartListener(editorListener);
    }

    public boolean isLinkedWithEditor(){
        return linkAction.isChecked();
    }

    private void addMenuActions() {
        getActionBars().getMenuManager().add(getOpenProjectAction());
        getActionBars().getMenuManager().add(
                new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        getSite().setSelectionProvider(treeViewer);
    }

    /**
     * This is how the framework determines which interfaces we implement. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @param key The desired class
     * @return An object of type key or null;
     */
    public Object getAdapter( Class key ) {
        if (key.equals(IPropertySheetPage.class))
            return getPropertySheetPage();
        return super.getAdapter(key);
    }

    /**
     * This accesses a cached version of the property sheet. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @return An IProperty page for the selected object
     */
    public IPropertySheetPage getPropertySheetPage() {
        if (propertySheetPage == null) {
            propertySheetPage = new PropertySheetPage(){
                public void makeContributions( IMenuManager menuManager,
                        IToolBarManager toolBarManager, IStatusLineManager statusLineManager ) {
                    super.makeContributions(menuManager, toolBarManager, statusLineManager);
                }

                public void setActionBars( IActionBars actionBars ) {
                    super.setActionBars(actionBars);
                }
            };
            propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProvider(
                    getAdapterFactory()));
        }

        return propertySheetPage;
    }

    /**
     * Gets the site's action bars
     * 
     * @return the site's action bars
     */
    public IActionBars getActionBars() {
        return getViewSite().getActionBars();
    }

    /**
     * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public void menuAboutToShow( IMenuManager manager ) {
        boolean addOpenAction = false;
        for( Iterator iter = ((IStructuredSelection) treeViewer.getSelection()).iterator(); iter
                .hasNext(); ) {
            Object obj = iter.next();
            if (obj instanceof IProjectElement) {
                addOpenAction = true;
                break;
            }
        }

        if (addOpenAction) {
            manager.add(getOpenAction());
        }
        manager.add(getOpenProjectAction());
        manager.add(getDeleteAction(getViewSite().getActionBars()));

        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
//        LayerApplicabilityMenuCreator creator = getApplicabilityMenu();
//        if (creator != null)
//            manager.add(creator.getMenuManager());

        manager.add(ApplicationGIS.getToolManager().createOperationsContextMenu(treeViewer.getSelection()));
        manager.add(new Separator());
        manager.add(ActionFactory.EXPORT.create(getSite().getWorkbenchWindow()));
        if (!treeViewer.getSelection().isEmpty()
                && ((IStructuredSelection) treeViewer.getSelection()).getFirstElement() instanceof Map) {
            manager.add(new Separator());
            manager.add(getPropertiesAction());
        }

    }

    private LayerApplicabilityMenuCreator applicabilityCreator;
    private LayerApplicabilityMenuCreator getApplicabilityMenu() {
        if (applicabilityCreator == null) {
            applicabilityCreator = new LayerApplicabilityMenuCreator();
        }

        StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
        for( Iterator iter = selection.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (!(element instanceof Layer))
                return null;
        }

        return applicabilityCreator;
    }
    /**
     * TODO summary sentence for getOpenProjectAction ...
     * 
     * @param manager
     * @return
     */
    private IAction getOpenProjectAction() {
        if (openProjectAction == null) {
            openProjectAction = new Action(){
                OpenProject delegate = new OpenProject();

                /**
                 * @see org.eclipse.jface.action.Action#run()
                 */
                public void run() {
                    if (PlatformUI.getWorkbench().isClosing())
                        return;

                    delegate.run(this);
                }
            };
            openProjectAction.setText(Messages.ProjectExplorer_openProject_text); 
            openProjectAction.setToolTipText(Messages.ProjectExplorer_openProject_tooltip); 
        }
        return openProjectAction;
    }

    private IAction getPropertiesAction() {
        if (propertiesAction == null) {
            final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            // propertiesAction=ActionFactory.PROPERTIES.create(getSite().getWorkbenchWindow());
            // propertiesAction.setEnabled(true);
            
            final PropertyDialogAction tmp = new PropertyDialogAction(
                    new SameShellProvider(shell), 
                    treeViewer);
            
            propertiesAction=new Action(){
                @Override
                public void runWithEvent( Event event ) {
                    ZoomingDialog dialog=new ZoomingDialog(shell, tmp.createDialog(), ZoomingDialog.calculateBounds(treeViewer.getTree().getSelection()[0], -1) );
                    dialog.open();
                }
            };
            
            propertiesAction.setText(tmp.getText());
            propertiesAction.setActionDefinitionId(tmp.getActionDefinitionId());
            propertiesAction.setDescription(tmp.getDescription());
            propertiesAction.setHoverImageDescriptor(tmp.getHoverImageDescriptor());
            propertiesAction.setImageDescriptor(tmp.getImageDescriptor());
            propertiesAction.setToolTipText(tmp.getToolTipText());
            
        }
        getActionBars().setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
        return propertiesAction;
    }

    private Action getOpenAction() {
        if (openAction == null)
            openAction = new Action(){
                /**
                 * @see org.eclipse.jface.action.Action#run()
                 */
                public void run() {
                    for( Iterator iter = ((IStructuredSelection) treeViewer.getSelection())
                            .iterator(); iter.hasNext(); ) {
                        open((IProjectElement) iter.next(), false);
                    }
                }
            };
        openAction.setText(Messages.ProjectExplorer_open_text); 
        return openAction;
    }

    class OpenWithActions extends Action {
        UDIGEditorInputDescriptor input;

        /**
         * Construct <code>ProjectExplorer.OpenWithActions</code>.
         * 
         * @param input the editor input that
         */
        public OpenWithActions( UDIGEditorInputDescriptor input ) {
            this.input = input;
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        @SuppressWarnings("unchecked") 
        public void run() {
            for( Iterator iter = ((IStructuredSelection) treeViewer.getSelection()).iterator(); iter
                    .hasNext(); ) {
                defaultEditorMap.put(input.getType(), this);
                open((IProjectElement) iter.next(), false);
            }
        }
    }

    @SuppressWarnings("unchecked") List getOpenWithActions( Class type ) { 
        List actions = (List) editorInputsMap.get(type);
        if (actions == null) {
            actions = new ArrayList();
            List<UDIGEditorInputDescriptor> inputs = ApplicationGIS.getEditorInputs(type);
            for( Iterator iter = inputs.iterator(); iter.hasNext(); ) {
                UDIGEditorInputDescriptor desc = (UDIGEditorInputDescriptor) iter.next();
                Action openWithAction = new OpenWithActions(desc);
                openWithAction.setText(desc.getName());
                actions.add(openWithAction);
            }
            if (actions != null)
                editorInputsMap.put(type, actions);
        }
        return actions;
    }

    /**
     * Maps between a class and a list of associated UDIGEditorInput Objects
     */
    java.util.Map editorInputsMap = new HashMap();

    /**
     * Maps between a class and the id of the editor input to use (from the list in editorInputsMap)
     */
    java.util.Map defaultEditorMap = new HashMap();


    /**
     * Opens a map or page in the editor.
     *  
     * @param obj the object to open
     */
    public void open( final IProjectElement obj ) {
        open( obj, false);
    }
    /**
     * Opens a map or page in the editor.
     * 
     * @param obj the object to open
     * @param wait indicates whether to block until the maps opens. 
     */
    public void open( final IProjectElement obj, boolean wait ) {
        ApplicationGIS.openProjectElement(obj, wait);
    }

    /**
     * Returns an UDIGEditorInputDescriptor for the provided object.
     * 
     * @return an UDIGEditorInputDescriptor for the provided object.
     */
    public UDIGEditorInputDescriptor getEditorInput( final IProjectElement obj ) {
        List inputs = getOpenWithActions(obj.getClass());
        String defaultEditor = (String) defaultEditorMap.get(obj.getClass());

        OpenWithActions action = null;
        if (defaultEditor == null) {
            action = (OpenWithActions) inputs.get(0);
        } else {
            for( Iterator iter = inputs.iterator(); iter.hasNext(); ) {
                OpenWithActions current = (OpenWithActions) iter.next();
                if (current.input.getEditorID().equals(defaultEditor)) {
                    action = current;
                    break;
                }
            }
        }

        OpenWithActions finalAction = action;
        if( finalAction!=null ){
            final UDIGEditorInputDescriptor input = finalAction.input;
            return input;
        }
        throw new Error("Unable to create a input descriptor for this object.  A plugin may not be installed correctly"); //$NON-NLS-1$
    }

    /*
     * @see org.eclipse.ui.part.ISetSelectionTarget#selectReveal(org.eclipse.jface.viewers.ISelection)
     */
    public void selectReveal( ISelection selection ) {
        treeViewer.setSelection(selection);
    }

    /**
     * Returns the ProjectExplorer view if it has been created, otherwise an object is created and
     * returned.
     * 
     * @return the ProjectExplorer view if it has been created, otherwise an object is created and
     *         returned.
     */
    public static ProjectExplorer getProjectExplorer() {
        return explorer;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        if (PlatformUI.getWorkbench().isClosing()) {
            ProjectPlugin.getPlugin().turnOffEvents();
        }
        treeViewer.getLabelProvider().dispose();
        contentProvider.dispose();
        super.dispose();
    }

    public Object getTarget(DropTargetEvent event) {
        return this;
    }

    public void collapseToLevel( IProject project, int i ) {
        treeViewer.collapseToLevel(project, i);
    }

    /**
     * Selects the project in the Project Explorer
     *
     * @param p sets the selection in the project explorer
     * @param reveal TODO
     */
    public void setSelection(final IProject p, final boolean reveal){
        Display d=Display.getCurrent();
        if( d==null ){
            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                    treeViewer.setSelection(new StructuredSelection(new Object[]{p}), reveal);
                }
            });
        }else
            treeViewer.setSelection(new StructuredSelection(new Object[]{p}), reveal);

    }
    
    /**
     * Selects the element in the tree. This is a non-blocking method and selection may take a while to take effect.
     *
     * @param element elements to select select
     */
    public void setSelection(final Collection<? extends IProjectElement> element, final boolean reveal){
        if ( treeViewer==null )
            return;
        Display d=Display.getCurrent();
        if( d==null ){
            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                        inDisplaySelect(element, reveal);
                }
            });
        }else
            inDisplaySelect(element, reveal);

    }
    AtomicReference<SetSelectionListener> inputChangedListener=
        new AtomicReference<SetSelectionListener>();
    
    private void inDisplaySelect( Collection<? extends IProjectElement> element, boolean reveal) {
        if( treeViewer==null )
            return;
        List<Object> visibleElements = getLoadedElements();
        if( visibleElements.containsAll(element) ){
            treeViewer.setSelection(new StructuredSelection(element.toArray()), reveal);
            return;
        }else{
            synchronized ( contentProvider ){
                SetSelectionListener old = inputChangedListener.getAndSet(new SetSelectionListener(element, reveal));
                contentProvider.removeListener(old);
                contentProvider.addListener(inputChangedListener.get());
            }
            for( IProjectElement element2 : element ) {
                treeViewer.setExpandedState(element2.getProject(), true);
            }
        }
    }

    private List<Object> getLoadedElements() {
        if( treeViewer==null )
            return Collections.emptyList();
        Tree tree=(Tree) treeViewer.getControl();
        TreeItem[] items = tree.getItems();
        List<Object> data=new ArrayList<Object>();
        collectData(items, data);
        return data;
    }

    private void collectData( TreeItem[] items, List<Object> data ) {
        if( items.length==0 ) 
            return;
        for( TreeItem item : items ) {
            Object data2 = item.getData();
            if( data==null )
                continue;
            data.add(data2);
            collectData(item.getItems(), data);
        }
    }

    /**
     * Selects the elementChild <em>NOT</em> the element.
     *
     * @param element An ancestor of elementChild.
     * @param elementChild the element to select.
     */
    public void setSelection( IProjectElement element, Object elementChild ){
        
    }

    private class SetSelectionListener implements InputChangedListener{

        private Collection< ? extends IProjectElement> elements;
        private boolean reveal;

        public SetSelectionListener( Collection< ? extends IProjectElement> element, boolean reveal ) {
            this.reveal=reveal;
            this.elements=element;
        }

        private boolean isValid(){
            synchronized ( contentProvider ){
                if( inputChangedListener.get()!=this ){
                    contentProvider.removeListener(this);
                    return false;
                }
                return true;
            }
        }
        
        private void trySetSelection() {
                List<Object> expandedElements = getLoadedElements();
                if( expandedElements.containsAll(elements) ){
                    treeViewer.setSelection(new StructuredSelection(elements.toArray()), reveal);
                    synchronized ( contentProvider ){
                        contentProvider.removeListener(this);
                        inputChangedListener.set(null);
                    }
                }
        }

        public void changed() {
            if( isValid() ){
                if( Display.getCurrent()==null ){
                    Display.getDefault().asyncExec(new Runnable(){
                        public void run() {                            
                            trySetSelection();
                        }
                    });
                }else
                    trySetSelection();
            }
        }
        
    }
}
