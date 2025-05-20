package io.seekerses.endlessgossip.gossip.misc;

import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.EventSource;
import io.seekerses.endlessgossip.gossip.knowledge.EventSourceType;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.handler.AcceptConversationHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.ApproveLeaderHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.DisapproveLeaderHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.DisposeGroupHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.EndConversationHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.InFightHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.InitConversationHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.InviteGroupHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.NominateLeaderHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.ProposePlanHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.ReportMessageHandler;
import io.seekerses.endlessgossip.gossip.knowledge.handler.TalkMessageHandler;
import net.minecraft.world.entity.Mob;

import java.time.LocalDateTime;

public class KnowledgeUtil {

	public static void registerSystemHandlers(Agent agent) {
		agent.registerKnowledgeHandler(InitConversationHandler.INSTANCE);
		agent.registerKnowledgeHandler(AcceptConversationHandler.INSTANCE);
		agent.registerKnowledgeHandler(TalkMessageHandler.INSTANCE);
		agent.registerKnowledgeHandler(EndConversationHandler.INSTANCE);
		agent.registerKnowledgeHandler(InviteGroupHandler.INSTANCE);
		agent.registerKnowledgeHandler(DisposeGroupHandler.INSTANCE);
		agent.registerKnowledgeHandler(InFightHandler.INSTANCE);
		agent.registerKnowledgeHandler(ReportMessageHandler.INSTANCE);
		agent.registerKnowledgeHandler(ProposePlanHandler.INSTANCE);
		agent.registerKnowledgeHandler(NominateLeaderHandler.INSTANCE);
		agent.registerKnowledgeHandler(ApproveLeaderHandler.INSTANCE);
	}

	public static EventSourceEntity createEventAtAgentLocation(Agent agent, Knowledge knowledge, long lifetime) {
		EventSourceEntity entity = new EventSourceEntity(agent.getRelatedMob().level());
		entity.setPos(agent.getRelatedMob().position());
		entity.setLifetime(lifetime);
		entity.setKnowledge(knowledge);
		agent.getRelatedMob().level().addFreshEntity(entity);
		return entity;
	}

	public static Knowledge createEmptySystemKnowledge(Agent agent) {
		return Knowledge.builder()
				.timestamp(LocalDateTime.now())
				.subject(agent.getRelatedMob())
				.strength(100)
				.type(KnowledgeType.SYSTEM)
				.source(new EventSource(agent.getRelatedMob(), EventSourceType.CHATTING))
				.build();
	}
}
