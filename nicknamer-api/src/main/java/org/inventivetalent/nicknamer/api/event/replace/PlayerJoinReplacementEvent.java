package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Event called when a name in a join-message is replaced
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PlayerJoinReplacementEvent extends ChatReplacementEvent {

	public PlayerJoinReplacementEvent(@NonNull Player disguised, @NonNull Collection<? extends Player> receivers, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receivers, ReplaceType.PLAYER_JOIN, context, original, replacement);
	}
}
