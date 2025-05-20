package io.seekerses.endlessgossip.gossip.memory;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.BlackboardListener;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public interface ShortTermMemory {
	<T> T read(@Nonnull String key);
	void write(@Nonnull String key, @Nonnull Object value);
	Map<String, Object> getBlackboard();
	void registerListener(@Nonnull BlackboardListener listener);
	boolean update(Knowledge knowledge);

	Location getLocation();
	void setLocation(Location location);

	void disposeGroup();
	void addToSquad(Agent agent);
	boolean isInSquad();
	void leaveSquad();

	void transmit();
	Set<Knowledge> getKnowledge(Object target);
	Set<Knowledge> getKnowledge();
	Knowledge getMostStrengthKnowledgeOfTag(Tag tag);

	String getDump();
	void restoreFromDump(String encodedDump, Level level);
}
