/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hudson.plugins.skype.im.transport;

import com.skype.SkypeException;
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
