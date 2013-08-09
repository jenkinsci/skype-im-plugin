package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.SkypeException;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessage;
import hudson.plugins.im.bot.Bot;
import hudson.plugins.skype.im.transport.SkypeChat;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.remoting.Callable;
import hudson.remoting.Channel;

/**
 *
 * @author jbh
 */
public class BotCommandCallable implements Callable<Boolean, SkypeIMException> {
    
    Chat chat = null;    
    String senderId = null;
    String toId = null;
    String content = null;
    public BotCommandCallable(Chat chat, ChatMessage msg) throws SkypeIMException {
        this.chat = chat;
        try {
            senderId = msg.getSenderId();
            toId = msg.getId();
            content = msg.getContent();
        } catch (SkypeException e) {
            throw new SkypeIMException(e);
        }
    }

    public Boolean call() throws SkypeIMException {
        SkypeChat skypeChat = new SkypeChat(chat) {

            @Override
            public void sendMessage(String msg) throws IMException {
                try {
                    SkypeChatCallable sender = new SkypeChatCallable(new String[]{senderId}, msg);
                    Channel.current().call(sender);                    
                    
                } catch (Exception ex) {
                    throw new IMException(ex);
                }
            }            
        };
        Bot bot = new Bot(skypeChat, "hudson",
                "hostname", "!", null);


        if (content != null) {
            // replay original message:

            bot.onMessage(new IMMessage(senderId, toId, content));//Ask skype

        }
        return Boolean.TRUE;
    }
}
