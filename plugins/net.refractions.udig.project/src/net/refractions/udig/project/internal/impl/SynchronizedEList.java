/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.project.internal.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;

import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.eclipse.emf.common.util.EList;

/**
 * A decorator that synchronizes on all methods of the list.
 * 
 * When iterating make sure to use:
 * list.lock();
 * try{
 *    do iterations
 * }finally{
 *    list.unlock();
 * }
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class SynchronizedEList implements EList {
    private EList wrapped;
    Lock lock=new UDIGDisplaySafeLock();
    
    /**
     * Lock this list.
     */
    public void lock() {
        lock.lock();
    }
    
    public void unlock() {
        lock.unlock();
    }
    
    public SynchronizedEList( EList list ){
        wrapped=list;
    }
    
    @SuppressWarnings("unchecked")
    public void add( int arg0, Object arg1 ) {
        lock.lock();
        try{
            wrapped.add(arg0, arg1);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean add( Object arg0 ) {
        lock.lock();
        try{
            return wrapped.add(arg0);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean addAll( Collection arg0 ) {
        lock.lock();
        try{
            return wrapped.addAll(arg0);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean addAll( int arg0, Collection arg1 ) {
        lock.lock();
        try{
            return wrapped.addAll(arg0, arg1);
        }finally{
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try{
            wrapped.clear();
        }finally{
            lock.unlock();
        }
    }

    public boolean contains( Object arg0 ) {
        lock.lock();
        try{
            return wrapped.contains(arg0);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean containsAll( Collection arg0 ) {
        lock.lock();
        try{
            return wrapped.containsAll(arg0);
        }finally{
            lock.unlock();
        }
    }

    public boolean equals( Object arg0 ) {
        lock.lock();
        try{
            return wrapped.equals(arg0);
        }finally{
            lock.unlock();
        }
    }

    public Object get( int arg0 ) {
        lock.lock();
        try{
            return wrapped.get(arg0);
        }finally{
            lock.unlock();
        }
    }

    public int hashCode() {
        lock.lock();
        try{
            return wrapped.hashCode();
        }finally{
            lock.unlock();
        }
    }

    public int indexOf( Object arg0 ) {
        lock.lock();
        try{
            return wrapped.indexOf(arg0);
        }finally{
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try{
            return wrapped.isEmpty();
        }finally{
            lock.unlock();
        }
    }

    public Iterator iterator() {
        lock.lock();
        try{
            return listIterator();
        }finally{
            lock.unlock();
        }
    }

    public int lastIndexOf( Object arg0 ) {
        lock.lock();
        try{
            return wrapped.lastIndexOf(arg0);
        }finally{
            lock.unlock();
        }
    }

    public ListIterator listIterator() {
        lock.lock();
        try{
            return listIterator(0);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public ListIterator listIterator( int arg0 ) {
        lock.lock();
        try{
        	final List<Object> copy = new ArrayList<Object>(wrapped);
            return  new ListIterator<Object>(){
            ListIterator<Object> iter=copy.listIterator();
            public boolean hasNext() {
                    return iter.hasNext();
            }

            public Object next() {
                    return iter.next();
            }

            public void remove() {
                    iter.remove();
            }

            public void add( Object o ) {
                    iter.add(o);
            }

            public boolean hasPrevious() {
                    return iter.hasPrevious();
            }

            public int nextIndex() {
                    return iter.nextIndex();
            }

            public Object previous() {
                    return iter.previous();
            }

            public int previousIndex() {
                    return iter.previousIndex();
            }

            public void set( Object o ) {
                    throw new UnsupportedOperationException("This is not an editable iterator");
            }
            
        };
        }finally{
            lock.unlock();
        }
    }

    public Object move( int arg0, int arg1 ) {
        lock.lock();
        try{
            return wrapped.move(arg0, arg1);
        }finally{
            lock.unlock();
        }
    }

    public void move( int arg0, Object arg1 ) {
        lock.lock();
        try{
            wrapped.move(arg0, arg1);
        }finally{
            lock.unlock();
        }
    }

    public Object remove( int arg0 ) {
        lock.lock();
        try{
            return wrapped.remove(arg0);
        }finally{
            lock.unlock();
        }
    }

    public boolean remove( Object arg0 ) {
        lock.lock();
        try{
            return wrapped.remove(arg0);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll( Collection arg0 ) {
        lock.lock();
        try{
            return wrapped.removeAll(arg0);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean retainAll( Collection arg0 ) {
        lock.lock();
        try{
            return wrapped.retainAll(arg0);
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public Object set( int arg0, Object arg1 ) {
        lock.lock();
        try{
            return wrapped.set(arg0, arg1);
        }finally{
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try{
            return wrapped.size();
        }finally{
            lock.unlock();
        }

    }

    public List subList( int arg0, int arg1 ) {
        lock.lock();
        try{
            return wrapped.subList(arg0, arg1);
        }finally{
            lock.unlock();
        }

    }

    public Object[] toArray() {
        lock.lock();
        try{
            return wrapped.toArray();
        }finally{
            lock.unlock();
        }

    }

    @SuppressWarnings("unchecked")
    public Object[] toArray( Object[] arg0 ) {
        lock.lock();
        try{
            return wrapped.toArray(arg0);
        }finally{
            lock.unlock();
        }
    }
}
