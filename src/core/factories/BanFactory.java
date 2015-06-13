package core.factories;

import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import core.Punish;
import core.utils.PlayerBan;

public class BanFactory {

	private Punish core;
	
	public BanFactory(Punish core) {
		this.core = core;
	}
	
	public void addBan(PlayerBan ban) {
		if (this.isBanned(ban.playerUUID))
			return;
		DBObject obj = new BasicDBObject("uuid", ban.playerUUID);
		obj.put("playerBan", Punish.gson.toJson(ban));
		this.core.playerBans.insert(obj);
		this.core.banLogs.insert(obj);
	}
	
	public void removeBan(UUID uuid) {
		this.core.playerBans.remove(new BasicDBObject("uuid", uuid));
	}
	
	public PlayerBan getPlayerBan(UUID uuid) {
		return Punish.gson.fromJson((String)this.core.playerBans.findOne(new BasicDBObject("uuid", uuid)).get("playerBan"), PlayerBan.class);
	}
	
	public boolean isBanned(UUID uuid) {
		return (this.core.playerBans.findOne(new BasicDBObject("uuid", uuid)) != null);
	}
	
	
	
}
