package com.skype;

/**
 *
 * @author Hans-Joachim Kliemeck <git@kliemeck.de>
 */
public class ChatUtils {
	public static String getChatTopic(Chat chat) throws SkypeException {
		return Utils.getProperty("CHAT", chat.getId(), "TOPIC");
	}
}
