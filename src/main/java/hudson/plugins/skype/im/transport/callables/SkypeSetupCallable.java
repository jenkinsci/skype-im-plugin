/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatMessageListener;
import com.skype.SkypeException;
import com.skype.SkypeImpl;
import hudson.plugins.im.bot.Bot;
import hudson.plugins.skype.im.transport.SkypeChat;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.plugins.skype.im.transport.SkypeMessage;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jbh
 */
public class SkypeSetupCallable implements Callable<Boolean, SkypeIMException> {

    static Collection<String> supportedArchs = null;

    static {
        supportedArchs = new ArrayList<String>();
        supportedArchs.add("x86");
        supportedArchs.add("i386");
        supportedArchs.add("i586");
    }

    public Boolean call() throws SkypeIMException {
        try {
            if (!supportedArchs.contains(System.getProperty("os.arch"))) {
                throw new RuntimeException("Cannot use skype server on a 64 bit jvm (" + System.getProperty("os.arch") + ")");
            }
            if (!SkypeImpl.isInstalled()) {
                throw new RuntimeException("Skype not installed.");
            }
            if (!SkypeImpl.isRunning()) {
                //throw new RuntimeException("Skype is not running.");
                System.err.println("Skype is probably not running");
            }
            SkypeImpl.setDebug(true);
            SkypeImpl.setDaemon(true);
            addSkypeListener(Channel.current());
            return true;
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
    }

    private void addSkypeListener(Channel channel) throws SkypeException {
        final IMListener listener = new SkypeSetupCallable.IMListener(channel);
        SkypeImpl.addChatMessageListener(listener);
        if (channel != null) {
            channel.addListener(new Channel.Listener() {
                @Override
                public void onClosed(Channel channel, IOException cause) {
                    SkypeImpl.removeChatMessageListener(listener);
                    System.err.println("Removed skype listener");
                }
            });
        }
    }

    private final class IMListener implements ChatMessageListener {

        Channel masterChannel = null;

        public IMListener(Channel channel) {
            masterChannel = channel;

        }

        public void chatMessageReceived(ChatMessage receivedChatMessage) throws SkypeException {
            if (receivedChatMessage.getType().equals(ChatMessage.Type.SAID)) {
                Logger.getLogger(this.getClass().getName()).info("Message from " + receivedChatMessage.getSenderDisplayName() + " : " + receivedChatMessage.getContent());

                final String chatPartner = receivedChatMessage.getSenderId();
                getChat(chatPartner, receivedChatMessage);
            }
        }

        public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException {
        }

        private void getChat(String chatPartner, ChatMessage receivedChatMessage) {
            final Chat chat;
            try {
                chat = receivedChatMessage.getChat();
                if (masterChannel != null) {
                    masterChannel.call(new BotCommandCallable(chat, receivedChatMessage));
                } else {
                    SkypeChat skypeChat = new SkypeChat(chat);
                    Bot bot = new Bot(skypeChat, "hudson",
                        "hostname", "!", null);
                    if (receivedChatMessage != null) {
                        // replay original message:
                        bot.onMessage(new SkypeMessage(receivedChatMessage, true));//Ask skype                        
                    }
                }
            } catch (SkypeException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
    };
}
