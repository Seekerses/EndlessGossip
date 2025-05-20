package io.seekerses.endlessgossip.entities.gossip.enums;

import io.seekerses.endlessgossip.gossip.knowledge.Tag;

public enum DomainTag implements Tag {
	MERCHANT,
	THIEF,
	STRANGER,
	VALUABLE
	;
	@Override
	public String getName() {
		return name();
	}

	@Override
	public void setName(String name) {

	}
}
