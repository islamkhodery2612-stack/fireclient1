package com.fireclient.replica;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class FireClientMod implements ClientModInitializer {
	public static final String MOD_ID = "fireclientreplica";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyMapping kitsKey;
	private static KeyMapping settingsKey;
	private static Supplier<Screen> pendingScreen;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	/** Opens a screen at the end of the next client tick (safe to call from commands). */
	public static void openNextTick(Supplier<Screen> screen) {
		pendingScreen = screen;
	}

	@Override
	public void onInitializeClient() {
		Config.load();

		KeyMapping.Category category = KeyMapping.Category.register(id("main"));
		kitsKey = new KeyMapping("key.fireclientreplica.kits", InputConstants.Type.KEYSYM, InputConstants.KEY_K, category);
		settingsKey = new KeyMapping("key.fireclientreplica.settings", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, category);
		KeyMappingHelper.registerKeyMapping(kitsKey);
		KeyMappingHelper.registerKeyMapping(settingsKey);

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id("coords"), CoordsHud::extract);

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (kitsKey.consumeClick()) {
				openNextTick(KitScreen::new);
			}
			while (settingsKey.consumeClick()) {
				openNextTick(SettingsScreen::new);
			}
			Supplier<Screen> next = pendingScreen;
			if (next != null) {
				pendingScreen = null;
				mc.gui.setScreen(next.get());
			}
		});

		FireClientCommands.register();
		LOGGER.info("FireClient (replica) loaded");
	}
}
