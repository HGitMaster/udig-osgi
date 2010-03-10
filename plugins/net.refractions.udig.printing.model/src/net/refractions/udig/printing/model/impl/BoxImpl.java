/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.refractions.udig.printing.model.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import net.refractions.udig.printing.model.Box;
import net.refractions.udig.printing.model.BoxPrinter;
import net.refractions.udig.printing.model.Connection;
import net.refractions.udig.printing.model.ModelPackage;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Box</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.refractions.udig.printing.model.impl.BoxImpl#getSourceConnections <em>Source Connections</em>}</li>
 *   <li>{@link net.refractions.udig.printing.model.impl.BoxImpl#getTargetConnections <em>Target Connections</em>}</li>
 *   <li>{@link net.refractions.udig.printing.model.impl.BoxImpl#getBoxPrinter <em>Box Printer</em>}</li>
 *   <li>{@link net.refractions.udig.printing.model.impl.BoxImpl#getID <em>ID</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BoxImpl extends ElementImpl implements Box {

	/**
     * The cached value of the '{@link #getSourceConnections() <em>Source Connections</em>}' reference list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see #getSourceConnections()
     * @generated
     * @ordered
     */
    protected EList<Connection> sourceConnections;

	/**
     * The cached value of the '{@link #getTargetConnections() <em>Target Connections</em>}' reference list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see #getTargetConnections()
     * @generated
     * @ordered
     */
    protected EList<Connection> targetConnections;

    /**
     * The default value of the '{@link #getBoxPrinter() <em>Box Printer</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getBoxPrinter()
     * @generated
     * @ordered
     */
    protected static final BoxPrinter BOX_PRINTER_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getBoxPrinter() <em>Box Printer</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getBoxPrinter()
     * @generated
     * @ordered
     */
    protected BoxPrinter boxPrinter = BOX_PRINTER_EDEFAULT;

	/**
     * The default value of the '{@link #getID() <em>ID</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getID()
     * @generated
     * @ordered
     */
    protected static final String ID_EDEFAULT = null;

	/**
     * The cached value of the '{@link #getID() <em>ID</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getID()
     * @generated
     * @ordered
     */
    protected String iD = ID_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    public BoxImpl() {
        super();
    }

	/**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ModelPackage.Literals.BOX;
    }

	/**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public List<Connection> getSourceConnections() {
        if (sourceConnections == null) {
            sourceConnections = new EObjectResolvingEList<Connection>(Connection.class, this, ModelPackage.BOX__SOURCE_CONNECTIONS);
        }
        return sourceConnections;
    }

	/**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public List<Connection> getTargetConnections() {
        if (targetConnections == null) {
            targetConnections = new EObjectResolvingEList<Connection>(Connection.class, this, ModelPackage.BOX__TARGET_CONNECTIONS);
        }
        return targetConnections;
    }

	/**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public BoxPrinter getBoxPrinter() {
        return boxPrinter;
    }

    public void setBoxPrinter( BoxPrinter value ) {
        value.setBox(this);
        setBoxPrinterGen(value);
    }

	/**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setBoxPrinterGen(BoxPrinter newBoxPrinter) {
        BoxPrinter oldBoxPrinter = boxPrinter;
        boxPrinter = newBoxPrinter;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.BOX__BOX_PRINTER, oldBoxPrinter, boxPrinter));
    }

	/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getID() {
        return iD;
    }

	/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setID(String newID) {
        String oldID = iD;
        iD = newID;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.BOX__ID, oldID, iD));
    }

	/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ModelPackage.BOX__SOURCE_CONNECTIONS:
                return getSourceConnections();
            case ModelPackage.BOX__TARGET_CONNECTIONS:
                return getTargetConnections();
            case ModelPackage.BOX__BOX_PRINTER:
                return getBoxPrinter();
            case ModelPackage.BOX__ID:
                return getID();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ModelPackage.BOX__SOURCE_CONNECTIONS:
                getSourceConnections().clear();
                getSourceConnections().addAll((Collection<? extends Connection>)newValue);
                return;
            case ModelPackage.BOX__TARGET_CONNECTIONS:
                getTargetConnections().clear();
                getTargetConnections().addAll((Collection<? extends Connection>)newValue);
                return;
            case ModelPackage.BOX__BOX_PRINTER:
                setBoxPrinter((BoxPrinter)newValue);
                return;
            case ModelPackage.BOX__ID:
                setID((String)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case ModelPackage.BOX__SOURCE_CONNECTIONS:
                getSourceConnections().clear();
                return;
            case ModelPackage.BOX__TARGET_CONNECTIONS:
                getTargetConnections().clear();
                return;
            case ModelPackage.BOX__BOX_PRINTER:
                setBoxPrinter(BOX_PRINTER_EDEFAULT);
                return;
            case ModelPackage.BOX__ID:
                setID(ID_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case ModelPackage.BOX__SOURCE_CONNECTIONS:
                return sourceConnections != null && !sourceConnections.isEmpty();
            case ModelPackage.BOX__TARGET_CONNECTIONS:
                return targetConnections != null && !targetConnections.isEmpty();
            case ModelPackage.BOX__BOX_PRINTER:
                return BOX_PRINTER_EDEFAULT == null ? boxPrinter != null : !BOX_PRINTER_EDEFAULT.equals(boxPrinter);
            case ModelPackage.BOX__ID:
                return ID_EDEFAULT == null ? iD != null : !ID_EDEFAULT.equals(iD);
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (boxPrinter: ");
        result.append(boxPrinter);
        result.append(", iD: ");
        result.append(iD);
        result.append(')');
        return result.toString();
    }

    public void add( Connection connection ) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null"); //$NON-NLS-1$
        }

        if (connection.getTarget() == connection.getSource()) {
            throw new IllegalArgumentException("Connection source and target cannot be the same"); //$NON-NLS-1$
        }

        if (connection.getSource() == this) {
            getSourceConnections().add(connection);
        } else if (connection.getTarget() == this) {
            getTargetConnections().add(connection);
        }
    }
    public void remove( Connection connection ) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null"); //$NON-NLS-1$
        }

        if (connection.getSource() == this) {
            getSourceConnections().remove(connection);
        } else if (connection.getTarget() == this) {
            getTargetConnections().remove(connection);
        }
    }

    CopyOnWriteArraySet<IPropertyChangeListener> listeners = new CopyOnWriteArraySet<IPropertyChangeListener>();

    public void notifyPropertyChange( final PropertyChangeEvent event ) {
        Display display = Display.getCurrent();
        if( display==null ){
            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                    for( IPropertyChangeListener l : listeners ) {
                        l.propertyChange(event);
                    }
                }
            });
        }else{
            for( IPropertyChangeListener l : listeners ) {
                l.propertyChange(event);
            }
            
        }
    }

    public void addPropertyChangeListener( IPropertyChangeListener l ) {
        listeners.add(l);
    }

    public void removePropertyChangeListener( IPropertyChangeListener l ) {
        listeners.remove(l);
    }

	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
	    if( getBoxPrinter()!=null ) {
	        return ((IAdaptable) getBoxPrinter()).getAdapter(adapter);
	    }
	    return null;
	}
    
} // BoxImpl
