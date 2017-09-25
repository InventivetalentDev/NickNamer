package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * Called when the target-playername for a scoreboard score is replaced
 */
public class ScoreboardScoreReplacementEvent extends ScoreboardReplacementEvent {
	public ScoreboardScoreReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receiver, ReplaceType.SCOREBOARD_SCORE, context, original, replacement);
	}
}
