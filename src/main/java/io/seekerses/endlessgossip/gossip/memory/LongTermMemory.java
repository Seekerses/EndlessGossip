package io.seekerses.endlessgossip.gossip.memory;

import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import io.seekerses.endlessgossip.gossip.misc.EntityReference;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LongTermMemory {
	KnowledgeStorage getKnowledgeHolder();
	Set<Knowledge> getKnowledge();
	Set<Knowledge> getKnowledge(Object subject);
	Knowledge getMostStrengthKnowledgeOfTag(Tag tag);
	float getRelation(Entity subject);
	EntityReference getAgentWithClosestRelation(float relation);
	Map<EntityReference, Float> getRelations();
	void addRelations(Map<EntityReference, Float> relations);

	boolean addKnowledge(Knowledge knowledge);
	void cleanWeak();
	Knowledge getWeakestKnowledge();
	List<Knowledge> getMostStrengthKnowledgeExcept(Set<Knowledge> knowledgeSet, long size);

	String getDump();
	void restoreFromDump(String encodedDump, Level level);
}
