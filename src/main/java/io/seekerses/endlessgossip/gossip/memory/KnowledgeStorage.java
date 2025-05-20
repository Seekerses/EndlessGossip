package io.seekerses.endlessgossip.gossip.memory;

import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface KnowledgeStorage {

	Long getMaxSize();
	void setMaxSize(Long maxSize);

	Set<Knowledge> getKnowledge();
	Set<Knowledge> getKnowledge(Object object);
	Map<String, Knowledge> getKnowledgeMap();
	@Nullable
	Knowledge getMostStrengthKnowledgeOfTag(Tag tag);

	@Nullable Knowledge findWeakest();
	boolean addKnowledge(Knowledge knowledge);
	void removeKnowledge(Knowledge knowledge);
	void cleanWeak();

	String getDump();
	void restoreFromDump(String dump, Level level);

	boolean containsKnowledge(Knowledge knowledge);
}
