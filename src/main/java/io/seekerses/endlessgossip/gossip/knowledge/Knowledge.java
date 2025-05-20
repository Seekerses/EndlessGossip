package io.seekerses.endlessgossip.gossip.knowledge;

import io.seekerses.endlessgossip.gossip.GossipConfig;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.misc.EntityReference;
import io.seekerses.endlessgossip.gossip.misc.Named;
import io.seekerses.endlessgossip.gossip.misc.ObjectUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Knowledge implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(Knowledge.class);

	private float strength;
	private EntityReference subject;
	private EntityReference object;
	private Action action;
	private Serializable value;
	private EventSource source;
	private Set<Tag> tags;
	private LocalDateTime timestamp;
	private KnowledgeType type;
	private UUID uuid;
	private float distortionFactor;
	private Knowledge originalKnowledge;

	private transient LocalDateTime lastStrengthCalculation;
	private transient float cachedActualStrength;

	public Knowledge(float strength, EntityReference subject, EntityReference object, Action action, Serializable value, EventSource source, Set<Tag> tags, LocalDateTime timestamp, KnowledgeType type, float distortionFactor) {
		this.strength = strength;
		this.subject = subject;
		this.object = object;
		this.action = action;
		this.value = value;
		this.source = source;
		this.tags = tags;
		this.timestamp = timestamp;
		this.type = type;
		this.uuid = UUID.randomUUID();
		this.distortionFactor = distortionFactor;
	}

	public Knowledge(Knowledge other) {
		this.strength = other.strength;
		this.subject = other.subject;
		this.object = other.object;
		this.action = other.action;
		this.value = other.value;
		this.source = other.source;
		this.tags = new HashSet<>();
		this.tags.addAll(other.tags);
		this.timestamp = other.timestamp;
		this.type = other.type;
		this.uuid = other.uuid;
		this.distortionFactor = other.distortionFactor;
		this.originalKnowledge = other.getOriginalKnowledge() == null ? other : other.getOriginalKnowledge();
	}

	public float getActualStrength(Agent agent) {
		LocalDateTime now = LocalDateTime.now();
		if (lastStrengthCalculation != null
			&& lastStrengthCalculation.plus(GossipConfig.UPDATE_STRENGTH_MILLIS, ChronoUnit.MILLIS).isAfter(now)) {
			return cachedActualStrength;
		}
		float subjectRelation = subject != null
								? Math.abs(agent.getMemory().getLongTermMemory().getRelation(subject.getEntity()))
								: 1f;
		float objectRelation = object != null
							   ? Math.abs(agent.getMemory().getLongTermMemory().getRelation(object.getEntity()))
							   : 1f;
		float interestSum = tags.stream()
				.map(agent::getInterest)
				.map(interest -> interest == null? 0f : interest)
				.reduce(0f, Float::sum);
		float oblivion = 1f;
		if (type != KnowledgeType.LIFETIME) {
			int passedHours = (int) ((LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - timestamp.toEpochSecond(ZoneOffset.UTC)) / 60);
			oblivion = calculateOblivion(passedHours);
		}
		float actualStrength = (subjectRelation * objectRelation * strength + interestSum) * oblivion;
		lastStrengthCalculation = now;
		cachedActualStrength = actualStrength;
		return actualStrength;
	}

	private float calculateOblivion(int hours) {
//		if (hours < 2) {
//			return 1;
//		} else {
			return (float) ( 1.84 / (GossipConfig.OBLIVION_FACTOR * Math.pow(Math.log(hours), 1.25) + 1.84));
//		}
	}

	public boolean hasTag(Tag tag) {
		return tags.contains(tag);
	}

	public float getStrength() {
		return strength;
	}

	public void setStrength(float strength) {
		this.strength = strength;
	}

	public EntityReference getSubject() {
		return subject;
	}

	public <T extends Entity> void setSubject(T subject) {
		this.subject = new EntityReference(subject);
	}

	public EntityReference getObject() {
		return object;
	}

	public <T extends Entity> void setObject(T object) {
		this.object = new EntityReference(object);
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		return (T) value;
	}

	public <T extends Serializable> void setValue(T value) {
		this.value = value;
	}

	public EventSource getSource() {
		return source;
	}

	public void setSource(EventSource source) {
		this.source = source;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public KnowledgeType getType() {
		return type;
	}

	public void setType(KnowledgeType type) {
		this.type = type;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public float getDistortionFactor() {
		return this.distortionFactor;
	}

	public void setDistortionFactor(float distortionFactor) {
		this.distortionFactor = distortionFactor;
	}

	public Knowledge getOriginalKnowledge() {
		return originalKnowledge;
	}

	public String serialize() {
		return Base64.getEncoder().encodeToString(("strength=" + strength +
												   "&subject=" + subject.getUuid() +
												   "&object=" + object.getUuid() +
												   "&action=" + action.getName() +
												   "&value=" + ObjectUtil.serialize(value) +
												   "&source=" + source.getType() + ":" + (source.getSource() != null? source.getSource().getUUID() : "null") +
												   "&tags=" + tags.stream().map(Tag::getName).collect(Collectors.joining(";")) +
												   "&timestamp=" + timestamp.toEpochSecond(ZoneOffset.UTC) +
												   "&type=" + type +
												   "&uuid=" + uuid.toString() +
												   "&distortion=" + distortionFactor).getBytes(StandardCharsets.UTF_8));
	}

	public static Knowledge deserialize(Level level, String encoded) {
		String decoded = new String(Base64.getDecoder().decode(encoded.getBytes(StandardCharsets.UTF_8)));
		Map<String, String> knowledgeParams = Stream.of(decoded.split("&"))
				.map(pair -> pair.split("=", 2))
				.collect(Collectors.toMap(parts -> parts[0], parts -> parts.length > 1? parts[1] : ""));

		Set<Tag> tags = new HashSet<>();
		String tagsList = knowledgeParams.get("tags");
		if (!StringUtil.isNullOrEmpty(tagsList)) {
			tags = Arrays.stream(tagsList.split(";")).map(GossipContext::getTag).collect(Collectors.toSet());
		}

		String[] sourceString = knowledgeParams.get("source").split(":");
		EventSource source = new EventSource(getByEncodedId(sourceString[1], level), EventSourceType.valueOf(sourceString[0]));

		return Knowledge.builder()
				.strength(Float.parseFloat(knowledgeParams.get("strength")))
				.subject(getByEncodedId(knowledgeParams.get("subject"), level))
				.object(getByEncodedId(knowledgeParams.get("object"), level))
				.action(GossipContext.getAction(knowledgeParams.get("action")))
				.value(ObjectUtil.deserialize(knowledgeParams.get("value")))
				.source(source)
				.knowledgeTag(tags)
				.timestamp(LocalDateTime.ofEpochSecond(Long.parseLong(knowledgeParams.get("timestamp")), 0, ZoneOffset.UTC))
				.type(KnowledgeType.valueOf(knowledgeParams.get("type")))
				.uuid(UUID.fromString(knowledgeParams.get("uuid")))
				.distortion(Float.parseFloat("distortion"))
				.build();
	}

	private static EntityReference getByEncodedId(String encodedId, Level level) {
		if (StringUtil.isNullOrEmpty(encodedId) || "null".equals(encodedId)) {
			return null;
		} else {
			Entity entity = ((ServerLevel) level).getEntity(UUID.fromString(encodedId));
			if (entity == null) {
				return new EntityReference(level, UUID.fromString(encodedId));
			} else {
				return new EntityReference(entity);
			}
		}
	}

	@Override
	public String toString() {
		return """
				Strength: %f
				Subject: %s
				Object: %s
				Action: %s
				Value: %s
				Source: %s
				Tags: %s
				Type: %s
				CachedActualStrength: %f
				UUID: %s
				Distortion: %s""".formatted(
				strength,
				subject == null? "null" : (subject.getEntity() instanceof GossipDrivenMob named? named.getAgent().getName() : subject.getEntity()),
				object == null? "null" : (object.getEntity() instanceof GossipDrivenMob named? named.getAgent().getName() : object.getEntity()),
				action.getName(),
				value == null? "null" : (value instanceof Named named? named.getName() : value),
				source.getType() + " of " + (source.getSource() instanceof GossipDrivenMob named? named.getAgent().getName() : source.getSource()),
				tags.stream().map(Tag::getName).collect(Collectors.joining(", ")),
				type.name(),
				cachedActualStrength,
				uuid.toString(),
				distortionFactor
		);
	}

	public int getUniqueHash() {
		if (type == KnowledgeType.DISPLACEABLE) {
			return Objects.hash(subject, object, action);
		}
		return Objects.hash(subject, object, action, timestamp, value);
	}

	public static KnowledgeBuilder builder() {
		return new KnowledgeBuilder();
	}

	public static class KnowledgeBuilder {
		private float strength;
		private EntityReference subject;
		private EntityReference object;
		private Action action;
		private Serializable value;
		private EventSource source;
		private Set<Tag> knowledgeTag = new HashSet<>();
		private LocalDateTime timestamp = LocalDateTime.now();
		private KnowledgeType type = KnowledgeType.DISPLACEABLE;
		private UUID uuid;
		private float distortion;

		public KnowledgeBuilder strength(float strength) {
			this.strength = strength;
			return this;
		}

		public KnowledgeBuilder subject(Entity subject) {
			this.subject = new EntityReference(subject);
			return this;
		}

		public KnowledgeBuilder subject(EntityReference reference) {
			this.subject = reference;
			return this;
		}

		public KnowledgeBuilder object(Entity object) {
			this.object = new EntityReference(object);
			return this;
		}

		public KnowledgeBuilder object(EntityReference reference) {
			this.object = reference;
			return this;
		}

		public KnowledgeBuilder action(Action action) {
			this.action = action;
			return this;
		}

		public KnowledgeBuilder value(Serializable value) {
			this.value = value;
			return this;
		}

		public KnowledgeBuilder source(EventSource source) {
			this.source = source;
			return this;
		}

		public KnowledgeBuilder knowledgeTag(Set<Tag> knowledgeTag) {
			this.knowledgeTag = knowledgeTag;
			return this;
		}

		public KnowledgeBuilder knowledgeTag(Tag knowledgeTag) {
			this.knowledgeTag = Set.of(knowledgeTag);
			return this;
		}

		public KnowledgeBuilder timestamp(LocalDateTime timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public KnowledgeBuilder type(KnowledgeType type) {
			this.type = type;
			return this;
		}

		public KnowledgeBuilder uuid(UUID uuid) {
			this.uuid = uuid;
			return this;
		}

		public KnowledgeBuilder distortion(float distortion) {
			this.distortion = distortion;
			return this;
		}

		public Knowledge build() {
			Knowledge knowledge = new Knowledge(strength, subject, object, action, value, source, knowledgeTag, timestamp, type, distortion);
			if (this.uuid != null) {
				knowledge.setUuid(uuid);
			}
			return knowledge;
		}
	}
}
