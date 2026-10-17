package com.fireclient.replica;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static boolean coordsEnabled = true;
	public static boolean showScaled = true;
	public static boolean showFacing = true;
	public static int hudX = 4;
	public static int hudY = 4;

	private static final class Data {
		boolean coordsEnabled = true;
		boolean showScaled = true;
		boolean showFacing = true;
		int hudX = 4;
		int hudY = 4;
	}

	private Config() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("fireclientreplica.json");
	}

	public static void load() {
		Path f = file();
		if (!Files.exists(f)) {
			save();
			return;
		}
		try {
			Data d = GSON.fromJson(Files.readString(f), Data.class);
			if (d == null) {
				return;
			}
			coordsEnabled = d.coordsEnabled;
			showScaled = d.showScaled;
			showFacing = d.showFacing;
			hudX = d.hudX;
			hudY = d.hudY;
		} catch (IOException | RuntimeException e) {
			FireClientMod.LOGGER.warn("Could not read config, using defaults", e);
		}
	}

	public static void save() {
		Data d = new Data();
		d.coordsEnabled = coordsEnabled;
		d.showScaled = showScaled;
		d.showFacing = showFacing;
		d.hudX = hudX;
		d.hudY = hudY;
		try {
			Files.writeString(file(), GSON.toJson(d));
		} catch (IOException e) {
			FireClientMod.LOGGER.warn("Could not save config", e);
		}
	}
}
