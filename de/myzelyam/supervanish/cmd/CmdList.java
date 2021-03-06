package de.myzelyam.supervanish.cmd;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.myzelyam.supervanish.SVUtils;

public class CmdList extends SVUtils {

	public CmdList(CommandSender p, String[] args, String label) {
		if (canDo(p, CommandAction.LIST)) {
			String listMessage = getMsg("ListMessage");
			StringBuffer sb = new StringBuffer();
			if (getInvisiblePlayers().size() == 0) {
				sb = sb.append("none");
			}
			for (int i = 0; i < getInvisiblePlayers().size(); i++) {
				String s = getInvisiblePlayers().get(i);
				UUID s2 = UUID.fromString(s);
				String pn = Bukkit.getOfflinePlayer(s2).getName();
				if (Bukkit.getPlayer(s2) == null) {
					pn = pn + ChatColor.RED + "[offline]" + ChatColor.WHITE;
				}
				sb = sb.append(pn);
				if (i != (getInvisiblePlayers().size() - 1))
					sb = sb.append(ChatColor.GREEN + ", " + ChatColor.WHITE);
			}
			listMessage = listMessage.replaceAll("%l", sb.toString());
			p.sendMessage(convertString(listMessage, p));
		}
	}
}
