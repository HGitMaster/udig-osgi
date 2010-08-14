/**
 * 
 */
package net.refractions.udig.libs.tests;

import static org.junit.Assert.*;

import net.refractions.udig.libs.internal.Activator;

import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;

/**
 * This JUnit Plugin Test verifies that GeoTools and other library dependencies
 * are correctly functioning in an OSGi environment.
 * 
 * @author Jody Garnett
 * @since 1.2.0
 */
public class LibsTest extends GeoToolsTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Bundle bundle = Platform.getBundle( Activator.ID );
        if( bundle == null ){
            throw new IllegalStateException("Please run as a JUnit Plug-in Test");
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

}
