package hudson.plugins.skype.im.transport;

import com.skype.ChatMessage;
import com.skype.SkypeException;
import hudson.plugins.im.IMMessage;
import java.io.Serializable;


public class SkypeMessage extends IMMessage implements Serializable {
    public SkypeMessage() {
        super("", "", "");
    }
    public SkypeMessage(ChatMessage msg, boolean authorized) throws SkypeException {
        super(msg.getSenderId(), msg.getId(), msg.getContent(), authorized);
    }
}
