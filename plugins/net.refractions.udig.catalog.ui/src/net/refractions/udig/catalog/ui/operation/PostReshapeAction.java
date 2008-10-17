/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog.ui.operation;

import net.refractions.udig.catalog.IGeoResource;

/**
 * A strategy for performing an action after a reshape operation has taken place.
 * 
 * @author jesse
 * @since 1.1.0
 */
public interface PostReshapeAction {
    void execute( IGeoResource original, IGeoResource reshaped );
}
