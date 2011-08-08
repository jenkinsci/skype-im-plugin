package hudson.plugins.skype.im.transport;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.plugins.im.DefaultIMMessageTarget;
import hudson.plugins.im.GroupChatIMMessageTarget;
import hudson.plugins.im.IMConnection;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessageTarget;
import hudson.plugins.im.IMMessageTargetConversionException;
import hudson.plugins.im.IMMessageTargetConverter;
import hudson.plugins.im.IMPublisher;
import hudson.plugins.im.MatrixJobMultiplier;
import hudson.plugins.im.build_notify.BuildToChatNotifier;
import hudson.plugins.skype.im.transport.callables.SkypeVerifyUserCallable;
import hudson.plugins.skype.user.SkypeUserProperty;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Mailer;
import hudson.tasks.Publisher;
import java.io.IOException;

import java.util.List;
import org.springframework.util.Assert;

/**
 * Skype-specific implementation of the {@link IMPublisher}.
 *
 * @author Christoph Kutzinski
 * @author Uwe Schaefer (original implementation)
 */
public class SkypePublisher extends IMPublisher {

    
    @Extension
    public static final SkypePublisherDescriptor DESCRIPTOR = new SkypePublisherDescriptor();
    static final IMMessageTargetConverter CONVERTER = new SkypeIMMessageTargetConverter();

    public SkypePublisher(List<IMMessageTarget> defaultTargets,
    		String notificationStrategyString,
    		boolean notifyGroupChatsOnBuildStart,
    		boolean notifySuspects,
    		boolean notifyCulprits,
    		boolean notifyFixers,
    		boolean notifyUpstreamCommitters,
            BuildToChatNotifier buildToChatNotifier,
            MatrixJobMultiplier matrixMultiplier) {
        super(defaultTargets, notificationStrategyString, notifyGroupChatsOnBuildStart,
                notifySuspects, notifyCulprits, notifyFixers, notifyUpstreamCommitters, buildToChatNotifier, matrixMultiplier);
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
        String result = null;
        if (skypeUserProperty != null && skypeUserProperty.getSkypeId() != null && skypeUserProperty.getSkypeId().length() > 0) {
            result = skypeUserProperty.getSkypeId();
        } else {
            try {                            
                Mailer.UserProperty prop = user.getProperty(Mailer.UserProperty.class);
                System.out.println("TRying "+prop);
                if (prop != null) {
                    SkypeVerifyUserCallable callable = new SkypeVerifyUserCallable(prop.getAddress());
                    result = ((SkypeIMConnection)getIMConnection()).getChannel().call(callable);                    
                    user.addProperty(new SkypeUserProperty(result));
                    user.save();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
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
    
    private static class SkypeIMMessageTargetConverter implements IMMessageTargetConverter {

        private void checkValidity(final String f) throws IMMessageTargetConversionException {
        }

        public IMMessageTarget fromString(final String targetAsString) throws IMMessageTargetConversionException {
            String f = targetAsString.trim();
            if (f.length() > 0) {
                IMMessageTarget target;
                if (f.startsWith("*")) {
                    f = f.substring(1);                    
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
        public String toString(final IMMessageTarget target) {
            Assert.notNull(target, "Parameter 'target' must not be null.");
            return target.toString();
        }
    }
}
