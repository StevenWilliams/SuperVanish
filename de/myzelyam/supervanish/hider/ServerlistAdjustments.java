package de.myzelyam.supervanish.hider;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import de.myzelyam.supervanish.SVUtils;
import de.myzelyam.supervanish.SuperVanish;

public class ServerlistAdjustments extends SVUtils {

	public static void setupProtocolLib() {
		if ((!cfg
				.getBoolean("Configuration.Serverlist.AdjustAmountOfOnlinePlayers"))
				&& (!cfg.getBoolean("Configuration.Serverlist.AdjustListOfLoggedInPlayers")))
			return;
		final SuperVanish sv = plugin;
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(plugin, ListenerPriority.NORMAL,
						PacketType.Status.Server.OUT_SERVER_INFO) {

					@Override
					public void onPacketSending(PacketEvent e) {
						try {
							if (e.getPacketType() == PacketType.Status.Server.OUT_SERVER_INFO) {
								WrappedServerPing ping = e.getPacket()
										.getServerPings().read(0);
								List<String> vpl = pd
										.getStringList("InvisiblePlayers");
								int vpls = 0;
								int pls = Bukkit.getOnlinePlayers().size();
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (vpl.contains(p.getUniqueId().toString())) {
										vpls++;
									}
								}
								if (cfg.getBoolean("Configuration.Serverlist.AdjustAmountOfOnlinePlayers")) {
									ping.setPlayersOnline(pls - vpls);
								}
								if (cfg.getBoolean("Configuration.Serverlist.AdjustListOfLoggedInPlayers")) {
									List<WrappedGameProfile> wgps = new ArrayList<WrappedGameProfile>();
									for (Player p : Bukkit.getOnlinePlayers()) {
										WrappedGameProfile wgp = WrappedGameProfile
												.fromPlayer(p);
										if (!vpl.contains(p.getUniqueId()
												.toString()))
											wgps.add(wgp);
									}
									ping.setPlayers(wgps);
								}
							}
						} catch (Exception er) {
							sv.printException(er);
						}
					}
				});
	}
}