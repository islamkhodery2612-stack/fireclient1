package com.fireclient.replica;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class CoordsHud {
	private CoordsHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (!Config.coordsEnabled || mc.player == null || mc.level == null) {
			return;
		}

		int x = Mth.floor(mc.player.getX());
		int y = Mth.floor(mc.player.getY());
		int z = Mth.floor(mc.player.getZ());

		List<String> lines = new ArrayList<>();
		lines.add("XYZ: " + x + " / " + y + " / " + z);

		if (Config.showScaled) {
			if (mc.level.dimension() == Level.OVERWORLD) {
				lines.add("Nether: " + Math.floorDiv(x, 8) + " / " + Math.floorDiv(z, 8));
			} else if (mc.level.dimension() == Level.NETHER) {
				lines.add("Overworld: " + (x * 8) + " / " + (z * 8));
			}
		}
		if (Config.showFacing) {
			lines.add("Facing: " + mc.player.getDirection().getName());
		}

		int lineHeight = mc.font.lineHeight + 2;
		int width = 0;
		for (String line : lines) {
			width = Math.max(width, mc.font.width(line));
		}

		int left = Config.hudX;
		int top = Config.hudY;
		graphics.fill(left - 2, top - 2, left + width + 2, top + lines.size() * lineHeight, 0x90000000);
		int drawY = top;
		for (String line : lines) {
			graphics.text(mc.font, line, left, drawY, 0xFFFFFFFF, true);
			drawY += lineHeight;
		}
	}
}
