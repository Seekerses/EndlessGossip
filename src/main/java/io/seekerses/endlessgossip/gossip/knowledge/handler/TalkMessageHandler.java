package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.GossipConfig;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.communication.gossipSystem.ConversationMessage;
import io.seekerses.endlessgossip.gossip.communication.gossipSystem.GossipSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.misc.RelationUtil;

public class TalkMessageHandler implements KnowledgeHandler {

	public static final TalkMessageHandler INSTANCE = new TalkMessageHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		ConversationMessage message = knowledge.getValue();
		if (knowledge.getSubject().getEntity() != null && knowledge.getSubject().getEntity() instanceof GossipDrivenMob mob) {
			if (RelationUtil.informationTrustCheck(agent, mob.getAgent())) {
				// trust reaction
				if (GossipSystemImpl.distortionCheck(agent)) {
					GossipSystemImpl.distortKnowledge(message.knowledge(), agent);
				}
				agent.receiveKnowledge(message.knowledge());
			} else {
				// no trust reaction
			}
			if (message.iteration() <= GossipConfig.MAX_CONVERSATION_LENGTH) {
				Knowledge nextTopic = GossipSystemImpl.findConversationTopic(agent, mob.getAgent());
				Knowledge nextMessage;
				if (nextTopic != null) {
					nextMessage = GossipSystemImpl.createTalkMessage(agent, nextTopic, message.iteration() + 1);
					GossipContext.logConversation(agent, mob.getAgent(), nextTopic);
				} else {
					agent.setInConversation(false);
					nextMessage = GossipSystemImpl.createEndConversation(agent);
				}
				mob.getAgent().receiveKnowledge(nextMessage);
			} else {
				agent.setInConversation(false);
				Knowledge endConversation = GossipSystemImpl.createEndConversation(agent);
				mob.getAgent().receiveKnowledge(endConversation);
			}
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.TALK_MESSAGE;
	}
}
