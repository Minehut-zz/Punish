package core.utils;

import java.util.Date;
import java.util.UUID;

import core.Punish;

public class PlayerBan {
	public UUID playerUUID;
	public String staff, reason;
	public long start;
	public int length;
	public PlayerBan(UUID uuid) {
		playerUUID = uuid;
	}
	public boolean expired() {
		if (start < 0) 
			return false;
		if (Punish.getSecondsFromDate(new Date(start)) >= length) 
			return true;
		return false;
	}
}