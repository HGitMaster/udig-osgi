package net.refractions.udig.project.element;

import org.eclipse.ui.IMemento;

public class AbstractGenericProjectElement implements IGenericProjectElement{

    private String m_extId;
    
    public String getExtensionId() {
        return m_extId;
    }

    public void init( IMemento memento ) {
    }

    public void save( IMemento memento ) {
    }

    public void setExtensionId( String extId ) {
        this.m_extId = extId; 
    }

}
