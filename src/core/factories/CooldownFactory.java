package core.factories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import core.utils.ChatMessage;
import core.utils.Timer;


public class CooldownFactory {
	
	public ArrayList<Timer> chatCooldown = new ArrayList<Timer>();
	
	public HashMap<UUID, ChatMessage> lastMessages = new HashMap<UUID, ChatMessage>();

	public void removeLastMessage(UUID uuid) {
		this.lastMessages.remove(uuid);
	}
	
	public String getLastMessage(UUID uuid) {
		if (this.lastMessages.containsKey(uuid)) {
			return this.lastMessages.get(uuid).message;
		}
		return "NULL";
	}
	
	public void setLastMessage(UUID uuid, String message) {
		this.lastMessages.put(uuid, new ChatMessage().setMessage(message));
	}
	
	public void addToChatCooldown(UUID uuid) {
		this.chatCooldown.add(new Timer(uuid, 3));
	}
	
	public boolean onChatCooldown(UUID uuid) {
		for (Timer timer : this.chatCooldown) {
			if (timer.playerUUID.equals(uuid)) {
				return timer.expired();
			}
		}
		return false;
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
