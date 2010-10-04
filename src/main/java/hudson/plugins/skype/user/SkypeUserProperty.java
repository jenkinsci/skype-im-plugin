/*
 * Created on Apr 22, 2007
 */
package hudson.plugins.skype.user;

import hudson.Extension;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

/**
 * Jabber user property.
 * @author Pascal Bleser
 */
public class SkypeUserProperty extends UserProperty {

    @Extension
    public static final SkypeUserPropertyDescriptor DESCRIPTOR = new SkypeUserPropertyDescriptor();
    private String skypeId;

    public SkypeUserProperty() {
        // public constructor needed for @Extension parsing
    }

    public SkypeUserProperty(final String jid) {
        if ((jid != null) && (!"".equals(jid)) && (!validateJID(jid))) {
            throw new IllegalArgumentException("malformed Skype ID " + jid);
        }
        if ("".equals(jid)) {
            this.skypeId = null;
        } else {
            this.skypeId = jid;
        }
    }

    public String getSkypeId() {
        return this.skypeId;
    }

    @Override
    public UserPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    private static final boolean validateJID(final String jid) {
        return (jid.trim().length() > 0);
    }
}
