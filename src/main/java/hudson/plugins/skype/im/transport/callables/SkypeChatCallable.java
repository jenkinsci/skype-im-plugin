/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
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
public class SkypeChatCallable implements Callable<ChatMessage, SkypeIMException> {
    private String[] skypeNames = null;
    private String message = null;

    public SkypeChatCallable(String[] names, String msg) {
        this.skypeNames = names;
        this.message = msg;

    }
    public ChatMessage call() throws SkypeIMException {
        try {
            Chat chat = SkypeImpl.chat(skypeNames);
            return chat.send(message);
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
    }

}
