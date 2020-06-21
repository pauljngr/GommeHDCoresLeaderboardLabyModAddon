package de.paul.addon.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import de.paul.addon.CoresLeaderboardAddon;
import de.paul.addon.CoresStats;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.ingamegui.moduletypes.ColoredTextModule;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;

public class CoresLeaderboardModule extends ColoredTextModule {

	private final int numLines = 6;

	private CoresLeaderboardAddon addon;

	private List<List<Text>> lastLines = null;

	public CoresLeaderboardModule(CoresLeaderboardAddon addon) {
		this.addon = addon;
	}

	@Override
	public int getLines() {
		return numLines;
	}

	@Override
	public List<List<Text>> getTexts() {

		if (lastLines != null && !addon.changes)
			return lastLines;

		addon.changes = false;

		List<List<Text>> lines = new ArrayList<List<Text>>();
		Collection<CoresStats> statsColl = addon.getPlayerStats();

		if (addon.inCoresRound() && statsColl != null) {

			CoresStats[] stats = statsColl.toArray(new CoresStats[statsColl.size()]);
			/*
			 * CoresStats[] stats = new CoresStats[3]; stats[0] = new
			 * CoresStats("pianoscool"); stats[1] = new CoresStats("test123"); stats[2] =
			 * new CoresStats("abcde"); stats[0].addKill(); stats[0].addDeath();
			 * stats[0].addKill(); stats[1].addKill(); stats[1].addKill();
			 * stats[1].addKill(); stats[2].addKill(); stats[2].addKill();
			 * stats[2].addDeath(); stats[2].addDeath();
			 */
			Arrays.sort(stats, 0, stats.length, new Comparator<CoresStats>() {
				@Override
				public int compare(CoresStats o1, CoresStats o2) {
					if (o1.getKills() > o2.getKills())
						return -1;
					else if (o1.getKills() == o2.getKills())
						if (o1.getKD() > o2.getKD())
							return -1;
						else if (o1.getKD() == o2.getKD())
							return 0;
						else
							return 1;
					else
						return 1;
				}
			});

			for (int i = 0; i < Math.min(numLines - 1, stats.length); i++) {
				addLine(lines, stats, i, false);
			}

			// you
			int yourIndex = -1;
			for (int i = 0; i < stats.length; i++) {
				if (stats[i].getPlayerName().equals(Minecraft.getMinecraft().thePlayer.getName())) {
					yourIndex = i;
					break;
				}
			}
			if (yourIndex != -1) {
				List<Text> line = new ArrayList<Text>();
				line.add(new Text("-", Color.LIGHT_GRAY.getRGB()));
				lines.add(line);
				addLine(lines, stats, yourIndex, true);
			}

			this.lastLines = lines;
			return lines;
		}

		List<Text> list = new ArrayList<Text>();
		list.add(new Text("Not in cores round (Right text alignment doesn't seem to work)", Color.WHITE.getRGB()));
		lines.add(list);
		return lines;
	}

	private void addLine(List<List<Text>> lines, CoresStats[] stats, int index, boolean lastLine) {
		List<Text> list = new ArrayList<Text>();
		list.add(new Text((index + 1) + ". ", Color.ORANGE.getRGB()));
		if (!lastLine && Minecraft.getMinecraft().thePlayer.getName().equals(stats[index].getPlayerName()))
			list.add(new Text(stats[index].getPlayerName(), Color.ORANGE.getRGB()));
		else if (stats[index].isBlueTeam())
			list.add(new Text(stats[index].getPlayerName(), Color.BLUE.getRGB()));
		else
			list.add(new Text(stats[index].getPlayerName(), Color.RED.getRGB()));

		list.add(new Text(" | ", Color.BLACK.getRGB()));

		list.add(new Text(String.valueOf(stats[index].getKills()), Color.WHITE.getRGB()));
		list.add(new Text("/", Color.ORANGE.getRGB()));
		list.add(new Text(String.valueOf(stats[index].getDeaths()), Color.WHITE.getRGB()));

		list.add(new Text("  ", Color.ORANGE.getRGB()));
		list.add(new Text(String.valueOf(stats[index].getKD()), Color.WHITE.getRGB()));

		lines.add(list);
	}
	
	@Override
	public boolean isShown() {
		return addon.inCoresRound() && addon.timeSinceLastMessage() < 20000;
	}

	@Override
	public String getDescription() {
		return "Shows the Cores Leaderboard";
	}

	@Override
	public IconData getIconData() {
		return new IconData(Material.BEACON);
	}

	@Override
	public ModuleCategory getCategory() {
		return ModuleCategoryRegistry.CATEGORY_INFO;
	}

	@Override
	public String getControlName() {
		return "Cores Leaderboard";
	}

	@Override
	public String getSettingName() {
		return "GommeHD_Cores_Leaderboard";
	}

	@Override
	public int getSortingId() {
		return 0;
	}

	@Override
	public void loadSettings() {
	}
}
