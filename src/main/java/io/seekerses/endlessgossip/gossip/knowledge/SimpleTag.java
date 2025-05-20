package io.seekerses.endlessgossip.gossip.knowledge;

import io.seekerses.endlessgossip.gossip.GossipContext;
import org.apache.commons.lang3.NotImplementedException;

public enum SimpleTag implements Tag {
	TEST,
	WARRIOR;

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public void setName(String name) {
		throw new NotImplementedException();
	}

	public static void registerStandardTags() {
		for (SimpleTag tag : SimpleTag.values()) {
			GossipContext.registerTag(tag);
		}
	}
}
