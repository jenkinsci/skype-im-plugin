package hudson.plugins.skype.im.transport;

import hudson.plugins.im.IMChat;
import hudson.plugins.im.IMMessageListener;

/**
 * 
 * @author kutzi
 */
public abstract class SkypeChat implements IMChat {

    public String getNickName(String sender) {
        return sender;
    }

    public void addMessageListener(IMMessageListener listener) {
        //this.messageListener = new SkypeMessageListenerAdapter(listener);
        //try {
        //    SkypeImpl.addChatMessageListener(messageListener);
        //} catch (SkypeException ex) {
        //    Logger.getLogger(SkypeChat.class.getName()).log(Level.SEVERE, null, ex);
       // }
    }

    public void removeMessageListener(IMMessageListener listener) {
        // doesn't work out-of the box with Smack

        //SkypeImpl.removeChatMessageListener(messageListener);

    }

    public String getIMId(String user) {
        return user;
    }

    public boolean isCommandsAccepted() {
        return true;
    }
}
