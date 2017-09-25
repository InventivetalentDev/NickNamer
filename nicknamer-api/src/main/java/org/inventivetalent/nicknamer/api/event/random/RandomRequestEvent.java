package org.inventivetalent.nicknamer.api.event.random;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class RandomRequestEvent extends Event {

	private static HandlerList handlerList = new HandlerList();
	private List<String> possibilities;

	public RandomRequestEvent(List<String> possibilities) {
		this.possibilities = possibilities;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

	public Collection<String> getPossibilities() {
		return possibilities;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
