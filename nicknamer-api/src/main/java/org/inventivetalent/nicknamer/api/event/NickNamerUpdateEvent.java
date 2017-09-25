package org.inventivetalent.nicknamer.api.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event called when the name or skin of a player is updated to a observer All changes made to the event only affect the current observer If the name or skin is set to null, the original value will be used
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NickNamerUpdateEvent extends Event implements Cancellable {

	private static HandlerList handlers = new HandlerList();
	private final OfflinePlayer player;
	private final Player observer;
	private String nick;
	private String skin;
	private boolean cancelled;

	public NickNamerUpdateEvent(OfflinePlayer who, Player observer, String nick, String skin) {
		super(true);

		this.player = who;
		this.observer = observer;
		this.nick = nick;
		this.skin = skin;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * @return The {@link Player} which is being updated
	 */
	public final Player getPlayer() {
		return player.getPlayer();
	}

	/**
	 * @return The {@link OfflinePlayer} which is being updated
	 */
	@Nonnull
	public final OfflinePlayer getOfflinePlayer() {
		return player;
	}

	/**
	 * @return The {@link Player} which sees the update
	 */
	@Nonnull
	public final Player getObserver() {
		return observer;
	}

	//	/**
	//	 * @return The {@link UUID} of the used Skin
	//	 */
	//	@Nullable
	//	public UUID getSkin() {
	//		return skin;
	//	}

	/**
	 * @return The nickname used by the player (or the real name, if no nickname is used)
	 */
	@Nullable
	public String getNick() {
		return nick;
	}

	//	/**
	//	 * Changes the skin
	//	 *
	//	 * @param skin The {@link UUID} of the new skin
	//	 */
	//	public void setSkin(@Nullable UUID skin) {
	//		this.skin = skin;
	//	}

	/**
	 * Changes the nickname
	 *
	 * @param nick The new nickname
	 */
	public void setNick(@Nullable String nick) {
		if (nick != null && nick.length() > 16) {
			throw new IllegalArgumentException("The name cannot be longer than 16 characters");
		}
		this.nick = nick;
	}

	/**
	 * @return The used Skin
	 */
	public String getSkin() {
		return skin;
	}

	/**
	 * Changes the skin
	 *
	 * @param skin The new Skin
	 */
	public void setSkin(String skin) {
		this.skin = skin;
	}

	/**
	 * @return <code>true</code> if the update is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Cancels the update (the real name and skin will be visible if <code>true</code>)
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
