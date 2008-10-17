package net.refractions.udig.internal.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

public class UDigByteAndLocalTransfer extends ByteArrayTransfer implements UDIGTransfer{
	private static UDigByteAndLocalTransfer _instance = new UDigByteAndLocalTransfer();

	static final String CFSTR_INETURL = "UniformResourceLocator"; //$NON-NLS-1$

	private static final int CFSTR_INETURLID = Transfer
			.registerType(CFSTR_INETURL);

	private long startTime;

	public Object object;

    @Override
    public boolean isSupportedType( TransferData transferData ) {
        return super.isSupportedType(transferData);
    }

	public UDigByteAndLocalTransfer() {

		// do nothing.
	}

	public static UDigByteAndLocalTransfer getInstance() {

		return _instance;
	}

	protected int[] getTypeIds() {

		return new int[] { CFSTR_INETURLID };
	}

	public String[] getTypeNames() {

		return new String[] { CFSTR_INETURL };
	}

	@Override
	public TransferData[] getSupportedTypes() {
		return super.getSupportedTypes();
	}
	
	@SuppressWarnings("unchecked") 
	@Override
	public void javaToNative(Object object, TransferData transferData) {

	    startTime = System.currentTimeMillis();
	    if( object instanceof IStructuredSelection){
	    	IStructuredSelection selection=(IStructuredSelection) object;
	    	List<Object> elements=new ArrayList<Object>();
	    	for (Iterator<Object> iter = selection.iterator(); iter.hasNext();) {
				elements.add(iter.next());
			}
	    	this.object=elements.toArray();
	    }
	    this.object = object;
	    if (transferData != null)
	    {
	      super.javaToNative(String.valueOf(startTime).getBytes(), transferData);
	    }
	}

	/**
	 * This implementation of <code>nativeToJava</code> converts a platform
	 * specific representation of a URL and optionally, a title to a java
	 * <code>String[]</code>. For additional information see
	 * <code>Transfer#nativeToJava</code>.
	 * 
	 * @param transferData
	 *            the platform specific representation of the data to be been
	 *            converted
	 * @return a java <code>String[]</code> containing a URL and optionally a
	 *         title if the conversion was successful; otherwise null
	 */
    public Object nativeToJava(TransferData transferData) {
        
        byte[] bytes = (byte[])super.nativeToJava(transferData); 
        if (bytes == null) return null;
        
        try
        {
          long startTime = Long.valueOf(new String(bytes)).longValue();
          return this.startTime == startTime ? object : null;
        }
        catch (NumberFormatException exception)
        {
            InputStreamReader reader = new InputStreamReader(
                    new ByteArrayInputStream(bytes));
            StringBuffer buf = new StringBuffer();
            char[] chars = new char[bytes.length / 2];
            int read=0;
            try {
                read=reader.read(chars);
            } catch (IOException e) {
                UiPlugin.log("Error reading transfer data", e); //$NON-NLS-1$
            }
            buf.append(chars,0,read);
            return buf.toString().trim();
        }

		
	}

	public boolean validate(Object object) {
		return true;
	}
}