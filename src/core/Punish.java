package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import core.commads.MuteCommand;
import core.factories.CooldownFactory;
import core.factories.FilterFactory;
import core.factories.MuteFactory;
import core.utils.ActiveMute;
import core.utils.ChatMessage;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Punish extends Plugin implements Listener {

	public static MongoClient mongoClient;
	
	private DB minehutDB;
	
	public DBCollection playerMutes;
	
	public MuteFactory muteFactory;
	
	public FilterFactory filterFactory;
	
	public CooldownFactory cooldownFactory;
	
	@Override
	public void onEnable() {
		this.getProxy().getPluginManager().registerListener(this, this);
		this.getProxy().getPluginManager().registerCommand(this, new MuteCommand(this));
		this.connect();
		this.cooldownFactory = new CooldownFactory();
		this.filterFactory = new FilterFactory();
		this.muteFactory = new MuteFactory(this);
		
		getProxy().getScheduler().schedule(this, new Runnable() {
		    @Override
		    public void run() {
		    	List<ActiveMute> list = Collections.synchronizedList(muteFactory.activeMutes);
		    	synchronized(list) {
		    		Iterator<ActiveMute> i = list.iterator();
		    		while (i.hasNext()) {
		    			
		    			ActiveMute mute = i.next();
		    			if (mute.expired()) {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
				
		    }
		  });
		
	}
	
	public MongoClient getMonogClient() {
		return this.mongoClient;
	}
	
	public void staffBroadcast(String message) {
		for (ProxiedPlayer player : this.getProxy().getPlayers()) {
			if (player.hasPermission("minehut.mod")) {
				player.sendMessage(new TextComponent("Staff> " + message));
			}
		}
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
		    out.println("[" + new Date() + "] (" + action + ") " + player.getName() + ": " + log);
		} catch (IOException e) {
		   e.printStackTrace();
		} 
	
	}
	
	@EventHandler
	public void onChat(ChatEvent event) {
		if (event.isCommand())
			 return;
		if (event.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer)event.getSender();
			if (player.hasPermission("minehut.mod")) {
				this.logStaffAction(player, "CHAT", event.getMessage());
			}
			Server server = (Server)event.getReceiver();
			if (server.getInfo().getName().contains("kingdom")) {
				return;
			}
			System.out.println("ChatEvent found: ("  + server.getInfo().getName() + ")" + player.getDisplayName() + ": " + event.getMessage());
			
			if (this.cooldownFactory.getLastMessage(player.getUniqueId()).equals("NULL")) {
				this.cooldownFactory.setLastMessage(player.getUniqueId(), event.getMessage());
			} else {
				ChatMessage chatMessage = this.cooldownFactory.lastMessages.get(player.getUniqueId());
				if (chatMessage.getSecondsFromSent()<=1) {
					event.setCancelled(true);
				} else
				if (chatMessage.message.equalsIgnoreCase(event.getMessage())) {
					event.setCancelled(true);
				}
			}
			this.cooldownFactory.setLastMessage(player.getUniqueId(), event.getMessage());
			
			if (this.muteFactory.isMuted(player.getUniqueId())) {
				event.setCancelled(true);
				player.sendMessage(new TextComponent("You are muted!"));
				return;
			}
			if (!this.filterFactory.safeMessage(event.getMessage())) {
				event.setCancelled(true);
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
