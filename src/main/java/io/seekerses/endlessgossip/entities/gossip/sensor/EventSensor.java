package io.seekerses.endlessgossip.entities.gossip.sensor;

import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.gossip.GossipConfig;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.knowledge.EventSource;
import io.seekerses.endlessgossip.gossip.knowledge.EventSourceType;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventSensor extends Sensor<LivingEntity> {

	private final Set<Knowledge> sensedEvents = new HashSet<>();
	private long lastClearTime = System.currentTimeMillis();

	@Override
	protected void doTick(@Nonnull ServerLevel level, @Nonnull LivingEntity npc) {
		if (npc instanceof GossipDrivenMob gossipMob) {
			Agent agent = gossipMob.getAgent();
			AABB detectionArea = npc.getBoundingBox().inflate(gossipMob.getSensorSize());
			List<EventSourceEntity> knowledgeEvents = level.getEntitiesOfClass(EventSourceEntity.class, detectionArea);

			if (!knowledgeEvents.isEmpty()) {
				knowledgeEvents.forEach(event -> {
					if (event.getKnowledge().getSubject() != null
						&& event.getKnowledge().getSubject().getEntity() == gossipMob) {
						return;
					}
					Knowledge knowledge = event.getKnowledge();
					if (!sensedEvents.contains(knowledge)) {
						knowledge.setSource(new EventSource(npc, EventSourceType.VISION));
						agent.receiveKnowledge(knowledge);
						sensedEvents.add(knowledge);
					}
				});
			}
		}
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastClearTime > GossipConfig.SENSORS_CLEAR_TIME) {
			sensedEvents.clear();
			lastClearTime = currentTime;
		}
	}

	@Override
	@Nonnull
	public Set<MemoryModuleType<?>> requires() {
		return Set.of();
	}
}
