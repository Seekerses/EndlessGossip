package io.seekerses.endlessgossip.gossip.knowledge;

import io.seekerses.endlessgossip.gossip.misc.EntityReference;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EventSource {
	private EntityReference source;
	private EventSourceType type;

	public EventSource(UUID source, Level level, EventSourceType type) {
		this.source = new EntityReference(level, source);
		this.type = type;
	}

	public EventSource(Entity entity, EventSourceType type) {
		this.source = new EntityReference(entity);
		this.type = type;
	}

	public EventSource(EntityReference source, EventSourceType type) {
		this.source = source;
		this.type = type;
	}

	public Entity getSource() {
		return source == null ? null : source.getEntity();
	}

	public EventSourceType getType() {
		return type;
	}
}
