package io.seekerses.endlessgossip;

import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.entities.gossip.GossipEntity;
import io.seekerses.endlessgossip.entities.gossip.MerchantVillager;
import io.seekerses.endlessgossip.entities.gossip.StrangerVillager;
import io.seekerses.endlessgossip.entities.gossip.ThiefVillager;
import io.seekerses.endlessgossip.entities.gossip.render.NoOpRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = EndlessGossipMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(EndlessGossipMod.GOSSIP_ENTITY.get(), VillagerRenderer::new);
        EntityRenderers.register(EndlessGossipMod.MERCHANT_ENTITY.get(), VillagerRenderer::new);
        EntityRenderers.register(EndlessGossipMod.THIEF_ENTITY.get(), VillagerRenderer::new);
        EntityRenderers.register(EndlessGossipMod.STRANGER_ENTITY.get(), VillagerRenderer::new);
        EntityRenderers.register(EndlessGossipMod.EVENT_SOURCE_ENTITY.get(), NoOpRenderer::new);
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(EndlessGossipMod.GOSSIP_ENTITY.get(), GossipEntity.createAttributes().build());
        event.put(EndlessGossipMod.EVENT_SOURCE_ENTITY.get(), EventSourceEntity.createAttribute().build());
        event.put(EndlessGossipMod.MERCHANT_ENTITY.get(), MerchantVillager.createAttributes().build());
        event.put(EndlessGossipMod.THIEF_ENTITY.get(), ThiefVillager.createAttributes().build());
        event.put(EndlessGossipMod.STRANGER_ENTITY.get(), StrangerVillager.createAttributes().build());
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }
}
