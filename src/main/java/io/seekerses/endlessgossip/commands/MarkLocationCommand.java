package io.seekerses.endlessgossip.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.seekerses.endlessgossip.entities.location.WorldLocation;
import io.seekerses.endlessgossip.entities.location.WorldLocationStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class MarkLocationCommand {

	private static final Map<Player, BlockPos> cornerMap = new HashMap<>();

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		 dispatcher.register(Commands.literal("marklocation")
                .then(Commands.argument("name", StringArgumentType.string())
						.executes(context
								-> markLocation(context.getSource(), StringArgumentType.getString(context, "name")))
                )
                .executes(context -> {
                    context.getSource().sendFailure(Component.literal("You must specify a name for the location!"));
                    return 0;
                })
        );
	}

	private static int markLocation(CommandSourceStack source, String name) {
		if (source.getEntity() instanceof Player player) {
			if (player.getMainHandItem().getItem() == Items.WOODEN_AXE) {
				BlockPos currentPos = player.blockPosition();

				if (!cornerMap.containsKey(player)) {
					// First corner
					cornerMap.put(player, currentPos);
					source.sendSuccess(() -> Component.literal("First corner set at: " + currentPos), false);
				} else {
					// Second corner
					BlockPos corner1 = cornerMap.get(player);

					// Create the location
					WorldLocation location = new WorldLocation(corner1, currentPos, name);
					WorldLocationStorage.getInstance((ServerLevel) player.level()).registerLocation(location); // Add the location to a manager (see Step 3)

					// Clear the first corner
					cornerMap.remove(player);

					source.sendSuccess(() -> Component.literal("Location marked with corners: " + corner1 + " and " + currentPos), false);
				}
			} else {
				source.sendFailure(Component.literal("You must hold a wooden axe to mark locations!"));
			}
		} else {
			source.sendFailure(Component.literal("This command can only be used by players!"));
		}

		return 1;
	}
}
