package com.fireclient.replica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class KitScreen extends Screen {
	private static final int PER_PAGE = 6;

	private EditBox nameBox;
	private int page = 0;
	private String status = "";
	private String lastName = "";
	private final List<String> visible = new ArrayList<>();
	private int rowsTop;

	public KitScreen() {
		super(Component.literal("FireClient Kits"));
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int top = 40;

		nameBox = new EditBox(this.font, cx - 150, top, 150, 20, Component.literal("Kit name"));
		nameBox.setMaxLength(32);
		nameBox.setValue(lastName);
		this.addRenderableWidget(nameBox);

		this.addRenderableWidget(Button.builder(Component.literal("Save inventory"), b -> onSave())
			.bounds(cx + 6, top, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Paste"), b -> onPaste())
			.bounds(cx + 110, top, 40, 20).build());

		List<String> kits = KitManager.list();
		int pages = Math.max(1, (kits.size() + PER_PAGE - 1) / PER_PAGE);
		page = Math.min(page, pages - 1);
		int start = page * PER_PAGE;
		int end = Math.min(kits.size(), start + PER_PAGE);

		visible.clear();
		rowsTop = top + 34;
		int y = rowsTop;
		for (int i = start; i < end; i++) {
			String name = kits.get(i);
			visible.add(name);
			this.addRenderableWidget(Button.builder(Component.literal("Load"), b -> onLoad(name))
				.bounds(cx + 10, y, 45, 20).build());
			this.addRenderableWidget(Button.builder(Component.literal("Copy"), b -> onCopy(name))
				.bounds(cx + 58, y, 45, 20).build());
			this.addRenderableWidget(Button.builder(Component.literal("Delete"), b -> onDelete(name))
				.bounds(cx + 106, y, 45, 20).build());
			y += 24;
		}

		int bottom = this.height - 28;
		this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
			captureName();
			page = Math.max(0, page - 1);
			this.rebuildWidgets();
		}).bounds(cx - 150, bottom, 30, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
			captureName();
			page = Math.min(pages - 1, page + 1);
			this.rebuildWidgets();
		}).bounds(cx - 116, bottom, 30, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Settings"), b -> {
			Minecraft.getInstance().gui.setScreen(new SettingsScreen());
		}).bounds(cx - 50, bottom, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
			.bounds(cx + 56, bottom, 94, 20).build());
	}

	private void captureName() {
		if (nameBox != null) {
			lastName = nameBox.getValue().trim();
		}
	}

	private void finish(KitManager.Result result) {
		status = result.message();
		this.rebuildWidgets();
	}

	private void onSave() {
		captureName();
		finish(KitManager.saveCurrent(lastName));
	}

	private void onPaste() {
		captureName();
		finish(KitManager.importFromClipboard(lastName));
	}

	private void onLoad(String name) {
		captureName();
		finish(KitManager.load(name));
	}

	private void onCopy(String name) {
		captureName();
		finish(KitManager.copyToClipboard(name));
	}

	private void onDelete(String name) {
		captureName();
		finish(KitManager.delete(name));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		String title = this.title.getString();
		graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 16, 0xFFFFFFFF, true);

		int cx = this.width / 2;
		int y = rowsTop;
		for (String name : visible) {
			graphics.text(this.font, name, cx - 150, y + 6, 0xFFFFFFFF, true);
			y += 24;
		}
		if (visible.isEmpty()) {
			graphics.text(this.font, "No kits yet: type a name and press Save inventory.", cx - 150, rowsTop + 6, 0xFFAAAAAA, true);
		}
		if (!status.isEmpty()) {
			graphics.text(this.font, status, cx - this.font.width(status) / 2, this.height - 44, 0xFFFFFF55, true);
		}
	}
}
