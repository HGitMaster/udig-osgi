package net.refractions.udig.catalog;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

public class URLUtilsTest extends TestCase {

    public void testPrefix() throws Exception {
        File file =new File("C:\\foo\\bar");
        assertEquals( "C", "C:", URLUtils.getPrefix( file ) );
        
        file =new File("D:\\foo\\bar");
        assertEquals( "D", "D:", URLUtils.getPrefix( file ) );
        
        file = new File( "foo.bar" );
        String prefix = URLUtils.getPrefix( file );
        assertNull( "none", prefix );
        
        file = new File( "\\\\machine\\share\\foo.txt" );
        prefix = URLUtils.getPrefix( file );
        assertEquals( "\\\\machine\\share", prefix );
        
    }
    
	public void testToRelativePath() throws Exception {
		URL url=new File("C:\\foo\\bar").toURI().toURL(); //$NON-NLS-1$
		File reference = new File( "C:/foo/bork/dooda" ); //$NON-NLS-1$
		
		URL result = URLUtils.toRelativePath(reference, url);
		assertEquals( "file:/../bar", result.toString()); //$NON-NLS-1$
		
		url=new File( "C:/foo/bork/dooda" ).toURI().toURL(); //$NON-NLS-1$
		result = URLUtils.toRelativePath(reference, url);
		assertEquals( "file:/./", result.toString()); //$NON-NLS-1$

		url=new File( "C:/foo/bork/BLEEP" ).toURI().toURL(); //$NON-NLS-1$
		result = URLUtils.toRelativePath(reference, url);
		assertEquals( "file:/BLEEP", result.toString()); //$NON-NLS-1$
		

		url=new URL("http://someurl.com"); //$NON-NLS-1$
		result = URLUtils.toRelativePath(reference, url);
		assertSame(url, result);
		
		url=new URL("file://C:/foo/bar#hi"); //$NON-NLS-1$
		result = URLUtils.toRelativePath(reference, url);
		assertSame(url, result);
		
		try{
			url = new URL("file:/C:/Users/Jody/Desktop/raster/norway/trond50geor.jpg");
			reference = new File("C:\\java\\udig\\runtime-udig.product\\.localCatalog");
			result = URLUtils.toRelativePath( reference, url);
			fail("we do not allow this right now");
		}catch (Exception e) {
			// expected
		}
		
	}

	public void testConstructURL() throws Exception {
		URL expected=new File("C:/foo/bar").toURL(); //$NON-NLS-1$
		File reference = new File( "C:/foo/bork/dooda" ); //$NON-NLS-1$
		        
	    URL result;
	    
	    result = URLUtils.constructURL(reference, "file:/../bar"); //$NON-NLS-1$
		assertEquals( expected.toString(), result.toString());
		
		expected=new File( "C:/foo/bork" ).toURL(); //$NON-NLS-1$
		result = URLUtils.constructURL(reference, "file:/./"); //$NON-NLS-1$
		assertEquals( expected.toString() , result.toString());

		expected=new File( "C:/foo/bork/BLEEP" ).toURL(); //$NON-NLS-1$
		result = URLUtils.constructURL(reference, "file:/BLEEP"); //$NON-NLS-1$
		assertEquals( expected.toString(), result.toString());
		

		expected=new URL("http://someurl.com"); //$NON-NLS-1$
		result = URLUtils.constructURL(reference, "http://someurl.com"); //$NON-NLS-1$
		assertEquals(expected.toString(), result.toString());

		expected=new URL("file:/C:/foo/bar#hi"); //$NON-NLS-1$
		result = URLUtils.constructURL(reference, "file:/C:/foo/bar#hi"); //$NON-NLS-1$
		assertEquals(expected.toString(), result.toString());

		expected=new URL("file:/D:/foo/bar#hi"); //$NON-NLS-1$
		result = URLUtils.constructURL(reference, "file:/D:/foo/bar#hi"); //$NON-NLS-1$
		assertEquals(expected.toString(), result.toString());
		
		File file = new File("."); //$NON-NLS-1$
		expected = new URL("file:/"+file.getCanonicalPath().replace('\\', '/')); //$NON-NLS-1$
		result = URLUtils.constructURL(reference, "file:/"+file.getCanonicalPath().replace('\\', '/')); //$NON-NLS-1$
		assertEquals(expected.toString(), result.toString());
		
		file = new File("E:/Rahul/d a t a/uDigSampleData/bc_border.shp");
        result = URLUtils.fileToURL( file );
        File file2 = URLUtils.urlToFile( result );
        assertEquals( file, file2 );
	}

}
