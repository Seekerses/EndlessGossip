package io.seekerses.endlessgossip.entities.location;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.knowledge.SimpleLocation;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class WorldLocation extends SimpleLocation {

	private final AABB boundingBox;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	public WorldLocation(BlockPos pos1, BlockPos pos2, String name) {
		super(name);
		this.boundingBox = new AABB(pos1, pos2);
	}

	public WorldLocation(String name, AABB aabb) {
		super(name);
		this.boundingBox = aabb;
	}

	public boolean contains(Entity entity) {
		return boundingBox.contains(entity.position());
	}

	public AABB getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Set<Agent> getAgents() {
		readWriteLock.readLock().lock();
		try {
			return super.getAgents();
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public void update(ServerLevel level) {
		readWriteLock.writeLock().lock();
		try {
			clear();
			Predicate<Entity> playerFilter = entity -> entity instanceof GossipDrivenMob;
			List<Entity> entities = level.getEntities((Entity) null, boundingBox, playerFilter);
			entities.forEach(entity -> {
				if (entity instanceof GossipDrivenMob gdm) {
					gdm.getAgent().getMemory().getShortTermMemory().write(DefaultMemoryKeys.LOCATION, this);
					addAgent(gdm.getAgent());
				}
			});
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
}
