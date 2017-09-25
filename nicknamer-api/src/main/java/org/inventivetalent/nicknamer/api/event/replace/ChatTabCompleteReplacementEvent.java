package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ChatTabCompleteReplacementEvent extends NameReplacementEvent {

	public ChatTabCompleteReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull String context, @NonNull String original, @Nullable String replacement) {
		super(disguised, receiver, ReplaceType.CHAT_TAB_COMPLETE, context, original, replacement);
	}
}
