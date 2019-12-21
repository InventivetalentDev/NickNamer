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

package org.inventivetalent.nicknamer.api.event.replace;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.inventivetalent.nicknamer.api.event.disguise.DisguiseEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event called whenever the API finds a name to disguise
 */
public class NameReplacementEvent extends DisguiseEvent implements Cancellable {

	private ReplaceType replaceType;
	private String      context;
	private String      original;
	private String      replacement;

	public NameReplacementEvent(@Nonnull Player disguised, @Nonnull Player receiver, @Nonnull ReplaceType replaceType, @Nonnull String context, @Nonnull String original, @Nullable String replacement, boolean async) {
		super(disguised, receiver, async);
		this.replaceType = replaceType;
		this.context = context;
		this.original = original;
		this.replacement = replacement;
	}


	public NameReplacementEvent(@Nonnull Player disguised, @Nonnull Player receiver, @Nonnull ReplaceType replaceType, @Nonnull String context, @Nonnull String original, @Nullable String replacement) {
		super(disguised, receiver);
		this.replaceType = replaceType;
		this.context = context;
		this.original = original;
		this.replacement = replacement;
	}

	/**
	 * @return The player whose name is being replaced
	 */
	@Nonnull
	@Override
	public OfflinePlayer getDisguised() {
		return super.getDisguised();
	}

	/**
	 * @return The player whose name is being replaced
	 */
	@Nullable
	@Override
	public Player getPlayer() {
		return super.getPlayer();
	}

	/**
	 * @return The {@link ReplaceType} of this event
	 */
	@Nonnull
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

	private static HandlerList handlerList = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
