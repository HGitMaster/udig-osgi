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
package net.refractions.udig.issues;

import java.util.Iterator;
import java.util.List;

import net.refractions.udig.core.IFixer;
import net.refractions.udig.core.enums.Resolution;
import net.refractions.udig.core.internal.ExtensionPointList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;

/**
 * Generic CheatSheet implementation of IFixer for IIssue objects.
 * 
 * @author chorner
 * @since 1.1.0
 */
public class CheatSheetIssueFixer implements IFixer {
    
    public static final String ATT_ID = "id";  //$NON-NLS-1$
    public static final String KEY_CHEATSHEET = "cheatSheetId"; //$NON-NLS-1$
    public static final String XPID_CHEATSHEET = "org.eclipse.ui.cheatsheets.cheatSheetContent"; //$NON-NLS-1$
    
	public boolean canFix(Object issue, IMemento fixerMemento) {
        if (issue == null || fixerMemento == null) {
            return false;
        }
        if (!(issue instanceof AbstractFixableIssue)) {
            return false;
        }
        String requiredExtension = fixerMemento.getString(KEY_CHEATSHEET);
        if (requiredExtension == null) {
            return false;
        }
        //figure out if the stated CheatSheet exists without instantiating it
        List<IConfigurationElement> extensionPointList = ExtensionPointList
                .getExtensionPointList(XPID_CHEATSHEET);
        Iterator<IConfigurationElement> it = extensionPointList.iterator();
        while( it.hasNext() ) {
            IConfigurationElement element = it.next();
            String id = element.getAttribute(ATT_ID);
            if (id != null && id.equalsIgnoreCase(requiredExtension)) {
                return true;
            }
        }

        return false;
	}

	public void fix(Object issue, IMemento fixerMemento) {
        //mark as in progress
        ((IIssue) issue).setResolution(Resolution.IN_PROGRESS);
        //instantiate the cheat sheet
        final String requiredExtension = fixerMemento.getString(KEY_CHEATSHEET);
        OpenCheatSheetAction openAction = new OpenCheatSheetAction(requiredExtension); //, "", null);
        openAction.run();

//TODO: investigate alternative (custom cheat sheet XML)
//        URL url = new URL(null, null, new URLStreamHandler()  {
//
//            @Override
//            protected URLConnection openConnection( URL arg0 ) throws IOException {
//                URLConnection conn = new URLConnection(arg0) {
//
//                    @Override
//                    public void connect() throws IOException {
//                    }
//                  
//                    @Override 
//                    public InputStream getInputStream() throws IOException {
//                        ByteArrayInputStream in=new ByteArrayInputStream(cheatSheetXML.getBytes());
//                        return in;
//                    }
//                    
//                };
//                return conn;
//            }
//            
//        });

	}
    
    public void complete(Object issue) {
        ((IIssue) issue).setResolution(Resolution.RESOLVED);
    }

}
