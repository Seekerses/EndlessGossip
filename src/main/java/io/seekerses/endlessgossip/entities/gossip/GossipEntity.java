package io.seekerses.endlessgossip.entities.gossip;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import io.seekerses.endlessgossip.EndlessGossipMod;
import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.entities.gossip.goal.SeeEventGoal;
import io.seekerses.endlessgossip.entities.location.WorldLocationStorage;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.agent.SimpleActionRelation;
import io.seekerses.endlessgossip.gossip.agent.SimpleAgent;
import io.seekerses.endlessgossip.gossip.knowledge.EventSource;
import io.seekerses.endlessgossip.gossip.knowledge.EventSourceType;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.knowledge.SimpleAction;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import io.seekerses.endlessgossip.gossip.misc.EntityReference;
import io.seekerses.endlessgossip.gossip.misc.ObjectUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GossipEntity extends Villager implements GossipDrivenMob {

	private static final AtomicInteger counter = new AtomicInteger(0);
	private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY);
	private static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(EndlessGossipMod.EVENT_SENSOR.get(), SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED);
	public static final long CONVERSATION_INTERVAL = 100;

	private final Agent agent;
	private long tickBeforeConversation = CONVERSATION_INTERVAL;
	private long tickBeforeTransmit = 100L;

	public GossipEntity(String name, EntityType<? extends GossipEntity> type, Level level) {
		super(type, level);
		this.agent = new SimpleAgent(name, this);
		setCustomName(Component.literal(agent.getName()));
		GossipContext.registerAgent(this.agent);
	}

	public GossipEntity(EntityType<? extends GossipEntity> type, Level level) {
		this("Villager_" + counter.getAndIncrement(), type, level);
	}

	public GossipEntity(Level level) {
		this(EndlessGossipMod.GOSSIP_ENTITY.get(), level);
	}

	public static AttributeSupplier.Builder createAttribute() {
		return Villager.createAttributes();
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new SeeEventGoal(this, 10.0f));
	}


	@Override
	@Nonnull
	protected Brain.Provider<Villager> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	@Nonnull
	protected Brain<?> makeBrain(@Nonnull Dynamic<?> brain) {
		Brain<Villager> br = this.brainProvider().makeBrain(brain);
		registerBrainGoals(br);
		return this.brainProvider().makeBrain(brain);
	}

	private void registerBrainGoals(Brain<Villager> p_35425_) {
		VillagerProfession villagerprofession = this.getVillagerData().getProfession();
		if (this.isBaby()) {
			p_35425_.setSchedule(Schedule.VILLAGER_BABY);
			p_35425_.addActivity(Activity.PLAY, VillagerGoalPackages.getPlayPackage(0.5F));
		} else {
			p_35425_.setSchedule(Schedule.VILLAGER_DEFAULT);
			p_35425_.addActivityWithConditions(Activity.WORK, VillagerGoalPackages.getWorkPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT)));
		}

		p_35425_.addActivity(Activity.CORE, VillagerGoalPackages.getCorePackage(villagerprofession, 0.5F));
		p_35425_.addActivityWithConditions(Activity.MEET, VillagerGoalPackages.getMeetPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT)));
		p_35425_.addActivity(Activity.REST, VillagerGoalPackages.getRestPackage(villagerprofession, 0.5F));
		p_35425_.addActivity(Activity.IDLE, VillagerGoalPackages.getIdlePackage(villagerprofession, 0.5F));
		p_35425_.addActivity(Activity.PANIC, VillagerGoalPackages.getPanicPackage(villagerprofession, 0.5F));
		p_35425_.addActivity(Activity.PRE_RAID, VillagerGoalPackages.getPreRaidPackage(villagerprofession, 0.5F));
		p_35425_.addActivity(Activity.RAID, VillagerGoalPackages.getRaidPackage(villagerprofession, 0.5F));
		p_35425_.addActivity(Activity.HIDE, VillagerGoalPackages.getHidePackage(villagerprofession, 0.5F));
		p_35425_.setCoreActivities(ImmutableSet.of(Activity.CORE));
		p_35425_.setDefaultActivity(Activity.IDLE);
		p_35425_.setActiveActivityIfPossible(Activity.IDLE);
		p_35425_.updateActivityFromSchedule(this.level().getDayTime(), this.level().getGameTime());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	public void tick() {
		super.tick();
		agent.delegateTick();
		tickBeforeConversation--;
		tickBeforeTransmit--;
		if (position() != null) {
			EventSourceEntity.builder()
					.level(level())
					.lifetime(3)
					.location(position())
					.knowledge(Knowledge.builder()
							.type(KnowledgeType.DISPLACEABLE)
							.source(new EventSource(this, EventSourceType.VISION))
							.action(SimpleAction.LOCATE)
							.subject(this)
							.timestamp(LocalDateTime.now())
							.strength(100)
							.value(position().x + " " + position().y + " " + position().z).build())
					.build();
		}
//		if (tickBeforeConversation < 0) {
//			Agent collocutor = GossipSystemImpl.findCollocutorOnLocation(agent);
//			if (collocutor == null) {
//				GossipSystemImpl.discoverAgentsOnLocation(agent);
//				collocutor = GossipSystemImpl.findCollocutorOnLocation(agent);
//			}
//			if (collocutor != null) {
//				GossipSystemImpl.createInitConversationEvent(this);
//			}
//			tickBeforeConversation = CONVERSATION_INTERVAL;
//		}
		if (tickBeforeTransmit < 0) {
			agent.getMemory().getShortTermMemory().transmit();
			agent.getMemory().getShortTermMemory().getKnowledge().forEach(knowledge -> knowledge.getActualStrength(getAgent()));
			agent.getMemory().getLongTermMemory().getKnowledge().forEach(knowledge -> knowledge.getActualStrength(getAgent()));
			tickBeforeTransmit = 100;
		}
	}

	@Override
	public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat("agent.extravert", agent.getExtravert());
		tag.putFloat("agent.randomness", agent.getRandomness());
		tag.putFloat("agent.attentiveness", agent.getAttentiveness());
		tag.putString("agent.tags", agent.getPersonTags()
				.entrySet().stream()
				.map(v -> v.getKey().getName() + "=" + v.getValue().toString())
				.collect(Collectors.joining(";")));
		tag.putString("agent.interests", agent.getInterests()
				.entrySet().stream()
				.map(v -> v.getKey().getName() + "=" + v.getValue().toString())
				.collect(Collectors.joining(";")));
		tag.putString("agent.actionRelations", agent.getActionRelations()
				.entrySet().stream()
				.map(v -> v.getKey().getName() + "=" + v.getValue().toSubject() + ":" + v.getValue().toObject())
				.collect(Collectors.joining(";")));
		tag.putString("agent.blackboard", agent.getMemory().getShortTermMemory()
				.getBlackboard()
				.entrySet().stream()
				.map(entry -> {
					if (entry.getKey().equals(DefaultMemoryKeys.LOCATION)) {
						return DefaultMemoryKeys.LOCATION + "=" + ((Location) entry.getValue()).getName();
					}
					if (entry.getValue() instanceof Entity ent) {
						return entry.getKey() + "=" + ent.getUUID();
					}
					if (entry.getValue() instanceof Serializable serializable) {
						return entry.getKey() + "=" + ObjectUtil.serialize(serializable);
					}
					return entry.getKey() + "=null";
				}).collect(Collectors.joining(";")));
		tag.putString("agent.short_knowledge", agent.getMemory().getShortTermMemory().getDump());
		tag.putString("agent.long_knowlede", agent.getMemory().getLongTermMemory().getDump());
		tag.putString("agent.relations", agent.getMemory().getLongTermMemory().getRelations()
				.entrySet().stream()
				.map(entry -> entry.getKey().getUuid() + "=" + entry.getValue())
				.collect(Collectors.joining(";")));
	}

	@Override
	public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		agent.setExtravert(tag.getFloat("agent.extravert"));
		agent.setRandomness(tag.getFloat("agent.randomness"));
		agent.setAttentiveness(tag.getFloat("agent.attentiveness"));
		String pTags = tag.getString("agent.tags");
		if (!StringUtil.isNullOrEmpty(pTags)) {
			agent.setPersonTags(Arrays.stream(pTags.split(";")).map(entry -> {
				String[] sep = entry.split("=");
				return new Pair<>(GossipContext.getTag(sep[0]), Float.parseFloat(sep[1]));
			}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
		}
		String interests = tag.getString("agent.interests");
		if (!StringUtil.isNullOrEmpty(interests)) {
			agent.setInterests(Arrays.stream(interests.split(";")).map(entry -> {
				String[] sep = entry.split("=");
				return new Pair<>(GossipContext.getTag(sep[0]), Float.parseFloat(sep[1]));
			}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
		}
		String acRel = tag.getString("agent.actionRelation");
		if (!StringUtil.isNullOrEmpty(acRel)) {
			agent.setActionRelations(Arrays.stream(acRel.split(";")).map(entry -> {
				String[] sep = entry.split("=");
				String[] rel = sep[1].split(":");
				return new Pair<>(GossipContext.getAction(sep[0]), new SimpleActionRelation(Float.parseFloat(rel[0]), Float.parseFloat(rel[1])));
			}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
		}
		String blackboardStr = tag.getString("agent.blackboard");
		if (!StringUtil.isNullOrEmpty(blackboardStr)) {
			Arrays.stream(blackboardStr.split(";")).forEach(entry -> {
				String[] kv = entry.split("=");
				if (DefaultMemoryKeys.LOCATION.equals(kv[0])) {
					agent.getMemory().getShortTermMemory().write(DefaultMemoryKeys.LOCATION, WorldLocationStorage.getInstance((ServerLevel) level()).getLocation(kv[1]));
					return;
				}
				if (!StringUtil.isNullOrEmpty(kv[0])) {
					if (kv[1].length() == 16) {
						Entity entity = ((ServerLevel) level()).getEntity(UUID.fromString(kv[1]));
						if (entity != null) {
							agent.getMemory().getShortTermMemory().write(kv[0], entity);
							return;
						}
					}
					Serializable value = ObjectUtil.deserialize(kv[1]);
					if (value != null) {
						agent.getMemory().getShortTermMemory().write(kv[0], value);
					}
				}
			});
		}
		String sK = tag.getString("agent.short_knowledge");
		if (!StringUtil.isNullOrEmpty(sK)) {
			agent.getMemory().getShortTermMemory().restoreFromDump(sK, level());
		}
		String lk = tag.getString("agent.long_knowledge");
		if (!StringUtil.isNullOrEmpty(lk)) {
			agent.getMemory().getLongTermMemory().restoreFromDump(lk, level());
		}
		String relations = tag.getString("agent.relations");
		if (!StringUtil.isNullOrEmpty(relations)) {
			agent.getMemory().getLongTermMemory().addRelations(Arrays.stream(relations.split(";")).map(entry -> {
				String[] relParam = entry.split("=");
				return new Pair<>(
						new EntityReference(level(), UUID.fromString(relParam[0])),
						Float.parseFloat(relParam[1]));
			}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float damage) {
		if (damageSource.getEntity() instanceof Mob || damageSource.getEntity() instanceof Player) {
			agent.setInFight(true);
		}
		return super.hurt(damageSource, damage);
	}

	@Override
	public Agent getAgent() {
		return this.agent;
	}

	@Override
	public int getSensorSize() {
		return 10;
	}
}
