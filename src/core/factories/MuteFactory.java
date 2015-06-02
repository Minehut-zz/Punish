package core.factories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
		if (online) {
			this.activeMutes.add(activeMute);
		}
		//Insert into mongo no matter what
		DBObject obj = new BasicDBObject("uuid", activeMute.playerUUID);
		obj.put("start", activeMute.start);
		obj.put("lengthSeconds", activeMute.lengthSeconds);
		obj.put("reason", activeMute.reason);
		obj.put("staff", activeMute.staff);
		this.core.playerMutes.insert(obj);
	}
	
	public void unmutePlayer(UUID uuid) {
		//if (this.isMuted(uuid)) {
			this.removePlayer(uuid);
		//}
		this.core.playerMutes.remove(new BasicDBObject("uuid", uuid));
	}
	
	public class RemoveSafelyRunnable implements Runnable {
		private UUID remove;
		public RemoveSafelyRunnable(UUID uuid) {
			remove = uuid;
		}
		@Override
	    public void run() {
	    	List<ActiveMute> list = Collections.synchronizedList(core.muteFactory.activeMutes);
	    	synchronized(list) {
	    		Iterator<ActiveMute> i = list.iterator();
	    		while (i.hasNext()) {
	    			
	    			ActiveMute mute = i.next();
	    			if (mute.playerUUID == remove) {
	    				i.remove();
	    			}
	    		}
	    	}
	    }
	}
	
	public void removePlayer(UUID uuid) {
		this.core.getProxy().getScheduler().runAsync(this.core, new RemoveSafelyRunnable(uuid));
	}
	
	public boolean isPlayerMutedInDatabase(UUID uuid) {
		return (this.core.playerMutes.findOne(new BasicDBObject("uuid", uuid)) != null);
	}
	
	public boolean isMuted(UUID uuid) {
		/*List<ActiveMute> list = Collections.synchronizedList(this.activeMutes);
    	synchronized(list) {
    		Iterator<ActiveMute> i = list.iterator();
    		while (i.hasNext()) {
    			
    			ActiveMute mute = i.next();
    			if (mute.playerUUID == uuid) {
    				return true;
    			}
    		}
    	}*/
    	for (ActiveMute mute : this.activeMutes) {
    		//System.out.println(uuid + "|" + mute.playerUUID);
    		
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
