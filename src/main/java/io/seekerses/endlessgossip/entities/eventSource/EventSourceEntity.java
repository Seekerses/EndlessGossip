package io.seekerses.endlessgossip.entities.eventSource;

import io.seekerses.endlessgossip.EndlessGossipMod;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EventSourceEntity extends Mob {

	private long lifetime = 0;
	private Knowledge knowledge;
	private long counter = -1;

	public EventSourceEntity(Level world) {
		this(EndlessGossipMod.EVENT_SOURCE_ENTITY.get(), world);
	}

	public EventSourceEntity(EntityType<? extends Mob> type, Level world) {
		super(type, world);
	}

	public static AttributeSupplier.Builder createAttribute() {
		return Mob.createMobAttributes();
	}

	public Knowledge getKnowledge() {
		return knowledge;
	}

	public long getCounter() {
		return this.counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putLong("lifetime", lifetime);
		tag.putString("knowledge", knowledge.serialize());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.lifetime = tag.getLong("lifetime");
		this.knowledge = Knowledge.deserialize(level(), tag.getString("knowledge"));
	}

	public void setKnowledge(Knowledge knowledge) {
		this.knowledge = knowledge;
	}

	public void setLifetime(long lifetime) {
		this.lifetime = lifetime;
	}

	@Override
	public void tick() {
		super.tick();
		lifetime--;
		if (lifetime <= 0) {
			remove(RemovalReason.DISCARDED);
		}
	}

	@Override
	public void baseTick() {
		super.baseTick();
	}

	public static EventSourceBuilder builder() {
		return new EventSourceBuilder();
	}

	public static class EventSourceBuilder {

		private Level level;
		private Vec3 location;
		private Knowledge knowledge;
		private long lifetime;
		private long counter = -1;

		public EventSourceBuilder knowledge(Knowledge knowledge) {
			this.knowledge = knowledge;
			return this;
		}

		public EventSourceBuilder lifetime(long lifetime) {
			this.lifetime = lifetime;
			return this;
		}

		public EventSourceBuilder location(Vec3 location) {
			this.location = location;
			return this;
		}

		public EventSourceBuilder level(Level level) {
			this.level = level;
			return this;
		}

		public EventSourceBuilder counter(long counter) {
			this.counter = counter;
			return this;
		}

		public EventSourceEntity build() {
			EventSourceEntity entity = new EventSourceEntity(level);
			entity.setKnowledge(knowledge);
			entity.setLifetime(lifetime);
			entity.setPos(location);
			entity.setCounter(counter);
			level.addFreshEntity(entity);
			return entity;
		}
	}
}
