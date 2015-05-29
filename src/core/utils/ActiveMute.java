package core.utils;

import java.util.Date;
import java.util.UUID;

import core.Punish;

public class ActiveMute {

	public UUID playerUUID;
	
	public long start = System.currentTimeMillis();
	
	public int lengthSeconds;
	
	public String reason, staff;
	
	public boolean expired() {
		if (this.start < 0) {
			return false;
		}
		
		if (Punish.getSecondsFromDate(new Date(start)) >= this.lengthSeconds) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "[playerUUID=" + this.playerUUID + ",staff=" + staff+ ",start=" + start + ",lengthSeconds=" + this.lengthSeconds + ",reason=" + reason + "]";
	}
	
}
