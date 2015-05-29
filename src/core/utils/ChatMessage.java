package core.utils;

import java.util.Date;

import core.factories.CooldownFactory;

public class ChatMessage {

	public String message = "";
	
	public long currentTime = System.currentTimeMillis();

	public ChatMessage setMessage(String message) {
		this.message = message;
		return this;
	}
	
	public int getSecondsFromSent() {
		return CooldownFactory.getSecondsFromDate(new Date(this.currentTime));
	}
	
}
