package com.skype;

/**
 *
 * @author Hans-Joachim Kliemeck <klk@mmw.ag>
 */
public class ChatUtils {
    public static String getChatTopic(Chat chat) throws SkypeException {
        return Utils.getProperty("CHAT", chat.getId(), "TOPIC");
    }
}
