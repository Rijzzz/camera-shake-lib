package com.rijz.camerashakelib.network;

import com.rijz.camerashakelib.CameraShakeMod;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import io.netty.buffer.ByteBuf;

public record CameraShakePayload(float intensity, int durationTicks, float frequency) implements CustomPacketPayload {
	public static final CameraShakePayload STOP = new CameraShakePayload(0.0f, 1, 1.0f);

	public static final CustomPacketPayload.Type<CameraShakePayload> TYPE = new CustomPacketPayload.Type<>(
			CameraShakeMod.id("shake"));

	public static final StreamCodec<ByteBuf, CameraShakePayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, CameraShakePayload::intensity, ByteBufCodecs.VAR_INT,
			CameraShakePayload::durationTicks, ByteBufCodecs.FLOAT, CameraShakePayload::frequency,
			CameraShakePayload::new);

	@Override
	public CustomPacketPayload.Type<CameraShakePayload> type() {
		return TYPE;
	}
}
