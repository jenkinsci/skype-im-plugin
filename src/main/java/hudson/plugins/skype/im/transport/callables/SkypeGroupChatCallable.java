package hudson.plugins.skype.im.transport.callables;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatUtils;
import com.skype.SkypeException;
import com.skype.Skype;
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
            Group group = Skype.getContactList().getGroup(chatName);
            Chat[] chats = Skype.getAllChats();
            Chat useChat = null;
            for (Chat chat : chats) {
                // get direct property due to the fact that topic != friendlyname
                String chatTopic = ChatUtils.getChatTopic(chat);
                if (chatTopic.contains(chatName)) {
                    useChat = chat;
                    break;
                }
            }
            if (useChat == null && group != null) {
                useChat = Skype.chat("");
                useChat.setTopic(chatName);
                useChat.addUsers(group.getAllFriends());              
            } else if (useChat == null) {              
                throw new SkypeIMException("Could not find group/category/chat "+chatName);
            } 
            return useChat.send(message);
            
        } catch (SkypeException ex) {
            throw new SkypeIMException(ex);
        }
    }

}
