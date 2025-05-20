package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.communication.gossipSystem.GossipSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class AcceptConversationHandler implements KnowledgeHandler {

	public static final AcceptConversationHandler INSTANCE = new AcceptConversationHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		Entity entity = knowledge.getSubject().getEntity();
		if (entity instanceof GossipDrivenMob mob) {
			agent.setInConversation(true);
			Agent collocutor = mob.getAgent();
			List<Agent> talkedRecently =  agent.getMemory().getShortTermMemory().read(DefaultMemoryKeys.TALKED_RECENTLY);
			if (talkedRecently != null) {
				talkedRecently.add(collocutor);
			}
			Knowledge topic = GossipSystemImpl.findConversationTopic(agent, collocutor);
			Knowledge nextMessage;
			if (topic != null) {
				nextMessage = GossipSystemImpl.createTalkMessage(agent, topic, 1);
				GossipContext.logConversation(agent, mob.getAgent(), topic);
			} else {
				nextMessage = GossipSystemImpl.createEndConversation(agent);
				agent.setInConversation(false);
			}
			mob.getAgent().receiveKnowledge(nextMessage);
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.ACCEPT_CONVERSATION;
	}
}
