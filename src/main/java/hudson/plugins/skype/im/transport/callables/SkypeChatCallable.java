package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.SkypeException;
import com.skype.Skype;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.remoting.Callable;

/**
 *
 * @author jbh
 */
public class SkypeChatCallable implements Callable<ChatMessage, SkypeIMException> {
    private String[] skypeNames = null;
    protected String message = null;

    public SkypeChatCallable(String[] names, String msg) {
        this.skypeNames = names;
        this.message = msg;

    }
    public ChatMessage call() throws SkypeIMException {
        try {
            Chat chat = Skype.chat(skypeNames);
            return chat.send(message);
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
    }

}
