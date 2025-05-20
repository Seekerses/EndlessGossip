package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.communication.gossipSystem.GossipSystemImpl;
import io.seekerses.endlessgossip.gossip.communication.sharedPerception.SharedPerceptionSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import io.seekerses.endlessgossip.gossip.misc.RelationUtil;

import java.util.List;

public class InitConversationHandler implements KnowledgeHandler {

	public static final InitConversationHandler INSTANCE = new InitConversationHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		if (knowledge.getSubject().getEntity() != null && knowledge.getSubject().getEntity() instanceof GossipDrivenMob mob) {
			Agent initiator = mob.getAgent();
			boolean checkPassed = RelationUtil.relationCheck(agent, initiator);
			if (!checkPassed) {
				GossipSystemImpl.discoverAgent(agent, initiator);
				checkPassed = RelationUtil.relationCheck(agent, initiator);
			}
			if (checkPassed) {
				agent.setInConversation(true);
				List<Agent> talkedRecently =  agent.getMemory().getShortTermMemory().read(DefaultMemoryKeys.TALKED_RECENTLY);
				if (talkedRecently != null) {
					talkedRecently.add(initiator);
				}
				initiator.receiveKnowledge(GossipSystemImpl.createAcceptConversation(agent));
			}
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.INIT_CONVERSATION;
	}
}
