package hudson.plugins.skype.im.transport;

import com.skype.ChatMessage;
import com.skype.SkypeException;
import hudson.plugins.im.IMMessage;


public class SkypeMessage extends IMMessage {

    public SkypeMessage(ChatMessage msg, boolean authorized) throws SkypeException {
        super(msg.getSenderId(), msg.getId(), msg.getContent(), authorized);
    }
}
