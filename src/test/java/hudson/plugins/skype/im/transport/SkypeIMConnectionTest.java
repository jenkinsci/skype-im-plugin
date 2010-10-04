/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hudson.plugins.skype.im.transport;

import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.Node.Mode;
import hudson.plugins.im.DefaultIMMessageTarget;
import hudson.plugins.im.IMMessageTarget;
import hudson.slaves.DumbSlave;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.RetentionStrategy;
import java.util.ArrayList;
import org.jvnet.hudson.test.HudsonTestCase;
/**
 *
 * @author jbh
 */
public class SkypeIMConnectionTest extends HudsonTestCase {
    SkypeIMConnection instance = null;
    public SkypeIMConnectionTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        DumbSlave slave = new DumbSlave("name", "nodeDescription", "c:\\slave", "1", Mode.NORMAL, "skype", new JNLPLauncher(),RetentionStrategy.INSTANCE,
                new ArrayList());

        Hudson.getInstance().addNode(slave);
        slave.getComputer().connect(true);
        System.out.println("Node:"+slave.getComputer().isOffline());
        System.out.println("comp:"+slave.getComputer());
        System.out.println("Node:"+Hudson.getInstance().getNodes().get(0).getNodeName());
        System.out.println("Channel:"+Hudson.getInstance().getNodes().get(0).getChannel());
        SkypeIMConnectionProvider connProv = (SkypeIMConnectionProvider)SkypeIMConnectionProvider.getInstance();
        SkypePublisherDescriptor descr = new SkypePublisherDescriptor();

        connProv.setDescriptor(new SkypePublisherDescriptor());
        instance = (SkypeIMConnection)connProv.createConnection();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of connect method, of class SkypeIMConnection.
     */
    public void testConnect() throws Exception {
        
        System.out.println("connect");
        
        boolean expResult = true;
        boolean result = instance.connect();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }

  
    /**
     * Test of send method, of class SkypeIMConnection.
     */
    public void testSend() throws Exception {
        System.out.println("send");
        IMMessageTarget target = new DefaultIMMessageTarget("no32717223");
        String text = "TESTACSE";       
        instance.send(target, text);
        
    }

    /**
     * Test of setPresence method, of class SkypeIMConnection.
     */
//    public void testSetPresence() throws Exception {
//        System.out.println("setPresence");
//        IMPresence impresence = null;
//        String statusMessage = "";
//        SkypeIMConnection instance = null;
//        instance.setPresence(impresence, statusMessage);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of isConnected method, of class SkypeIMConnection.
     */
//    public void testIsConnected() {
//        System.out.println("isConnected");
//        SkypeIMConnection instance = null;
//        boolean expResult = false;
//        boolean result = instance.isConnected();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
   

    /**
     * Test of addConnectionListener method, of class SkypeIMConnection.
     */
//    public void testAddConnectionListener() {
//        System.out.println("addConnectionListener");
//        IMConnectionListener listener = null;
//        SkypeIMConnection instance = null;
//        instance.addConnectionListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of removeConnectionListener method, of class SkypeIMConnection.
     */
//    public void testRemoveConnectionListener() {
//        System.out.println("removeConnectionListener");
//        IMConnectionListener listener = null;
//        SkypeIMConnection instance = null;
//        instance.removeConnectionListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
