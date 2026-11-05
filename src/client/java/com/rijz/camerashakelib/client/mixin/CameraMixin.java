package com.rijz.camerashakelib.client.mixin;

import com.rijz.camerashakelib.client.CameraShakeManager;
import com.rijz.camerashakelib.client.CameraShakeManager.Offsets;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	@Final
	private Quaternionf rotation;

	@Shadow
	@Final
	private Vector3f forwards;

	@Shadow
	@Final
	private Vector3f up;

	@Shadow
	@Final
	private Vector3f left;

	@Shadow
	private int matrixPropertiesDirty;

	@Shadow
	public abstract float getCameraEntityPartialTicks(DeltaTracker deltaTracker);

	@Inject(method = "update", at = @At("TAIL"))
	private void onCameraUpdate(DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!CameraShakeManager.isShaking()) {
			return;
		}

		float partialTick = getCameraEntityPartialTicks(deltaTracker);
		Offsets off = CameraShakeManager.computeOffsets(partialTick);

		if (off.yaw() != 0.0f) {
			rotation.rotateY((float) Math.toRadians(off.yaw()));
		}
		if (off.pitch() != 0.0f) {
			rotation.rotateX((float) Math.toRadians(off.pitch()));
		}
		if (off.roll() != 0.0f) {
			rotation.rotateZ((float) Math.toRadians(off.roll()));
		}

		forwards.set(0.0f, 0.0f, -1.0f).rotate(rotation);
		up.set(0.0f, 1.0f, 0.0f).rotate(rotation);
		left.set(-1.0f, 0.0f, 0.0f).rotate(rotation);

		// mark dirty so vanilla updates the view matrix
		matrixPropertiesDirty |= 3;
	}
}
