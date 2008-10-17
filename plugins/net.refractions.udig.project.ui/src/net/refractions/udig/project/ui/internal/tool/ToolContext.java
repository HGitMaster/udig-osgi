/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.project.ui.internal.tool;

import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.command.factory.NavigationCommandFactory;
import net.refractions.udig.project.command.factory.SelectionCommandFactory;
import net.refractions.udig.project.internal.AbstractContext;
import net.refractions.udig.project.ui.commands.DrawCommandFactory;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;

/**
 * Context used by a tool to access the model and issue commands.
 * <p>
 * In addition to the references available in the Toolkit class, ToolContext provides access to
 * command factories and and to sendCommand methods.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Provide access to the objects that an extension can use for its operations.</li>
 * <li>Provide convenience methods for extension developers to use.</li>
 * <li>Provide a consistent interface for extensions which will not easily change in future
 * versions</li>
 * </ul>
 * </p>
 * 
 * @author Jesse
 * @since 0.5
 */
public interface ToolContext extends IToolContext, AbstractContext {
    /**
     * Casts getDisplay to ViewportPane;
     * 
     * @return getDisplay cast to ViewportPane
     */
    ViewportPane getViewportPane();
    /**
     * Returns a DrawCommandFactory
     * 
     * @return a DrawCommandFactory
     */
    DrawCommandFactory getDrawFactory();
    /**
     * Returns a EditCommandFactory
     * 
     * @return a EditCommandFactory
     */
    EditCommandFactory getEditFactory();

    /**
     * Returns a NavigationCommandFactory
     * 
     * @return a NavigationCommandFactory
     * @deprecated Please use navigation commands directly
     */
    NavigationCommandFactory getNavigationFactory();
    /**
     * Returns the current workbench.
     * <p>
     * Convenience for PlatformUI.getWorkbench()
     * 
     * @return the current workbench.
     */
    IWorkbench getWorkbench();
    /**
     * Returns the default display.
     * <p>
     * Convenience for Display.getDefault()
     * </p>
     * 
     * @return the default display.
     */
    Display getDisplay();
    /**
     * Logs an exception to the current plugin.
     * 
     * @param currentPlugin the plugin that the exception will be logged in.
     * @param message the message to log
     * @param severity the severity of the exception. IF null ERROR will be assumed.
     *        {@linkplain org.eclipse.core.runtime.IStatus#ERROR},
     *        {@linkplain org.eclipse.core.runtime.IStatus#INFO},
     *        {@linkplain org.eclipse.core.runtime.IStatus#WARNING}
     * @param exception the exception to log. Can be null.
     */
    void log( Plugin currentPlugin, String message, int severity, Throwable exception );
    /**
     * Returns a SelectionCommandFactory
     * 
     * @return a SelectionCommandFactory�
     */
    SelectionCommandFactory getSelectionFactory();
    public ToolContext copy();
 }