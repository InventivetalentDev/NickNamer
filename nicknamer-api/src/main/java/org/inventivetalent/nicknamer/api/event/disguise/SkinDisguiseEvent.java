package org.inventivetalent.nicknamer.api.event.disguise;

import lombok.NonNull;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;

import javax.annotation.Nullable;

/**
 * Event called when a player's skins is disguised
 * <p>
 * The skin won't be changed if
 * - the skin is equal to the player's name
 * - the skin is null
 * - the event is cancelled
 *
 * @see ProfileDisguiseEvent
 * @see NickDisguiseEvent
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SkinDisguiseEvent extends ProfileDisguiseEvent implements Cancellable {

	private static HandlerList handlerList = new HandlerList();
	private final String originalSkin;
	private String skin;

	public SkinDisguiseEvent(@NonNull OfflinePlayer disguised, @NonNull Player receiver, @NonNull GameProfileWrapper gameProfile, @Nullable String skin) {
		super(disguised, receiver, gameProfile);
		this.originalSkin = this.skin = skin;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

	/**
	 * @return The player's skin, or the player's name
	 */
	public String getSkin() {
		return skin;
	}

	/**
	 * @param skin The new skin
	 */
	public void setSkin(String skin) {
		this.skin = skin;
	}

	/**
	 * @return <code>true</code> if the skin is disguised
	 */
	public boolean isDisguised() {
		return !isCancelled() && skin != null && !originalSkin.equals(skin);
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
