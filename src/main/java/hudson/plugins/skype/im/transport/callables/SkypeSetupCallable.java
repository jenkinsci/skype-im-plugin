package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatMessageListener;
import com.skype.SkypeException;
import com.skype.Skype;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import hudson.plugins.skype.im.transport.LocalSkypeChat;
import hudson.plugins.skype.im.transport.RemoteSkypeChat;
import hudson.plugins.skype.im.transport.SkypeChat;
import hudson.plugins.skype.im.transport.SkypeIMException;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jbh
 */
public class SkypeSetupCallable implements Callable<Boolean, SkypeIMException> {

    public Boolean call() throws SkypeIMException {
        try {
            if (!Skype.isInstalled()) {
                throw new RuntimeException("Skype not installed.");
            }
            if (!Skype.isRunning()) {
                //throw new RuntimeException("Skype is not running.");
                System.err.println("Skype is probably not running");
            }
            Skype.setDebug(true);
            Skype.setDaemon(true);
            addSkypeListener(Channel.current());
            return true;
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
    }

    private void addSkypeListener(Channel channel) throws SkypeException {
        final IMListener listener = new SkypeSetupCallable.IMListener(channel);
        Skype.addChatMessageListener(listener);
        if (channel != null) {
            channel.addListener(new Channel.Listener() {
                @Override
                public void onClosed(Channel channel, IOException cause) {
                    Skype.removeChatMessageListener(listener);
                    System.err.println("Removed skype listener");

                    try {
                        Connector.getInstance().dispose();
                    } catch (ConnectorException ex) {
                        System.err.println("dispose failed");
                    }
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
            try {
                if (masterChannel != null) {
                    // we got a slave
                    SkypeChat skypeChat = new RemoteSkypeChat(receivedChatMessage);
                    masterChannel.call(new BotCommandCallable(receivedChatMessage, skypeChat));
                } else {
                    // we are on the master
                    SkypeChat skypeChat = new LocalSkypeChat(receivedChatMessage);
                    new BotCommandCallable(receivedChatMessage, skypeChat).call();
                }
            } catch (SkypeException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (SkypeIMException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
    };
}
