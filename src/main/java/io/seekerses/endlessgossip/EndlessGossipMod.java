package io.seekerses.endlessgossip;

import com.mojang.logging.LogUtils;
import io.seekerses.endlessgossip.commands.MarkLocationCommand;
import io.seekerses.endlessgossip.entities.eventSource.EventSourceEntity;
import io.seekerses.endlessgossip.entities.gossip.GossipEntity;
import io.seekerses.endlessgossip.entities.eventSource.TestEventSource;
import io.seekerses.endlessgossip.entities.gossip.MerchantVillager;
import io.seekerses.endlessgossip.entities.gossip.StrangerVillager;
import io.seekerses.endlessgossip.entities.gossip.ThiefVillager;
import io.seekerses.endlessgossip.entities.gossip.sensor.EventSensor;
import io.seekerses.endlessgossip.entities.location.WorldLocationStorage;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.knowledge.SimpleAction;
import io.seekerses.endlessgossip.gossip.knowledge.SimpleTag;
import io.seekerses.endlessgossip.util.DayTime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EndlessGossipMod.MOD_ID)
public class EndlessGossipMod {
	public static final String MOD_ID = "endlessgossip";
	private static final Logger LOGGER = LogUtils.getLogger();

	// REGISTERS

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, MOD_ID);

	// SENSORS

	public static final RegistryObject<SensorType<EventSensor>> EVENT_SENSOR = SENSORS.register("event_sensor",
			() -> new SensorType<>(EventSensor::new));

	// ENTITIES

	public static final RegistryObject<EntityType<GossipEntity>> GOSSIP_ENTITY = ENTITIES.register("gossip_npc",
			() -> EntityType.Builder
					.of((EntityType.EntityFactory<GossipEntity>) (type, level) -> new GossipEntity(level), MobCategory.MISC)
					.sized(0.6f, 1.95f)
					.clientTrackingRange(10)
					.build("gossip_npc"));
	public static final RegistryObject<EntityType<MerchantVillager>> MERCHANT_ENTITY = ENTITIES.register("gossip_merchant",
			() -> EntityType.Builder
					.of((EntityType.EntityFactory<MerchantVillager>) (type, level) -> new MerchantVillager(level), MobCategory.MISC)
					.sized(0.6f, 1.95f)
					.clientTrackingRange(10)
					.build("gossip_merchant"));
	public static final RegistryObject<EntityType<ThiefVillager>> THIEF_ENTITY = ENTITIES.register("gossip_thief",
			() -> EntityType.Builder
					.of((EntityType.EntityFactory<ThiefVillager>) (type, level) -> new ThiefVillager(level), MobCategory.MISC)
					.sized(0.6f, 1.95f)
					.clientTrackingRange(10)
					.build("gossip_thief"));
	public static final RegistryObject<EntityType<StrangerVillager>> STRANGER_ENTITY = ENTITIES.register("gossip_stranger",
			() -> EntityType.Builder
					.of((EntityType.EntityFactory<StrangerVillager>) (type, level) -> new StrangerVillager(level), MobCategory.MISC)
					.sized(0.6f, 1.95f)
					.clientTrackingRange(10)
					.build("gossip_stranger"));
	public static final RegistryObject<EntityType<EventSourceEntity>> EVENT_SOURCE_ENTITY = ENTITIES.register("event_source_entity",
			() -> EntityType.Builder
					.of((EntityType.EntityFactory<EventSourceEntity>) (type, level) -> new EventSourceEntity(level), MobCategory.CREATURE)
					.sized(0.1f, 0.1f)
					.requiredFeatures()
					.build("event_source_entity"));
	public static final RegistryObject<EntityType<TestEventSource>> TEST_EVENT_SOURCE_ENTITY = ENTITIES.register("test_event_source_entity",
			() -> EntityType.Builder
					.of((EntityType.EntityFactory<TestEventSource>) (type, level) -> new TestEventSource(level), MobCategory.CREATURE)
					.sized(0.1f, 0.1f)
					.build("test_event_source_entity"));
	// ITEMS

	public static final RegistryObject<Item> GOSSIP_ENTITY_SPAWN_EGG = ITEMS.register("gossip_entity_spawn_egg",
			() -> new ForgeSpawnEggItem(GOSSIP_ENTITY, 0x000000, 0x000000, new Item.Properties()));
	public static final RegistryObject<Item> MERCHANT_SPAWN_EGG = ITEMS.register("gossip_merchant_spawn_egg",
			() -> new ForgeSpawnEggItem(MERCHANT_ENTITY, 0xFF0000, 0x770000, new Item.Properties()));
	public static final RegistryObject<Item> THIEF_SPAWN_EGG = ITEMS.register("gossip_thief_spawn_egg",
			() -> new ForgeSpawnEggItem(THIEF_ENTITY, 0x00FF00, 0x007700, new Item.Properties()));
	public static final RegistryObject<Item> STRANGER_SPAWN_EGG = ITEMS.register("gossip_stranger_spawn_egg",
			() -> new ForgeSpawnEggItem(STRANGER_ENTITY, 0x0000FF, 0x000077, new Item.Properties()));
	public static final RegistryObject<Item> TEST_EVENT_SOURCE_ENTITY_SPAWN_EGG = ITEMS.register("test_event_source_entity_spawn_egg",
			() -> new ForgeSpawnEggItem(TEST_EVENT_SOURCE_ENTITY, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

	// TABS

	public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("endlessgossip_tab", () ->
			CreativeModeTab.builder()
					.withTabsBefore(CreativeModeTabs.COMBAT)
					.title(Component.translatable("item_group." + MOD_ID + ".endlessgossip_tab"))
					.icon(Items.DIAMOND::getDefaultInstance)
					.displayItems((parameters, output) -> {
						output.accept(TEST_EVENT_SOURCE_ENTITY_SPAWN_EGG.get());
						output.accept(GOSSIP_ENTITY_SPAWN_EGG.get());
						output.accept(MERCHANT_SPAWN_EGG.get());
						output.accept(THIEF_SPAWN_EGG.get());
						output.accept(STRANGER_SPAWN_EGG.get());
					}).build());

	public EndlessGossipMod() {

		GossipContext.init();
		SimpleAction.registerStandardActions();
		SimpleTag.registerStandardTags();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);

		// Register the Deferred Register to the mod event bus so blocks get registered
		BLOCKS.register(modEventBus);
		// Register the Deferred Register to the mod event bus so items get registered
		ITEMS.register(modEventBus);
		// Register the Deferred Register to the mod event bus so tabs get registered
		CREATIVE_MODE_TABS.register(modEventBus);

		ENTITIES.register(modEventBus);

		SENSORS.register(modEventBus);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.addListener(Config::onEntityAttributeCreation);
		MinecraftForge.EVENT_BUS.addListener(Config::onClientSetup);
		MinecraftForge.EVENT_BUS.addListener(Config::onLoad);
		MinecraftForge.EVENT_BUS.addListener(EndlessGossipMod::onServerTick);

		// Register the item to a creative tab
		modEventBus.addListener(this::addCreative);

		// Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		// Some common setup code
		LOGGER.info("HELLO FROM COMMON SETUP");

		if (Config.logDirtBlock) {
			LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
		}
		;

		LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

		Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
	}

	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
			event.accept(TEST_EVENT_SOURCE_ENTITY_SPAWN_EGG);
			event.accept(GOSSIP_ENTITY_SPAWN_EGG);
			event.accept(MERCHANT_SPAWN_EGG);
			event.accept(THIEF_SPAWN_EGG);
			event.accept(STRANGER_SPAWN_EGG);
		}
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		// Do something when the server starts
		LOGGER.info("HELLO from server starting");
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		MarkLocationCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void onWorldLoad(LevelEvent.Load event) {
		if (event.getLevel() instanceof ServerLevel serverLevel) {
			WorldLocationStorage storage = WorldLocationStorage.getInstance(serverLevel);
		}
	}

	@SubscribeEvent
	public void onWorldSave(LevelEvent.Save event) {
		if (event.getLevel() instanceof ServerLevel serverLevel) {
			WorldLocationStorage.getInstance(serverLevel);
		}
	}

	private static DayTime lastDay = new DayTime(0);
	public static boolean conversationTime = false;
	private static final int CONV_INTERVAL = 20;
	private static int currInt = CONV_INTERVAL;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;

		ServerLevel level = event.getServer().overworld();
		DayTime time = new DayTime(level.getDayTime());

		currInt--;
		if (currInt < 0) {
			if (conversationTime) {
				conversationTime = false;
				GossipContext.writeConversation();
				GossipContext.dumpRelation();
				GossipContext.dumpKnowledges();
			} else {
				conversationTime = true;
			}
			currInt = CONV_INTERVAL;
		}
	}

	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			// Some client setup code
			LOGGER.info("HELLO FROM CLIENT SETUP");
			LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
		}
	}
}
