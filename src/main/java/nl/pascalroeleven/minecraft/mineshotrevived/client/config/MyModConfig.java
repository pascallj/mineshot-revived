package nl.pascalroeleven.minecraft.mineshotrevived.client.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MyModConfig {
	public static final ClientConfig CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;
	static {
		final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	public static int captureWidth;
	public static int captureHeight;

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == MyModConfig.CLIENT_SPEC) {
			bakeConfig();
		}
	}

	public static void bakeConfig() {
		captureWidth = CLIENT.captureWidth.get();
		captureHeight = CLIENT.captureHeight.get();
	}

	public static class ClientConfig {
		public static final int MAX_TARGA_SIZE = 0xffff;

		public final IntValue captureWidth;
		public final IntValue captureHeight;

		public ClientConfig(ForgeConfigSpec.Builder builder) {
			captureWidth = builder.comment("The width (in pixels) to capture").defineInRange("captureWidth", 3840, 1,
					MAX_TARGA_SIZE);
			captureHeight = builder.comment("The height (in pixels) to capture").defineInRange("captureHeight", 2160, 1,
					MAX_TARGA_SIZE);
		}
	}
}