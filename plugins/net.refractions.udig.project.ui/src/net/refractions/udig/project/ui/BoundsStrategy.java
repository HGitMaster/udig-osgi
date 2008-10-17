package net.refractions.udig.project.ui;

import net.refractions.udig.project.internal.render.ViewportModel;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

/**
 * A strategy for determining the bounds of the map to display... Because
 * the aspect ration of the final image is likely not the same as the
 * viewport. Or a particualr scale is required
 * 
 * This is used by
 * {@link ApplicationGIS#drawMap(net.refractions.udig.project.ui.ApplicationGIS.DrawMapParameter)}
 * 
 * @author jesse
 * 
 * @see ApplicationGIS#drawMap(net.refractions.udig.project.ui.ApplicationGIS.DrawMapParameter)
 */
public class BoundsStrategy {

	protected double scaleDenominator;
	protected ReferencedEnvelope boundsToDisplay;

	/**
	 * For extenders
	 */
	protected BoundsStrategy() {
	}

	/**
	 * Create a new instance that zooms to the given scale denominator with
	 * the center of the map staying unchanged.
	 * 
	 * 
	 * @param scaleDenominator
	 */
	public BoundsStrategy(double scaleDenominator) {
		this.scaleDenominator = scaleDenominator;
		this.boundsToDisplay = null;
	}

	/**
	 * Create a new strategy that will make the map zoom to the given
	 * envelope
	 * 
	 * @param boundsToDisplay
	 */
	public BoundsStrategy(ReferencedEnvelope boundsToDisplay) {
		this.scaleDenominator = -1;
		this.boundsToDisplay = boundsToDisplay;
	}

	/**
	 * Sets the bounds on the viewport model. This may be overridden. This
	 * implementation will set the model to {@link #boundsToDisplay} if it
	 * is non-null otherwise will use the scale denominator. Or if the
	 * scaleDenominator is <1 it will zoom to the model's current bounds.
	 * 
	 * @param model
	 *            the viewport model to set the bounds on.
	 * @param currentBounds 
	 * 			  the bounds of the original map
	 */
	public void setBounds(ViewportModel model, ReferencedEnvelope currentBounds) {
		if (boundsToDisplay != null) {
			ReferencedEnvelope destinationBox = boundsToDisplay;
			try {
				destinationBox = boundsToDisplay.transform(model.getCRS(), true);
			} catch (TransformException e) {
				ProjectUIPlugin.log("Unable to transform to the viewport's CRS (ApplicationGIS#drawMap()", e);
			} catch (FactoryException e) {
				ProjectUIPlugin.log("Unable to transform to the viewport's CRS (ApplicationGIS#drawMap()", e);
			}
			model.zoomToBox(destinationBox);
		}else{
			model.setBounds(currentBounds);
			if( scaleDenominator>0 ){
				model.setScale(scaleDenominator);
			}
		}
	}

}