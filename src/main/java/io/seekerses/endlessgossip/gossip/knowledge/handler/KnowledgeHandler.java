package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;

public interface KnowledgeHandler {
	/**
	 * true if operation is terminal
	 */
	boolean processKnowledge(Agent agent, Knowledge knowledge);

	Action getSupportedAction();
}
