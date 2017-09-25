package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

/**
 * Event called when a name in a message sent by a player is being replaced
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class ChatReplacementEvent extends NameReplacementEvent {

	private Collection<Player> receivers = new HashSet<>();

	public ChatReplacementEvent(@NonNull Player disguised, @NonNull Collection<? extends Player> receivers, @NonNull String context, @NonNull String original, String replacement) {
		this(disguised, receivers, ReplaceType.PLAYER_CHAT, context, original, replacement);
	}

	public ChatReplacementEvent(@NonNull Player disguised, @NonNull Collection<? extends Player> receivers, @NonNull ReplaceType replaceType, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receivers.iterator().next(), replaceType, context, original, replacement);
		this.receivers.addAll(receivers);
	}

	public Collection<Player> getReceivers() {
		return receivers;
	}
}
