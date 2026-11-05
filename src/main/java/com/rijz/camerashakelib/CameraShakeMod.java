package com.rijz.camerashakelib;

import com.rijz.camerashakelib.command.CameraShakeCommand;
import com.rijz.camerashakelib.network.CameraShakePayload;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraShakeMod implements ModInitializer {
	public static final String MOD_ID = "camera-shake-lib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		PayloadTypeRegistry.clientboundPlay().register(CameraShakePayload.TYPE, CameraShakePayload.STREAM_CODEC);

		CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
			CameraShakeCommand.register(dispatcher);
		});

		LOGGER.info("Camera Shake Lib initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
