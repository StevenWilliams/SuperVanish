package de.myzelyam.supervanish.hider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import de.myzelyam.supervanish.SuperVanish;

public class ActionBarManager {

	private static ActionBarManager instance;

	private ActionBarManager(SuperVanish plugin) {
		startTimerTask(plugin);
	}

	public static ActionBarManager getInstance(SuperVanish plugin) {
		if (instance == null) {
			instance = new ActionBarManager(plugin);
		}
		return instance;
	}

	private List<Player> actionBars = new ArrayList<>();

	private void startTimerTask(final SuperVanish plugin) {
		new BukkitRunnable() {

			@Override
			public void run() {
				for (Player p : actionBars) {
					sendActionBar(
							p,
							plugin.convertString(
									plugin.getMsg("ActionBarMessage"), p));
				}
			}
		}.runTaskTimer(plugin, 0, 2 * 20);
	}

	private void sendActionBar(Player p, String bar) {
		String json = "{\"text\": \""
				+ ChatColor.translateAlternateColorCodes('&', bar) + "\"}";
		WrappedChatComponent msg = WrappedChatComponent.fromJson(json);
		PacketContainer chatMsg = new PacketContainer(
				PacketType.Play.Server.CHAT);
		chatMsg.getChatComponents().write(0, msg);
		chatMsg.getBytes().write(0, (byte) 2);
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, chatMsg);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Cannot send packet " + chatMsg, e);
		}
	}

	public void addActionBar(Player p) {
		if (!actionBars.contains(p))
			actionBars.add(p);
	}

	public void removeActionBar(Player p) {
		actionBars.remove(p);
	}
}
