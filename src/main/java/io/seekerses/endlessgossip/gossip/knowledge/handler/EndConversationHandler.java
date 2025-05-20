package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;

import java.util.List;

public class EndConversationHandler implements KnowledgeHandler {

	public static final EndConversationHandler INSTANCE = new EndConversationHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		agent.setInConversation(false);

		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.END_CONVERSATION;
	}
}
