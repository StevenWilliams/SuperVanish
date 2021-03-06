package de.myzelyam.supervanish.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hider.PlayerHider;
import de.myzelyam.supervanish.hider.TabManager;
import de.myzelyam.supervanish.hider.TabManager.SVTabAction;

public class WorldChangeEvent extends PlayerHider implements Listener {

	private SuperVanish plugin = (SuperVanish) Bukkit.getServer()
			.getPluginManager().getPlugin("SuperVanish");

	private static WorldChangeEvent instance;

	public static WorldChangeEvent getInstance() {
		if (instance == null)
			instance = new WorldChangeEvent();
		return instance;
	}

	private WorldChangeEvent() {
		// compatibility delays
		hideDelay = cfg
				.getInt("Configuration.CompatibilityOptions.AdctionDelay.HideDelayOnWorldChangeInTicks");
		if (!cfg.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
			hideDelay = 0;
		invisDelay = cfg
				.getInt("Configuration.CompatibilityOptions.ActionDelay.InvisibilityPotionDelayOnWorldChangeInTicks");
		if (!cfg.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
			invisDelay = 0;
		tabDelay = cfg
				.getInt("Configuration.CompatibilityOptions.ActionDelay.TabNameChangeDelayOnWorldChangeInTicks");
		if (!cfg.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
			tabDelay = 0;
	}

	private int hideDelay;

	private int invisDelay;

	private int tabDelay;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWC(PlayerChangedWorldEvent e) {
		try {
			final Player p = e.getPlayer();
			if (!isHidden(p))
				return;
			// check auto-reappear-option
			if (cfg.getBoolean("Configuration.Players.ReappearOnWorldChange")) {
				showPlayer(p);
				return;
			}
			// rehide
			if (hideDelay > 0) {
				Bukkit.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override
							public void run() {
								hideToAll(p);
							}
						}, hideDelay);
			} else {
				hideToAll(p);
			}
			// readd invis
			if ((!p.getActivePotionEffects().contains(
					PotionEffectType.INVISIBILITY))
					&& cfg.getBoolean("Configuration.Players.EnableGhostPlayers")) {
				if (invisDelay > 0) {
					Bukkit.getServer().getScheduler()
							.scheduleSyncDelayedTask(plugin, new Runnable() {

								@Override
								public void run() {
									p.addPotionEffect(new PotionEffect(
											PotionEffectType.INVISIBILITY,
											Integer.MAX_VALUE, 1));
								}
							}, invisDelay);
				} else {
					p.addPotionEffect(new PotionEffect(
							PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
				}
			}
			// readd night vision (removed in tp event)
			if (cfg.getBoolean("Configuration.Players.AddNightVision"))
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
			// readjust tab name
			if (cfg.getBoolean("Configuration.Tablist.ChangeTabNames")) {
				if (tabDelay > 0) {
					Bukkit.getServer().getScheduler()
							.scheduleSyncDelayedTask(plugin, new Runnable() {

								@Override
								public void run() {
									TabManager.getInstance().adjustTabname(p,
											SVTabAction.SET_CUSTOM_TABNAME);
								}
							}, tabDelay);
				} else {
					TabManager.getInstance().adjustTabname(p,
							SVTabAction.SET_CUSTOM_TABNAME);
				}
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}
}