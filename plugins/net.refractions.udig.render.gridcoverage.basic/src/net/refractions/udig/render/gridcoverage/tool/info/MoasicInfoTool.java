package net.refractions.udig.render.gridcoverage.tool.info;

import java.awt.Rectangle;

import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.commands.SelectionBoxCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.AbstractModalTool;
import net.refractions.udig.project.ui.tool.ModalTool;
import net.refractions.udig.render.gridcoverage.tool.info.internal.MosaicInfoView;
import net.refractions.udig.render.internal.gridcoverage.basic.RendererPlugin;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class MoasicInfoTool extends AbstractModalTool implements ModalTool {

    /**
     * ID of current tool
     */
    public static final String ID = "net.refractions.udig.render.gridcoverage.tool.info.mosaicInfoTool"; //$NON-NLS-1$
    public static final String CATEGORY_ID = "net.refractions.udig.tool.category.info"; //$NON-NLS-1$

    public MoasicInfoTool() {
        super(MOUSE | MOTION);
    }

    @Override
    public void mousePressed( MapMouseEvent e ) {
        draw.setValid(true); // make sure context.getViewportPane().repaint() knows about us
        context.sendASyncCommand(draw); // should of isValided us
        feedback(e);

    }
    @Override
    public void mouseDragged( MapMouseEvent e ) {
        feedback(e);

    }

    SelectionBoxCommand draw = new SelectionBoxCommand();

    /**
     * Provides user feedback
     * 
     * @param e
     */
    public void feedback( MapMouseEvent e ) {
        draw.setShape(new Rectangle(e.x - 3, e.y - 3, 5, 5));
        context.getViewportPane().repaint(e.x - 4, e.y - 4, 7, 7);

        super.mouseDragged(e);
    }

    /**
     * What's this then?
     * <p>
     * See class description for intended workflow.
     * </p>
     * 
     * @see net.refractions.udig.project.ui.tool.AbstractTool#mouseReleased(MapMouseEvent)
     */
    public void mouseReleased( MapMouseEvent e ) {
        try {

            ReferencedEnvelope bbox = context.getBoundingBox(e.getPoint(), 5);

            // set up request
            final MosaicInfoView.InfoRequest request = new MosaicInfoView.InfoRequest();
            request.bbox = bbox;
            request.layers = context.getMapLayers();

            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                    MosaicInfoView infoView = (MosaicInfoView) ApplicationGIS.getView(true,
                            MosaicInfoView.VIEW_ID);

                    // JONES: deselect current feature so it won't flash when view is activated (it
                    // won't be valid
                    // one the new search passes.
                    if (infoView != null)
                        if (infoView.getSite().getSelectionProvider() != null)
                            infoView.getSite().getSelectionProvider().setSelection(
                                    new StructuredSelection());

                    // JONES: activate view now that there is no current selection.
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if (!page.isPartVisible(infoView))
                        page.bringToTop(infoView);
                                       
                    // we got here and info was null? Don't want to fail on first attempt
                    infoView = (MosaicInfoView) ApplicationGIS.getView(false,MosaicInfoView.VIEW_ID);
                    infoView.updateInfo(request);
                }
            });
        } catch (Throwable e1) {
            // Should log problem ..
            RendererPlugin.log("Could not display information", e1); //$NON-NLS-1$
        } finally {
            draw.setValid(false); // get us off the draw stack for
            // context.getViewportPane().repaint();
            context.getViewportPane().repaint();
        }
    }

    /**
     * @see net.refractions.udig.project.ui.tool.Tool#dispose()
     */
    public void dispose() {
        super.dispose();
    }

}