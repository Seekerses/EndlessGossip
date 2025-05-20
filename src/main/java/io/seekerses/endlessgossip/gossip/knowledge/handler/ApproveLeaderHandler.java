package io.seekerses.endlessgossip.gossip.knowledge.handler;

import com.mojang.logging.LogUtils;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.agent.Squad;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import org.slf4j.Logger;

public class ApproveLeaderHandler implements KnowledgeHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final ApproveLeaderHandler INSTANCE = new ApproveLeaderHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		if (knowledge.getSubject().getEntity() instanceof GossipDrivenMob gdm) {
			LOGGER.info("Just got APPROVE_VOTE from " + gdm.getAgent().getName());
			Squad squad = agent.getMemory().getShortTermMemory().read(DefaultMemoryKeys.SQUAD);
			Integer leaderCount = agent.getMemory().getShortTermMemory().read(DefaultMemoryKeys.LEADER_COUNT);
			Integer leaderResult = agent.getMemory().getShortTermMemory().read(DefaultMemoryKeys.LEADER_RESULT);
			if (leaderCount == null) leaderCount = 0;
			if (leaderResult == null) leaderResult = 0;
			leaderResult++;
			leaderCount++;
			if (leaderCount == squad.getAgents().size() - 1)
				if (leaderResult > (squad.getAgents().size() - 1) / 2) {
					LOGGER.info("Leader nomination approved.");
					squad.chooseLeader();
				} else {
					LOGGER.info("Leader nomination declined.");
			}
			agent.getMemory().getShortTermMemory().write(DefaultMemoryKeys.LEADER_COUNT, leaderCount);
			agent.getMemory().getShortTermMemory().write(DefaultMemoryKeys.LEADER_RESULT, leaderResult);
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.APPROVE_LEADER;
	}
}
