package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

public class ScoreboardTeamReplacementEvent extends ScoreboardReplacementEvent {
	public ScoreboardTeamReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receiver, ReplaceType.SCOREBOARD_TEAM, context, original, replacement);
	}
}
