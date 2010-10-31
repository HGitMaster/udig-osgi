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
package eu.udig.style.advanced.lines;

import static eu.udig.style.advanced.utils.Utilities.sb;

import java.net.MalformedURLException;
import java.util.List;

import net.refractions.udig.style.sld.SLD;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.TextSymbolizer;

import eu.udig.style.advanced.common.BoderParametersComposite;
import eu.udig.style.advanced.common.FiltersComposite;
import eu.udig.style.advanced.common.IStyleChangesListener;
import eu.udig.style.advanced.common.styleattributeclasses.LineSymbolizerWrapper;
import eu.udig.style.advanced.common.styleattributeclasses.RuleWrapper;
import eu.udig.style.advanced.common.styleattributeclasses.TextSymbolizerWrapper;
import eu.udig.style.advanced.lines.widgets.LineGeneralParametersComposite;
import eu.udig.style.advanced.lines.widgets.LineLabelsParametersComposite;
import eu.udig.style.advanced.utils.Utilities;

/**
 * The line properties composite.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class LinePropertiesComposite implements ModifyListener, IStyleChangesListener {

    private RuleWrapper ruleWrapper;

    private Composite simplePolygonComposite = null;
    private LinePropertiesEditor linePropertiesEditor;
    private Composite mainComposite;
    private StackLayout mainStackLayout;

    private String[] numericAttributesArrays;

    private Composite parentComposite;

    private final Composite parent;

    private LineGeneralParametersComposite generalParametersComposite;

    private BoderParametersComposite borderParametersComposite;

    private LineLabelsParametersComposite labelsParametersComposite;

    private String[] allAttributesArrays;

    private FiltersComposite filtersComposite;

    public LinePropertiesComposite( final LinePropertiesEditor polygonPropertiesEditor, Composite parent ) {
        this.linePropertiesEditor = polygonPropertiesEditor;
        this.parent = parent;
    }

    public void setRule( RuleWrapper ruleWrapper ) {
        this.ruleWrapper = ruleWrapper;

        System.out.println("setting rule: " + ruleWrapper.getName());

        if (mainComposite == null) {
            init();
            createSimpleComposite();
        } else {
            update();
        }
        setRightPanel();
    }

    private void update() {
        generalParametersComposite.update(ruleWrapper);
        borderParametersComposite.update(ruleWrapper);
        labelsParametersComposite.update(ruleWrapper);
        filtersComposite.update(ruleWrapper);

        linePropertiesEditor.refreshTreeViewer(ruleWrapper);
        linePropertiesEditor.refreshPreviewCanvasOnStyle();
    }

    private void init() {
        // System.out.println("open: " + rule.getName());

        List<String> numericAttributeNames = linePropertiesEditor.getNumericAttributeNames();
        numericAttributesArrays = (String[]) numericAttributeNames.toArray(new String[numericAttributeNames.size()]);
        List<String> allAttributeNames = linePropertiesEditor.getAllAttributeNames();
        allAttributesArrays = (String[]) allAttributeNames.toArray(new String[allAttributeNames.size()]);
        // geometryPropertyName = linePropertiesEditor.getGeometryPropertyName().getLocalPart();

        parentComposite = new Composite(parent, SWT.NONE);
        parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parentComposite.setLayout(new GridLayout(1, false));

        mainComposite = new Composite(parentComposite, SWT.NONE);
        mainStackLayout = new StackLayout();
        mainComposite.setLayout(mainStackLayout);
        GridData mainCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComposite.setLayoutData(mainCompositeGD);
        // mainStackLayout.topControl = l;
    }

    private void setRightPanel() {
        mainStackLayout.topControl = simplePolygonComposite;
        mainComposite.layout();
    }

    public Composite getComposite() {
        return parentComposite;
    }

    private void createSimpleComposite() {
        simplePolygonComposite = new Composite(mainComposite, SWT.None);
        simplePolygonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        simplePolygonComposite.setLayout(new GridLayout(1, false));

        // rule name
        Composite nameComposite = new Composite(simplePolygonComposite, SWT.NONE);
        nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nameComposite.setLayout(new GridLayout(2, true));

        // use an expandbar for the properties
        Group propertiesGroup = new Group(simplePolygonComposite, SWT.SHADOW_ETCHED_IN);
        propertiesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        propertiesGroup.setLayout(new GridLayout(1, false));
        propertiesGroup.setText("Style Properties");

        TabFolder tabFolder = new TabFolder(propertiesGroup, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        generalParametersComposite = new LineGeneralParametersComposite(tabFolder, numericAttributesArrays);
        generalParametersComposite.init(ruleWrapper);
        generalParametersComposite.addListener(this);
        Composite generalParametersInternalComposite = generalParametersComposite.getComposite();

        TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
        tabItem1.setText("General");
        tabItem1.setControl(generalParametersInternalComposite);

        // BORDER GROUP
        borderParametersComposite = new BoderParametersComposite(tabFolder, numericAttributesArrays);
        borderParametersComposite.init(ruleWrapper);
        borderParametersComposite.addListener(this);
        Composite borderParametersInternalComposite = borderParametersComposite.getComposite();

        TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
        tabItem2.setText("Border  ");
        tabItem2.setControl(borderParametersInternalComposite);

        // Label GROUP
        labelsParametersComposite = new LineLabelsParametersComposite(tabFolder, numericAttributesArrays, allAttributesArrays);
        labelsParametersComposite.init(ruleWrapper);
        labelsParametersComposite.addListener(this);
        Composite labelParametersInternalComposite = labelsParametersComposite.getComposite();

        TabItem tabItem3 = new TabItem(tabFolder, SWT.NULL);
        tabItem3.setText("Labels  ");
        tabItem3.setControl(labelParametersInternalComposite);

        // Filter GROUP
        filtersComposite = new FiltersComposite(tabFolder);
        filtersComposite.init(ruleWrapper);
        filtersComposite.addListener(this);
        Composite filtersInternalComposite = filtersComposite.getComposite();

        TabItem tabItem4 = new TabItem(tabFolder, SWT.NULL);
        tabItem4.setText("Filter  ");
        tabItem4.setControl(filtersInternalComposite);

    }

    public void modifyText( ModifyEvent e ) {
        // Object source = e.getSource();
        // if (source.equals(graphicsPathText)) {
        // try {
        // String text = graphicsPathText.getText();
        // ruleWrapper.getGeometrySymbolizersWrapper().setStrokeExternalGraphicStrokePath(text);
        // } catch (MalformedURLException e1) {
        // e1.printStackTrace();
        // return;
        // }
        // }
        // linePropertiesEditor.refreshTreeViewer(ruleWrapper);
        // linePropertiesEditor.refreshPreviewCanvasOnStyle();
    }

    public void onStyleChanged( Object source, String[] values, boolean fromField, STYLEEVENTTYPE styleEventType ) {
        String value = values[0];

        LineSymbolizerWrapper lineSymbolizerWrapper = ruleWrapper.getGeometrySymbolizersWrapper().adapt(
                LineSymbolizerWrapper.class);

        TextSymbolizerWrapper textSymbolizerWrapper = ruleWrapper.getTextSymbolizersWrapper();

        switch( styleEventType ) {
        // GENERAL PARAMETERS
        case NAME:
            ruleWrapper.setName(value);
            break;
        case OFFSET:
            lineSymbolizerWrapper.setOffset(value);
            break;
        case MAXSCALE:
            ruleWrapper.setMaxScale(value);
            break;
        case MINSCALE:
            ruleWrapper.setMinScale(value);
            break;
        // BORDER PARAMETERS
        case BORDERENABLE: {
            boolean enabled = Boolean.parseBoolean(value);
            lineSymbolizerWrapper.setHasStroke(enabled);
            break;
        }
        case BORDERWIDTH: {
            lineSymbolizerWrapper.setStrokeWidth(value, fromField);
            break;
        }
        case BORDERCOLOR: {
            lineSymbolizerWrapper.setStrokeColor(value);
            break;
        }
        case BORDEROPACITY: {
            lineSymbolizerWrapper.setStrokeOpacity(value, fromField);
            break;
        }
        case GRAPHICSPATHBORDER: {
            String url = values[0];
            String width = values[1];
            String size = values[2];

            try {
                lineSymbolizerWrapper.setStrokeExternalGraphicStrokePath(url);
                Graphic graphicStroke = lineSymbolizerWrapper.getStrokeGraphicStroke();
                graphicStroke.setSize(Utilities.ff.literal(size));
                graphicStroke.setGap(Utilities.ff.literal(width));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            break;
        }
        case DASH: {
            lineSymbolizerWrapper.setDash(value);
            break;
        }
        case DASHOFFSET: {
            lineSymbolizerWrapper.setDashOffset(value);
            break;
        }
        case LINECAP: {
            lineSymbolizerWrapper.setLineCap(value);
            break;
        }
        case LINEJOIN: {
            lineSymbolizerWrapper.setLineJoin(value);
            break;
        }
            // LABEL PARAMETERS
        case LABELENABLE: {
            boolean doEnable = Boolean.parseBoolean(value);
            if (doEnable) {
                if (textSymbolizerWrapper == null) {
                    TextSymbolizer textSymbolizer = Utilities.createDefaultTextSymbolizer(SLD.LINE);
                    ruleWrapper.addSymbolizer(textSymbolizer, TextSymbolizerWrapper.class);
                    labelsParametersComposite.update(ruleWrapper);
                }
            } else {
                ruleWrapper.removeTextSymbolizersWrapper();
            }
            break;
        }
        case LABEL: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setLabelName(value, fromField);
            break;
        }
        case LABELFONT: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            String name = values[0];
            int style = Integer.parseInt(values[1]);
            int height = Integer.parseInt(values[2]);
            Font font = sb.createFont(name, style == SWT.ITALIC, style == SWT.BOLD, height);

            textSymbolizerWrapper.setFont(font);
            break;
        }
        case LABELCOLOR: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setColor(value);
            break;
        }
        case LABELHALOCOLOR: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setHaloColor(value);
            break;
        }
        case LABELHALORADIUS: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setHaloRadius(value);
            break;
        }
        case LABELINITIALGAP: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setInitialGap(value);
            break;
        }
        case LABELPERPENDICULAROFFSET: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setPerpendicularOffset(value);
            break;
        }
        case LABELROTATION: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setRotation(value, fromField);
            break;
        }
        case LABELMAXDISPLACEMENT_VO: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setMaxDisplacementVO(value);
            break;
        }
        case LABELREPEAT_VO: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setRepeatVO(value);
            break;
        }
        case LABELAUTOWRAP_VO: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setAutoWrapVO(value);
            break;
        }
        case LABELSPACEAROUND_VO: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setSpaceAroundVO(value);
            break;
        }
        case LABELFOLLOWLINE_VO: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setFollowLineVO(value);
            break;
        }
        case LABELMAXANGLEDELTA_VO: {
            if (textSymbolizerWrapper == null) {
                break;
            }
            textSymbolizerWrapper.setMaxAngleDeltaVO(value);
            break;
        }
        case FILTER: {
            if (value.length() > 0) {
                try {
                    ruleWrapper.setFilter(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }

        default:
            break;
        }

        linePropertiesEditor.refreshTreeViewer(ruleWrapper);
        linePropertiesEditor.refreshPreviewCanvasOnStyle();

    }
}
