/*
 * Created on 06.03.2007
 */
package hudson.plugins.skype.im.transport;

import hudson.plugins.skype.im.transport.callables.SkypeMoodCallable;
import com.skype.Profile;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Slave;
import hudson.model.User;
import hudson.plugins.im.AbstractIMConnection;
import hudson.plugins.im.GroupChatIMMessageTarget;
import hudson.plugins.im.IMConnection;
import hudson.plugins.im.IMConnectionListener;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessageTarget;
import hudson.plugins.im.IMPresence;
import hudson.plugins.im.bot.Bot;
import hudson.plugins.im.tools.Assert;
import hudson.plugins.im.tools.ExceptionHelper;
import hudson.plugins.skype.im.transport.callables.SkypeChatCallable;
import hudson.plugins.skype.im.transport.callables.SkypeSetupCallable;
import hudson.plugins.skype.im.transport.callables.SkypeVerifyUserCallable;
import hudson.remoting.VirtualChannel;
import java.io.IOException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.event.ConnectionListener;

import org.acegisecurity.Authentication;

/**
 * Smack-specific implementation of {@link IMConnection}.
 *
 * @author kutzi
 * @author Uwe Schaefer (original author)
 */
class SkypeIMConnection extends AbstractIMConnection {

    private static final Logger LOGGER = Logger.getLogger(SkypeIMConnection.class.getName());
    private final Set<Bot> bots = new HashSet<Bot>();
    private final String passwd;
    private final String botCommandPrefix;
    /**
     * Jabber 'nick'. This is just the username-part of the Jabber-ID.
     * I.e. for 'john.doe@gmail.com' it is 'john.doe'.
     */
    private final String nick = "hudson";
    /**
     * The nick name of the Hudson bot to use in group chats.
     * May be null in which case the nick is used.
     */
    private final String groupChatNick;
    /**
     * Server name of the Jabber server.
     */
    private final String hostname;
    private final int port;
    private final String[] groupChats;
    private IMPresence impresence;
    private String imStatusMessage;
    private final SkypePublisherDescriptor desc;
    private final Authentication authentication;
    private Slave skypeSlave = null; 

    SkypeIMConnection(SkypePublisherDescriptor desc, Authentication authentication) throws IMException {
        super(desc);
        Assert.isNotNull(desc, "Parameter 'desc' must not be null.");
        this.desc = desc;
        this.authentication = authentication;
        this.hostname = desc.getHost();
        this.port = desc.getPort();
        this.passwd = desc.getPassword();

        this.groupChatNick = desc.getGroupChatNickname() != null
                ? desc.getGroupChatNickname() : this.nick;
        this.botCommandPrefix = desc.getCommandPrefix();
        if (desc.getInitialGroupChats() != null) {
            this.groupChats = desc.getInitialGroupChats().trim().split("\\s");
        } else {
            this.groupChats = new String[0];
        }
        this.impresence = desc.isExposePresence() ? IMPresence.AVAILABLE : IMPresence.UNAVAILABLE;       
    }

    @Override
    public boolean connect() {
        lock();
        boolean connected = false;
        try {
            try {
                if (!isConnected()) {
                    if (createConnection()) {
                        LOGGER.info("Connected to Skype");

                        // I've read somewhere that status must be set, before one can do anything other
                        // Don't know if it's true, but can't hurt, either.
                        sendPresence();
                        connected = true;

                    } else {
                        LOGGER.warning("Cannot connect to Skype");
                    }
                }

            } catch (final Exception e) {
                e.printStackTrace();
                LOGGER.warning(ExceptionHelper.dump(e));
            }
        } finally {
            unlock();
        }
        return connected;
    }

    @Override
    public void close() {
        lock();
        try {
           
        } finally {
            unlock();
        }
    }

    private synchronized boolean createConnection() throws IMException {
        System.err.println("createConnection ");
        SkypeSetupCallable callable = new SkypeSetupCallable();
        Boolean result = Boolean.FALSE;
        if (!System.getProperty("os.arch").contains("x86")) {
            List<Node> nodes = Hudson.getInstance().getNodes();
            Label labelToFind = Label.get("skype");
            if (labelToFind.isAssignable()) {
                for (Node node : labelToFind.getNodes()) {
                    Slave slave = (Slave) node;
                    if (slave != null && slave.getComputer() != null && slave.getComputer().isAcceptingTasks()) {
                        try {
                            result = (Boolean) slave.getChannel().call(callable);
                            if (result) {
                                skypeSlave = slave;
                                break;
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(SkypeIMConnection.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SkypeIMConnection.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        result = Boolean.FALSE;
                    }
                }
            } else {
                Logger.getLogger(SkypeIMConnection.class.getName()).log(Level.SEVERE, "Cannot find nodes with label skype");
            }
        } else {

                result = (Boolean) callable.call();
            }
            return result;
        }

 

    public

     void send(final IMMessageTarget target, final String text)
            throws IMException {
        Assert.isNotNull(target, "Parameter 'target' must not be null.");
        Assert.isNotNull(text, "Parameter 'text' must not be null.");
        LOGGER.info("Send to " + target + " val " + text);
        try {
            // prevent long waits for lock
            if (!tryLock(5, TimeUnit.SECONDS)) {
                return;
            }
            try {
                if (target instanceof GroupChatIMMessageTarget) {
                } else {
                    verifyUser(target);
                    //final ChatMessage chat = skypeServ.chat(target.toString(), text);
                    SkypeChatCallable callable = new SkypeChatCallable(new String[]{target.toString()}, text);
                    try {
                        getChannel().call(callable);
                    } catch (IOException ex) {
                        throw new SkypeIMException(ex);
                    } catch (InterruptedException ex) {
                        throw new SkypeIMException(ex);
                    }
                }
            } catch (final SkypeIMException e) {
                // server unavailable ? Target-host unknown ? Well. Just skip this
                // one.
                LOGGER.warning(ExceptionHelper.dump(e));
                // TODO ? tryReconnect();
            } finally {
                unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // ignore
        }
    }

    private VirtualChannel getChannel() {
        if (skypeSlave != null && skypeSlave.getComputer() != null && skypeSlave.getComputer().isOnline()) {
            return skypeSlave.getChannel();
        } else {
            return null;
        }
    }

    private void verifyUser(final IMMessageTarget target) throws SkypeIMException {
        try {
            User usr = Hudson.getInstance().getUser(target.toString());

            SkypeVerifyUserCallable callable = new SkypeVerifyUserCallable(new String[]{target.toString()});
            String result = getChannel().call(callable);
            if (result != null && !result.isEmpty()) {
                throw new SkypeIMException(result);
            }
        } catch (IOException ex) {
            throw new SkypeIMException(ex);
        } catch (InterruptedException ex) {
            throw new SkypeIMException(ex);
        }
    }

    /**
     * This implementation ignores the new presence if
     * {@link JabberPublisherDescriptor#isExposePresence()} is false.
     */
    @Override
    public void setPresence(final IMPresence impresence, String statusMessage)
            throws IMException {
        Assert.isNotNull(impresence, "Parameter 'impresence' must not be null.");
        if (this.desc.isExposePresence()) {
            this.impresence = impresence;
            this.imStatusMessage = statusMessage;
            sendPresence();
        } else {
            // Ignore new presence.
            // Don't re-send presence, either. It would result in disconnecting from
            // all joined group chats
        }
    }

    private void sendPresence() {

        try {
            // prevent long waits for lock
            if (!tryLock(5, TimeUnit.SECONDS)) {
                return;
            }
            try {
                if (!isConnected()) {
                    return;
                }
                Profile.Status presence;
                switch (this.impresence) {
                    case AVAILABLE:
                        presence = Profile.Status.ONLINE;
                        break;

                    case OCCUPIED:
                        presence = Profile.Status.AWAY;
                        break;

                    case DND:
                        presence = Profile.Status.DND;
                        break;

                    case UNAVAILABLE:
                        presence = Profile.Status.OFFLINE;
                        break;

                    default:
                        presence = Profile.Status.UNKNOWN;
                }
                SkypeMoodCallable callable = new SkypeMoodCallable(this.imStatusMessage, presence);
                try {
                    getChannel().call(callable);
                } catch (IOException ex) {
                    Logger.getLogger(SkypeIMConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            } finally {
                unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // ignore
        }
    }

    @Override
    public boolean isConnected() {
        lock();
        boolean conn = getChannel() != null;
        try {
        } finally {
            unlock();
        }
        return conn;
    }

    public boolean isAuthorized(String xmppAddress) throws SkypeIMException {
        //skypeServ.validateUser(xmppAddress);
        return true;
    }
    private final Map<IMConnectionListener, ConnectionListener> listeners =
            new ConcurrentHashMap<IMConnectionListener, ConnectionListener>();

    @Override
    public void addConnectionListener(final IMConnectionListener listener) {
        lock();
        try {
        } finally {
            unlock();
        }
    }

    @Override
    public void removeConnectionListener(IMConnectionListener listener) {
        lock();
        try {
        } finally {
            unlock();
        }
    }
}
