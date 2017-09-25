package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * Event called when a name in scoreboard objectives is replaced
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ScoreboardReplacementEvent extends NameReplacementEvent {

	public ScoreboardReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receiver, ReplaceType.SCOREBOARD, context, original, replacement);
	}

	public ScoreboardReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull ReplaceType replaceType, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receiver, replaceType, context, original, replacement);
	}
}
