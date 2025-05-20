package io.seekerses.endlessgossip.entities.gossip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NoOpRenderer<T extends Entity> extends EntityRenderer<T> {
	public NoOpRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		// Do nothing (no-op)
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		// Return a dummy texture location (required by the superclass)
		return new ResourceLocation("minecraft", "textures/block/stone.png");
	}
}

