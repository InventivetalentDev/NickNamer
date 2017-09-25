package org.inventivetalent.nicknamer.api.event.random;

import java.util.List;

public class RandomSkinRequestEvent extends RandomRequestEvent {
	public RandomSkinRequestEvent(List<String> possibilities) {
		super(possibilities);
	}
}
