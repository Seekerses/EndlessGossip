package io.seekerses.endlessgossip.gossip.memory;

import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;

import java.util.Set;

public interface AgentMemory {
	ShortTermMemory getShortTermMemory();
	LongTermMemory getLongTermMemory();

	Set<Knowledge> getKnowledge();
	Set<Knowledge> getKnowledge(Object object);
	Knowledge getMostStrengthKnowledgeOfTag(Tag tag);
}
