package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.Skype;
import com.skype.SkypeException;
import hudson.plugins.im.IMException;
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
public class BotCommandCallable implements Callable<Boolean, SkypeIMException>, Serializable {
    private SkypeChat skypeChat = null;
    private String senderId = null;
    private String toId = null;
    private String content = null;

    public BotCommandCallable(Chat chat, ChatMessage msg) throws SkypeIMException {
        skypeChat = new RemoteSkypeChat(chat, msg);

        try {
            senderId = msg.getSenderId();
            toId = msg.getId();
            content = msg.getContent();
        } catch (SkypeException e) {
            throw new SkypeIMException(e);
        }
    }

    public Boolean call() throws SkypeIMException {
        Bot bot = new Bot(skypeChat, "hudson", "hostname", "!", null);
        if (content != null) {
            // replay original message:
            bot.onMessage(new IMMessage(senderId, toId, content));//Ask skype
        }

        return Boolean.TRUE;
    }
}
