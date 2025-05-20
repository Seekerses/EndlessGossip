package io.seekerses.endlessgossip.gossip.memory;

import com.mojang.datafixers.util.Pair;
import io.seekerses.endlessgossip.gossip.GossipConfig;
import io.seekerses.endlessgossip.gossip.agent.ActionRelation;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import io.seekerses.endlessgossip.gossip.misc.EntityReference;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LongTermMemoryImpl implements LongTermMemory {

	private final Agent agent;
	private final KnowledgeStorage knowledgeStorage;
	private final Map<EntityReference, Float> relations;

	public LongTermMemoryImpl(Agent agent) {
		this(agent, new KnowledgeStorageImpl(agent, GossipConfig.LONG_TERM_KNOWLEDGE_SIZE), new HashMap<>());
	}

	public LongTermMemoryImpl(Agent agent, KnowledgeStorage knowledgeStorage, Map<EntityReference, Float> relations) {
		this.agent = agent;
		this.knowledgeStorage = knowledgeStorage;
		this.relations = relations;
	}

	@Override
	public KnowledgeStorage getKnowledgeHolder() {
		return knowledgeStorage;
	}

	@Override
	public Set<Knowledge> getKnowledge() {
		return knowledgeStorage.getKnowledge();
	}

	@Override
	public Set<Knowledge> getKnowledge(Object subject) {
		return knowledgeStorage.getKnowledge(subject);
	}

	@Override
	public Knowledge getMostStrengthKnowledgeOfTag(Tag tag) {
		return knowledgeStorage.getMostStrengthKnowledgeOfTag(tag);
	}

	@Override
	public float getRelation(Entity subject) {
		return relations.getOrDefault(new EntityReference(subject), GossipConfig.DEFAULT_RELATION);
	}

	@Override
	public EntityReference getAgentWithClosestRelation(float relation) {
		Optional<Pair<Map.Entry<EntityReference, Float>, Float>> foundEntity = relations.entrySet().stream()
				.map(entry -> Pair.of(entry, Math.abs(relation - entry.getValue())))
				.min(Comparator.comparingDouble(Pair::getSecond));
		return foundEntity.map(entryFloatPair -> entryFloatPair.getFirst().getKey()).orElse(null);
	}

	@Override
	public Map<EntityReference, Float> getRelations() {
		return relations;
	}

	@Override
	public void addRelations(Map<EntityReference, Float> relations) {
		this.relations.putAll(relations);
	}

	@Override
	public boolean addKnowledge(Knowledge knowledge) {
		boolean result = knowledgeStorage.addKnowledge(knowledge);
		if (result) {
			recomputeRelation(knowledge);
		}
		return result;
	}

	private void recomputeRelation(Knowledge knowledge) {
		if (knowledge.getAction().equals(SystemAction.HAVE_TAG)) {
			if (knowledge.getSubject() != null) {
				Float relationChange = agent.getInterest(knowledge.getValue());
				if (relationChange != null) {
					relations.merge(knowledge.getSubject(), relationChange, Float::sum);
				}
			}
		} else {
			ActionRelation actionRelation = agent.getActionRelation(knowledge.getAction());
			if (actionRelation != null) {
				// To subject
				if (knowledge.getSubject() != null) {
					relations.merge(knowledge.getSubject(), actionRelation.toSubject(), Float::sum);
				}
				if (knowledge.getObject() != null) {
					relations.merge(knowledge.getObject(), actionRelation.toObject(), Float::sum);
				}
			}
		}
	}

	@Override
	public void cleanWeak() {
		knowledgeStorage.cleanWeak();
	}

	@Nullable
	@Override
	public Knowledge getWeakestKnowledge() {
		return knowledgeStorage.findWeakest();
	}

	@Override
	public List<Knowledge> getMostStrengthKnowledgeExcept(Set<Knowledge> knowledgeSet, long size) {
		return knowledgeStorage.getKnowledge().stream().filter(knowledge -> knowledgeSet.stream().noneMatch(kn -> kn.getUuid().equals(knowledge.getUuid())))
				.sorted((o1, o2) -> {
					float result = o1.getActualStrength(agent) - o2.getActualStrength(agent);
					if (result == 0) {
						return 0;
					}
					if (result > 0) {
						return 1;
					} else {
						return -1;
					}
				}).limit(size).toList();
	}

	@Override
	public String getDump() {
		return knowledgeStorage.getDump();
	}

	@Override
	public void restoreFromDump(String encodedDump, Level level) {
		this.knowledgeStorage.restoreFromDump(encodedDump, level);
	}
}
