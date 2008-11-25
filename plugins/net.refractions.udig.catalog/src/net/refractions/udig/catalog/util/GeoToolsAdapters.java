package net.refractions.udig.catalog.util;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IServiceInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.ResourceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.geotools.util.SimpleInternationalString;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * This class provides support for GeoTools Java 1.4 catalog interfaces.
 * <p>
 * This factory produces Java 5 wrappers around base GeoTools constructs.
 * 
 * @author Jody Garnett
 */
@SuppressWarnings("deprecation")
public class GeoToolsAdapters {

    static public IGeoResourceInfo info( final ResourceInfo info ){
        return new IGeoResourceInfo(){
            @Override
            public ReferencedEnvelope getBounds() {
                return (ReferencedEnvelope) info.getBounds();
            }
            @Override
            public CoordinateReferenceSystem getCRS() {
                return info.getCRS();
            }
            @Override
            public String getDescription() {
                return info.getDescription();
            }
            @Override
            public String getName() {
                return info.getName();
            }
            @Override
            public URI getSchema() {
                return info.getSchema();
            }
            @Override
            public String getTitle() {
                return info.getTitle();
            }
        };
    }
    static public IServiceInfo info( final ServiceInfo info ){
        return new IServiceInfo(){
            public String getAbstract() {
                return info.getDescription();
            }
            public String getDescription() {
                return info.getDescription();
            }
            public Set<String> getKeywords() {
                return new HashSet<String>( info.getKeywords());
            }
            public URI getPublisher() {
                return super.getPublisher();
            }
            public URI getSchema() {
                return info.getSchema();
            }
            public URI getSource() {
                return info.getSource();
            }
            public String getTitle() {
                return info.getTitle();
            }            
        };
    }
	static public ProgressListener progress(final IProgressMonitor monitor) {
		if( monitor == null ) return null;
		return new ProgressListener(){
			private String description;
			private int progress;
			private InternationalString task;
			
			public void complete() {
				monitor.done();
			}
			public void dispose() {
				description = null;
			}
			public void exceptionOccurred(Throwable arg0) {				
			}
			public String getDescription() {
				return description;
			}
			public boolean isCanceled() {
				return monitor.isCanceled();
			}
			public void progress(float amount) {
				int current = (int)(100.0 * amount);
				monitor.worked( current - progress );
				progress = current;
			}

			public void setCanceled(boolean arg0) {
				monitor.setCanceled(true);
			}

			public void setDescription(String text) {
				description = text;
			}

			public void started() {
				monitor.beginTask( description, 100);
			}

			public void warningOccurred(String arg0, String arg1, String arg2) {				
			}
			public InternationalString getTask() {
				return task;
			}
			public void setTask(InternationalString task) {
				this.task=task;
				
			}
            public float getProgress() {
                return progress;
            }			
		};
	}
	
	static public IProgressMonitor progress(final org.opengis.util.ProgressListener monitor) {
        if( monitor == null ) return null;
        return new IProgressMonitor(){
            int total;
            int amount;
            public void beginTask( String name, int totalWork ) {
                amount = 0;
                total = totalWork;
                monitor.setTask( new SimpleInternationalString(name));
                monitor.progress( work() );
            }
            float work(){
                return (float) amount / (float) total;
            }
            public void done() {
                amount = total;
                monitor.complete();
                monitor.dispose();
            }
            public void internalWorked( double work ) {
            }
            public boolean isCanceled() {
                return monitor.isCanceled();
            }
            public void setCanceled( boolean cancel ) {
                monitor.setCanceled( cancel );
            }
            public void setTaskName( String name ) {
                monitor.setTask( new SimpleInternationalString( name ));
            }
            public void subTask( String name ) {
                monitor.setTask( new SimpleInternationalString( name ));
            }
            public void worked( int work ) {
                amount += total;
            }
        };
    }
	
    static public IProgressMonitor progress(final ProgressListener monitor) {
        if( monitor == null ) return null;
        return new IProgressMonitor(){
            int total;
            int amount;
			public void beginTask( String name, int totalWork ) {
                amount = 0;
                total = totalWork;
                monitor.setDescription( name );
                monitor.progress( work() );
            }
            float work(){
                return (float) amount / (float) total;
            }
            public void done() {
                amount = total;
                monitor.complete();
                monitor.dispose();
            }
            public void internalWorked( double work ) {
            }
            public boolean isCanceled() {
                return monitor.isCanceled();
            }
            public void setCanceled( boolean cancel ) {
                monitor.setCanceled( cancel );
            }
            public void setTaskName( String name ) {
                monitor.setDescription( name );
            }
            public void subTask( String name ) {
                monitor.setDescription( name );
            }
            public void worked( int work ) {
                amount += total;
            }
        };
    }
}
