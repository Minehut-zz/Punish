package core.commads;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

import com.google.gson.Gson;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import core.Punish;
import core.Punish.BroadcastType;
import core.commads.MuteCommand.PlayerInfo;

public class UnbanCommand extends Command {
	
	private Punish core;
	
	public UnbanCommand(Punish core) {
		super("gunban");
		this.core = core;
	}

	@Override
	public void execute(CommandSender commandSender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer)commandSender;
		if (!player.hasPermission("minehut.mod")) {
			return;
		}
		if (args == null || args.length == 0) {
			core.broadcastToPlayer(BroadcastType.ERROR, player, "/gunban (Player)");
			core.broadcastToPlayer(BroadcastType.ERROR, player, "Example: /gunban Snick");
            return;
		}
		
		if (args[0]!=null) {
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
			if (playerUUID != null) {
				if (this.core.banFactory.isBanned(playerUUID)) {
					this.core.banFactory.removeBan(playerUUID);
					core.broadcastToPlayer(BroadcastType.INFO, player, "Player has been unbanned.");	
				} else {
					core.broadcastToPlayer(BroadcastType.ERROR, player, "Player is not banned!");
				}
				
			} else {
				core.broadcastToPlayer(BroadcastType.ERROR, player, "Error unbanning player!");
			}
			
		} else {
			core.broadcastToPlayer(BroadcastType.ERROR, player, "Please input a proper player name!");
		}
		
	}
}