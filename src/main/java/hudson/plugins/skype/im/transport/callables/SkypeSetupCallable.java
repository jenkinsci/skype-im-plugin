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
import hudson.plugins.im.IMConnectionListener;
import hudson.plugins.im.bot.Bot;
import hudson.plugins.skype.im.transport.SkypeChat;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.plugins.skype.im.transport.SkypeMessage;
import hudson.plugins.skype.im.transport.SkypeMessageListenerAdapter;
import hudson.remoting.Callable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author jbh
 */
public class SkypeSetupCallable implements Callable<Boolean, SkypeIMException> {

    public Boolean call() throws SkypeIMException {
        try {
            if (!System.getProperty("os.arch").contains("x86")) {
                throw new RuntimeException("Cannot use skype server on a 64 bit jvm (" + System.getProperty("os.arch") + ")");
            }
            if (!SkypeImpl.isInstalled()) {
                throw new RuntimeException("Skype not installed.");
            }
            if (!SkypeImpl.isRunning()) {
                throw new RuntimeException("Skype is not running.");
            }
            SkypeImpl.setDebug(true);
            SkypeImpl.setDaemon(true);
            SkypeImpl.addChatMessageListener(new SkypeSetupCallable.IMListener());
            return true;
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
    }
   

    private final class IMListener implements ChatMessageListener {

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

                Bot bot = new Bot(new SkypeChat(chat), "hudson",
                        "hostname", "!", null);
                

                if (receivedChatMessage != null) {
                    // replay original message:
                    bot.onMessage(new SkypeMessage(receivedChatMessage, true ));//Ask skype
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
