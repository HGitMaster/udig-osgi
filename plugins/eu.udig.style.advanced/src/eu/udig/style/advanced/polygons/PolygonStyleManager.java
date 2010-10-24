/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.udig.style.advanced.polygons;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import net.refractions.udig.ui.graphics.AWTSWTImageUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.geotools.styling.Style;

import eu.udig.style.advanced.StylePlugin;
import eu.udig.style.advanced.common.StyleFilter;
import eu.udig.style.advanced.common.StyleManager;
import eu.udig.style.advanced.common.styleattributeclasses.FeatureTypeStyleWrapper;
import eu.udig.style.advanced.common.styleattributeclasses.RuleWrapper;
import eu.udig.style.advanced.common.styleattributeclasses.StyleWrapper;
import eu.udig.style.advanced.utils.Utilities;

/**
 * A style viewer that manages {@link Style}s.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PolygonStyleManager extends StyleManager {

    /**
     * Creates a new style manager pointing to the default preferences folder for style.
     */
    public PolygonStyleManager() {
        IPath stateLocation = StylePlugin.getDefault().getStateLocation();
        File stateLocationFile = stateLocation.toFile();

        styleFolderFile = new File(stateLocationFile, POLYGONSTYLEFOLDER);
        if (!styleFolderFile.exists()) {
            styleFolderFile.mkdirs();
        }

    }

    /**
     * Creates the style viewer panel.
     * 
     * @param parent the parent {@link Composite}.
     */
    public void init( Composite parent ) {
        createStylesTableViewer(parent);

        reloadStyleFolder();
    }

    private TableViewer createStylesTableViewer( Composite parent ) {
        final StyleFilter filter = new StyleFilter();
        final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
        searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        searchText.addKeyListener(new KeyAdapter(){
            public void keyReleased( KeyEvent ke ) {
                filter.setSearchText(searchText.getText());
                stylesViewer.refresh();
            }

        });

        stylesViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER);
        Table table = stylesViewer.getTable();
        GridData tableGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGD.heightHint = 200;
        table.setLayoutData(tableGD);

        stylesViewer.addFilter(filter);
        stylesViewer.setContentProvider(new IStructuredContentProvider(){
            public Object[] getElements( Object inputElement ) {
                if (inputElement instanceof List< ? >) {
                    List< ? > styles = (List< ? >) inputElement;
                    StyleWrapper[] array = (StyleWrapper[]) styles.toArray(new StyleWrapper[styles.size()]);
                    return array;
                }
                return null;
            }
            public void dispose() {
            }
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }
        });

        stylesViewer.setLabelProvider(new LabelProvider(){
            public Image getImage( Object element ) {
                if (element instanceof StyleWrapper) {
                    StyleWrapper styleWrapper = (StyleWrapper) element;
                    List<FeatureTypeStyleWrapper> featureTypeStyles = styleWrapper.getFeatureTypeStylesWrapperList();
                    int iconSize = 48;
                    BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = image.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    for( FeatureTypeStyleWrapper featureTypeStyle : featureTypeStyles ) {
                        List<RuleWrapper> rules = featureTypeStyle.getRulesWrapperList();
                        BufferedImage tmpImage = Utilities.polygonRulesWrapperToImage(rules, iconSize, iconSize);
                        g2d.drawImage(tmpImage, 0, 0, null);
                    }
                    g2d.dispose();
                    Image convertToSWTImage = AWTSWTImageUtils.convertToSWTImage(image);
                    return convertToSWTImage;
                }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof StyleWrapper) {
                    StyleWrapper styleWrapper = (StyleWrapper) element;
                    String styleName = styleWrapper.getName();
                    if (styleName == null || styleName.length() == 0) {
                        styleName = Utilities.DEFAULT_STYLENAME;
                        styleName = Utilities.checkSameNameStyle(getStyles(), styleName);
                        styleWrapper.setName(styleName);
                    }
                    return styleName;
                }
                return ""; //$NON-NLS-1$
            }
        });

        stylesViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged( SelectionChangedEvent event ) {
                ISelection selection = event.getSelection();
                if (!(selection instanceof IStructuredSelection)) {
                    return;
                }
                IStructuredSelection sel = (IStructuredSelection) selection;
                if (sel.isEmpty()) {
                    return;
                }

                Object selectedItem = sel.getFirstElement();
                if (selectedItem == null) {
                    // unselected, show empty panel
                    return;
                }

                if (selectedItem instanceof StyleWrapper) {
                    currentSelectedStyleWrapper = (StyleWrapper) selectedItem;
                }
            }

        });
        return stylesViewer;
    }

    // /**
    // * Add a style to the {@link TableViewer viewer} from a file.
    // *
    // * @param files the array of files to import.
    // * @throws Exception
    // */
    // public void importToStyle( File... files ) throws Exception {
    // List<Style> styles = getStyles();
    //
    // for( File file : files ) {
    // String name = file.getName();
    // File newFile = new File(styleFolderFile, name);
    // FileUtils.copyFile(file, newFile);
    // Style style = Utilities.createStyleFromGraphic(newFile);
    // styles.add(style);
    // styleToDisk(style);
    // }
    // stylesViewer.refresh(false, true);
    // }

}
