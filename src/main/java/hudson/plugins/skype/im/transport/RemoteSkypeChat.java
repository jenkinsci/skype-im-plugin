package hudson.plugins.skype.im.transport;

import com.skype.Chat.Status;
import com.skype.ChatMessage;
import com.skype.SkypeException;
import hudson.plugins.im.IMException;
import hudson.plugins.skype.im.transport.callables.SkypeChatCallable;
import hudson.remoting.Channel;
import java.io.Serializable;

/**
 * 
 * @author Hans-Joachim Kliemeck <klk@mmw.ag>
 */
public class RemoteSkypeChat extends SkypeChat implements Serializable {
    private String senderId;
    private boolean multiChat;

    public RemoteSkypeChat(ChatMessage msg) throws SkypeException {
        this.senderId = msg.getSenderId();
        this.multiChat = msg.getChat().getStatus().equals(Status.MULTI_SUBSCRIBED);
    }

    public void sendMessage(String msg) throws IMException {
        try {
            SkypeChatCallable sender = new SkypeChatCallable(new String[]{senderId}, msg);
            Channel.current().call(sender);                    
        } catch (Exception ex) {
            throw new IMException(ex);
        }
    }

    public boolean isMultiUserChat() {
        return multiChat;
    }
}
