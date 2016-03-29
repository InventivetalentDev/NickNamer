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

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.inventivetalent.nicknamer.api.wrapper.GameProfileWrapper;

import javax.annotation.Nonnull;

/**
 * Base event for player disguises
 * The {@link NickDisguiseEvent} and {@link SkinDisguiseEvent} are called every time a name/skin update is sent to another player.
 */
public abstract class ProfileDisguiseEvent extends DisguiseEvent implements Cancellable {

	private GameProfileWrapper gameProfile;

	private boolean cancelled;

	public ProfileDisguiseEvent(@Nonnull Player disguised, @Nonnull Player receiver, @Nonnull GameProfileWrapper gameProfile) {
		super(disguised, receiver);
		this.gameProfile = gameProfile;
	}

	/**
	 * @return The GameProfile
	 */
	@Nonnull
	public GameProfileWrapper getGameProfile() {
		return gameProfile;
	}

	/**
	 * @param gameProfile The new GameProfile
	 */
	public void setGameProfile(@Nonnull GameProfileWrapper gameProfile) {
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
