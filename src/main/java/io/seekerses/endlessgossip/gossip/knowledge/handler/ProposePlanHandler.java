package io.seekerses.endlessgossip.gossip.knowledge.handler;

import com.mojang.logging.LogUtils;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.SimpleAgent;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import org.slf4j.Logger;

import static io.seekerses.endlessgossip.gossip.knowledge.SystemAction.PROPOSE_PLAN;

public class ProposePlanHandler implements KnowledgeHandler {


	private static final Logger LOGGER = LogUtils.getLogger();

	public static final ProposePlanHandler INSTANCE = new ProposePlanHandler();
	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		LOGGER.info("{} running plan !", agent.getName());
		((SimpleAgent.RunnableSerializable) knowledge.getValue()).runnable.run();
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return PROPOSE_PLAN;
	}
}
