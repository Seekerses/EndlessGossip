package io.seekerses.endlessgossip.entities.location;

import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WorldLocationStorage extends SavedData {

	private final Map<String, WorldLocation> locations = new HashMap<>();

	public static WorldLocationStorage getInstance(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				WorldLocationStorage::load,
				WorldLocationStorage::new,
				"world_locations");
	}

	public void registerLocation(WorldLocation location) {
		locations.put(location.getName(), location);
		GossipContext.registerLocation(location);
		setDirty();
	}

	public Location getLocation(String locationName) {
		return locations.get(locationName);
	}

	public Collection<WorldLocation> getLocations() {
		return locations.values();
	}

	public static WorldLocationStorage load(CompoundTag tag) {
		WorldLocationStorage data = new WorldLocationStorage();
		ListTag locationTag = tag.getList("locations", Tag.TAG_COMPOUND);

		for (Tag tg : locationTag) {
			CompoundTag locTag = (CompoundTag) tg;
			AABB boundingBox = new AABB(
					locTag.getDouble("max_x"),
					locTag.getDouble("max_y"),
					locTag.getDouble("max_z"),
					locTag.getDouble("min_x"),
					locTag.getDouble("min_y"),
					locTag.getDouble("min_z")

			);
			String name = locTag.getString("name");
			data.registerLocation(new WorldLocation(name, boundingBox));
		}
		data.registerLocation(new WorldLocation("undefined", new AABB(0, 0, 0, 0, 0, 0)));
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag listTag = new ListTag();
		locations.values().forEach(loc -> {
			CompoundTag locationTag = new CompoundTag();
			locationTag.putString("name", loc.getName());
			AABB aabb = loc.getBoundingBox();
			locationTag.putDouble("min_x", aabb.minX);
			locationTag.putDouble("min_y", aabb.minY);
			locationTag.putDouble("min_z", aabb.minZ);
			locationTag.putDouble("max_x", aabb.maxX);
			locationTag.putDouble("max_y", aabb.maxY);
			locationTag.putDouble("max_z", aabb.maxZ);
			listTag.add(locationTag);
		});
		tag.put("locations", listTag);
		return tag;
	}
}
