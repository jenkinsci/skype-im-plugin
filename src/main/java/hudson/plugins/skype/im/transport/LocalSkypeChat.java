package hudson.plugins.skype.im.transport;

import com.skype.Chat;
import com.skype.Chat.Status;
import com.skype.SkypeException;
import hudson.plugins.im.IMException;

/**
 * 
 * @author Hans-Joachim Kliemeck <klk@mmw.ag>
 */
public class LocalSkypeChat extends SkypeChat {

    private final Chat chat;

    public LocalSkypeChat(Chat chat) {
        this.chat = chat;
    }

    public void sendMessage(String msg) throws IMException {
        try {
            this.chat.send(msg);
        } catch (SkypeException e) {
            throw new IMException(e);
        }
    }

    public boolean isMultiUserChat() {
        try {
            return chat.getStatus().equals(Status.MULTI_SUBSCRIBED);
        } catch (SkypeException ex) {
            throw new RuntimeException(ex);
        }
    }
}
