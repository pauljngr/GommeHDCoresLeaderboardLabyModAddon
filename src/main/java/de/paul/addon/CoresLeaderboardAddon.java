package de.paul.addon;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.paul.addon.modules.CoresLeaderboardModule;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.MessageReceiveEvent;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class CoresLeaderboardAddon extends LabyModAddon {

	private boolean enabled = false;

	private Minecraft mc = Minecraft.getMinecraft();

	private boolean inCoresRound = false;

	private HashMap<String, CoresStats> playerStats;

	private long lastCoresMessage = 0;

	public boolean changes = false;

	@Override
	protected void fillSettings(List<SettingsElement> settings) {
		System.out.println("test");
		settings.add(new BooleanElement("Enabled", new IconData(Material.LEVER), new Consumer<Boolean>() {
			@Override
			public void accept(Boolean bool) {
				enabled = bool;
				getConfig().addProperty("Enabled", bool);
			}
		}, false));
	}

	@Override
	public void loadConfig() {
		this.enabled = getConfig().has("Enabled") ? getConfig().get("Enabled").getAsBoolean() : true;
	}

	private void addKill(String playerName, boolean blueTeam) {
		CoresStats stats = getPlayerStats(playerName, blueTeam);
		if (stats != null)
			stats.addKill();
	}

	private void addDeath(String playerName, boolean blueTeam) {
		CoresStats stats = getPlayerStats(playerName, blueTeam);
		if (stats != null)
			stats.addDeath();
	}

	private CoresStats getPlayerStats(String playerName, boolean blueTeam) {
		if (playerName != null && playerName.length() > 0) {
			if (playerStats.containsKey(playerName))
				return playerStats.get(playerName);
			else {
				CoresStats stats = new CoresStats(playerName, blueTeam);
				playerStats.put(playerName, stats);
				return stats;
			}
		} else
			return null;
	}

	@Override
	public void onEnable() {
		System.out.println("CoresLeaderboard Addon started");
		this.getApi().registerForgeListener(this);
		this.getApi().getEventManager().register(new MessageReceiveEvent() {
			@Override
			public boolean onReceive(String arg0, String arg1) {
				System.out.println(arg0 + " ||| " + arg1);
				
				if (arg1.contains("hat das Spiel betreten") && arg1.contains(mc.thePlayer.getName())) {
					playerStats = new HashMap<String, CoresStats>();
					inCoresRound = true;
					System.out.println("Entered cores round");
					changes = true;
				}
				
				if (arg0.contains("Cores")) {
					lastCoresMessage = System.currentTimeMillis();
					if (!inCoresRound) {
						playerStats = new HashMap<String, CoresStats>();
						inCoresRound = true;
						System.out.println("Entered cores round");
					}

					if (arg1.contains("ist gestorben")) {
						System.out.println("died: " + arg1);
						String[] s = arg1.split(" ");
						if (s.length >= 2) {
							boolean blueTeam = isBlueTeam(arg0.split(" ")[1]);
							addDeath(s[1], blueTeam);
							System.out.println("death: " + s[1]);
						}
					} else if (arg1.contains("wurde von")) {
						String[] s = arg1.split(" ");
						if (s.length >= 5) {
							addDeath(s[1], isBlueTeam(arg0.split(" ")[1]));
							addKill(s[4], isBlueTeam(arg0.split(" ")[4]));
							System.out.println("kill: " + s[1] + " -> " + s[4]);
						}
					}

					/*
					 * for (CoresStats stats : playerStats.values()) {
					 * System.out.println(stats.getPlayerName() + " " + stats.getKills() + " " +
					 * stats.getDeaths() + " " + stats.getKD()); }
					 */

					changes = true;
				}
				return false;
			}
		});

		this.getApi().registerModule(new CoresLeaderboardModule(this));
	}

	private boolean isBlueTeam(String nameWithColor) {
		if(nameWithColor.contains("§c"))
			return false;
		else if (nameWithColor.contains("§9"))
			return true;
		else {
			System.err.println("unknown color: " + nameWithColor);
			return true;
		}
	}

	public Collection<CoresStats> getPlayerStats() {
		if (playerStats != null)
			return playerStats.values();
		return null;
	}

	public boolean inCoresRound() {
		return inCoresRound;
	}

	public long timeSinceLastMessage() {
		return System.currentTimeMillis() - lastCoresMessage;
	}
	
	@SubscribeEvent
	public void onTick(final ClientTickEvent event) {
		if (inCoresRound && System.currentTimeMillis() - lastCoresMessage > 120000) {
			inCoresRound = false;
			System.out.println("Left cores round");
			changes = true;
		}
	}
}
