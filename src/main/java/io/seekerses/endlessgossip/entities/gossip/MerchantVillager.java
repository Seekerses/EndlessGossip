package io.seekerses.endlessgossip.entities.gossip;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import io.seekerses.endlessgossip.EndlessGossipMod;
import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.entities.gossip.enums.DomainAction;
import io.seekerses.endlessgossip.entities.gossip.enums.DomainTag;
import io.seekerses.endlessgossip.entities.location.WorldLocation;
import io.seekerses.endlessgossip.entities.location.WorldLocationStorage;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.ActionRelation;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.SimpleActionRelation;
import io.seekerses.endlessgossip.gossip.agent.SimpleAgent;
import io.seekerses.endlessgossip.gossip.agent.Squad;
import io.seekerses.endlessgossip.gossip.communication.gossipSystem.GossipSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.BlackboardListener;
import io.seekerses.endlessgossip.gossip.knowledge.CustomAction;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import io.seekerses.endlessgossip.util.DayTime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MerchantVillager extends GossipEntity {


	private static final Logger LOGGER = LogUtils.getLogger();

	private static final AtomicInteger counter = new AtomicInteger(0);
	public static final String CONVERSATION_LOCATION = "Tavern";
	public static final int TRADE_INTERVAL = 1000;

	private boolean isInConversationLocation = false;
	private boolean movingToTavern = false;
	private boolean alreadyTalked = false;
	private int ticksBeforeTrade = TRADE_INTERVAL;
	private int convCount = 0;

	public MerchantVillager(Level level) {
		super("Merchant_" + counter.getAndIncrement(), EndlessGossipMod.MERCHANT_ENTITY.get(), level);
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
				GossipContext.getTag(DomainTag.MERCHANT.getName()), 100f,
				GossipContext.getTag(DomainTag.THIEF.getName()), -10f,
				GossipContext.getTag(DomainTag.STRANGER.getName()), 75f
		));
		getAgent().setPersonTags(Map.of(GossipContext.getTag(DomainTag.MERCHANT.name()), 20f));
		getAgent().setRandomness(20f);
		getAgent().setExtravert(30f);
		getAgent().setAttentiveness(50f);
		getAgent().setActionRelations(Map.of(
				GossipContext.getAction(DomainAction.TRADE.getName()), new SimpleActionRelation(10f, 10f),
				GossipContext.getAction(DomainAction.TELL.getName()), new SimpleActionRelation(15f, 15f),
				GossipContext.getAction(DomainAction.STEAL.getName()), new SimpleActionRelation(-10f, 0f),
				GossipContext.getAction(DomainAction.GRIEF.getName()), new SimpleActionRelation(-10f, 0f),
				GossipContext.getAction(DomainAction.CHARITY.getName()), new SimpleActionRelation(50f, 0f)
		));
	}

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
				collocutor.receiveKnowledge(GossipSystemImpl.createInitConversation(getAgent()));
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
		if (ticksBeforeTrade < 0) {
			Random rand = new Random();
			float change = rand.nextFloat();
			if (change < 0.5) {
				grief();
			} else {
				trade();
			}
			ticksBeforeTrade = TRADE_INTERVAL;
		}
		Squad squad = getAgent().getMemory().getShortTermMemory().read(DefaultMemoryKeys.SQUAD);
		if (squad != null && squad.getAgents().size() > 1) {
			if (squad.getLeader() == null) {
				getAgent().proposeLeaderElection();
			}
			if (squad.getLeader() == getAgent()) {
				getAgent().proposePlan(new SimpleAgent.RunnableSerializable(() -> LOGGER.info("CALL TO ARMS !!!")));
				squad.setLeader(null);
			}
		}
		ticksBeforeTrade--;
	}


	public void trade() {
		List<MerchantVillager> merchants = level().getEntitiesOfClass(MerchantVillager.class, getBoundingBox().inflate(10));
		if (!merchants.isEmpty()) {
			EventSourceEntity.builder()
					.location(position())
					.level(level())
					.counter(1)
					.knowledge(Knowledge.builder()
							.subject(this)
							.object(merchants.get(0))
							.value("Something valuable")
							.strength(100)
							.timestamp(LocalDateTime.now())
							.type(KnowledgeType.REGULAR)
							.action(GossipContext.getAction(DomainAction.TRADE.getName()))
							.knowledgeTag(Set.of(GossipContext.getTag(DomainTag.VALUABLE.getName())))
							.build())
					.lifetime(10)
					.build();
		}
	}

	public void grief() {
		List<MerchantVillager> merchants = level().getEntitiesOfClass(MerchantVillager.class, getBoundingBox().inflate(10));
		if (!merchants.isEmpty()) {
			EventSourceEntity.builder()
					.location(position())
					.level(level())
					.counter(1)
					.knowledge(Knowledge.builder()
							.subject(this)
							.object(merchants.get(0))
							.value("Something valuable")
							.strength(120)
							.timestamp(LocalDateTime.now())
							.type(KnowledgeType.REGULAR)
							.action(GossipContext.getAction(DomainAction.GRIEF.getName()))
							.knowledgeTag(Set.of(GossipContext.getTag(DomainTag.VALUABLE.getName())))
							.build())
					.lifetime(10)
					.build();
		}
	}

	public void charity() {
		List<MerchantVillager> merchants = level().getEntitiesOfClass(MerchantVillager.class, getBoundingBox().inflate(10));
		if (!merchants.isEmpty()) {
			EventSourceEntity.builder()
					.location(position())
					.level(level())
					.counter(1)
					.knowledge(Knowledge.builder()
							.subject(this)
							.object(merchants.get(0))
							.value("Something valuable")
							.strength(120)
							.timestamp(LocalDateTime.now())
							.type(KnowledgeType.REGULAR)
							.action(GossipContext.getAction(DomainAction.CHARITY.getName()))
							.knowledgeTag(Set.of(GossipContext.getTag(DomainTag.VALUABLE.getName())))
							.build())
					.lifetime(10)
					.build();
		}
	}
}
