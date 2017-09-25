package org.inventivetalent.nicknamer.api.event.disguise;

import lombok.NonNull;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;

/**
 * Base event for player disguises
 * The {@link NickDisguiseEvent} and {@link SkinDisguiseEvent} are called every time a name/skin update is sent to another player.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ProfileDisguiseEvent extends DisguiseEvent implements Cancellable {

	private GameProfileWrapper gameProfile;

	private boolean cancelled;

	public ProfileDisguiseEvent(@NonNull OfflinePlayer disguised, @NonNull Player receiver, @NonNull GameProfileWrapper gameProfile) {
		super(disguised, receiver);
		this.gameProfile = gameProfile;
	}

	/**
	 * @return The GameProfile
	 */
	@NonNull
	public GameProfileWrapper getGameProfile() {
		return gameProfile;
	}

	/**
	 * @param gameProfile The new GameProfile
	 */
	public void setGameProfile(@NonNull GameProfileWrapper gameProfile) {
		this.gameProfile = gameProfile;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		cancelled = b;
	}

}
