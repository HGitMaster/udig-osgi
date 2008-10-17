/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.catalog.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IResolveDeltaVisitor;

/**
 * Catalog delta.
 * 
 * @author jgarnett
 * @since 0.6.0
 */
public class ResolveDelta implements IResolveDelta {
    private List<IResolveDelta> children;
    private Kind kind = Kind.NO_CHANGE;
    private IResolve handle = null;
    private IResolve newHandle = null;
    private final Object newValue;
    private final Object oldValue;

    /**
     * Delta saying something changed somewhere.
     * <p>
     * Handle is the root of the catalog. Indicates that all labels should refresh, handles are
     * still considered valid so layers can simply ask new info (they don't need to search for their
     * content again).
     * </p>
     */
    public ResolveDelta() {
        this.kind = Kind.CHANGED;
        this.children = NO_CHILDREN;
        handle = CatalogPlugin.getDefault().getLocalCatalog();
        newValue=oldValue=null;
        newHandle = null;
    }

    /**
     * Delta for a changed handle, ie handle state refresh.
     * <p>
     * Used to communicate that new Info is available and labels should be refreshed.
     * </p>
     */
    public ResolveDelta( IResolve handle, List<IResolveDelta> changes ) {
        this.kind = Kind.CHANGED;
        this.children = Collections.unmodifiableList(changes);
        this.handle = handle;
        if (kind == Kind.REPLACED) {
            throw new IllegalArgumentException(Messages.ResolveDelta_error_newHandleRequired); 
        }
        newHandle = null;
        newValue=oldValue=null;
    }
    /** 
     * Simple change used for Add and Remove with no children 
     */
    public ResolveDelta( IResolve handle, Kind kind ) {
        this.kind = kind;
        this.children = NO_CHILDREN;
        this.handle = handle;
        if (kind == Kind.REPLACED) {
            throw new IllegalArgumentException(Messages.ResolveDelta_error_newHandleRequired); 
        }
        newHandle = null;
        newValue=oldValue=null;
    }
    /** Delta for a specific change */
    public ResolveDelta( IResolve handle, Kind kind, List<IResolveDelta> changes ) {
        this.kind = kind;
        List< ? extends IResolveDelta> changes2=changes;
        if (changes2 == null)
            changes2 = new ArrayList<IResolveDelta>();
        this.children = Collections.unmodifiableList(changes2);
        this.handle = handle;
        if (kind == Kind.REPLACED) {
            throw new IllegalArgumentException(Messages.ResolveDelta_error_newHandleRequired); 
        }
        newHandle = null;
        newValue=oldValue=null;
    }
    /**
     * Delta for handle repalcement.
     * <p>
     * Used to indicate the actual connection used by the handle has been replaced. Layers should
     * foget everything they no and latch onto the newHandle.
     * </p>
     */
    public ResolveDelta( IResolve handle, IResolve newHandle, List<IResolveDelta> changes ) {
        this.kind = Kind.REPLACED;
        List< ? extends IResolveDelta> changes2=changes;
        if (changes2 == null)
            changes2 = new ArrayList<IResolveDelta>();

        this.children = Collections.unmodifiableList(changes2);
        this.handle = handle;
        this.newHandle = newHandle;
        newValue=oldValue=null;
    }

    /**
     * Indicates a IResolve has changed.  (Kind==Kind.CHANGED).  
     * 
     * @param handle resolve that has changed
     * @param oldValue old value before change
     * @param newValue new value after change
     */
    public ResolveDelta( IResolve handle, Object oldValue, Object newValue ) {
        kind=Kind.CHANGED;
        this.children = NO_CHILDREN;
        this.handle = handle;
        newHandle = null;
        this.newValue=newValue;
        this.oldValue=oldValue;
    }

    /*
     * @see net.refractions.udig.catalog.ICatalogDelta#accept(net.refractions.udig.catalog.IServiceVisitor)
     */
    public void accept( IResolveDeltaVisitor visitor ) throws IOException {
        if (visitor.visit(this)) {
            for( IResolveDelta delta : children ) {
                if (delta != null && visitor.visit(delta)) {
                    delta.accept(visitor);
                }
            }
        }
    }

    /*
     * @see net.refractions.udig.catalog.ICatalogDelta#getAffected()
     */
    public List<IResolveDelta> getChildren() {
        return children;
    }

    /*
     * @see net.refractions.udig.catalog.ICatalogDelta#getAffected(int, int)
     */
    public List<IResolveDelta> getChildren( EnumSet<Kind> kindMask ) {
        List<IResolveDelta> list = new ArrayList<IResolveDelta>();
        for( IResolveDelta delta : children ) {
            if (delta != null && kindMask.contains(delta.getKind())) {
                list.add(delta);
            }
        }
        return list;
    }

    /*
     * @see net.refractions.udig.catalog.IDelta#getKind()
     */
    public Kind getKind() {
        return kind;
    }

    public IResolve getResolve() {
        return handle;
    }

    public IResolve getNewResolve() {
        return newHandle;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        /*
         * private List<IResolveDelta> children; private Kind kind = Kind.NO_CHANGE; private
         * IResolve handle = null; private IResolve newHandle = null;
         */
        buffer.append(" ResolveDelta: ["); //$NON-NLS-1$
        buffer.append(kind);

        if (handle != null) {
            buffer.append(","); //$NON-NLS-1$
            buffer.append(handle);
        }

        if (newHandle != null) {
            buffer.append(","); //$NON-NLS-1$
            buffer.append(newHandle);
        }

        if (children != null) {
            buffer.append("children ["); //$NON-NLS-1$
            for( int i = 0; i < children.size(); i++ ) {
                buffer.append(children.get(i).getKind());
            }
            buffer.append("] "); //$NON-NLS-1$
        }

        buffer.append("] "); //$NON-NLS-1$
        return buffer.toString();
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }
}
