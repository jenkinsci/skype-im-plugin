package hudson.plugins.skype.im.transport.callables;

import com.skype.Profile;
import com.skype.SkypeException;
import com.skype.Skype;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.remoting.Callable;

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
                Skype.getProfile().setStatus(status);
            }
            if (mood != null) {
                Skype.getProfile().setMoodMessage(mood);
            }
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
        return null;
    }
}
