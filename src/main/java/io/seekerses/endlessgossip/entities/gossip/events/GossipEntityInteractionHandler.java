package io.seekerses.endlessgossip.entities.gossip.events;

import io.seekerses.endlessgossip.entities.gossip.GossipEntity;
import io.seekerses.endlessgossip.entities.gossip.menu.AgentInspector;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GossipEntityInteractionHandler {

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.getTarget() instanceof GossipEntity entity && event.getEntity() != null) {
			Minecraft.getInstance().execute(() ->
							Minecraft.getInstance().setScreen(new AgentInspector(entity.getAgent())));
		}
	}
}
