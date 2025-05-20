package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.misc.RelationUtil;

public class ReportMessageHandler implements KnowledgeHandler {

	public static final ReportMessageHandler INSTANCE = new ReportMessageHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		Knowledge message = knowledge.getValue();
		if (knowledge.getSubject().getEntity() != null && knowledge.getSubject().getEntity() instanceof GossipDrivenMob mob) {
			if (RelationUtil.informationTrustCheck(agent, mob.getAgent())) {
				// trust reaction
				agent.getMemory().getShortTermMemory().update(knowledge);
			} else {
				// no trust reaction
			}
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.REPORT_MESSAGE;
	}
}
