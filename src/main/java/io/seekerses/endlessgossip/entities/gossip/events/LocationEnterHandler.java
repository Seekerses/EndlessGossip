package io.seekerses.endlessgossip.entities.gossip.events;

import io.seekerses.endlessgossip.entities.location.WorldLocation;
import io.seekerses.endlessgossip.entities.location.WorldLocationStorage;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class LocationEnterHandler {

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (ServerLevel level: event.getServer().getAllLevels()) {
				for (Location location : WorldLocationStorage.getInstance(level).getLocations()) {
					if (location instanceof WorldLocation worldLocation) {
						worldLocation.update(level);
					}
				}
			}
		}
	}
}
