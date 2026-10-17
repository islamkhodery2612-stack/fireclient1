package com.fireclient.replica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen extends Screen {
	private static final int STEP = 4;

	public SettingsScreen() {
		super(Component.literal("FireClient Settings"));
	}

	private static Component label(String name, boolean on) {
		return Component.literal(name + ": " + (on ? "ON" : "OFF"));
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int y = 40;

		this.addRenderableWidget(Button.builder(label("Coordinates", Config.coordsEnabled), b -> {
			Config.coordsEnabled = !Config.coordsEnabled;
			Config.save();
			b.setMessage(label("Coordinates", Config.coordsEnabled));
		}).bounds(cx - 100, y, 200, 20).build());
		y += 24;

		this.addRenderableWidget(Button.builder(label("Scaled dimension coords", Config.showScaled), b -> {
			Config.showScaled = !Config.showScaled;
			Config.save();
			b.setMessage(label("Scaled dimension coords", Config.showScaled));
		}).bounds(cx - 100, y, 200, 20).build());
		y += 24;

		this.addRenderableWidget(Button.builder(label("Facing direction", Config.showFacing), b -> {
			Config.showFacing = !Config.showFacing;
			Config.save();
			b.setMessage(label("Facing direction", Config.showFacing));
		}).bounds(cx - 100, y, 200, 20).build());
		y += 32;

		this.addRenderableWidget(Button.builder(Component.literal("Left"), b -> move(-STEP, 0))
			.bounds(cx - 100, y, 48, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Right"), b -> move(STEP, 0))
			.bounds(cx - 50, y, 48, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Up"), b -> move(0, -STEP))
			.bounds(cx, y, 48, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Down"), b -> move(0, STEP))
			.bounds(cx + 50, y, 50, 20).build());
		y += 24;

		this.addRenderableWidget(Button.builder(Component.literal("Reset HUD position"), b -> {
			Config.hudX = 4;
			Config.hudY = 4;
			Config.save();
		}).bounds(cx - 100, y, 200, 20).build());
		y += 32;

		this.addRenderableWidget(Button.builder(Component.literal("Open kits"), b -> {
			Minecraft.getInstance().gui.setScreen(new KitScreen());
		}).bounds(cx - 100, y, 200, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
			.bounds(cx - 100, this.height - 28, 200, 20).build());
	}

	private static void move(int dx, int dy) {
		Config.hudX = Math.max(0, Config.hudX + dx);
		Config.hudY = Math.max(0, Config.hudY + dy);
		Config.save();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		String title = this.title.getString();
		graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 16, 0xFFFFFFFF, true);
	}
}
