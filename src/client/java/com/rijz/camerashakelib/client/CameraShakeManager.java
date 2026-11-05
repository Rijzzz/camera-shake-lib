package com.rijz.camerashakelib.client;

import com.rijz.camerashakelib.ShakeInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CameraShakeManager {
	private static final List<ShakeInstance> shakes = new CopyOnWriteArrayList<>();
	private static long ticks = 0;

	private CameraShakeManager() {
	}

	static void addShake(ShakeInstance instance) {
		if (instance != null && instance.intensity() > 0.0f) {
			shakes.add(instance);
		}
	}

	static void stopAll() {
		shakes.clear();
	}

	public static boolean isShaking() {
		return !shakes.isEmpty();
	}

	static void onClientTick() {
		ticks++;
		for (ShakeInstance s : shakes) {
			s.tick();
		}
		shakes.removeIf(ShakeInstance::isFinished);
	}

	public static Offsets computeOffsets(float partialTick) {
		if (shakes.isEmpty()) {
			return Offsets.ZERO;
		}

		float pitch = 0.0f;
		float yaw = 0.0f;
		float roll = 0.0f;

		for (ShakeInstance s : shakes) {
			float trauma = s.getTrauma(partialTick);
			if (trauma <= 0.0f) {
				continue;
			}

			// offset frequencies so axes dont line up
			float t = (ticks + partialTick) * 0.45f * s.frequency();
			pitch += (float) (Math.sin(t) + 0.5 * Math.sin(t * 2.3)) * trauma * 3.5f;
			yaw += (float) (Math.cos(t * 0.9) + 0.5 * Math.cos(t * 2.7)) * trauma * 3.5f;
			roll += (float) (Math.sin(t * 1.3) + 0.5 * Math.sin(t * 3.1)) * trauma * 2.5f;
		}

		// cap angles so stacked shakes dont flip the camera
		float clampedPitch = Math.clamp(pitch, -45.0f, 45.0f);
		float clampedYaw = Math.clamp(yaw, -45.0f, 45.0f);
		float clampedRoll = Math.clamp(roll, -30.0f, 30.0f);

		return new Offsets(clampedPitch, clampedYaw, clampedRoll);
	}

	public record Offsets(float pitch, float yaw, float roll) {
		public static final Offsets ZERO = new Offsets(0.0f, 0.0f, 0.0f);
	}
}
