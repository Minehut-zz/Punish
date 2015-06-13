package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import core.commads.BanCommand;
import core.commads.MuteCommand;
import core.commads.StaffChatCommand;
import core.commads.UnbanCommand;
import core.commads.UnmuteCommand;
import core.commads.MuteCommand.PlayerInfo;
import core.factories.BanFactory;
import core.factories.CooldownFactory;
import core.factories.FilterFactory;
import core.factories.MuteFactory;
import core.utils.ActiveMute;
import core.utils.ChatMessage;
import core.utils.PlayerBan;
import core.utils.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Punish extends Plugin implements Listener {

	public static MongoClient mongoClient;
	
	private DB minehutDB;
	public java.text.DecimalFormat nft = new java.text.DecimalFormat("#00.###");
	public DBCollection playerMutes, playerBans, ipBans, players, muteLogs, banLogs;
	public MuteFactory muteFactory;
	
	public BanFactory banFactory;
	
	public FilterFactory filterFactory;
	
	public CooldownFactory cooldownFactory;
	
	public static Gson gson;
	
	static {
		gson = new Gson();
	}
	
	public String formatTimeFromSeconds(int seconds) {
		int trueseconds = seconds % 60;
		int minutes = seconds / 60;
		return (minutes<=0?"":nft.format(minutes) + "m ") + nft.format(trueseconds) + "s";
	}
	
	@Override
	public void onEnable() {
		this.getProxy().getPluginManager().registerListener(this, this);
		this.getProxy().getPluginManager().registerCommand(this, new MuteCommand(this));
		this.getProxy().getPluginManager().registerCommand(this, new UnmuteCommand(this));
		this.getProxy().getPluginManager().registerCommand(this, new StaffChatCommand(this));
		this.getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
		this.getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
		this.connect();
		this.cooldownFactory = new CooldownFactory();
		this.filterFactory = new FilterFactory();
		this.muteFactory = new MuteFactory(this);
		this.banFactory = new BanFactory(this);
		
		getProxy().getScheduler().schedule(this, new Runnable() {
		    @Override
		    public void run() {
		    	List<ActiveMute> list = Collections.synchronizedList(muteFactory.activeMutes);
		    	synchronized(list) {
		    		Iterator<ActiveMute> i = list.iterator();
		    		while (i.hasNext()) {
		    			ActiveMute mute = i.next();
		    			if (mute.expired()) {
		    				playerMutes.remove(new BasicDBObject("uuid", mute.playerUUID));
		    				if (getProxy().getPlayer(mute.playerUUID)!=null) {
		    					broadcastToPlayer(BroadcastType.INFO, mute.playerUUID, "You have been unmuted!");
		    				}
		    				i.remove();
		    			}
		    		}
		    	}
		    }
		  }, 0, 2, TimeUnit.MINUTES);
	}
	
	private void connect() {
		getProxy().getScheduler().runAsync(this, new Runnable() {
		    @Override
		    public void run() {
		    	try {
					mongoClient = new MongoClient("127.0.0.1", 27017); //27017
					minehutDB = mongoClient.getDB("minehut");
					playerMutes = minehutDB.getCollection("playermutes");
					playerBans = minehutDB.getCollection("playerbans");
					ipBans = minehutDB.getCollection("ipbans");
					players = minehutDB.getCollection("players");
					muteLogs = minehutDB.getCollection("mutelogs");
					banLogs = minehutDB.getCollection("banlogs");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
		    }
		  });
		
	}
	
	private boolean onlineMode = true;
    public Rank getRank(UUID uuid) {
        if(onlineMode) {
            DBObject r = new BasicDBObject("uuid", uuid.toString());
            DBObject found = players.findOne(r);

            if (found != null) {
                String rankName = (String) found.get("rank");
                return Rank.getRank(rankName);
            } else {
            /* Player not found, return default */
                return Rank.regular;
            }
        } else {
            return Rank.regular;
        }
    }
    
	public MongoClient getMonogClient() {
		return Punish.mongoClient;
	}
	
	public enum BroadcastType {
		INFO("&9&lInfo"), WARN("&e&lWarn"), ERROR("&4&lError"), MSG("&a&lMSG"), STAFF("&b&lStaff");
		private String type;
		private BroadcastType(String s) {
			this.type = s;
		}
		public String getType() {
			return this.type;
		}
	}
	
	public void broadcastToPlayers(BroadcastType type, String message) {
		for (ProxiedPlayer player : this.getProxy().getPlayers()) {
			player.sendMessage(this.formatMessage(type, message));
		}
	}
	
	public TextComponent formatMessage(BroadcastType type, String message) {
		TextComponent text = new TextComponent(type.getType().replaceAll("&", "§") + "> ");
		TextComponent textMessage = new TextComponent(message);
		textMessage.setColor((type==BroadcastType.STAFF)?ChatColor.AQUA:ChatColor.LIGHT_PURPLE);
		//textMessage.setBold(true);
		
		text.addExtra(textMessage);
		return text;
	}
	
	public void broadcastToPlayer(BroadcastType type, UUID uuid, String message) {
		this.broadcastToPlayer(type, this.getProxy().getPlayer(uuid), message);
	}
	
	public void broadcastToPlayer(BroadcastType type, ProxiedPlayer player, String message) {
		player.sendMessage(this.formatMessage(type, message));
	}
	
	public void broadcastToPlayersWithPerm(BroadcastType type, String perm, String message) {
		for (ProxiedPlayer player : this.getProxy().getPlayers()) {
			if (player.hasPermission(perm)) {
				player.sendMessage(this.formatMessage(type, message));
			}
		}
	}
	
	public void broadcastToStaff(BroadcastType type, String message) {
		this.broadcastToPlayersWithPerm(type, "minehut.mod", message);
	}
	
	public void logStaffAction(ProxiedPlayer player, String action, String log) {
		
		File staffFolder = new File("staff");
		if (!staffFolder.exists()) {
			staffFolder.mkdir();
		}
		File file = new File("staff/" + player.getName() + ".log");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("staff/" + player.getName()+".log", true)))) {
		    out.println("[" + new Date() + "] {" + player.getServer().getInfo().getName() + "} (" + action + ") " + player.getName() + ": " + log);
		} catch (IOException e) {
		   e.printStackTrace();
		} 
	
	}
	
	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		this.muteFactory.removePlayer(event.getPlayer().getUniqueId());
		this.cooldownFactory.removeLastMessage(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onLogin(final PostLoginEvent event) {
		
		this.getProxy().getScheduler().runAsync(this, new Runnable() {
			@Override
			public void run() {
				if (getRank(event.getPlayer().getUniqueId()).equals(Rank.Mod)) {
					setModPerms(event.getPlayer());
				} else
				if (getRank(event.getPlayer().getUniqueId()).equals(Rank.Manager)||
					getRank(event.getPlayer().getUniqueId()).equals(Rank.Admin) ||
					getRank(event.getPlayer().getUniqueId()).equals(Rank.Dev) ||
					getRank(event.getPlayer().getUniqueId()).equals(Rank.Owner)) {
					setAdminPerms(event.getPlayer());
				}
			}
		});
	}
	
	public void setModPerms(ProxiedPlayer player) {
		player.setPermission("minehut.mod", true);
		player.setPermission("bungeecord.command.send", true);
		player.setPermission("bungeecord.command.server", true);
		player.setPermission("bungeecord.command.find", true);
	}
	
	public void setAdminPerms(ProxiedPlayer player) {
		this.setModPerms(player);
		player.setPermission("bungeecord.command.alert", true);
	}
	

	@EventHandler
	public void onLogin(final PreLoginEvent event) {
		//event.registerIntent(this);
		this.getProxy().getScheduler().runAsync(this, new Runnable() {
			@Override
			public void run() {
				UUID playerUUID = null;
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + event.getConnection().getName()).openStream()));
					
					PlayerInfo info = gson.fromJson(in, PlayerInfo.class);
					playerUUID = UUID.fromString(info.id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
					in.close();
				} catch	 (Exception e) {
					e.printStackTrace();
				}
				if (playerUUID!=null) {
					if (banFactory.isBanned(playerUUID)) {
						PlayerBan ban = banFactory.getPlayerBan(playerUUID);
						if (ban.expired()) {
							banFactory.removeBan(playerUUID);
						} else {
							TextComponent text = new TextComponent("You have been banned by ");// + sender.getName() + " for " + args[1] + "\n\rReason: " + reason);
							text.setBold(true);
							TextComponent staff = new TextComponent(ban.staff);
							staff.setBold(true);
							staff.setColor(ChatColor.RED);
							text.addExtra(staff);
							TextComponent time = new TextComponent("\r\nReason:" + ban.reason + "\r\nTime Left: " + formatTimeFromSeconds(ban.length - (int)Punish.getSecondsFromDate(new Date(ban.start))));
							time.setBold(true);
							time.setColor(ChatColor.WHITE);
							text.addExtra(time);
							event.getConnection().disconnect(text);
							return;
						}
					}
					if (muteFactory.isPlayerMutedInDatabase(playerUUID)) {
						DBObject obj = playerMutes.findOne(new BasicDBObject("uuid", playerUUID));
						ActiveMute mute = new ActiveMute();
						mute.playerUUID = playerUUID;
						mute.lengthSeconds = (int)obj.get("lengthSeconds");
						mute.start = (long)obj.get("start");
						mute.reason = (String)obj.get("reason");
						mute.staff = (String)obj.get("staff");
						muteFactory.activeMutes.add(mute);
					}
				}
				
			}
		});
		//event.completeIntent(this);
	}
	
	@EventHandler
	public void onChat(ChatEvent event) {
		if (event.isCommand()) {
			
			if (event.getSender() instanceof ProxiedPlayer) {
				ProxiedPlayer player = (ProxiedPlayer)event.getSender();
				if (player.hasPermission("minehut.mod")) {
					this.logStaffAction(player, "COMMAND", event.getMessage());
				}
			}
			
			
			return;
		}
			 
		if (event.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer)event.getSender();
			if (player.hasPermission("minehut.mod")) {
				this.logStaffAction(player, "CHAT", event.getMessage());
			}
			Server server = (Server)event.getReceiver();
			if (server.getInfo().getName().contains("kingdom")) {
				return;
			}
			//System.out.println("ChatEvent found: ("  + server.getInfo().getName() + ")" + player.getDisplayName() + ": " + event.getMessage());
			if (this.cooldownFactory.getLastMessage(player.getUniqueId()).equals("NULL")) {
				this.cooldownFactory.setLastMessage(player.getUniqueId(), event.getMessage());
			} else {
				ChatMessage chatMessage = this.cooldownFactory.lastMessages.get(player.getUniqueId());
				if (chatMessage.getSecondsFromSent()<=1) {
					this.broadcastToPlayer(BroadcastType.INFO, player, "Please slow down with your chat!");
					event.setCancelled(true);
					return;
				} else
				if (chatMessage.message.equalsIgnoreCase(event.getMessage())) {
					this.broadcastToPlayer(BroadcastType.INFO, player, "Please do not send the same message twice!");
					event.setCancelled(true);
					return;
				}
			}
			this.cooldownFactory.setLastMessage(player.getUniqueId(), event.getMessage());
			
			if (this.muteFactory.isMuted(player.getUniqueId())) {
				event.setCancelled(true);
				this.broadcastToPlayer(BroadcastType.INFO, player, "You are muted!");
				//player.sendMessage(new TextComponent("You are muted!"));
				return;
			}
			if (!this.filterFactory.safeMessage(event.getMessage())) {
				event.setCancelled(true);
				this.broadcastToPlayer(BroadcastType.WARN, player, "Please do not use bad characters in the chat!");
				this.broadcastToPlayer(BroadcastType.INFO, player, "Contact a staff member to get one added to the whitelist.");
				return;
			}
		}
	}
	
	public static int getSecondsFromDate(Date date) {
		int out = 0;
		Map<TimeUnit, Long> tempTimes = computeDiff(date, new Date());
		for (Entry<TimeUnit, Long> set : tempTimes.entrySet()) {
			if (set.getKey().equals(TimeUnit.SECONDS)) {
				out += set.getValue();
			} else
			if (set.getKey().equals(TimeUnit.MINUTES)) {
				out += set.getValue() * 60;
			} else
			if (set.getKey().equals(TimeUnit.HOURS)) {
				out += (set.getValue() * 60) * 60;
			} else 
			if (set.getKey().equals(TimeUnit.DAYS)) {
				out += ((set.getValue() * 24) * 60) * 60;
			}
		}
		return out;
	}
	
	public static Map<TimeUnit, Long> computeDiff(Date date1, Date date2) {
    	long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
     	Collections.reverse(units);
     	
     	Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit,Long>();
     	long milliesRest = diffInMillies;
        for ( TimeUnit unit : units ) {
        	long diff = unit.convert(milliesRest,TimeUnit.MILLISECONDS);
        	long diffInMilliesForUnit = unit.toMillis(diff);
        	milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit,diff);
        }
        return result;
    }
	
}
