package core.factories;

import java.util.ArrayList;
import java.util.Iterator;
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
	
	public void mutePlayer(ActiveMute activeMute, boolean online) {
		if (online) 
			this.activeMutes.add(activeMute);
		//Insert into mongo no matter what
		DBObject obj = new BasicDBObject("uuid", activeMute.playerUUID);
		obj.put("start", activeMute.start);
		obj.put("lengthSeconds", activeMute.lengthSeconds);
		obj.put("reason", activeMute.reason);
		obj.put("staff", activeMute.staff);
		this.core.playerMutes.insert(obj);
	}
	
	public void unmutePlayer(UUID uuid) {
		this.removePlayer(uuid);
		this.core.playerMutes.remove(new BasicDBObject("uuid", uuid));
	}
	
	public void removePlayer(UUID uuid) {
		Iterator<ActiveMute> i = activeMutes.iterator();
		while (i.hasNext()) {
			ActiveMute mute = i.next();
			if (mute.playerUUID.toString().equals(uuid.toString())) {
				i.remove();
			}
		}
	}
	
	public boolean isPlayerMutedInDatabase(UUID uuid) {
		return (this.core.playerMutes.findOne(new BasicDBObject("uuid", uuid)) != null);
	}
	
	public boolean isMuted(UUID uuid) {
    	for (ActiveMute mute : this.activeMutes) {
    		if (mute.playerUUID.toString().equals(uuid.toString())) {
    			return true;
    		}
    	}
		return false;
	}
	
	public Punish getCore() {
		return this.core;
	}
	
}
