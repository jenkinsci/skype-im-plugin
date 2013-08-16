package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ContactList;
import com.skype.Skype;
import com.skype.SkypeException;
import hudson.plugins.im.IMMessage;
import hudson.plugins.im.bot.Bot;
import hudson.plugins.skype.im.transport.RemoteSkypeChat;
import hudson.plugins.skype.im.transport.SkypeChat;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.remoting.Callable;
import java.io.Serializable;

/**
 *
 * @author jbh
 */
public class BotCommandCallable implements Callable<Void, SkypeIMException>, Serializable {
    private SkypeChat chat = null;
    private String senderId = null;
    private String toId = null;
    private String content = null;

    public BotCommandCallable(ChatMessage msg, SkypeChat chat) throws SkypeException {
        this.chat = chat;

        senderId = msg.getSenderId();
        toId = msg.getId();
        content = msg.getContent();
    }

    public Void call() throws SkypeIMException {
        boolean isFriend;
        try {
            ContactList contacts = Skype.getContactList();
            isFriend = contacts.getFriend(senderId) != null;
        } catch (SkypeException e) {
            throw new SkypeIMException(e);
        }

        Bot bot = new Bot(chat, "hudson", "hostname", "!", null);
        if (content != null) {
            // replay original message:
            bot.onMessage(new IMMessage(senderId, toId, content, isFriend));
        }

        return null;
    }
}
