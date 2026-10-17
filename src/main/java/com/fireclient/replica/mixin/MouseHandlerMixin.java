package com.fireclient.replica.mixin;

import com.fireclient.replica.Config;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Scroll click: while enabled and in-game (no screen open), one wheel notch becomes one mouse click
 * instead of changing the hotbar slot. Scroll up = left click, scroll down = right click
 * (swapped if "invert" is on). Uses the default mouse-button bindings.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void fireclientreplica$scrollClick(long window, double xOffset, double yOffset, CallbackInfo ci) {
		if (!Config.scrollClick || yOffset == 0.0) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.gui.screen() != null) {
			return;
		}
		boolean up = yOffset > 0.0;
		if (Config.scrollClickInvert) {
			up = !up;
		}
		int button = up ? InputConstants.MOUSE_BUTTON_LEFT : InputConstants.MOUSE_BUTTON_RIGHT;
		KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(button));
		ci.cancel();
	}
}
