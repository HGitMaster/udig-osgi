/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This
 * library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.catalog.internal.ui;

import net.refractions.udig.catalog.ui.CatalogUIPlugin;

/**
 * Here's how to reference the help context in code: WorkbenchHelp.setHelp(actionOrControl,
 * HelpContextIds.NAME_DEFIED_BELOW);
 */
public interface IHelpContextIds {

    /** <code>PREFIX</code> field */
    static final String PREFIX = CatalogUIPlugin.ID + "."; //$NON-NLS-1$

    //
    // Dialogs
    //
    /** <code>DATASTORE_CONNECTION</code> field */
    public static final String DATASTORE_CONNECTION = PREFIX + "datastore_connection"; //$NON-NLS-1$

    /** <code>EXPORT_FEATURETYPE_SELECTION_DIALOG</code> field */
    // Different uses of the FeatureTypeSelectionDialog
    public static final String EXPORT_FEATURETYPE_SELECTION_DIALOG = PREFIX
            + "export_featuretype_selection_dialog_context"; //$NON-NLS-1$

    /** <code>MAP_FEATURETYPE_SELECTION_DIALOG</code> field */
    public static final String MAP_FEATURETYPE_SELECTION_DIALOG = PREFIX
            + "map_featuretype_selection_dialog_context"; //$NON-NLS-1$

    /** <code>DATASOURCE_WIZARD_SELECTION_PAGE</code> field */

    //
    // Wizard Pages
    //
    public static final String DATASOURCE_WIZARD_SELECTION_PAGE = PREFIX
            + "datasource_wizard_selection_page_context"; //$NON-NLS-1$

    /** <code>DATASTORE_FACTORY_SELECTION_PAGE</code> field */
    public static final String DATASTORE_FACTORY_SELECTION_PAGE = PREFIX
            + "datastore_factory_selection_page_context"; //$NON-NLS-1$

    /** <code>DATASTORE_FACTORY_PARAM_PAGE</code> field */
    public static final String DATASTORE_FACTORY_PARAM_PAGE = PREFIX
            + "datastore_factory_param_page_context"; //$NON-NLS-1$

    /** <code>PREF_DETERMINE_SERVER_VERSION</code> field */
    // Preference Pages
    public static final String PREF_DETERMINE_SERVER_VERSION = PREFIX + "determine_server_version"; //$NON-NLS-1$

    //
    // Local Registry View
    //
    /** <code>CATALOG_VIEW</code> field */
    public static final String CATALOG_VIEW = PREFIX + "catalog_view_context"; //$NON-NLS-1$

    /** <code>ADD_DATASOURCE_ACTION</code> field */
    // Actions
    public static final String IMPORT_SERVICE_ACTION = PREFIX + "import_service_action_context"; //$NON-NLS-1$

    /** <code>REMOVE_DATASOURCE_ACTION</code> field */
    public static final String REMOVE_SERVICE_ACTION = PREFIX + "remove_service_action_context"; //$NON-NLS-1$

    /** <code>REFRESH_ACTION</code> field */
    // Viewmenu actions
    public static final String REFRESH_ACTION = PREFIX + "refresh_action_context"; //$NON-NLS-1$

    /** <code>FILE_PROPERTY_PAGE</code> field */
    // properties pages
    public static final String FILE_PROPERTY_PAGE = PREFIX + "file_property_page_context"; //$NON-NLS-1$

    //
    // Search View
    //
    /** <code>SEARCH_VIEW</code> field */
    public static final String SEARCH_VIEW = PREFIX + "search_view_context"; //$NON-NLS-1$

    // Viewers
    /** <code>SEARCH_VIEWER</code> field */
    public static final String SEARCH_VIEWER = PREFIX + "search_viewer_context"; //$NON-NLS-1$

    /** <code>SEARCH_FIELD</code> field */
    public static final String SEARCH_FIELD = PREFIX + "search_field_context"; //$NON-NLS-1$

    /** <code>SEARCH_BBOX</code> field */
    public static final String SEARCH_BBOX = PREFIX + "search_bbox_context"; //$NON-NLS-1$

    public static final String CANCEL_SEARCH_ACTION = PREFIX + "search_cancel_context"; //$NON-NLS-1$
}