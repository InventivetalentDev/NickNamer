package org.inventivetalent.nicknamer.api.event.random;

import java.util.List;

public class RandomNickRequestEvent extends RandomRequestEvent {
	public RandomNickRequestEvent(List<String> possibilities) {
		super(possibilities);
	}
}
