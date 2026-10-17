package com.fireclient.replica;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Client-side kits: snapshots of the player's inventory stored as shareable .json files. */
public final class KitManager {
	public record Result(boolean ok, String message) {
	}

	private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");

	private KitManager() {
	}

	private static Path dir() {
		return FabricLoader.getInstance().getConfigDir().resolve("fireclientreplica").resolve("kits");
	}

	private static Path fileFor(String name) {
		return dir().resolve(name + ".json");
	}

	public static boolean validName(String name) {
		return name != null && VALID_NAME.matcher(name).matches();
	}

	private static Result badName() {
		return new Result(false, "Invalid name: use 1-32 letters, digits, - or _.");
	}

	public static List<String> list() {
		List<String> names = new ArrayList<>();
		Path d = dir();
		if (!Files.isDirectory(d)) {
			return names;
		}
		try (Stream<Path> files = Files.list(d)) {
			files.map(p -> p.getFileName().toString())
				.filter(n -> n.endsWith(".json"))
				.map(n -> n.substring(0, n.length() - 5))
				.filter(KitManager::validName)
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.forEach(names::add);
		} catch (IOException e) {
			FireClientMod.LOGGER.warn("Could not list kits", e);
		}
		return names;
	}

	private static JsonObject capture(String name) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return null;
		}
		var ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		JsonObject root = new JsonObject();
		root.addProperty("format", 1);
		root.addProperty("name", name);
		JsonArray items = new JsonArray();
		Inventory inv = mc.player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			Optional<JsonElement> encoded = ItemStack.CODEC.encodeStart(ops, stack).result();
			if (encoded.isEmpty()) {
				continue;
			}
			JsonObject entry = new JsonObject();
			entry.addProperty("slot", i);
			entry.add("stack", encoded.get());
			items.add(entry);
		}
		root.add("items", items);
		return root;
	}

	private static Result write(String name, JsonObject json) {
		try {
			Files.createDirectories(dir());
			Files.writeString(fileFor(name), json.toString());
			return new Result(true, "Kit '" + name + "' saved.");
		} catch (IOException e) {
			FireClientMod.LOGGER.warn("Could not write kit", e);
			return new Result(false, "Could not write the kit file.");
		}
	}

	private static JsonObject read(String name) {
		try {
			JsonElement e = JsonParser.parseString(Files.readString(fileFor(name)));
			return e.isJsonObject() ? e.getAsJsonObject() : null;
		} catch (IOException | RuntimeException ex) {
			return null;
		}
	}

	public static Result saveCurrent(String name) {
		if (!validName(name)) {
			return badName();
		}
		JsonObject json = capture(name);
		if (json == null) {
			return new Result(false, "Join a world first.");
		}
		return write(name, json);
	}

	public static Result delete(String name) {
		if (!validName(name)) {
			return badName();
		}
		try {
			boolean deleted = Files.deleteIfExists(fileFor(name));
			return deleted ? new Result(true, "Kit '" + name + "' deleted.") : new Result(false, "No such kit.");
		} catch (IOException e) {
			return new Result(false, "Could not delete the kit file.");
		}
	}

	public static Result copyToClipboard(String name) {
		if (!validName(name)) {
			return badName();
		}
		JsonObject json = read(name);
		if (json == null) {
			return new Result(false, "No such kit.");
		}
		Minecraft.getInstance().keyboardHandler.setClipboard(json.toString());
		return new Result(true, "Kit '" + name + "' copied to clipboard.");
	}

	public static Result importFromClipboard(String name) {
		if (!validName(name)) {
			return badName();
		}
		String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
		try {
			JsonElement e = JsonParser.parseString(clip);
			if (!e.isJsonObject() || !e.getAsJsonObject().has("items") || !e.getAsJsonObject().get("items").isJsonArray()) {
				return new Result(false, "Clipboard does not contain a kit.");
			}
			JsonObject json = e.getAsJsonObject();
			json.addProperty("name", name);
			return write(name, json);
		} catch (RuntimeException ex) {
			return new Result(false, "Clipboard does not contain a kit.");
		}
	}

	/** Applies a kit to the inventory. Needs Creative mode, because the client cannot set items on its own. */
	public static Result load(String name) {
		if (!validName(name)) {
			return badName();
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.gameMode == null) {
			return new Result(false, "Join a world first.");
		}
		if (!mc.player.isCreative()) {
			return new Result(false, "Loading a kit needs Creative mode.");
		}
		JsonObject json = read(name);
		if (json == null || !json.has("items") || !json.get("items").isJsonArray()) {
			return new Result(false, "No such kit.");
		}
		var ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		int applied = 0;
		for (JsonElement el : json.getAsJsonArray("items")) {
			if (!el.isJsonObject()) {
				continue;
			}
			JsonObject entry = el.getAsJsonObject();
			if (!entry.has("slot") || !entry.has("stack")) {
				continue;
			}
			int menuSlot = toMenuSlot(entry.get("slot").getAsInt());
			if (menuSlot < 0) {
				continue;
			}
			Optional<ItemStack> parsed = ItemStack.CODEC.parse(ops, entry.get("stack")).result();
			if (parsed.isEmpty()) {
				continue;
			}
			mc.gameMode.handleCreativeModeItemAdd(parsed.get(), menuSlot);
			applied++;
		}
		return new Result(true, "Kit '" + name + "' loaded (" + applied + " stacks).");
	}

	/** Inventory index (0-35 main, 36-39 armor feet-head, 40 offhand) to InventoryMenu slot. */
	private static int toMenuSlot(int invSlot) {
		if (invSlot >= 0 && invSlot <= 8) {
			return 36 + invSlot;
		}
		if (invSlot >= 9 && invSlot <= 35) {
			return invSlot;
		}
		if (invSlot >= 36 && invSlot <= 39) {
			return 44 - invSlot;
		}
		if (invSlot == 40) {
			return 45;
		}
		return -1;
	}
}
