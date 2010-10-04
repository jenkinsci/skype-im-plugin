/**
 * 
 */
package hudson.plugins.skype.im.transport;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessageTarget;
import hudson.plugins.im.IMMessageTargetConversionException;
import hudson.plugins.im.IMMessageTargetConverter;
import hudson.plugins.im.IMPublisherDescriptor;
import hudson.plugins.im.NotificationStrategy;
import hudson.plugins.im.tools.Assert;
import hudson.plugins.im.tools.ExceptionHelper;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class SkypePublisherDescriptor extends BuildStepDescriptor<Publisher> implements IMPublisherDescriptor {

    private static final Logger LOGGER = Logger.getLogger(SkypePublisherDescriptor.class.getName());
    private static final String PREFIX = "skypePlugin.";
    public static final String PARAMETERNAME_ENABLED = SkypePublisherDescriptor.PREFIX + "enabled";
    public static final String PARAMETERNAME_PORT = SkypePublisherDescriptor.PREFIX + "port";
    public static final String PARAMETERNAME_PRESENCE = SkypePublisherDescriptor.PREFIX + "exposePresence";
    public static final String PARAMETERNAME_GROUP_NICKNAME = SkypePublisherDescriptor.PREFIX + "groupNick";
    public static final String PARAMETERNAME_TARGETS = SkypePublisherDescriptor.PREFIX + "targets";
    public static final String PARAMETERNAME_STRATEGY = SkypePublisherDescriptor.PREFIX + "strategy";
    public static final String PARAMETERNAME_NOTIFY_START = SkypePublisherDescriptor.PREFIX + "notifyStart";
    public static final String PARAMETERNAME_NOTIFY_SUSPECTS = SkypePublisherDescriptor.PREFIX + "notifySuspects";
    public static final String PARAMETERNAME_NOTIFY_CULPRITS = SkypePublisherDescriptor.PREFIX + "notifyCulprits";
    public static final String PARAMETERNAME_NOTIFY_FIXERS = SkypePublisherDescriptor.PREFIX + "notifyFixers";
    public static final String PARAMETERNAME_NOTIFY_UPSTREAM_COMMITTERS = SkypePublisherDescriptor.PREFIX + "notifyUpstreamCommitters";
    public static final String PARAMETERNAME_INITIAL_GROUPCHATS = SkypePublisherDescriptor.PREFIX + "initialGroupChats";
    public static final String PARAMETERNAME_COMMAND_PREFIX = SkypePublisherDescriptor.PREFIX + "commandPrefix";
    public static final String PARAMETERNAME_DEFAULT_ID_SUFFIX = SkypePublisherDescriptor.PREFIX + "defaultIdSuffix";
    public static final String PARAMETERNAME_HUDSON_LOGIN = SkypePublisherDescriptor.PREFIX + "hudsonLogin";
    public static final String PARAMETERNAME_HUDSON_PASSWORD = SkypePublisherDescriptor.PREFIX + "hudsonPassword";
    public static final String[] PARAMETERVALUE_STRATEGY_VALUES = NotificationStrategy.getDisplayNames();
    public static final String PARAMETERVALUE_STRATEGY_DEFAULT = NotificationStrategy.STATECHANGE_ONLY.getDisplayName();
    public static final String DEFAULT_COMMAND_PREFIX = "!";
    private static final int DEFAULT_PORT = 5222;
    // big Boolean to support backwards compatibility
    private Boolean enabled;
    private int port = DEFAULT_PORT;
    private String hostname;
    // the following 2 are actually the Jabber nick and password. For backward compatibility I cannot rename them
    private String hudsonNickname;
    private String hudsonPassword;
    private String groupChatNickname;
    private boolean exposePresence = true;
    private boolean enableSASL = true;
    private String initialGroupChats;
    private String commandPrefix = DEFAULT_COMMAND_PREFIX;
    private String defaultIdSuffix;
    private String hudsonCiLogin;
    private String hudsonCiPassword;

    public SkypePublisherDescriptor() {
        super(SkypePublisher.class);
        load();

        if (isEnabled()) {
            try {
                SkypeIMConnectionProvider.setDesc(this);
            } catch (final Exception e) {
                // Server temporarily unavailable or misconfigured?
                LOGGER.warning(ExceptionHelper.dump(e));
            }
        } else {
            try {
                SkypeIMConnectionProvider.setDesc(null);
            } catch (IMException e) {
                // ignore
                LOGGER.info(ExceptionHelper.dump(e));
            }
        }
    }

    @Override
    public void load() {
        super.load();
        if (this.enabled == null) {
            // migrate from plugin < v1.0
            if (Util.fixEmptyAndTrim(this.hudsonNickname) != null) {
                this.enabled = Boolean.TRUE;
            } else {
                this.enabled = Boolean.FALSE;
            }
        }
    }

    private void applyGroupChatNickname(final HttpServletRequest req) throws FormException {
        this.groupChatNickname = req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_GROUP_NICKNAME);
        if (this.groupChatNickname != null && this.groupChatNickname.trim().length() == 0) {
            this.groupChatNickname = null;
        }
    }

    private void applyPort(final HttpServletRequest req) throws FormException {
        final String p = Util.fixEmptyAndTrim(req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_PORT));
        if (p != null) {
            try {
                final int i = Integer.parseInt(p);
                if ((i < 0) || (i > 65535)) {
                    throw new FormException("Port out of range.", SkypePublisherDescriptor.PARAMETERNAME_PORT);
                }
                this.port = i;
            } catch (final NumberFormatException e) {
                throw new FormException("Port cannot be parsed.", SkypePublisherDescriptor.PARAMETERNAME_PORT);
            }
        } else {
            this.port = DEFAULT_PORT;
        }
    }

    private void applyInitialGroupChats(final HttpServletRequest req) {
        this.initialGroupChats = Util.fixEmptyAndTrim(req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_INITIAL_GROUPCHATS));
    }

    private void applyCommandPrefix(final HttpServletRequest req) {
        String prefix = req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_COMMAND_PREFIX);
        if ((prefix != null) && (prefix.trim().length() > 0)) {
            this.commandPrefix = prefix;
        } else {
            this.commandPrefix = DEFAULT_COMMAND_PREFIX;
        }
    }

    private void applyDefaultIdSuffix(final HttpServletRequest req) {
        String suffix = req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_DEFAULT_ID_SUFFIX);
        if ((suffix != null) && (suffix.trim().length() > 0)) {
            this.defaultIdSuffix = suffix.trim();
        } else {
            this.defaultIdSuffix = "";
        }
    }

    private void applyHudsonLoginPassword(HttpServletRequest req) throws FormException {
        this.hudsonCiLogin = Util.fixEmptyAndTrim(req.getParameter(PARAMETERNAME_HUDSON_LOGIN));
        this.hudsonCiPassword = Util.fixEmptyAndTrim(req.getParameter(PARAMETERNAME_HUDSON_PASSWORD));
        if (this.hudsonCiLogin != null) {
            Authentication auth = new UsernamePasswordAuthenticationToken(this.hudsonCiLogin, this.hudsonCiPassword);
            try {
                Hudson.getInstance().getSecurityRealm().getSecurityComponents().manager.authenticate(auth);
            } catch (AuthenticationException e) {
                throw new FormException(e, "Bad Hudson credentials");
            }
        }
    }

    /**
     * This human readable name is used in the configuration screen.
     */
    @Override
    public String getDisplayName() {
        return "Skype Notification";
    }

    @Override
    public String getPluginDescription() {
        return "Skype plugin";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }

    @Override
    @Deprecated
    public String getHostname() {
        return this.hostname;
    }

    /**
     * Returns the jabber ID.
     * 
     * The jabber ID may have the syntax <user>[@<domain>[/<resource]]
     */
    public String getSkypeId() {
        return this.hudsonNickname;
    }

    @Override
    public String getPassword() {
        return this.hudsonPassword;
    }

    public String getGroupChatNickname() {
        return this.groupChatNickname;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the text to be put into the form field.
     * If the port is default, leave it empty.
     */
    public String getPortString() {
        if (port == 5222) {
            return null;
        } else {
            return String.valueOf(port);
        }
    }

    public boolean isEnableSASL() {
        return enableSASL;
    }

    public boolean isExposePresence() {
        return this.exposePresence;
    }

    /**
     * Gets the whitespace separated list of group chats to join,
     * or null if nothing is configured.
     */
    public String getInitialGroupChats() {
        return Util.fixEmptyAndTrim(this.initialGroupChats);
    }

    @Override
    public String getDefaultIdSuffix() {
        return null;
    }

    @Override
    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    /**
     * Creates a new instance of {@link SkypePublisher} from a submitted form.
     */
    @Override
    public SkypePublisher newInstance(final StaplerRequest req, JSONObject formData) throws FormException {
        Assert.isNotNull(req, "Parameter 'req' must not be null.");
        final String t = req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_TARGETS);
        final String[] split;
        if (t != null) {
            split = t.split("\\s");
        } else {
            split = new String[0];
        }

        List<IMMessageTarget> targets = new ArrayList<IMMessageTarget>(split.length);


        try {
            final IMMessageTargetConverter conv = getIMMessageTargetConverter();
            for (String fragment : split) {
                IMMessageTarget createIMMessageTarget;
                createIMMessageTarget = conv.fromString(fragment);
                if (createIMMessageTarget != null) {
                    targets.add(createIMMessageTarget);
                }
            }
        } catch (IMMessageTargetConversionException e) {
            throw new FormException("Invalid SkypeID", e, SkypePublisherDescriptor.PARAMETERNAME_TARGETS);
        }

        String n = req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_STRATEGY);
        if (n == null) {
            n = PARAMETERVALUE_STRATEGY_DEFAULT;
        } else {
            boolean foundStrategyValueMatch = false;
            for (final String strategyValue : PARAMETERVALUE_STRATEGY_VALUES) {
                if (strategyValue.equals(n)) {
                    foundStrategyValueMatch = true;
                    break;
                }
            }
            if (!foundStrategyValueMatch) {
                n = PARAMETERVALUE_STRATEGY_DEFAULT;
            }
        }
        boolean notifyStart = "on".equals(req.getParameter(PARAMETERNAME_NOTIFY_START));
        boolean notifySuspects = "on".equals(req.getParameter(PARAMETERNAME_NOTIFY_SUSPECTS));
        boolean notifyCulprits = "on".equals(req.getParameter(PARAMETERNAME_NOTIFY_CULPRITS));
        boolean notifyFixers = "on".equals(req.getParameter(PARAMETERNAME_NOTIFY_FIXERS));
        boolean notifyUpstream = "on".equals(req.getParameter(PARAMETERNAME_NOTIFY_UPSTREAM_COMMITTERS));
        try {
            return new SkypePublisher(targets, n, notifyStart, notifySuspects, notifyCulprits,
                    notifyFixers, notifyUpstream);
        } catch (final IMMessageTargetConversionException e) {
            throw new FormException(e, SkypePublisherDescriptor.PARAMETERNAME_TARGETS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
        String en = req.getParameter(PARAMETERNAME_ENABLED);
        this.enabled = Boolean.valueOf(en != null);
        this.exposePresence = req.getParameter(SkypePublisherDescriptor.PARAMETERNAME_PRESENCE) != null;

        applyPort(req);
        applyGroupChatNickname(req);
        applyInitialGroupChats(req);
        applyCommandPrefix(req);
        applyDefaultIdSuffix(req);
        applyHudsonLoginPassword(req);

        if (isEnabled()) {
            try {
                SkypeIMConnectionProvider.setDesc(this);
                SkypeIMConnectionProvider.getInstance().currentConnection();
            } catch (final Exception e) {
                //throw new FormException("Unable to create Client: " + ExceptionHelper.dump(e), null);
                LOGGER.warning(ExceptionHelper.dump(e));
            }
        } else {
            SkypeIMConnectionProvider.getInstance().releaseConnection();
            try {
                SkypeIMConnectionProvider.setDesc(null);
            } catch (IMException e) {
                // ignore
                LOGGER.info(ExceptionHelper.dump(e));
            }
            LOGGER.info("No hostname specified.");
        }
        save();
        return super.configure(req, json);
    }

    /**
     * Validates the connection information.
     */
    public FormValidation doServerCheck(@QueryParameter final String hostname,
            @QueryParameter final String port) {
        if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) {
            return FormValidation.ok();
        }
        String host = Util.fixEmptyAndTrim(hostname);
        String p = Util.fixEmptyAndTrim(port);
        if (host == null) {
            return FormValidation.ok();
        } else {
            try {
                checkHostAccessibility(host, port);
                return FormValidation.ok();
            } catch (UnknownHostException e) {
                return FormValidation.error("Unknown host " + host);
            } catch (NumberFormatException e) {
                return FormValidation.error("Invalid port " + port);
            } catch (IOException e) {
                return FormValidation.error("Unable to connect to " + hostname + ":" + p + " : " + e.getMessage());
            }
        }
    }

    private static void checkHostAccessibility(String hostname, String port)
            throws UnknownHostException, IOException, NumberFormatException {
        hostname = Util.fixEmptyAndTrim(hostname);
        port = Util.fixEmptyAndTrim(port);
        int iPort = DEFAULT_PORT;
        InetAddress address = InetAddress.getByName(hostname);

        if (port != null) {
            iPort = Integer.parseInt(port);
        }

        Socket s = new Socket(address, iPort);
        s.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    /**
     *Returns the skypeid.
     */
    @Override
    public String getUserName() {
        return getSkypeId();
    }

    @Override
    public String getHudsonPassword() {
        return this.hudsonCiPassword;
    }

    @Override
    public String getHudsonUserName() {
        return this.hudsonCiLogin;
    }

    @Override
    public IMMessageTargetConverter getIMMessageTargetConverter() {
        return SkypePublisher.CONVERTER;
    }

    @Override
    public List<IMMessageTarget> getDefaultTargets() {
        // not implemented for Skype bot
        return Collections.emptyList();
    }

    public String getHost() {
        return "localhost";
    }
}
