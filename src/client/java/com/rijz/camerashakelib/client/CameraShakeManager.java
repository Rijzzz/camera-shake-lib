package com.rijz.camerashakelib.client;

import com.rijz.camerashakelib.ShakeInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CameraShakeManager {
	private static final int MAX_SHAKES = 64;
	private static final List<ShakeInstance> shakes = new CopyOnWriteArrayList<>();
	private static long ticks = 0;

	private CameraShakeManager() {
	}

	static synchronized void addShake(ShakeInstance instance) {
		if (shakes.size() < MAX_SHAKES && instance != null && instance.intensity() > 0.0f && !instance.isFinished()) {
			shakes.add(instance.copy());
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

		double pitch = 0.0;
		double yaw = 0.0;
		double roll = 0.0;

		for (ShakeInstance s : shakes) {
			float trauma = s.getTrauma(partialTick);
			if (trauma <= 0.0f) {
				continue;
			}

			// offset frequencies so axes dont line up
			double t = (ticks + (double) partialTick) * 0.45 * s.frequency();
			pitch += (Math.sin(t) + 0.5 * Math.sin(t * 2.3)) * trauma * 3.5;
			yaw += (Math.cos(t * 0.9) + 0.5 * Math.cos(t * 2.7)) * trauma * 3.5;
			roll += (Math.sin(t * 1.3) + 0.5 * Math.sin(t * 3.1)) * trauma * 2.5;
		}

		// cap angles so stacked shakes dont flip the camera
		float p = (float) Math.clamp(pitch, -45.0, 45.0);
		float y = (float) Math.clamp(yaw, -45.0, 45.0);
		float r = (float) Math.clamp(roll, -30.0, 30.0);

		return new Offsets(p, y, r);
	}

	public record Offsets(float pitch, float yaw, float roll) {
		public static final Offsets ZERO = new Offsets(0.0f, 0.0f, 0.0f);
	}
}
