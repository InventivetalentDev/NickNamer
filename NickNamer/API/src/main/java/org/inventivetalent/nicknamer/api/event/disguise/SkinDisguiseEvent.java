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

package org.inventivetalent.nicknamer.api.event.disguise;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;

import javax.annotation.Nonnull;
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
public class SkinDisguiseEvent extends ProfileDisguiseEvent implements Cancellable {

	private final String originalSkin;
	private       String skin;

	public SkinDisguiseEvent(@Nonnull OfflinePlayer disguised, @Nonnull Player receiver, @Nonnull GameProfileWrapper gameProfile, @Nullable String skin) {
		super(disguised, receiver, gameProfile);
		this.originalSkin = this.skin = skin;
	}

	/**
	 * @return The player's skin, or the player's name
	 */
	@Nullable
	public String getSkin() {
		return skin;
	}

	/**
	 * @param skin The new skin
	 */
	public void setSkin(@Nullable String skin) {
		this.skin = skin;
	}

	/**
	 * @return <code>true</code> if the skin is disguised
	 */
	public boolean isDisguised() {
		return !isCancelled() && skin != null && !originalSkin.equals(skin);
	}

	private static HandlerList handlerList = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
