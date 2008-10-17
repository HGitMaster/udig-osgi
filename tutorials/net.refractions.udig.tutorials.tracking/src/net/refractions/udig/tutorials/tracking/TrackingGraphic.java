package net.refractions.udig.tutorials.tracking;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.tutorials.tracking.trackingitem.SeagullFlock;
import net.refractions.udig.tutorials.tracking.trackingitem.TrackingItem;
import net.refractions.udig.tutorials.tracking.trackingitem.TrackingItemListener;

public class TrackingGraphic implements MapGraphic {
 
    public final static String LISTENER_BLACKBOARD_KEY = 
        "net.refractions.udig.tutorials.tracking.TrackingItemListenerImpl"; //$NON-NLS-1$
    
    public final static String NEEDSREFRESH_BLACKBOARD_KEY = 
        "net.refractions.udig.tutorials.tracking.TrackingItemListenerImpl"; //$NON-NLS-1$
    
    public final static String REFRESHJOB_BLACKBOARD_KEY = 
        "net.refractions.udig.tutorials.tracking.CheckRefreshJob"; //$NON-NLS-1$
    
    /**
     * This job will check if the layer needs a refresh and then reschedule itself to check
     * again.
     * 
     * @author GDavis
     * @since 1.1.0
     */
    private class CheckRefreshJob extends Job {
        private long delay;
        private ILayer layer;
        private Envelope bounds;

        public CheckRefreshJob(ILayer layer, Envelope bounds, long delay) {
            super("Refresh Layer Job");   //$NON-NLS-1$
            this.layer = layer;
            this.bounds = bounds;
            this.delay = delay;
        }

        protected IStatus run(IProgressMonitor monitor) {
            // check the blackboard of the given layer to see if it should be refreshed
            if (layer == null) {
                return Status.OK_STATUS;  // we have no valid layer 
            }
            
            IBlackboard blackboard = layer.getBlackboard();      
            Boolean needsRefresh = (Boolean) blackboard.get(NEEDSREFRESH_BLACKBOARD_KEY); 
            
            // remove this job from the blackboard since it is now running
            blackboard.put(REFRESHJOB_BLACKBOARD_KEY, null);
            
            if (needsRefresh == null) {
                // reschedule this job to go again
                //rescheduleJob();                
                return Status.OK_STATUS; // no refresh needed
            }   
            
            // refresh if needed and reset the blackboard value
            if (needsRefresh) {
                layer.refresh(bounds);
                needsRefresh = false;
                blackboard.put(NEEDSREFRESH_BLACKBOARD_KEY, needsRefresh);
            }
            
            // reschedule this job to go again
            //rescheduleJob();

            return Status.OK_STATUS;
        }
        
//        private void rescheduleJob() {
//            // reschedule this job to go again, add the new job to the blackboard
//            CheckRefreshJob newJob = new CheckRefreshJob(layer, bounds, delay);
//            newJob.schedule(delay);    
//            IBlackboard blackboard = layer.getBlackboard();
//            blackboard.put(REFRESHJOB_BLACKBOARD_KEY, newJob);
//        }
    }        

    public TrackingGraphic() {      
    }
    
    /**
     *  Implementation of a TrackingItemListener that will refresh the given
     *  context's layer and will also be responsible for cleaning itself up.
     *  
     * @author GDavis
     * @since 1.1.0
     */
    protected class TrackingItemListenerImpl implements TrackingItemListener {
        // this listener needs to know what layer to refresh
        private ILayer layer;
        
        public TrackingItemListenerImpl(ILayer layer) {
            this.layer = layer;
        }


        public void notifyChanged( TrackingItem trackingItem ) {
            // refresh the area only around this item
            //Envelope bounds = trackingItem.getBounds();
            //System.out.println("redrawing map: " + layer.getMap().getID()); //$NON-NLS-1$
            
            // set the layer to be refreshed
            if (layer != null) {
                IBlackboard blackboard = layer.getBlackboard();      
                Boolean needsRefresh = true;
                blackboard.put(NEEDSREFRESH_BLACKBOARD_KEY, needsRefresh);
            }
            else if (trackingItem != null) {
                // Layer is gone, remove listener
                trackingItem.removeListener(this);
            }
            // double check that the layer/map is still displayed
            if (layer.getMap() == null || layer.getMap().getProject() == null) {
                // Layer is gone, remove listener
                trackingItem.removeListener(this);
                if (layer != null) {
                    IBlackboard blackboard = layer.getBlackboard();
                    blackboard.put(LISTENER_BLACKBOARD_KEY, null);
                }
            }
        }
        
        public ILayer getLayer() {
            return layer;
        }
    }    

    public void draw( MapGraphicContext context ) {
        // get this map's flock and listener
        IBlackboard blackboard = getActiveBlackBoard(context); 
        List<SeagullFlock> flocks = (List<SeagullFlock>) blackboard.get(SeagullFlock.BLACKBOARD_KEY);
        TrackingItemListenerImpl listener = (TrackingItemListenerImpl) blackboard.get(LISTENER_BLACKBOARD_KEY);
       
        // if no flocks, nothing to draw
        if (flocks == null) {
            return; // no seagull flocks to draw
        }   
        // if no listener, or the old one points to a different layer, then create a new one
        boolean isNewListener = false;
        TrackingItemListenerImpl oldListener = null;
        if (listener == null || listener.getLayer() != context.getLayer()) {
            oldListener = listener;
            ILayer layer = context.getLayer();
            listener = new TrackingItemListenerImpl(layer);
            blackboard.put(LISTENER_BLACKBOARD_KEY, listener);
            isNewListener = true;
        }
        
        // for each flock, draw it and ensure it has our listener added (new flocks 
        // added since the last draw will not have them yet)
        for (SeagullFlock flock : flocks) {
           flock.draw(context);
           // if there is a new listener, remove any previous one first
           if (isNewListener) {
               flock.removeListener(oldListener);
           }
           flock.addListener(listener); // won't be added if it already has it
        }
        
        // set the graphic to look at refreshing every 0.5 secs if it is not already
        CheckRefreshJob job = (CheckRefreshJob) blackboard.get(REFRESHJOB_BLACKBOARD_KEY);
        if (job != null) {
            job.cancel();
        }
        long delay = 500;
        job = new CheckRefreshJob(context.getLayer(), null, delay);
        blackboard.put(REFRESHJOB_BLACKBOARD_KEY, job);
        job.schedule(delay);       
    }
    
    private IBlackboard getActiveBlackBoard(MapGraphicContext context) {
        //get the layer blackboard
        IBlackboard blackboard;
        if (context == null) {
            IMap map = ApplicationGIS.getActiveMap();
            if (map == null)
                return null; 
            
            blackboard = map.getBlackboard();  
        }
        else {
            IMap map = context.getLayer().getMap();
            if (map == null)
                return null;            
            blackboard = context.getLayer().getMap().getBlackboard();
        }
        
        return blackboard;
    }  

}
