package core.commads;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

import core.Punish;
import core.utils.ActiveMute;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MuteCommand extends Command {
	
	private Punish core;
	
	public MuteCommand(Punish core) {
		super("mute");
		this.core = core;
	}
 
	public class PlayerInfo {
		public String id, name;
		public boolean legacy;
	}
	
	public class MuteRunnable implements Runnable {

		private CommandSender commandSender;
		private String[] args;
		
		public MuteRunnable(CommandSender commandSender, String[] args) {
			this.commandSender = commandSender;
			this.args = args;
		}
		
		@Override
		public void run() {
			if (args == null || args.length == 0) {
				commandSender.sendMessage(new TextComponent("/mute (Player) (Time) (Reason)"));
				commandSender.sendMessage(new TextComponent("Example: /mute Snick 10m Please don't spam"));
	            return;
			}
			
			if (args.length >= 3) {
				//if (this.core.getProxy().getPlayer(args[0])!=null) {
				UUID playerUUID;
				ProxiedPlayer pPlayer = null;
				
				if (core.getProxy().getPlayer(args[0])!=null) {
					pPlayer = core.getProxy().getPlayer(args[0]);
					playerUUID = pPlayer.getUniqueId();
				} else {
					commandSender.sendMessage(new TextComponent("That player is not online or that name is not valid!"));
					playerUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + args[0]).getBytes(Charsets.UTF_8));
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream()));
						Gson gson = new Gson();
						PlayerInfo info = gson.fromJson(in, PlayerInfo.class);
						playerUUID = UUID.fromString(info.id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
						//uuid = (((JsonObject)new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
						
						in.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("Offline UUID: " + playerUUID);
				}
					
					
				if (core.muteFactory.isMuted(playerUUID)) {
					commandSender.sendMessage(new TextComponent("Player is already muted!"));	
					return;
				}
					
				int muteTimeInSeconds = 0;
				if (args[1].contains("m")) {
					muteTimeInSeconds = Integer.parseInt(args[1].replace("m", "")) * 60;
				} else 
				if (args[1].contains("h")) {
					muteTimeInSeconds = (Integer.parseInt(args[1].replace("h", "")) * 24) * 60;
				}
				if (muteTimeInSeconds <= 0) {
					commandSender.sendMessage(new TextComponent("Please input a proper time format! (20m, 3h)"));	
					return;
				}
					
				if (args[2]==null) {
					commandSender.sendMessage(new TextComponent("Please input a reason!"));
					return;
				}
				
				String reason = "";
				for (int i = 2; i < args.length; i++) {
					reason = reason + ((i==2)?"":" ") + args[i];
				}
				
				ActiveMute newMute = new ActiveMute();
				newMute.lengthSeconds = muteTimeInSeconds;
				newMute.playerUUID = playerUUID;
				newMute.reason = reason;
				newMute.staff = commandSender.getName();
				core.muteFactory.mutePlayer(newMute);
					
				System.out.println(newMute.toString());
				if (pPlayer!=null) {
					commandSender.sendMessage(new TextComponent("Player " + pPlayer.getDisplayName() + " has been muted!"));
				}
			} else {
				commandSender.sendMessage(new TextComponent("/mute (Player) (Time) (Reason)"));
				commandSender.sendMessage(new TextComponent("Example: /mute Snick 10m Please don't spam"));
	            return;
			}
		}
		
	}
	
	@Override
	public void execute(CommandSender commandSender, String[] args) {
		if (commandSender.hasPermission("minehut.mod")) {
			this.core.getProxy().getScheduler().runAsync(this.core, new MuteRunnable(commandSender, args)); //UGHHH fuck mojang and bungeeeee
		} else {
			commandSender.sendMessage(new TextComponent("You do not have permission minehut.mod!"));
		}
	}
}