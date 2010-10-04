package hudson.plugins.skype.im.transport;

import hudson.Plugin;
import hudson.plugins.im.IMPlugin;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

/**
 * Plugin entry point used to start/stop the plugin.
 *
 * @author Uwe Schaefer
 * @author kutzi
 */
public class SkypePluginImpl extends Plugin {

    private transient final IMPlugin imPlugin;

    public SkypePluginImpl() {
        this.imPlugin = new IMPlugin(SkypeIMConnectionProvider.getInstance());

    }

   

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws Exception {
        super.start();
        this.imPlugin.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        this.imPlugin.stop();
        super.stop();
    }
}
