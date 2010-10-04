/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.skype.im.transport.callables;

import com.skype.SkypeException;
import com.skype.SkypeImpl;
import com.skype.User;
import com.skype.User.BuddyStatus;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.remoting.Callable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jbh
 */
public class SkypeVerifyUserCallable implements Callable<String, SkypeIMException> {

    private String[] skypeNames = null;

    public SkypeVerifyUserCallable(String[] names) {
        this.skypeNames = names;
    }

    public String call() throws SkypeIMException {
        StringBuilder res = new StringBuilder();
        for (String skypeId : skypeNames) {
            User usr = SkypeImpl.getUser(skypeId);
            
                try {
                    if (usr != null && usr.getFullName() != null && !usr.getFullName().isEmpty()) {
                        if (!usr.isAuthorized()) {
                            usr.setAuthorized(true);
                        }
                        System.out.println("BDY ("+usr.getDisplayName()+"):'" + usr.getBuddyStatus() + "' :'" + BuddyStatus.ADDED + "'");
                        if (!usr.getBuddyStatus().equals(BuddyStatus.ADDED)) {
                            try {
                                SkypeImpl.getContactList().addFriend(usr, "The Skype Service on " + InetAddress.getLocalHost().getHostName() + " wants to notify you");
                            } catch (UnknownHostException ex) {
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                                throw new SkypeIMException(ex);
                            }
                        }
                    } else {
                        res.append("Could not find ").append(skypeId);
                    }
                } catch (SkypeException ex) {
                    throw new SkypeIMException(ex);
                }

            
        }
        return res.toString();
    }
}
