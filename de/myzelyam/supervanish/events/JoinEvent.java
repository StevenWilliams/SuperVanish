package de.myzelyam.supervanish.events;

import java.util.List;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hider.ActionBarManager;
import de.myzelyam.supervanish.hider.PlayerHider;
import de.myzelyam.supervanish.hider.TabManager;
import de.myzelyam.supervanish.hider.TabManager.SVTabAction;
import de.myzelyam.supervanish.hooks.EssentialsHook;

public class JoinEvent extends PlayerHider implements EventExecutor, Listener {

	@Override
	public void execute(Listener l, Event event) {
		try {
			if (event instanceof PlayerJoinEvent) {
				PlayerJoinEvent e = (PlayerJoinEvent) event;
				final Player p = e.getPlayer();
				final List<String> vpl = pd.getStringList("InvisiblePlayers");
				// compatibility delays
				int hideDelay = cfg
						.getInt("Configuration.CompatibilityOptions.AdctionDelay.HideDelayOnJoinInTicks");
				if (!cfg.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
					hideDelay = 0;
				int tabDelay = cfg
						.getInt("Configuration.CompatibilityOptions.ActionDelay.TabNameChangeDelayOnJoinInTicks");
				if (!cfg.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
					tabDelay = 0;
				int invisDelay = cfg
						.getInt("Configuration.CompatibilityOptions.ActionDelay.InvisibilityPotionDelayOnJoinInTicks");
				if (!cfg.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
					invisDelay = 0;
				// ghost players
				if (cfg.getBoolean("Configuration.Players.EnableGhostPlayers")
						&& plugin.ghostTeam != null
						&& !plugin.ghostTeam.hasPlayer(p)) {
					if (p.hasPermission("sv.see") || p.hasPermission("sv.use")
							|| vpl.contains(p.getUniqueId().toString()))
						plugin.ghostTeam.addPlayer(p);
				}
				// Join-Message
				if (cfg.getBoolean(
						"Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible",
						true)
						&& vpl.contains(p.getUniqueId().toString())) {
					e.setJoinMessage(null);
				}
				// vanished:
				if (vpl.contains(p.getUniqueId().toString())) {
					// Essentials
					if (plugin.getServer().getPluginManager()
							.getPlugin("Essentials") != null
							&& cfg.getBoolean("Configuration.Hooks.EnableEssentialsHook")) {
						EssentialsHook.hidePlayer(p);
					}
					// remember message
					if (cfg.getBoolean("Configuration.Messages.RememberInvisiblePlayersOnJoin")) {
						p.sendMessage(plugin.convertString(
								getMsg("RememberMessage"), p));
					}
					// BAR-API
					if (plugin.getServer().getPluginManager()
							.getPlugin("BarAPI") != null
							&& cfg.getBoolean("Configuration.Messages.UseBarAPI")) {
						displayBossBar(p);
					}
					// HIDE/SHOW
					if (hideDelay > 0) {
						Bukkit.getServer()
								.getScheduler()
								.scheduleSyncDelayedTask(plugin,
										new Runnable() {

											@Override
											public void run() {
												hideToAll(p);
											}
										}, hideDelay);
					} else {
						hideToAll(p);
					}
					// readd invisibility if required
					if ((!p.getActivePotionEffects().contains(
							PotionEffectType.INVISIBILITY))
							&& cfg.getBoolean("Configuration.Players.EnableGhostPlayers")) {
						if (invisDelay > 0) {
							Bukkit.getServer()
									.getScheduler()
									.scheduleSyncDelayedTask(plugin,
											new Runnable() {

												@Override
												public void run() {
													p.addPotionEffect(new PotionEffect(
															PotionEffectType.INVISIBILITY,
															Integer.MAX_VALUE,
															1));
												}
											}, invisDelay);
						} else {
							p.addPotionEffect(new PotionEffect(
									PotionEffectType.INVISIBILITY,
									Integer.MAX_VALUE, 1));
						}
					}
					// readd action bar
					if (plugin.getServer().getPluginManager()
							.getPlugin("ProtocolLib") != null
							&& cfg.getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
							&& !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
						ActionBarManager.getInstance(plugin).addActionBar(p);
					}
					//
				}
				// not necessarily vanished:
				if (hideDelay > 0) {
					Bukkit.getServer().getScheduler()
							.scheduleSyncDelayedTask(plugin, new Runnable() {

								@Override
								public void run() {
									hideAllInvisibleTo(p);
								}
							}, hideDelay);
				} else {
					hideAllInvisibleTo(p);
				}
				// TAB //
				if (cfg.getBoolean("Configuration.Tablist.ChangeTabNames")
						&& vpl.contains(p.getUniqueId().toString())) {
					if (tabDelay > 0) {
						Bukkit.getServer()
								.getScheduler()
								.scheduleSyncDelayedTask(plugin,
										new Runnable() {

											@Override
											public void run() {
												TabManager
														.getInstance()
														.adjustTabname(
																p,
																SVTabAction.SET_CUSTOM_TABNAME);
											}
										}, tabDelay);
					} else {
						TabManager.getInstance().adjustTabname(p,
								SVTabAction.SET_CUSTOM_TABNAME);
					}
				}
				// remove invisibility if required
				if (pd.getBoolean("PlayerData.Player."
						+ p.getUniqueId().toString() + ".remInvis")
						&& !vpl.contains(p.getUniqueId().toString())) {
					remInvis(p);
				}
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	private void displayBossBar(final Player p) {
		final String bbvm = getMsg("Messages.BossBarVanishMessage");
		String bbremembermsg = getMsg("Messages.BossBarRememberMessage");
		BarAPI.setMessage(p, plugin.convertString(bbremembermsg, p), 100f);
		Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						final List<String> vpl2 = pd
								.getStringList("InvisiblePlayers");
						if (vpl2.contains(p.getUniqueId().toString()))
							BarAPI.setMessage(p, plugin.convertString(bbvm, p),
									100f);
					}
				}, 100);
	}

	private void remInvis(Player p) {
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		pd.set("PlayerData.Player." + p.getUniqueId().toString() + ".remInvis",
				null);
		plugin.spd();
	}
}