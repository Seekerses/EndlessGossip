package io.seekerses.endlessgossip.gossip.memory;

import io.seekerses.endlessgossip.gossip.GossipConfig;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KnowledgeStorageImpl implements KnowledgeStorage {

	private final Agent agent;
	private final Map<Object, Set<Knowledge>> regularKnowledgeMap;
	private final Map<Object, Set<Knowledge>> lifetimeKnowledgeMap;
	private final Map<Integer, Knowledge> knowledgeHashes;
	private long maxSize;

	public KnowledgeStorageImpl(Agent agent, final long maxSize) {
		this.agent = agent;
		this.maxSize = maxSize;
		this.regularKnowledgeMap = new HashMap<>();
		this.lifetimeKnowledgeMap = new HashMap<>();
		this.knowledgeHashes = new HashMap<>();
	}

	@Override
	public Long getMaxSize() {
		return maxSize;
	}

	@Override
	public void setMaxSize(Long maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public Set<Knowledge> getKnowledge() {
		return new HashSet<>(knowledgeHashes.values());
	}

	private Stream<Knowledge> getKnowledgeStream() {
		return knowledgeHashes.values().stream();
	}

	@Override
	public Set<Knowledge> getKnowledge(Object object) {
		Set<Knowledge> allKnowledges = new HashSet<>();
		allKnowledges.addAll(regularKnowledgeMap.getOrDefault(object, new HashSet<>()));
		allKnowledges.addAll(lifetimeKnowledgeMap.getOrDefault(object, new HashSet<>()));
		return allKnowledges;
	}

	@Override
	public Map<String, Knowledge> getKnowledgeMap() {
		return Map.of();
	}

	@Override
	@Nullable
	public Knowledge getMostStrengthKnowledgeOfTag(Tag tag) {
		Optional<Knowledge> strengthenKnowledge = getKnowledgeStream()
				.filter(knowledge -> knowledge.hasTag(tag))
				.max((o1, o2) -> Double.compare(o1.getActualStrength(agent), o2.getActualStrength(agent)));
		return strengthenKnowledge.orElse(null);
	}

	@Override
	public Knowledge findWeakest() {
		return getKnowledgeStream()
				.filter(knowledge -> knowledge.getType() != KnowledgeType.LIFETIME)
				.min((a, b) -> {
					float diff = a.getActualStrength(agent) - b.getActualStrength(agent);
					return diff == 0
						   ? 0
						   : diff > 0
							 ? 1
							 : -1;
				}).orElse(null);
	}

	@Override
	public boolean addKnowledge(Knowledge knowledge) {
		switch (knowledge.getType()) {
			case DISPLACEABLE: {
				boolean displaced = tryDisplace(knowledge);
				if (displaced) {
					return true;
				}
			}
			case REGULAR: {
				boolean haveCapacity = knowledgeHashes.size() < maxSize;
				if (!haveCapacity) {
					Knowledge weakest = findWeakest();
					if (weakest != null
						&& weakest.getActualStrength(agent) < knowledge.getActualStrength(agent)) {
						removeKnowledge(weakest);
						haveCapacity = true;
					}
				}
				if (!haveCapacity) {
					return false;
				} else {
					actuallyAdd(knowledge, regularKnowledgeMap);
					return true;
				}
			}
			case LIFETIME: {
				actuallyAdd(knowledge, lifetimeKnowledgeMap);
				return true;
			}
			default: {
				return false;
			}
		}
	}

	private boolean tryDisplace(Knowledge knowledge) {
		Knowledge displacedKnowledge = knowledgeHashes.get(knowledge.getUniqueHash());
		if (displacedKnowledge != null && knowledge.getTimestamp() != null && displacedKnowledge.getTimestamp() != null && knowledge.getTimestamp().isAfter(displacedKnowledge.getTimestamp())) {
			removeKnowledge(displacedKnowledge);
			actuallyAdd(knowledge, regularKnowledgeMap);
			return true;
		}
		return false;
	}

	private void actuallyAdd(Knowledge knowledge, Map<Object, Set<Knowledge>> knowledgeMap) {
		knowledgeHashes.put(knowledge.getUniqueHash(), knowledge);
		if (knowledge.getSubject() != null) {
			knowledgeMap.computeIfAbsent(knowledge.getSubject(), k -> new HashSet<>()).add(knowledge);
		}
		if (knowledge.getObject() != null) {
			knowledgeMap.computeIfAbsent(knowledge.getObject(), k -> new HashSet<>()).add(knowledge);
		}
	}

	@Override
	public void removeKnowledge(Knowledge knowledge) {
		knowledgeHashes.remove(knowledge.getUniqueHash());
		if (knowledge.getSubject() != null) {
			Set<Knowledge> subjKnowledge = regularKnowledgeMap.get(knowledge.getSubject());
			if (subjKnowledge != null) {
				subjKnowledge.remove(knowledge);
			}
		}
		if (knowledge.getObject() != null) {
			Set<Knowledge> objKnowledge = regularKnowledgeMap.get(knowledge.getObject());
			if (objKnowledge != null) {
				objKnowledge.remove(knowledge);
			}
		}
	}

	@Override
	public void cleanWeak() {
		List<Knowledge> knowledgesToRemove = getKnowledgeStream()
				.filter(knowledge -> knowledge.getType() != KnowledgeType.LIFETIME)
				.filter(knowledge -> knowledge.getActualStrength(agent) < GossipConfig.MINIMAL_KNOWLEDGE_STRENGTH)
				.toList();
		knowledgesToRemove.forEach(this::removeKnowledge);
	}

	@Override
	public String getDump() {
		return new String(Base64.getEncoder()
				.encode(getKnowledgeStream()
						.map(Knowledge::serialize)
						.collect(Collectors.joining("&&&"))
						.getBytes(StandardCharsets.UTF_8)));
	}

	@Override
	public void restoreFromDump(String dump, Level level) {
		String dumpDecoded = new String(Base64.getDecoder().decode(dump.getBytes(StandardCharsets.UTF_8)));
		Arrays.stream(dumpDecoded.split("&&&"))
				.map(str -> Knowledge.deserialize(level, str))
				.forEach(knowledge -> {
					if (knowledge.getType() != KnowledgeType.LIFETIME) {
						actuallyAdd(knowledge, lifetimeKnowledgeMap);
					} else {
						actuallyAdd(knowledge, regularKnowledgeMap);
					}
				});
	}

	@Override
	public boolean containsKnowledge(Knowledge knowledge) {
		return knowledgeHashes.containsKey(knowledge.getUniqueHash());
	}
}
