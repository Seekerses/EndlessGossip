package io.seekerses.endlessgossip.entities.gossip.render;

import io.seekerses.endlessgossip.entities.gossip.GossipEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GossipEntityRenderer extends MobRenderer<GossipEntity, VillagerModel<GossipEntity>> {

	private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

	public GossipEntityRenderer(EntityRendererProvider.Context p_174304_, VillagerModel<GossipEntity> p_174305_, float p_174306_) {
		super(p_174304_, p_174305_, p_174306_);
	}

	@Override
	public ResourceLocation getTextureLocation(GossipEntity entity) {
		return VILLAGER_BASE_SKIN;
	}
}
