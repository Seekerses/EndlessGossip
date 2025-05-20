package io.seekerses.endlessgossip.gossip.knowledge.handler;

import com.mojang.logging.LogUtils;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.communication.sharedPerception.SharedPerceptionSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import org.slf4j.Logger;

public class NominateLeaderHandler implements KnowledgeHandler {

	private static final Logger LOGGER = LogUtils.getLogger();

	public static final NominateLeaderHandler INSTANCE = new NominateLeaderHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		if (knowledge.getSubject().getEntity() instanceof GossipDrivenMob gdm) {
			LOGGER.info("NOMINATE LEADER received. Sending APPROVE_LEADER");
			gdm.getAgent().receiveKnowledge(SharedPerceptionSystemImpl.approveLeader(agent));
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.NOMINATE_LEADER;
	}
}
