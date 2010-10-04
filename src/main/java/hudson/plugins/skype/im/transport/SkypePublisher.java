package hudson.plugins.skype.im.transport;

import hudson.Extension;
import hudson.model.User;
import hudson.plugins.im.DefaultIMMessageTarget;
import hudson.plugins.im.GroupChatIMMessageTarget;
import hudson.plugins.im.IMConnection;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessageTarget;
import hudson.plugins.im.IMMessageTargetConversionException;
import hudson.plugins.im.IMMessageTargetConverter;
import hudson.plugins.im.IMPublisher;
import hudson.plugins.im.tools.Assert;
import hudson.plugins.skype.user.SkypeUserProperty;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;

import java.util.List;

/**
 * Skype-specific implementation of the {@link IMPublisher}.
 *
 * @author Christoph Kutzinski
 * @author Uwe Schaefer (original implementation)
 */
public class SkypePublisher extends IMPublisher {

    private static class SkypeIMMessageTargetConverter implements IMMessageTargetConverter {

        private void checkValidity(final String f) throws IMMessageTargetConversionException {

        }

        @Override
        public IMMessageTarget fromString(final String targetAsString) throws IMMessageTargetConversionException {
            String f = targetAsString.trim();
            if (f.length() > 0) {
                IMMessageTarget target;
                if (f.startsWith("*")) {
                    f = f.substring(1);
                    // group chat
                    if (!f.contains("@")) {
                        f += "@conference." + SkypePublisher.DESCRIPTOR.getHostname();
                    }
                    target = new GroupChatIMMessageTarget(f);
                } else if (f.contains("@conference.")) {
                    target = new GroupChatIMMessageTarget(f);
                } else {                    
                    target = new DefaultIMMessageTarget(f);
                }
                checkValidity(f);
                return target;
            } else {
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString(final IMMessageTarget target) {
            Assert.isNotNull(target, "Parameter 'target' must not be null.");
            return target.toString();
        }
    }
    @Extension
    public static final SkypePublisherDescriptor DESCRIPTOR = new SkypePublisherDescriptor();
    static final IMMessageTargetConverter CONVERTER = new SkypeIMMessageTargetConverter();

    public SkypePublisher(List<IMMessageTarget> targets, String notificationStrategy,
            boolean notifyGroupChatsOnBuildStart,
            boolean notifySuspects,
            boolean notifyCulprits,
            boolean notifyFixers,
            boolean notifyUpstreamCommitters) throws IMMessageTargetConversionException {
        super(targets, notificationStrategy, notifyGroupChatsOnBuildStart,
                notifySuspects, notifyCulprits, notifyFixers, notifyUpstreamCommitters);
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return SkypePublisher.DESCRIPTOR;
    }

    @Override
    protected IMConnection getIMConnection() throws IMException {        
        return SkypeIMConnectionProvider.getInstance().currentConnection();
    }

    @Override
    protected String getPluginName() {
        return "Skype notifier plugin";
    }

    @Override
    protected String getConfiguredIMId(User user) {
        SkypeUserProperty skypeUserProperty = (SkypeUserProperty) user.getProperties().get(SkypeUserProperty.DESCRIPTOR);
        if (skypeUserProperty != null) {
            return skypeUserProperty.getSkypeId();
        }
        return null;
    }

    @Override
    public String getTargets() {
        List<IMMessageTarget> notificationTargets = getNotificationTargets();

        StringBuilder sb = new StringBuilder();
        for (IMMessageTarget target : notificationTargets) {
            if ((target instanceof GroupChatIMMessageTarget) && (!target.toString().contains("@conference."))) {
                sb.append("*");
            }
            sb.append(getIMDescriptor().getIMMessageTargetConverter().toString(target));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    // since Hudson 1.319:
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
}
