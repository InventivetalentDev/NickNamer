package org.inventivetalent.nicknamer.api.event;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when skin/name are updated to the player themselves
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NickNamerSelfUpdateEvent extends Event implements Cancellable {

	private static HandlerList handlerList = new HandlerList();
	private Player player;
	private String name;
	private Object gameProfile;
	private Difficulty difficulty;
	private GameMode gameMode;
	private boolean cancelled;

	public NickNamerSelfUpdateEvent(Player player, String name, Object gameProfile, Difficulty difficulty, GameMode gameMode) {
		this.player = player;
		this.name = name;
		this.gameProfile = gameProfile;
		this.difficulty = difficulty;
		this.gameMode = gameMode;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

	/**
	 * @return The updated {@link Player}
	 */
	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getGameProfile() {
		return gameProfile;
	}

	public void setGameProfile(Object gameProfile) {
		this.gameProfile = gameProfile;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		cancelled = b;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
