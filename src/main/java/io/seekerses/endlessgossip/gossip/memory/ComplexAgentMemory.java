package io.seekerses.endlessgossip.gossip.memory;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;

import java.util.HashSet;
import java.util.Set;

public class ComplexAgentMemory implements AgentMemory {

	private final Agent agent;
	private final ShortTermMemory shortTermMemory;
	private final LongTermMemory longTermMemory;

	public ComplexAgentMemory(Agent agent) {
		this(agent, new ShortTermMemoryImpl(agent), new LongTermMemoryImpl(agent));
	}

	public ComplexAgentMemory(Agent agent, ShortTermMemory shortTermMemory, LongTermMemory longTermMemory) {
		this.agent = agent;
		this.shortTermMemory = shortTermMemory;
		this.longTermMemory = longTermMemory;
	}

	@Override
	public ShortTermMemory getShortTermMemory() {
		return shortTermMemory;
	}

	@Override
	public LongTermMemory getLongTermMemory() {
		return longTermMemory;
	}

	@Override
	public Set<Knowledge> getKnowledge() {
		Set<Knowledge> knowledges = new HashSet<>();
		knowledges.addAll(longTermMemory.getKnowledge());
		knowledges.addAll(shortTermMemory.getKnowledge());
		return knowledges;
	}

	@Override
	public Set<Knowledge> getKnowledge(Object object) {
		Set<Knowledge> knowledges = new HashSet<>();
		knowledges.addAll(longTermMemory.getKnowledge(object));
		knowledges.addAll(shortTermMemory.getKnowledge(object));
		return knowledges;
	}

	@Override
	public Knowledge getMostStrengthKnowledgeOfTag(Tag tag) {
		Knowledge longKnowledge = longTermMemory.getMostStrengthKnowledgeOfTag(tag);
		Knowledge shortKnowledge = shortTermMemory.getMostStrengthKnowledgeOfTag(tag);
		if (shortKnowledge == null) {
			return longKnowledge;
		}
		if (longKnowledge == null) {
			return shortKnowledge;
		}
		return longKnowledge.getActualStrength(agent) > shortKnowledge.getActualStrength(agent)
			   ? longKnowledge
			   : shortKnowledge;
	}

}
