package org.inventivetalent.nicknamer.api.event.disguise;

import lombok.NonNull;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;

/**
 * Event called when a player's name is disguised
 * <p>
 * The name won't be changed if
 * - the nick is equal to the player's name
 * - the nick is null
 * - the event is cancelled
 *
 * @see ProfileDisguiseEvent
 * @see SkinDisguiseEvent
 */
@SuppressWarnings("unused")
public class NickDisguiseEvent extends ProfileDisguiseEvent implements Cancellable {

	private static HandlerList handlerList = new HandlerList();
	private final String originalNick;
	private String nick;

	public NickDisguiseEvent(@NonNull OfflinePlayer disguised, @NonNull Player receiver, @NonNull GameProfileWrapper gameProfile, String nick) {
		super(disguised, receiver, gameProfile);
		this.originalNick = this.nick = nick;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

	/**
	 * @return The player's nick, or the player's actual name
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * @param nick The new nick
	 */
	public void setNick(String nick) {
		this.nick = nick;
	}

	/**
	 * @return <code>true</code> if the name is disguised
	 */
	public boolean isDisguised() {
		return !isCancelled() && nick != null && !originalNick.equals(nick);
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
