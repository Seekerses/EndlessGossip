package io.seekerses.endlessgossip.gossip.communication.sharedPerception;

import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.Squad;
import io.seekerses.endlessgossip.gossip.knowledge.EventSource;
import io.seekerses.endlessgossip.gossip.knowledge.EventSourceType;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.knowledge.handler.KnowledgeHandler;
import net.minecraft.world.entity.Mob;

import java.io.Serializable;
import java.time.LocalDateTime;

import static io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys.SQUAD;
import static io.seekerses.endlessgossip.gossip.misc.KnowledgeUtil.createEmptySystemKnowledge;

public class SharedPerceptionSystemImpl implements SharedPerceptionSystem {

	public static void tick(Agent agent) {

	}

	public static void reportEvent(Agent agent, Knowledge knowledge) {
		Squad squad = agent.getMemory().getShortTermMemory().read(SQUAD);
		squad.getAgents().forEach(receiver -> receiver.receiveKnowledge(createReportMessage(agent, knowledge)));
	}


	public static boolean processHandlers(Agent agent, Knowledge knowledge) {
		KnowledgeHandler handler = agent.getKnowledgeHandler(knowledge.getAction());
		return handler == null || handler.processKnowledge(agent, knowledge);
	}

	public static void createGroupInvitation(Mob agent, Mob other) {
		EventSourceEntity entity = EventSourceEntity.builder()
				.level(agent.level())
				.lifetime(10)
				.location(agent.position())
				.knowledge(Knowledge.builder()
						.timestamp(LocalDateTime.now())
						.action(SystemAction.INVITE_GROUP)
						.subject(agent)
						.object(other)
						.strength(100)
						.type(KnowledgeType.DISPLACEABLE)
						.source(new EventSource(agent, EventSourceType.CHATTING))
						.build())
				.build();
		agent.level().addFreshEntity(entity);
	}

	public static Knowledge createGroupDisposalKnowledge(Agent agent) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.DISPOSE_GROUP);
		return knowledge;
	}

	public static Knowledge createReportMessage(Agent agent, Knowledge report) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.REPORT_MESSAGE);
		knowledge.setValue(report);
		return knowledge;
	}

	public static Knowledge proposePlan(Agent agent, Serializable plan) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.PROPOSE_PLAN);
		knowledge.setValue(plan);
		return knowledge;
	}

	public static Knowledge approvePlan(Agent agent, Serializable planId) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.APPROVE_PLAN);
		knowledge.setValue(planId);
		return knowledge;
	}

	public static Knowledge disapprovePlan(Agent agent, Serializable planId) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.DISAPPROVE_PLAN);
		knowledge.setValue(planId);
		return knowledge;
	}

	public static Knowledge acceptPlan(Agent agent, Serializable planId) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.ACCEPT_PLAN);
		knowledge.setValue(planId);
		return knowledge;
	}

	public static Knowledge declinePlan(Agent agent, Serializable planId) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.DECLINE_PLAN);
		knowledge.setValue(planId);
		return knowledge;
	}

	public static Knowledge nominateLeader(Agent agent) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.NOMINATE_LEADER);
		return knowledge;
	}

	public static Knowledge approveLeader(Agent agent) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.APPROVE_LEADER);
		return knowledge;
	}

	public static Knowledge disapproveLeader(Agent agent) {
		Knowledge knowledge = createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.DISAPPROVE_LEADER);
		return knowledge;
	}
}
