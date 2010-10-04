/*
 * Created on Apr 22, 2007
 */
package hudson.plugins.skype.user;

import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for Jabber user property.
 * @author Pascal Bleser
 */
public class SkypeUserPropertyDescriptor extends UserPropertyDescriptor {
	
	public static final String PARAMETERNAME_JID = "skype.user.jid";
	
	public SkypeUserPropertyDescriptor() {
		super(SkypeUserProperty.class);
	}

	@Override
	public UserProperty newInstance(User user) {
		return new SkypeUserProperty(null);
	}

	@Override
	public UserProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
		try {
			return new SkypeUserProperty(req.getParameter(PARAMETERNAME_JID));
		} catch (IllegalArgumentException e) {
			throw new FormException("invalid Jabber ID", PARAMETERNAME_JID);
		}
	}

	@Override
	public String getDisplayName() {
		return "Skype ID";
	}


}
