package io.seekerses.endlessgossip.gossip.knowledge;

import io.seekerses.endlessgossip.gossip.GossipContext;
import org.apache.commons.lang3.NotImplementedException;

public enum SimpleAction implements Action {
	TEST,
	ATTACK,
	TRADE,
	CRAFT,
	TALK,
	PICK_UP,
	DROP,
	KILL,
	ENTER,
	LOCATE;

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public void setName(String name) {
		throw new NotImplementedException();
	}

	public static void registerStandardActions() {
		for (SimpleAction action : SimpleAction.values()) {
			GossipContext.registerAction(action);
		}
	}
}
