package core.factories;

import java.util.ArrayList;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import core.Punish;
import core.utils.ActiveMute;

public class MuteFactory {

	private Punish core;
	
	public ArrayList<ActiveMute> activeMutes = new ArrayList<ActiveMute>();
	
	public MuteFactory(Punish core) {
		this.core = core;
	}
	
	public void mutePlayer(ActiveMute activeMute) {
		this.activeMutes.add(activeMute);
		DBObject obj = new BasicDBObject("uuid", activeMute.playerUUID);
		obj.put("start", activeMute.start);
		obj.put("lengthSeconds", activeMute.lengthSeconds);
		obj.put("reason", activeMute.reason);
		obj.put("staff", activeMute.staff);
		this.core.playerMutes.insert(obj);
	}
	
	public boolean isMuted(UUID uuid) {
		for (ActiveMute mute : this.activeMutes) {
			if (mute.playerUUID == uuid) {
				return true;
			}
		}
		return false;
	}
	
	public Punish getCore() {
		return this.core;
	}
	
	
	
}
