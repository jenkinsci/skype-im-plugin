package hudson.plugins.skype.im.transport;

import hudson.plugins.im.IMException;

/**
 *
 * @author jbh
 */
public class SkypeIMException extends IMException {
    public SkypeIMException(Exception cause) {
        super(cause);
    }
    public SkypeIMException(String msg) {
        super(msg);
    }
    
}
