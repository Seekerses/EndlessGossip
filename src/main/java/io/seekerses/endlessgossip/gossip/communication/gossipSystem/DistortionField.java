package io.seekerses.endlessgossip.gossip.communication.gossipSystem;

import com.mojang.datafixers.util.Pair;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import io.seekerses.endlessgossip.gossip.misc.EntityReference;
import io.seekerses.endlessgossip.gossip.misc.RandomUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static io.seekerses.endlessgossip.gossip.communication.gossipSystem.DistortionType.TRANSFORMATION;
import static io.seekerses.endlessgossip.gossip.communication.gossipSystem.DistortionType.LOSS;

public enum DistortionField {
	SUBJECT(Set.of(LOSS, TRANSFORMATION), 1.7f, 1.0f, ((knowledge, agent) -> {
		DistortionType type = TRANSFORMATION;
		if (knowledge.getObject() != null) {
			if (RandomUtil.random.nextBoolean()) {
				type = LOSS;
			}
		}

		if (type == LOSS) {
			knowledge.setSubject(null);
			knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 1.0f);
		} else {
			EntityReference replacingEntity = agent.getMemory().getLongTermMemory().getAgentWithClosestRelation(
					agent.getMemory().getLongTermMemory().getRelation(knowledge.getSubject().getEntity()));
			if (replacingEntity != null && replacingEntity.getEntity() != null) {
				knowledge.setSubject(replacingEntity.getEntity());
				knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 1.0f);
			} else if (knowledge.getObject() != null) {
				knowledge.setSubject(null);
				knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 1.0f);
			}

		}
	})),
	OBJECT(Set.of(LOSS, TRANSFORMATION), 1.2f, 0.75f, ((knowledge, agent) -> {
		DistortionType type = TRANSFORMATION;
		if (knowledge.getSubject() != null) {
			if (RandomUtil.random.nextBoolean()) {
				type = LOSS;
			}
		}

		if (type == LOSS) {
			knowledge.setObject(null);
			knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 0.75f);
		} else {
			EntityReference replacingEntity = agent.getMemory().getLongTermMemory().getAgentWithClosestRelation(
					agent.getMemory().getLongTermMemory().getRelation(knowledge.getSubject().getEntity()));
			if (replacingEntity != null && replacingEntity.getEntity() != null) {
				knowledge.setObject(replacingEntity.getEntity());
				knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 1.0f);
			} else if (knowledge.getObject() != null) {
				knowledge.setObject(null);
				knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 1.0f);
			}

		}
	})),
	TAGS(Set.of(LOSS), 0f, 0.35f, (knowledge, agent) -> {
		int removedTagIndex = RandomUtil.random.nextInt(knowledge.getTags().size());
		Tag removeTag = null;
		int i = 0;
		for (Tag tag : knowledge.getTags()) {
			if (i == removedTagIndex) {
				removeTag = tag;
				break;
			}
			i++;
		}
		if (removeTag != null) {
			knowledge.getTags().remove(removeTag);
		}
		knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 0.35f);
	}),
	VALUE(Set.of(LOSS), 0.5f, 0.7f, ((knowledge, agent) -> {
		knowledge.setValue(null);
		knowledge.setDistortionFactor(knowledge.getDistortionFactor() + 0.7f);
	}));

	private final Set<DistortionType> supportedDistortionTypes;
	private final float minimalDistortion;
	private final float distortionGain;
	private final BiConsumer<Knowledge, Agent> modifier;

	public Set<DistortionType> getSupportedDistortionTypes() {
		return this.supportedDistortionTypes;
	}

	public float getMinimalDistortion() {
		return minimalDistortion;
	}

	public float getDistortionGain() {
		return distortionGain;
	}

	public BiConsumer<Knowledge, Agent> getModifier() {
		return modifier;
	}

	private DistortionField(Set<DistortionType> supportedDistortionTypes, float minimalDistortion, float distortionGain, BiConsumer<Knowledge, Agent> modifier) {
		this.supportedDistortionTypes = supportedDistortionTypes;
		this.minimalDistortion = minimalDistortion;
		this.distortionGain = distortionGain;
		this.modifier = modifier;
	}

	public static DistortionField pickType(Knowledge knowledge) {
		List<DistortionField> possibleDistortions = Arrays.stream(DistortionField.values())
				.filter(type -> knowledge.getDistortionFactor() >= type.getMinimalDistortion()).toList();
		float distSum = possibleDistortions.stream()
				.map(DistortionField::getDistortionGain).reduce(Float::sum).orElse(0f);
		List<Pair<Float, DistortionField>> fieldWeights = possibleDistortions.stream()
				.map(field -> Pair.of(distSum / field.getDistortionGain(), field))
				.toList();
		float weightsSum = fieldWeights.stream()
				.map(Pair::getFirst)
				.reduce(Float::sum).orElse(0f);
		List<Pair<Float, DistortionField>> distortionProbabilities = new ArrayList<>();
		for (Pair<Float, DistortionField> field : fieldWeights) {
			if (distortionProbabilities.isEmpty()) {
				distortionProbabilities.add(Pair.of(field.getFirst() / weightsSum, field.getSecond()));
			} else {
				distortionProbabilities.add(Pair.of(
						distortionProbabilities.get(distortionProbabilities.size() - 1).getFirst()
						+ field.getFirst() / weightsSum,
						field.getSecond()));
			}
		}
		// Выбор искажения
		float randomValue = RandomUtil.random.nextFloat();
		for (Pair<Float, DistortionField> field : distortionProbabilities) {
			if (randomValue <= field.getFirst()) {
				return field.getSecond();
			}
		}
		return null;
	}
}
