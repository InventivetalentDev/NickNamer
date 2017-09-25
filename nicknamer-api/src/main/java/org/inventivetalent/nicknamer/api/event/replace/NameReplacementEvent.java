package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.inventivetalent.nicknamer.api.event.disguise.DisguiseEvent;

/**
 * Event called whenever the API finds a name to disguise
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NameReplacementEvent extends DisguiseEvent implements Cancellable {

	private static HandlerList handlerList = new HandlerList();
	private ReplaceType replaceType;
	private String context;
	private String original;
	private String replacement;

	public NameReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull ReplaceType replaceType, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receiver);
		this.replaceType = replaceType;
		this.context = context;
		this.original = original;
		this.replacement = replacement;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

	/**
	 * @return The player whose name is being replaced
	 */
	@NonNull
	@Override
	public OfflinePlayer getDisguised() {
		return super.getDisguised();
	}

	/**
	 * @return The player whose name is being replaced
	 */
	@Override
	public Player getPlayer() {
		return super.getPlayer();
	}

	/**
	 * @return The {@link ReplaceType} of this event
	 */
	@NonNull
	public ReplaceType getReplaceType() {
		return replaceType;
	}

	/**
	 * @return The full message the name was replaced in
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @return The original name
	 */
	public String getOriginal() {
		return original;
	}

	/**
	 * @return The replacement name (or the original name, if nothing has been replaced yet)
	 */
	public String getReplacement() {
		return replacement;
	}

	/**
	 * @param replacement The replacement name
	 */
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
