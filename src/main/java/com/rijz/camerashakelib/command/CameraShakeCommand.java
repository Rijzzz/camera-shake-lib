package com.rijz.camerashakelib.command;

import com.rijz.camerashakelib.CameraShake;
import com.rijz.camerashakelib.ShakeInstance;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.util.Collection;
import java.util.Collections;

public final class CameraShakeCommand {
	private static final SimpleCommandExceptionType NO_PLAYER = new SimpleCommandExceptionType(
			Component.translatable("camera-shake-lib.command.error.player_only").withStyle(ChatFormatting.RED));

	private static final SimpleCommandExceptionType NO_TARGETS = new SimpleCommandExceptionType(
			Component.translatable("camera-shake-lib.command.error.no_targets").withStyle(ChatFormatting.RED));

	private static final DynamicCommandExceptionType BAD_INTENSITY = new DynamicCommandExceptionType(val -> Component
			.translatable("camera-shake-lib.command.error.intensity",
					Component.literal(String.valueOf(val)).withStyle(ChatFormatting.YELLOW))
			.withStyle(ChatFormatting.RED));

	private static final DynamicCommandExceptionType BAD_DURATION = new DynamicCommandExceptionType(val -> Component
			.translatable("camera-shake-lib.command.error.duration",
					Component.literal(String.valueOf(val)).withStyle(ChatFormatting.YELLOW))
			.withStyle(ChatFormatting.RED));

	private static final DynamicCommandExceptionType BAD_FREQ = new DynamicCommandExceptionType(val -> Component
			.translatable("camera-shake-lib.command.error.frequency",
					Component.literal(String.valueOf(val)).withStyle(ChatFormatting.YELLOW))
			.withStyle(ChatFormatting.RED));

	private CameraShakeCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		// numbers first or brigadier treats them as player names

		dispatcher.register(Commands.literal("camerashake").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.argument("intensity", FloatArgumentType.floatArg())
						.then(Commands.argument("duration", FloatArgumentType.floatArg())
								.executes(ctx -> run(ctx, null, 1.0f))
								.then(Commands.argument("frequency", FloatArgumentType.floatArg()).executes(
										ctx -> run(ctx, null, FloatArgumentType.getFloat(ctx, "frequency"))))))
				.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("intensity", FloatArgumentType.floatArg())
								.then(Commands.argument("duration", FloatArgumentType.floatArg())
										.executes(ctx -> run(ctx, EntityArgument.getPlayers(ctx, "targets"), 1.0f))
										.then(Commands.argument("frequency", FloatArgumentType.floatArg())
												.executes(ctx -> run(ctx, EntityArgument.getPlayers(ctx, "targets"),
														FloatArgumentType.getFloat(ctx, "frequency")))))))
				.then(Commands.literal("stop").executes(ctx -> stop(ctx, null))
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> stop(ctx, EntityArgument.getPlayers(ctx, "targets"))))));
	}

	private static int run(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, float freq)
			throws CommandSyntaxException {
		CommandSourceStack src = ctx.getSource();
		final Collection<ServerPlayer> players;
		if (targets == null) {
			ServerPlayer self = src.getPlayer();
			if (self == null) {
				throw NO_PLAYER.create();
			}
			players = Collections.singleton(self);
		} else if (targets.isEmpty()) {
			throw NO_TARGETS.create();
		} else {
			players = targets;
		}

		float intensity = FloatArgumentType.getFloat(ctx, "intensity");
		if (intensity < 0.01f || intensity > 10.0f) {
			throw BAD_INTENSITY.create(intensity);
		}

		float rawDur = FloatArgumentType.getFloat(ctx, "duration");
		if (rawDur < 1.0f || rawDur > ShakeInstance.MAX_TICKS) {
			throw BAD_DURATION.create(rawDur);
		}

		if (freq < 0.1f || freq > 10.0f) {
			throw BAD_FREQ.create(freq);
		}

		int dur = Math.round(rawDur);
		for (ServerPlayer player : players) {
			CameraShake.send(player, intensity, dur, freq);
		}

		if (players.size() == 1) {
			ServerPlayer p = players.iterator().next();
			src.sendSuccess(() -> Component
					.translatable("camera-shake-lib.command.success.single", p.getDisplayName(),
							Component.literal(String.valueOf(intensity)).withStyle(ChatFormatting.AQUA),
							Component.literal(String.valueOf(dur)).withStyle(ChatFormatting.AQUA))
					.withStyle(ChatFormatting.GREEN), false);
		} else {
			src.sendSuccess(() -> Component
					.translatable("camera-shake-lib.command.success.multiple",
							Component.literal(String.valueOf(players.size())).withStyle(ChatFormatting.YELLOW),
							Component.literal(String.valueOf(intensity)).withStyle(ChatFormatting.AQUA),
							Component.literal(String.valueOf(dur)).withStyle(ChatFormatting.AQUA))
					.withStyle(ChatFormatting.GREEN), false);
		}

		return players.size();
	}

	private static int stop(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets)
			throws CommandSyntaxException {
		CommandSourceStack src = ctx.getSource();
		final Collection<ServerPlayer> players;
		if (targets == null) {
			ServerPlayer self = src.getPlayer();
			if (self == null) {
				throw NO_PLAYER.create();
			}
			players = Collections.singleton(self);
		} else if (targets.isEmpty()) {
			throw NO_TARGETS.create();
		} else {
			players = targets;
		}

		for (ServerPlayer player : players) {
			CameraShake.stop(player);
		}

		if (players.size() == 1) {
			ServerPlayer p = players.iterator().next();
			src.sendSuccess(() -> Component.translatable("camera-shake-lib.command.stop.single", p.getDisplayName())
					.withStyle(ChatFormatting.GREEN), false);
		} else {
			src.sendSuccess(() -> Component
					.translatable("camera-shake-lib.command.stop.multiple",
							Component.literal(String.valueOf(players.size())).withStyle(ChatFormatting.YELLOW))
					.withStyle(ChatFormatting.GREEN), false);
		}

		return players.size();
	}
}
