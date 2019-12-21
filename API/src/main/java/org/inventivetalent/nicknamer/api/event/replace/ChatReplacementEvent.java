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

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Event called when a name in a message sent by a player is being replaced
 */
public class ChatReplacementEvent extends NameReplacementEvent {

	private Collection<Player> receivers = new HashSet<>();

	public ChatReplacementEvent(@Nonnull Player disguised, @Nonnull Collection<? extends Player> receivers, @Nonnull String context, @Nonnull String original, @Nullable String replacement, boolean async) {
		this(disguised, receivers, ReplaceType.PLAYER_CHAT, context, original, replacement, async);
	}

	public ChatReplacementEvent(@Nonnull Player disguised, @Nonnull Collection<? extends Player> receivers, @Nonnull String context, @Nonnull String original, @Nullable String replacement) {
		this(disguised, receivers, ReplaceType.PLAYER_CHAT, context, original, replacement);
	}

	public ChatReplacementEvent(@Nonnull Player disguised, @Nonnull Collection<? extends Player> receivers, @Nonnull ReplaceType replaceType, @Nonnull String context, @Nonnull String original, @Nullable String replacement, boolean async) {
		super(disguised, receivers.iterator().next(), replaceType, context, original, replacement, async);
		this.receivers.addAll(receivers);
	}

	public ChatReplacementEvent(@Nonnull Player disguised, @Nonnull Collection<? extends Player> receivers, @Nonnull ReplaceType replaceType, @Nonnull String context, @Nonnull String original, @Nullable String replacement) {
		super(disguised, receivers.iterator().next(), replaceType, context, original, replacement);
		this.receivers.addAll(receivers);
	}

	public Collection<Player> getReceivers() {
		return receivers;
	}
}
