package com.rijz.camerashakelib.client;

import com.rijz.camerashakelib.CameraShake;
import com.rijz.camerashakelib.ShakeInstance;
import com.rijz.camerashakelib.network.CameraShakePayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CameraShakeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		CameraShake.registerClientHandler(new CameraShake.ClientShakeHandler() {
			@Override
			public void shake(ShakeInstance instance) {
				CameraShakeManager.addShake(instance);
			}

			@Override
			public void stopAll() {
				CameraShakeManager.stopAll();
			}

			@Override
			public boolean isShaking() {
				return CameraShakeManager.isShaking();
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(CameraShakePayload.TYPE, (payload, ctx) -> {
			ctx.client().execute(() -> {
				if (payload.intensity() <= 0.0f) {
					CameraShakeManager.stopAll();
				} else {
					CameraShakeManager.addShake(
							new ShakeInstance(payload.intensity(), payload.durationTicks(), payload.frequency()));
				}
			});
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> CameraShakeManager.onClientTick());
	}
}
