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

public class KickCommand extends Command {

    private Punish core;

    public KickCommand(Punish core) {
        super("gkick");
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer)sender;
        if (!player.hasPermission("minehut.mod")) {
            return;
        }
        if (args == null || args.length == 0) {
            core.broadcastToPlayer(BroadcastType.ERROR, player, "/gkick (Player) (Reason)");
            core.broadcastToPlayer(BroadcastType.ERROR, player, "Example: /gkick Snick Please don't spam");
            return;
        }

        if (args.length >= 2) {
            UUID playerUUID = null;
            ProxiedPlayer pPlayer = null;
            if (core.getProxy().getPlayer(args[0])!=null) {
                pPlayer = core.getProxy().getPlayer(args[0]);
                playerUUID = pPlayer.getUniqueId();
            } else {
//                core.broadcastToPlayer(BroadcastType.INFO, player, "That player is not online or that name is not valid!");
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

            if (args[1]==null) {
                core.broadcastToPlayer(BroadcastType.ERROR, player, "Please input a reason!");
                return;
            }

            String reason = "";
            for (int i = 1; i < args.length; i++) {
                reason = reason + ((i==1)?"":" ") + args[i];
            }
            if (pPlayer!=null) {
                TextComponent text = new TextComponent("You have been kicked by ");// + sender.getName() + " for " + args[1] + "\n\rReason: " + reason);
                text.setBold(true);
                TextComponent staff = new TextComponent(sender.getName());
                staff.setBold(true);
                staff.setColor(ChatColor.RED);
                text.addExtra(staff);
                TextComponent reasonText = new TextComponent("\r\nReason:" + reason);
                reasonText.setBold(true);
                reasonText.setColor(ChatColor.WHITE);
                text.addExtra(reasonText);


                pPlayer.getPendingConnection().disconnect(text);
                //core.broadcastToPlayer(BroadcastType.WARN, pPlayer, "You have been muted by " + sender.getName() + " for " + args[1]);
            }

            core.broadcastToStaff(BroadcastType.INFO, ChatColor.YELLOW + args[0] + ChatColor.WHITE + " was kicked!");
            core.broadcastToStaff(BroadcastType.INFO, ChatColor.GRAY + "Staff: " + ChatColor.AQUA + sender.getName());
            core.broadcastToStaff(BroadcastType.INFO, ChatColor.GRAY + "Reason: " + ChatColor.YELLOW + reason);

        } else {
            core.broadcastToPlayer(BroadcastType.ERROR, player, "/gkick (Player) (Reason)");
            core.broadcastToPlayer(BroadcastType.ERROR, player, "Example: /gkick Snick Please don't spam");
            return;
        }

    }

}