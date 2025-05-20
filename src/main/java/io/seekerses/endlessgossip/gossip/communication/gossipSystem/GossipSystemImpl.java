package io.seekerses.endlessgossip.gossip.communication.gossipSystem;

import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.entities.gossip.GossipEntity;
import io.seekerses.endlessgossip.gossip.GossipConfig;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.EventSource;
import io.seekerses.endlessgossip.gossip.knowledge.EventSourceType;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import io.seekerses.endlessgossip.gossip.misc.KnowledgeUtil;
import io.seekerses.endlessgossip.gossip.misc.RandomUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GossipSystemImpl implements GossipSystem {

	public static void tick(Agent agent) {

	}

	public static Agent findCollocutorOnLocation(Agent agent) {
		Location location = agent.getMemory().getShortTermMemory().getLocation();
		return location.getAgents().stream()
				.filter(locAgent -> locAgent != agent)
				.filter(locAgent -> agent.getMemory().getLongTermMemory().getRelation(locAgent.getRelatedMob()) != GossipConfig.DEFAULT_RELATION)
				.filter(locAgent -> !agent.getMemory().getShortTermMemory().<List<Agent>>read(DefaultMemoryKeys.TALKED_RECENTLY).contains(locAgent))
				.filter(locAgent -> agent.getMemory().getLongTermMemory().getRelation(locAgent.getRelatedMob()) > GossipConfig.MINIMAL_RELATION_FOR_CONVERSATION)
				.max(Comparator
						.comparing((locAgent -> agent.getMemory().getLongTermMemory().getRelation(locAgent.getRelatedMob()))))
				.orElse(null);
	}

	public static void discoverAgentsOnLocation(Agent agent) {
		Location currentLocation = agent.getMemory().getShortTermMemory().getLocation();
		currentLocation.getAgents().stream()
				.filter(locAgent -> locAgent != agent)
				.forEach(locAgent -> discoverAgent(agent, locAgent));
	}

	public static void discoverAgent(Agent agent, Agent target) {
		target.getPersonTags()
				.entrySet().stream()
				.filter(entry -> attentivenessCheck(agent, entry.getValue()))
				.map(Map.Entry::getKey)
				.map(tag -> createHaveTag(target, tag))
				.forEach(agent::receiveKnowledge);
	}

	public static Knowledge findConversationTopic(Agent sender, Agent receiver) {
		List<Knowledge> knowledges = sender.getMostStrengthKnowledge(
				receiver.getMemory().getKnowledge(),
				GossipConfig.TOP_KNOWLEDGE_FOR_CONVERSATION_SIZE);
		if (knowledges == null || knowledges.isEmpty()) {
			return null;
		}
		return knowledges.get(0);
	}

	public static void conversation(Agent initiator, Agent contragent) {
		contragent.receiveKnowledge(createInitConversation(initiator));
	}

	public static boolean attentivenessCheck(Agent observer, float obscurity) {
		return observer.getAttentiveness() - obscurity + RandomUtil.random.nextFloat(observer.getRandomness()) > 0;
	}

	public static boolean distortionCheck(Agent agent) {
		return RandomUtil.random.nextFloat() > (GossipConfig.DISTORTION_FACTOR * Math.log(agent.getAttentiveness()) / Math.E);
	}

	public static void distortKnowledge(Knowledge knowledge, Agent agent) {
		DistortionField field = DistortionField.pickType(knowledge);
		if (field != null) {
			field.getModifier().accept(knowledge, agent);
		}
	}

	public static EventSourceEntity createInitConversationEvent(GossipEntity initiator) {
		return EventSourceEntity.builder()
				.location(initiator.position())
				.lifetime(5)
				.level(initiator.level())
				.knowledge(createInitConversation(initiator.getAgent()))
				.build();
	}

	public static Knowledge createHaveTag(Agent subject, Tag tag) {
		Knowledge knowledge = KnowledgeUtil.createEmptySystemKnowledge(subject);
		knowledge.setAction(SystemAction.HAVE_TAG);
		knowledge.setStrength(GossipConfig.DISCOVER_AGENT_STRENGTH);
		knowledge.setValue(tag);
		knowledge.setSource(new EventSource(subject.getRelatedMob(), EventSourceType.VISION));
		knowledge.setType(KnowledgeType.DISPLACEABLE);
		return knowledge;
	}

	public static Knowledge createInitConversation(Agent initiator) {
		Knowledge knowledge = KnowledgeUtil.createEmptySystemKnowledge(initiator);
		knowledge.setAction(SystemAction.INIT_CONVERSATION);
		return knowledge;
	}

	public static Knowledge createAcceptConversation(Agent colluctor) {
		Knowledge knowledge = KnowledgeUtil.createEmptySystemKnowledge(colluctor);
		knowledge.setAction(SystemAction.ACCEPT_CONVERSATION);
		return knowledge;
	}

	public static Knowledge createEndConversation(Agent agent) {
		Knowledge knowledge = KnowledgeUtil.createEmptySystemKnowledge(agent);
		knowledge.setAction(SystemAction.END_CONVERSATION);
		return knowledge;
	}

	public static Knowledge createTalkMessage(Agent sender, Knowledge topic, int iteration) {
		Knowledge knowledge = KnowledgeUtil.createEmptySystemKnowledge(sender);
		knowledge.setAction(SystemAction.TALK_MESSAGE);
		Knowledge topicClone = new Knowledge(topic);
		topicClone.setStrength(topicClone.getStrength() * GossipConfig.GOSSIP_FACTOR);
		topicClone.setSource(new EventSource(sender.getRelatedMob(), EventSourceType.CHATTING));
		knowledge.setValue(new ConversationMessage(topicClone, iteration));
		return knowledge;
	}
}
