/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.nicknamer.api.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event called when the name or skin of a player is updated to a observer <br/>
 * All changes made to the event only affect the current observer <br/>
 * If the name or skin is set to null, the original value will be used
 */
public class NickNamerUpdateEvent extends Event implements Cancellable {

	private final OfflinePlayer player;
	private final Player        observer;
	private       String        nick;
	private       String        skin;

	private boolean cancelled;

	public NickNamerUpdateEvent(OfflinePlayer who, Player observer, String nick, String skin) {
		super(true);

		this.player = who;
		this.observer = observer;
		this.nick = nick;
		this.skin = skin;
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

	/**
	 * @return The nickname used by the player (or the real name, if no nickname is used)
	 */
	@Nullable
	public String getNick() {
		return nick;
	}

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

	//	/**
	//	 * @return The {@link UUID} of the used Skin
	//	 */
	//	@Nullable
	//	public UUID getSkin() {
	//		return skin;
	//	}

	/**
	 * @return The used Skin
	 */
	public String getSkin() {
		return skin;
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
	 * Changes the skin
	 *
	 * @param skin The new Skin
	 */
	public void setSkin(String skin) {
		this.skin = skin;
	}

	/**
	 * Cancels the update (the real name and skin will be visible if <code>true</code>)
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * @return <code>true</code> if the update is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	private static HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
