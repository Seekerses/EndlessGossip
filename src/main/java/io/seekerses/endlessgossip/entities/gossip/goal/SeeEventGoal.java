package io.seekerses.endlessgossip.entities.gossip.goal;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.Collection;

public class SeeEventGoal extends Goal {

	public static final Logger LOGGER = LogUtils.getLogger();
	private PathfinderMob npc;
	private final double detectionRange;

	public SeeEventGoal(PathfinderMob npc, double detectionRange) {
		this.npc = npc;
		this.detectionRange = detectionRange;
	}

	@Override
	public boolean canUse() {
		AABB detectionArea = npc.getBoundingBox().inflate(detectionRange);
		Level level = npc.level();
		return !level.getEntitiesOfClass(Player.class, detectionArea).isEmpty();
	}

	@Override
	public void start() {
		npc.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS).stream().flatMap(Collection::stream).forEach(player -> player.sendSystemMessage(Component.literal("I see you...")));
		LOGGER.info("Player Detected !");
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public void stop() {
		LOGGER.info("Player no longer detected !");
	}


}
