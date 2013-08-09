package hudson.plugins.skype.im.transport;

import com.skype.Chat;
import com.skype.Group;
import com.skype.Skype;
import junit.framework.TestCase;

/**
 *
 * @author jbh
 */
public class testskype extends TestCase {
     
     public void testSkype() throws Exception {
         
         Skype.setDebug(true);
          Group group = Skype.getContactList().getGroup("devs");
          for (Group tg : Skype.getContactList().getAllSystemGroups()) {
              System.out.println("group:"+tg.getType()+":"+tg.getDisplayName());
          }
          Chat useChat = null;
        for (Chat chat : Skype.getAllChats()) {
            System.out.println(chat.getWindowTitle()+" "+chat.getStatus());
            if (chat.getWindowTitle().equals("devs")) {
                useChat = chat;
            }
        }
        if (useChat == null) {
            useChat = Skype.chat("");
            useChat.setTopic("devs");
            useChat.addUsers(group.getAllFriends());
        }
        useChat.send("JADA");
        
    }
}
