package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Event called whan a name in a quit-message is replaced
 */
public class PlayerQuitReplacementEvent extends ChatReplacementEvent {

	public PlayerQuitReplacementEvent(@NonNull Player disguised, @NonNull Collection<? extends Player> receivers, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receivers, ReplaceType.PLAYER_QUIT, context, original, replacement);
	}

}
