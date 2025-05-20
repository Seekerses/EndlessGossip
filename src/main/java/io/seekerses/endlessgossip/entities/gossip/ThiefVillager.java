package io.seekerses.endlessgossip.entities.gossip;

import io.seekerses.endlessgossip.EndlessGossipMod;
import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.entities.gossip.enums.DomainAction;
import io.seekerses.endlessgossip.entities.gossip.enums.DomainTag;
import io.seekerses.endlessgossip.entities.location.WorldLocation;
import io.seekerses.endlessgossip.entities.location.WorldLocationStorage;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.SimpleActionRelation;
import io.seekerses.endlessgossip.gossip.communication.gossipSystem.GossipSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.BlackboardListener;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import io.seekerses.endlessgossip.util.DayTime;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ThiefVillager extends GossipEntity {

	private static final AtomicInteger counter = new AtomicInteger(0);
	public static final String CONVERSATION_LOCATION = "Tavern";
	public static final int STEAL_INTERVAL = 1000;

	private boolean isInConversationLocation = false;
	private boolean movingToTavern = false;
	private boolean alreadyTalked = false;
	private int ticksBeforeSteal = STEAL_INTERVAL;
	private int convCount = 0;

	public ThiefVillager(Level level) {
		super("Thief_" + counter.getAndIncrement(), EndlessGossipMod.THIEF_ENTITY.get(), level);
		this.getAgent().getMemory().getShortTermMemory().registerListener(new BlackboardListener() {
			@Override
			public String getProperty() {
				return DefaultMemoryKeys.LOCATION;
			}

			@Override
			public void processChange(Object old, Object updated) {
				Location location = WorldLocationStorage.getInstance((ServerLevel) level()).getLocation(CONVERSATION_LOCATION);
				isInConversationLocation = updated == location;
				movingToTavern = false;
			}
		});
		getAgent().setInterests(Map.of(
				GossipContext.getTag(DomainTag.MERCHANT.getName()), 15f,
				GossipContext.getTag(DomainTag.THIEF.getName()), 100f,
				GossipContext.getTag(DomainTag.STRANGER.getName()), 75f
		));
		getAgent().setPersonTags(Map.of(GossipContext.getTag(DomainTag.THIEF.name()), 45f));
		getAgent().setRandomness(50f);
		getAgent().setExtravert(40f);
		getAgent().setAttentiveness(70f);
		getAgent().setActionRelations(Map.of(
				GossipContext.getAction(DomainAction.TRADE.getName()), new SimpleActionRelation(-5f, -5f),
				GossipContext.getAction(DomainAction.TELL.getName()), new SimpleActionRelation(5f, 5f),
				GossipContext.getAction(DomainAction.STEAL.getName()), new SimpleActionRelation(10f, -5f)
		));
	}

	@Override
	public void tick() {
		super.tick();
		DayTime dayTime = DayTime.of(level().getDayTime());
//		if (!movingToTavern && dayTime
//				.isOdd()
//			&& !isInConversationLocation) {
//			WorldLocation location = (WorldLocation) GossipContext.getLocation("Tavern");
//			if (location != null) {
//				Vec3 center = location.getBoundingBox().getCenter();
//				BlockPos targetPos = BlockPos.containing(center);
//				getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
//			}
//		}
		if (!alreadyTalked && EndlessGossipMod.conversationTime) {
			Agent collocutor = GossipSystemImpl.findCollocutorOnLocation(getAgent());
			if (collocutor == null) {
				GossipSystemImpl.discoverAgentsOnLocation(getAgent());
				collocutor = GossipSystemImpl.findCollocutorOnLocation(getAgent());
			}
			if (collocutor != null) {
				GossipSystemImpl.createInitConversationEvent(this);
				alreadyTalked = true;
				convCount++;
			}
		}
		if (alreadyTalked && !EndlessGossipMod.conversationTime) {
			alreadyTalked = false;
			if (convCount > 3) {
				getAgent().getMemory().getShortTermMemory().write(DefaultMemoryKeys.TALKED_RECENTLY, new ArrayList<>());
				convCount = 0;
			}
		}
		if (ticksBeforeSteal < 0) {
			steal();
			ticksBeforeSteal = STEAL_INTERVAL;
		}
		ticksBeforeSteal--;
	}

	public void steal() {
		List<MerchantVillager> merchants = level().getEntitiesOfClass(MerchantVillager.class, getBoundingBox().inflate(10));
		if (!merchants.isEmpty()) {
			EventSourceEntity.builder()
					.location(position())
					.level(level())
					.knowledge(Knowledge.builder()
							.subject(this)
							.object(merchants.get(0))
							.value("Something")
							.strength(100)
							.timestamp(LocalDateTime.now())
							.type(KnowledgeType.REGULAR)
							.action(GossipContext.getAction("STEAL"))
							.knowledgeTag(GossipContext.getTag("CRIMINAL")).build())
					.lifetime(10)
					.build();
		}
	}
}
