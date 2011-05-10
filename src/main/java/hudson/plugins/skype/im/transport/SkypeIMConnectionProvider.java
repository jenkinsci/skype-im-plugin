package hudson.plugins.skype.im.transport;

import hudson.plugins.im.IMConnection;
import hudson.plugins.im.IMConnectionProvider;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMPublisherDescriptor;

/**
 * Jabber implementation of an {@link IMConnectionProvider}.
 * 
 * @author Uwe Schaefer
 * @author kutzi
 */
final class SkypeIMConnectionProvider extends IMConnectionProvider
{
     private SkypeIMConnectionProvider() {
    	super();
       
    	
    }
    private static final SkypeIMConnectionProvider INSTANCE = new SkypeIMConnectionProvider();
    
    static SkypeIMConnectionProvider getInstance() {
        return INSTANCE;
    }
    
    static void setDesc(IMPublisherDescriptor desc) throws IMException {
    	synchronized(INSTANCE) {
            INSTANCE.setDescriptor(desc);                  
        }        
    	
    }
    @Override
    public void setDescriptor(IMPublisherDescriptor desc) {
        super.setDescriptor(desc);
        init();
    }

    @Override
    public IMConnection createConnection() throws IMException {       
        synchronized(INSTANCE) {
            if (getDescriptor() == null) {
        	throw new RuntimeException  ("No descriptor");
            }
            IMConnection imConnection = new SkypeIMConnection((SkypePublisherDescriptor)getDescriptor(),
                            null);
            if (imConnection.connect()) {
                    return imConnection;
            }
        }
        throw new IMException("Connection failed");
        
    }
   
}
