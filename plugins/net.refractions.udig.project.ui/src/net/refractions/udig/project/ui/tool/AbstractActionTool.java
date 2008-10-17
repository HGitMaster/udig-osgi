package net.refractions.udig.project.ui.tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for action tools.
 * 
 * 
 * @author Vitalus
 * @since UDIG 1.1
 *
 */
public abstract class AbstractActionTool implements ActionTool {

	
	/**
	 * Tool context.
	 */
    protected IToolContext context;
    
    private Map<String, Object> properties = new HashMap<String, Object>(5);
    
    /**
     * 
     * Tool's lifecycle listeners.
     */
    private Set<ToolLifecycleListener> listeners = new HashSet<ToolLifecycleListener>();

    
    private boolean enabled = true;
	
	public AbstractActionTool() {
	}
	
    /**
     * @see net.refractions.udig.project.ui.tool.Tool#setContext(net.refractions.udig.project.ui.tool.ToolContext)
     */
    public void setContext( IToolContext toolContext ) {
    	this.context = toolContext;
    }

    /**
     * @see net.refractions.udig.project.ui.tool.Tool#getContext()
     */
	public IToolContext getContext() {
		return context;
	}

    /**
     * @see net.refractions.udig.project.ui.tool.Tool#getProperty()
     */
	public Object getProperty(String key) {
		return properties.get(key);

	}

    /**
     * @see net.refractions.udig.project.ui.tool.Tool#setProperty()
     */
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
    
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		
		
	}

	public void addListener(ToolLifecycleListener listener) {
		listeners.add(listener);
		
	}

	public void removeListener(ToolLifecycleListener listener) {
		listeners.remove(listener);
		
	}
    

}
