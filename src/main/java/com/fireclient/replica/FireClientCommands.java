package com.fireclient.replica;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public final class FireClientCommands {
	private static final SuggestionProvider<FabricClientCommandSource> KIT_NAMES =
		(context, builder) -> SharedSuggestionProvider.suggest(KitManager.list(), builder);

	private FireClientCommands() {
	}

	private static int report(CommandContext<FabricClientCommandSource> ctx, KitManager.Result result) {
		if (result.ok()) {
			ctx.getSource().sendFeedback(Component.literal(result.message()));
			return 1;
		}
		ctx.getSource().sendError(Component.literal(result.message()));
		return 0;
	}

	private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource, String> kitArg(
		Function<String, KitManager.Result> action) {
		return ClientCommands.argument("name", StringArgumentType.word())
			.suggests(KIT_NAMES)
			.executes(ctx -> report(ctx, action.apply(StringArgumentType.getString(ctx, "name"))));
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
			dispatcher.register(ClientCommands.literal("fireclient")
				.requires(FabricClientCommandSource::attended)
				.executes(ctx -> {
					FireClientMod.openNextTick(SettingsScreen::new);
					return 1;
				}));

			dispatcher.register(ClientCommands.literal("ftext")
				.requires(FabricClientCommandSource::attended)
				.then(ClientCommands.argument("text", StringArgumentType.greedyString())
					.executes(ctx -> {
						String out = SmallText.convert(StringArgumentType.getString(ctx, "text"));
						Minecraft.getInstance().keyboardHandler.setClipboard(out);
						ctx.getSource().sendFeedback(Component.literal(out));
						ctx.getSource().sendFeedback(Component.literal("(copied to clipboard)"));
						return 1;
					})));

			dispatcher.register(ClientCommands.literal("fcalc")
				.requires(FabricClientCommandSource::attended)
				.then(ClientCommands.argument("expression", StringArgumentType.greedyString())
					.executes(ctx -> {
						String expr = StringArgumentType.getString(ctx, "expression");
						try {
							double value = Calc.eval(expr);
							ctx.getSource().sendFeedback(Component.literal(expr + " -> " + Calc.describe(value)));
							return 1;
						} catch (IllegalArgumentException e) {
							ctx.getSource().sendError(Component.literal("Invalid expression: " + e.getMessage()));
							return 0;
						}
					})));

			dispatcher.register(ClientCommands.literal("fkit")
				.requires(FabricClientCommandSource::attended)
				.executes(ctx -> {
					FireClientMod.openNextTick(KitScreen::new);
					return 1;
				})
				.then(ClientCommands.literal("list").executes(ctx -> {
					var kits = KitManager.list();
					ctx.getSource().sendFeedback(Component.literal(
						kits.isEmpty() ? "No kits saved." : "Kits: " + String.join(", ", kits)));
					return 1;
				}))
				.then(ClientCommands.literal("save").then(kitArg(KitManager::saveCurrent)))
				.then(ClientCommands.literal("load").then(kitArg(KitManager::load)))
				.then(ClientCommands.literal("delete").then(kitArg(KitManager::delete)))
				.then(ClientCommands.literal("copy").then(kitArg(KitManager::copyToClipboard)))
				.then(ClientCommands.literal("paste").then(kitArg(KitManager::importFromClipboard))));
		});
	}
}
