package io.seekerses.endlessgossip.entities.gossip.enums;

import io.seekerses.endlessgossip.gossip.knowledge.Action;

public enum DomainAction implements Action {
	TRADE,
	GRIEF,
	CHARITY,
	STEAL,
	TELL;

	@Override
	public String getName() {
		return name();
	}

	@Override
	public void setName(String name) {

	}
}
