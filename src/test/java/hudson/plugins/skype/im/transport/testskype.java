/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.skype.im.transport;

import com.skype.Chat;
import com.skype.Group;
import com.skype.SkypeImpl;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 *
 * @author jbh
 */
public class testskype extends TestCase {
     
     public void testSkype() throws Exception {
         
         SkypeImpl.setDebug(true);
          Group group = SkypeImpl.getContactList().getGroup("devs");
          for (Group tg : SkypeImpl.getContactList().getAllSystemGroups()) {
              System.out.println("group:"+tg.getType()+":"+tg.getDisplayName());
          }
          Chat useChat = null;
        for (Chat chat : SkypeImpl.getAllChats()) {
            System.out.println(chat.getWindowTitle()+" "+chat.getStatus());
            if (chat.getWindowTitle().equals("devs")) {
                useChat = chat;
            }
        }
        if (useChat == null) {
            useChat = SkypeImpl.chat("");
            useChat.setTopic("devs");
            useChat.addUsers(group.getAllFriends());
        }
        useChat.send("JADA");
        
    }
}
