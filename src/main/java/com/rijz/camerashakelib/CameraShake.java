package com.rijz.camerashakelib;

import com.rijz.camerashakelib.network.CameraShakePayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class CameraShake {
	private static ClientShakeHandler clientHandler;

	private CameraShake() {
	}

	public static void registerClientHandler(ClientShakeHandler handler) {
		clientHandler = handler;
	}

	public static boolean isClientAvailable() {
		return clientHandler != null;
	}

	public static void shake(float intensity, int dur) {
		shake(intensity, dur, 1.0f);
	}

	public static void shake(float intensity, int dur, float freq) {
		if (clientHandler != null && intensity > 0.0f && dur > 0) {
			clientHandler.shake(new ShakeInstance(intensity, dur, freq));
		}
	}

	public static void shake(ShakeInstance instance) {
		if (clientHandler != null && instance != null) {
			clientHandler.shake(instance);
		}
	}

	public static void stopAll() {
		if (clientHandler != null) {
			clientHandler.stopAll();
		}
	}

	public static boolean isShaking() {
		return clientHandler != null && clientHandler.isShaking();
	}

	public static boolean isSupported(ServerPlayer player) {
		return player != null && ServerPlayNetworking.canSend(player, CameraShakePayload.TYPE);
	}

	public static void stop(ServerPlayer player) {
		if (player != null && isSupported(player)) {
			ServerPlayNetworking.send(player, new CameraShakePayload(0.0f, 1, 1.0f));
		}
	}

	public static void send(ServerPlayer player, float intensity, int dur) {
		send(player, intensity, dur, 1.0f);
	}

	public static void send(ServerPlayer player, float intensity, int dur, float freq) {
		if (player == null || intensity <= 0.0f || dur <= 0) {
			return;
		}

		if (isSupported(player)) {
			ServerPlayNetworking.send(player, new CameraShakePayload(intensity, dur, freq));
		} else {
			// play hurt tilt if they dont have the mod
			float yaw = (float) (Math.random() * 360.0);
			player.connection.send(new ClientboundHurtAnimationPacket(player.getId(), yaw));
		}
	}

	public static void sendNearby(ServerLevel lvl, Vec3 pos, double rad, float maxIntensity, int dur) {
		sendNearby(lvl, pos, rad, maxIntensity, dur, 1.0f);
	}

	public static void sendNearby(ServerLevel lvl, Vec3 pos, double rad, float maxIntensity, int dur, float freq) {
		if (lvl == null || pos == null || rad <= 0.0 || maxIntensity <= 0.0f || dur <= 0) {
			return;
		}

		double radSq = rad * rad;
		for (ServerPlayer player : lvl.players()) {
			double distSq = player.distanceToSqr(pos);
			if (distSq > radSq) {
				continue;
			}

			// weaker further away
			double dist = Math.sqrt(distSq);
			float falloff = (float) (1.0 - (dist / rad));
			float intensity = maxIntensity * falloff;
			if (intensity > 0.01f) {
				send(player, intensity, dur, freq);
			}
		}
	}

	public static void sendAll(MinecraftServer server, float intensity, int dur) {
		sendAll(server, intensity, dur, 1.0f);
	}

	public static void sendAll(MinecraftServer server, float intensity, int dur, float freq) {
		if (server == null || intensity <= 0.0f || dur <= 0) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			send(player, intensity, dur, freq);
		}
	}

	public interface ClientShakeHandler {
		void shake(ShakeInstance instance);

		void stopAll();

		boolean isShaking();
	}
}
