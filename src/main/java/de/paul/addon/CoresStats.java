package de.paul.addon;

public class CoresStats {

	private final String playerName;
	private final boolean blueTeam;

	private int kills = 0;
	private int deaths = 0;
	
	
	public CoresStats(String playerName, boolean blueTeam) {
		this.playerName = playerName;
		this.blueTeam = blueTeam;
	}
	
	public int getKills() {
		return kills;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public void addKill() {
		kills++;
	}
	
	public void addDeath() {
		deaths++;
	}
	
	public double getKD() {
		return Math.floor((double) kills / deaths * 100) / 100;
	}
	
	public String getPlayerName() {
		return this.playerName;
	}
	
	public boolean isBlueTeam() {
		return this.blueTeam;
	}
}
