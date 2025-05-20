package io.seekerses.endlessgossip.gossip.knowledge;

import io.seekerses.endlessgossip.gossip.GossipContext;
import org.apache.commons.lang3.NotImplementedException;

public enum SystemAction implements Action {
	INIT_CONVERSATION,
	ACCEPT_CONVERSATION,
	TALK_MESSAGE,
	END_CONVERSATION,
	HAVE_TAG,
	IN_FIGHT,
	DISPOSE_GROUP,
	INVITE_GROUP,
	REPORT_MESSAGE,
	PROPOSE_PLAN,
	APPROVE_PLAN,
	DISAPPROVE_PLAN,
	ACCEPT_PLAN,
	NOMINATE_LEADER,
	APPROVE_LEADER,
	DISAPPROVE_LEADER,
	DECLINE_PLAN;

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public void setName(String name) {
		throw new NotImplementedException();
	}

	public static void registerSystemActions() {
		for (SystemAction action : SystemAction.values()) {
			GossipContext.registerAction(action);
		}
	}
}
