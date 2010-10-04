package hudson.plugins.skype.im.transport;

import com.skype.Chat;
import com.skype.SkypeImpl;
import com.skype.SkypeException;
import hudson.plugins.im.IMChat;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessageListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 1-on-1 Jabber chat.
 * 
 * @author kutzi
 */
public class SkypeChat implements IMChat {
    private final Chat chat;
    
    private SkypeMessageListenerAdapter messageListener;

    public SkypeChat(Chat chat) {
        this.chat = chat;
        
    }

    public void sendMessage(String msg) throws IMException {
        try {
            this.chat.send(msg);
        } catch (SkypeException e) {
            throw new IMException(e);
        }
    }

    public String getNickName(String sender) {
    	return sender;
    }
    
    public void addMessageListener(IMMessageListener listener) {
        this.messageListener = new SkypeMessageListenerAdapter(listener);
        try {
            SkypeImpl.addChatMessageListener(messageListener);
        } catch (SkypeException ex) {
            Logger.getLogger(SkypeChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeMessageListener(IMMessageListener listener) {
		// doesn't work out-of the box with Smack
    	
            SkypeImpl.removeChatMessageListener(messageListener);
        
	}

	public boolean isMultiUserChat() {
        return false;
    }

    public String getIMId(String user) {
        return user;
    }
}
