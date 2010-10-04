/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.skype.im.transport.callables;

import com.skype.Profile;
import com.skype.SkypeException;
import com.skype.SkypeImpl;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.remoting.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jbh
 */
public class SkypeMoodCallable implements Callable<Object, SkypeIMException> {

    private String mood;
    private Profile.Status status;

    public SkypeMoodCallable(String mood, Profile.Status status) {
        this.mood = mood;
        this.status = status;
    }

    public Object call() throws SkypeIMException {
        try {
            if (status != null) {
                SkypeImpl.getProfile().setStatus(status);
            }
            if (mood != null) {
                SkypeImpl.getProfile().setMoodMessage(mood);
            }
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
        return null;
    }
}
