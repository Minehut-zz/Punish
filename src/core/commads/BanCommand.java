package core.commads;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

import com.google.gson.Gson;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import core.Punish;
import core.Punish.BroadcastType;
import core.commads.MuteCommand.PlayerInfo;
import core.utils.PlayerBan;

public class BanCommand extends Command {
	
	private Punish core;
	
	public BanCommand(Punish core) {
		super("gban");
		this.core = core;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer)sender;
		if (!player.hasPermission("minehut.mod")) {
			return;
		}
		if (args == null || args.length == 0) {
			core.broadcastToPlayer(BroadcastType.ERROR, player, "/gban (Player) (Time) (Reason)");
			core.broadcastToPlayer(BroadcastType.ERROR, player, "Example: /gban Snick 10m Please don't spam");
            return;
		}
		
		if (args.length >= 3) {
			UUID playerUUID = null;
			ProxiedPlayer pPlayer = null;
			if (core.getProxy().getPlayer(args[0])!=null) {
				pPlayer = core.getProxy().getPlayer(args[0]);
				playerUUID = pPlayer.getUniqueId();
			} else {
//				core.broadcastToPlayer(BroadcastType.INFO, player, "That player is not online or that name is not valid!");
				//playerUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + args[0]).getBytes(Charsets.UTF_8));
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream()));
					Gson gson = new Gson();
					PlayerInfo info = gson.fromJson(in, PlayerInfo.class);
					playerUUID = UUID.fromString(info.id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (playerUUID == null) {
				core.broadcastToPlayer(BroadcastType.ERROR, player, "That is not a valid player name!");
				return;
			}
			
			if (core.banFactory.isBanned(playerUUID)) {
				core.broadcastToPlayer(BroadcastType.ERROR, player, "Player is already banned!");
				return;
			}
				
			int banTimeInSeconds = 0;
			if (args[1].contains("m")) {
				banTimeInSeconds = Integer.parseInt(args[1].replace("m", "")) * 60;
			} else 
			if (args[1].contains("h")) {
				banTimeInSeconds = (Integer.parseInt(args[1].replace("h", "")) * 60) * 60;
			} else
			if (args[1].contains("d")) {
				banTimeInSeconds = ((Integer.parseInt(args[1].replace("d", "")) * 24) * 60) * 60;
			}
			if (banTimeInSeconds <= 0) {
				core.broadcastToPlayer(BroadcastType.ERROR, player, "Please input a proper time format! (20m, 3h)");
				return;
			}
				
			if (args[2]==null) {
				core.broadcastToPlayer(BroadcastType.ERROR, player, "Please input a reason!");
				return;
			}
			
			String reason = "";
			for (int i = 2; i < args.length; i++) {
				reason = reason + ((i==2)?"":" ") + args[i];
			}
			
			
			PlayerBan ban = new PlayerBan(playerUUID);
			ban.length = banTimeInSeconds;
			ban.staff = sender.getName();
			ban.reason = reason;
			ban.start = System.currentTimeMillis();
			core.banFactory.addBan(ban);
			/*
			ActiveMute newMute = new ActiveMute();
			newMute.lengthSeconds = muteTimeInSeconds;
			newMute.playerUUID = playerUUID;
			newMute.reason = reason;
			newMute.staff = commandSender.getName();
			core.muteFactory.mutePlayer(newMute, (pPlayer!=null)); */
				
			//System.out.println(newMute.toString());
			if (pPlayer!=null) {
				TextComponent text = new TextComponent("You have been banned by ");// + sender.getName() + " for " + args[1] + "\n\rReason: " + reason);
				text.setBold(true);
				TextComponent staff = new TextComponent(sender.getName());
				staff.setBold(true);
				staff.setColor(ChatColor.RED);
				text.addExtra(staff);
				TextComponent time = new TextComponent(" for " + core.formatTimeFromSeconds(banTimeInSeconds) + "\r\nReason:" + reason);
				time.setBold(true);
				time.setColor(ChatColor.WHITE);
				text.addExtra(time);
				
				
				pPlayer.getPendingConnection().disconnect(text);
				//core.broadcastToPlayer(BroadcastType.WARN, pPlayer, "You have been muted by " + sender.getName() + " for " + args[1]);
			}
			core.broadcastToStaff(BroadcastType.INFO, ChatColor.YELLOW + args[0] + ChatColor.WHITE + " was banned!");
			core.broadcastToStaff(BroadcastType.INFO, ChatColor.GRAY + "Staff: " + ChatColor.AQUA + sender.getName());
			core.broadcastToStaff(BroadcastType.INFO, ChatColor.GRAY + "Length: " + ChatColor.YELLOW + args[1]);
			core.broadcastToStaff(BroadcastType.INFO, ChatColor.GRAY + "Reason: " + ChatColor.YELLOW + reason);
			
		} else {
			core.broadcastToPlayer(BroadcastType.ERROR, player, "/gban (Player) (Time) (Reason)");
			core.broadcastToPlayer(BroadcastType.ERROR, player, "Example: /gban Snick 10m Please don't spam");
            return;
		}
		
	}

}