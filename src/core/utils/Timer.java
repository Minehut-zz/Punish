package core.utils;

import java.util.Date;
import java.util.UUID;

import core.factories.CooldownFactory;

public class Timer {

	public UUID playerUUID = UUID.randomUUID();
	
	public long start = System.currentTimeMillis();
	
	public int secondsTimer = 60;
	
	public Timer(UUID uuid, int time) {
		
	}
	
	public boolean expired() {
		if (this.start < 0) {
			return false;
		}
		
		if (CooldownFactory.getSecondsFromDate(new Date(start)) >= this.secondsTimer) {
			return true;
		}
		
		return false;
	}
	
}
