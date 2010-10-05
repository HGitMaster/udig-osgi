/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
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
package eu.udig.style.jgrass.categories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.internal.Layer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.geotools.gce.grassraster.JGrassConstants;

import eu.udig.catalog.jgrass.activeregion.dialogs.JGRasterChooserDialog;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.core.JGrassMapsetGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;
import eu.udig.style.jgrass.JGrassrasterStyleActivator;

/**
 * The composite holding the JGrass Raster map editing logic
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class CategoryEditor extends Composite implements SelectionListener {

    private ArrayList<CategoryRule> listOfRules = null;
    private Button addRuleButton = null;
    private Button removeRuleButton = null;
    private Button moveRuleUpButton = null;
    private Button moveRuleDownButton = null;
    private Composite rulesComposite = null;
    private ScrolledComposite scrolledRulesComposite = null;
    private Layer layer;
    private String[] mapsetPathAndMapName;
    private File catsFile;
    private final Label alphaLabel = null;
    private Button loadFromFileButton = null;
    private Button loadFromMapButton = null;

    public CategoryEditor( Composite parent, int style ) {
        super(parent, style);
        listOfRules = new ArrayList<CategoryRule>();
        initialize();
    }

    private void initialize() {
        GridData gridData21 = new GridData();
        gridData21.horizontalSpan = 2;
        gridData21.verticalAlignment = GridData.CENTER;
        gridData21.horizontalAlignment = GridData.FILL;
        GridData gridData11 = new GridData();
        gridData11.horizontalAlignment = GridData.FILL;
        gridData11.horizontalSpan = 2;
        gridData11.verticalAlignment = GridData.CENTER;
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.FILL;
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.verticalAlignment = GridData.CENTER;
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.verticalAlignment = GridData.CENTER;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.verticalAlignment = GridData.CENTER;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.CENTER;
        addRuleButton = new Button(this, SWT.NONE);
        addRuleButton.setText("+");
        addRuleButton.setLayoutData(gridData);
        addRuleButton.addSelectionListener(this);
        removeRuleButton = new Button(this, SWT.NONE);
        removeRuleButton.setText("-");
        removeRuleButton.setLayoutData(gridData1);
        removeRuleButton.addSelectionListener(this);
        moveRuleUpButton = new Button(this, SWT.UP | SWT.ARROW);
        moveRuleUpButton.setLayoutData(gridData2);
        moveRuleUpButton.addSelectionListener(this);
        moveRuleDownButton = new Button(this, SWT.DOWN | SWT.ARROW);
        moveRuleDownButton.setLayoutData(gridData3);
        moveRuleDownButton.addSelectionListener(this);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        gridLayout.makeColumnsEqualWidth = true;
        this.setLayout(gridLayout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.setLayoutData(gd);
        createScrolledRulesComposite();
        loadFromFileButton = new Button(this, SWT.NONE);
        loadFromFileButton.setText("load from file");
        loadFromFileButton.setLayoutData(gridData11);
        loadFromFileButton.addSelectionListener(this);
        loadFromMapButton = new Button(this, SWT.NONE);
        loadFromMapButton.setText("load from map");
        loadFromMapButton.setLayoutData(gridData21);
        loadFromMapButton.addSelectionListener(this);
        createRulesComposite();
        // setSize(new Point(395, 331));
    }

    /**
     * This method initializes rulesComposite
     */
    private void createRulesComposite() {
        GridData gridData4 = new GridData();
        gridData4.horizontalSpan = 4;
        gridData4.verticalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.grabExcessVerticalSpace = true;
        gridData4.horizontalAlignment = GridData.FILL;
        rulesComposite = new Composite(scrolledRulesComposite, SWT.NONE);
        rulesComposite.setLayout(new GridLayout());
        rulesComposite.setLayoutData(gridData4);
        scrolledRulesComposite.setContent(rulesComposite);
    }

    /**
     * This method initializes scrolledRulesComposite
     */
    private void createScrolledRulesComposite() {
        GridData gridData7 = new GridData();
        gridData7.horizontalSpan = 4;
        gridData7.verticalAlignment = GridData.FILL;
        gridData7.grabExcessVerticalSpace = true;
        gridData7.grabExcessHorizontalSpace = true;
        gridData7.horizontalAlignment = GridData.FILL;

        scrolledRulesComposite = new ScrolledComposite(this, SWT.V_SCROLL | SWT.BORDER);
        scrolledRulesComposite.setLayoutData(gridData7);
        scrolledRulesComposite.setAlwaysShowScrollBars(true);
        scrolledRulesComposite.setExpandHorizontal(true);
        scrolledRulesComposite.setExpandVertical(true);
        scrolledRulesComposite.setMinSize(300, 500);
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
    }

    public void widgetSelected( SelectionEvent e ) {
        Button selectedButton = (Button) e.getSource();
        if (selectedButton.equals(addRuleButton)) {
            // add an empty rule to te composite
            CategoryRule r = new CategoryRule();
            listOfRules.add(r);
            redoLayout();
        } else if (selectedButton.equals(removeRuleButton)) {
            for( int i = 0; i < listOfRules.size(); i++ ) {
                CategoryRule r = listOfRules.get(i);
                if (r.isActive()) {
                    listOfRules.remove(r);
                    // if (i > 0) {
                    // i--;
                    // }
                }
            }
            redoLayout();
        } else if (selectedButton.equals(moveRuleUpButton)) {
            for( int i = 0; i < listOfRules.size(); i++ ) {
                CategoryRule r = listOfRules.get(i);
                if (r.isActive()) {
                    if (i > 0) {
                        listOfRules.remove(r);
                        listOfRules.add(i - 1, r);
                    }
                }
            }
            redoLayout();
        } else if (selectedButton.equals(moveRuleDownButton)) {
            for( int i = 0; i < listOfRules.size(); i++ ) {
                CategoryRule r = listOfRules.get(i);
                if (r.isActive()) {
                    if (i < listOfRules.size() - 1) {
                        listOfRules.remove(r);
                        listOfRules.add(i + 1, r);
                        i++;
                    }
                }
            }
            redoLayout();
        } else if (selectedButton.equals(loadFromFileButton)) {

            FileDialog fileDialog = new FileDialog(this.getShell(), SWT.OPEN);
            String path = fileDialog.open();

            if (path == null) {
                return;
            }

            makeSomeCategories(path);

        } else if (selectedButton.equals(loadFromMapButton)) {

            JGRasterChooserDialog tree = new JGRasterChooserDialog(null);
            tree.open(this.getShell(), SWT.SINGLE);

            update(tree.getSelectedResources());
        }
    }

    public void makeSomeCategories( String catspath ) {

        File catsFile = new File(catspath);
        BufferedReader inputReader;
        try {
            inputReader = new BufferedReader(new FileReader(catsFile));
        } catch (FileNotFoundException e) {
            JGrassrasterStyleActivator.log("JGrassrasterStyleActivator problem", e); //$NON-NLS-1$
            e.printStackTrace();
            return;
        }

        String line = null;
        LinkedHashMap<String, String> cats = new LinkedHashMap<String, String>();
        if (catsFile.exists()) {
            try {
                // jump over the first 4 lines
                line = inputReader.readLine();
                line = inputReader.readLine();
                line = inputReader.readLine();
                line = inputReader.readLine();

                while( (line = inputReader.readLine()) != null ) {
                    if (line == null || line.equals("")) {
                        return;
                    }

                    StringTokenizer valtok = new StringTokenizer(line, ":");
                    if (valtok.countTokens() > 1) {
                        cats.put(valtok.nextToken(), valtok.nextToken());
                    } else {
                        return;
                    }
                }
            } catch (IOException e1) {
                JGrassrasterStyleActivator.log("JGrassrasterStyleActivator problem", e1); //$NON-NLS-1$
                e1.printStackTrace();
                return;
            }

            // Iterate over the keys in the map
            Iterator it = cats.keySet().iterator();

            listOfRules.clear();
            while( it.hasNext() ) {
                String key = (String) it.next();
                listOfRules.add(new CategoryRule(key, cats.get(key), true));
            }
        }
        this.setLayer(layer);

        this.setRulesList(listOfRules);
    }

    /**
     * Set the layer that called this style editor. Needed for putting the alpha value into the
     * blackboard whenever it something changes.
     * 
     * @param layer
     */
    public void setLayer( Layer layer ) {
        this.layer = layer;
        IGeoResource resource = layer.getGeoResource();
        mapsetPathAndMapName = JGrassCatalogUtilities
                .getMapsetpathAndMapnameFromJGrassMapGeoResource(resource);
        catsFile = new File(mapsetPathAndMapName[0] + File.separator + JGrassConstants.CATS
                + File.separator + mapsetPathAndMapName[1]);
    }

    public void setRulesList( ArrayList<CategoryRule> listOfRules ) {
        this.listOfRules = listOfRules;
        redoLayout();
    }

    protected void redoLayout() {
        // remove the rules from the composite
        Control[] rulesControls = rulesComposite.getChildren();
        for( int i = 0; i < rulesControls.length; i++ ) {
            rulesControls[i].dispose();
        }

        // recreate the rules composites from the list
        for( CategoryRule rule : listOfRules ) {
            new CategoryRuleComposite(rulesComposite, SWT.BORDER, rule);
        }

        rulesComposite.layout();
    }

    /**
     * write the rules to file
     */
    public synchronized void makePersistent() {
        // write to disk
        System.out.println("CATS PERSISTENCE");
        if (catsFile != null) {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(catsFile));
            } catch (IOException e) {
                JGrassrasterStyleActivator.log("JGrassrasterStyleActivator problem", e); //$NON-NLS-1$
                e.printStackTrace();
            }

            if (listOfRules.size() == 0) {
                return;
            }

            StringBuffer header = new StringBuffer();
            header.append("# " + listOfRules.size() + "\n");
            header.append(mapsetPathAndMapName[1] + "\n");
            header.append("\n");
            header.append("0.00 0.00 0.00 0.00\n");
            try {
                bw.write(header.toString());

                for( CategoryRule r : listOfRules ) {
                    if (r.isActive())
                        bw.write(r.ruleToString() + "\n");
                }

                bw.close();
            } catch (IOException e1) {
                JGrassrasterStyleActivator.log("JGrassrasterStyleActivator problem", e1); //$NON-NLS-1$
                e1.printStackTrace();
            }

        }

    }

    public void update( Object updatedObject ) {
        if (updatedObject instanceof List) {
            String mapName = null;
            String mapsetPath = null;
            List layers = (List) updatedObject;
            for( Object layer : layers ) {
                if (layer instanceof JGrassMapGeoResource) {
                    JGrassMapGeoResource rasterMapResource = (JGrassMapGeoResource) layer;
                    try {
                        mapName = rasterMapResource.getInfo(null).getTitle();
                        mapsetPath = ((JGrassMapsetGeoResource) rasterMapResource.parent(null))
                                .getFile().getAbsolutePath();
                        if (mapName != null && mapsetPath != null) {
                            String catsPath = mapsetPath + File.separator + JGrassConstants.CATS
                                    + File.separator + mapName;
                            makeSomeCategories(catsPath);
                        }
                    } catch (IOException e) {
                        JGrassrasterStyleActivator.log("JGrassrasterStyleActivator problem", e); //$NON-NLS-1$
                        e.printStackTrace();
                    }
                }
            }
        }
    }
} // @jve:decl-index=0:visual-constraint="10,10"
