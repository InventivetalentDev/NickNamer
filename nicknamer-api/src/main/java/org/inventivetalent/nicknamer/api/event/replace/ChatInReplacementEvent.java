package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * Event called when a name in an incoming chat message is replaced
 */
public class ChatInReplacementEvent extends NameReplacementEvent {

	public ChatInReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receiver, ReplaceType.CHAT_IN, context, original, replacement);
	}
}
