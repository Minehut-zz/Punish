package core.commads;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import core.Punish;
import core.Punish.BroadcastType;

public class StaffChatCommand extends Command {
	
	private Punish core;
	
	public StaffChatCommand(Punish core) {
		super("s");
		this.core = core;
	}

	@Override
	public void execute(CommandSender commandSender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer)commandSender;
		if (!player.hasPermission("minehut.mod")) {
			return;
		}
		if (args == null || args.length == 0) {
			core.broadcastToPlayer(BroadcastType.ERROR, player, "/s (Message)");
			core.broadcastToPlayer(BroadcastType.ERROR, player, "Example: /s I need help!");
            return;
		}
		
		
		String message = "[" + player.getServer().getInfo().getName() + "]" + player.getName() + "> ";
		if (args[0]!=null) {
			for (int i = 0; i < args.length; i++) {
				message = message + ((i==0)?"":" ") + args[i];
			}
		} else {
			core.broadcastToPlayer(BroadcastType.ERROR, player, "Please input a message!");
			return;
		}
		
		core.broadcastToStaff(BroadcastType.STAFF, message);
		
	}
}
