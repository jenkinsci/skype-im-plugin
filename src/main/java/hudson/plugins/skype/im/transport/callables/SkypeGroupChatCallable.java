/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.Friend;
import com.skype.SkypeException;
import com.skype.SkypeImpl;
import com.skype.Group;
import hudson.plugins.skype.im.transport.SkypeIMException;

/**
 *
 * @author jbh
 */
public class SkypeGroupChatCallable extends SkypeChatCallable {
    private String chatName = null;    

    public SkypeGroupChatCallable(String chatName, String msg) {
        super(null, msg);
        this.chatName = chatName;        

    }
    @Override
    public ChatMessage call() throws SkypeIMException {
        try {
            Group group = SkypeImpl.getContactList().getGroup(chatName);

            Chat[] chats = SkypeImpl.getAllChats();
            Chat useChat = null;
            for (Chat chat : chats) {
                if (chat.getWindowTitle().contains(chatName)) {
                    useChat = chat;
                }
            }
            if (useChat == null && group != null) {
                useChat = SkypeImpl.chat("");
                useChat.setTopic(chatName);
                useChat.addUsers(group.getAllFriends());              
            } else {
                throw new SkypeIMException("Could not find group/category/chat "+chatName);
            } 
            return useChat.send(message);
            
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
    }

}
