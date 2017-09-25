package org.inventivetalent.nicknamer.api.event.skin;

import lombok.NonNull;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SkinLoadedEvent extends Event {

	private static HandlerList handlerList = new HandlerList();
	private String owner;
	private GameProfileWrapper gameProfileWrapper;

	public SkinLoadedEvent(@NonNull String owner, @NonNull GameProfileWrapper gameProfileWrapper) {
		this.owner = owner;
		this.gameProfileWrapper = gameProfileWrapper;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

	public String getOwner() {
		return owner;
	}

	public GameProfileWrapper getGameProfile() {
		return gameProfileWrapper;
	}

	public void setGameProfile(@NonNull GameProfileWrapper gameProfileWrapper) {
		this.gameProfileWrapper = gameProfileWrapper;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
