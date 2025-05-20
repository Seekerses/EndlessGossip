package io.seekerses.endlessgossip.gossip.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

public class EntityReference {
	private Entity entity;
	private Level level;
	private UUID uuid;

	public EntityReference(Level level, UUID uuid) {
		this.uuid = uuid;
		this.level = level;
	}

	public EntityReference(Entity entity) {
		this.entity = entity;
		this.level = entity.level();
		this.uuid = entity.getUUID();
	}

public Entity getEntity() {
	if (entity == null && uuid != null) {
		entity = ((ServerLevel) level).getEntity(uuid);
	}
	return entity;
}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(uuid);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EntityReference that)) {
			return false;
		}
		return Objects.equals(uuid, that.uuid);
	}
}
