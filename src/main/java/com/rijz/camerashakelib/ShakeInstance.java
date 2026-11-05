package com.rijz.camerashakelib;

public class ShakeInstance {
	private float intensity;
	private int dur;
	private float freq;
	private boolean decay;
	private int ticksLeft;

	public ShakeInstance(float intensity, int dur) {
		this(intensity, dur, 1.0f, true);
	}

	public ShakeInstance(float intensity, int dur, float freq) {
		this(intensity, dur, freq, true);
	}

	public ShakeInstance(float intensity, int dur, float freq, boolean decay) {
		this.intensity = Math.max(0.0f, intensity);
		this.dur = Math.max(1, dur);
		this.freq = Math.max(0.1f, freq);
		this.decay = decay;
		this.ticksLeft = this.dur;
	}

	public ShakeInstance freq(float freq) {
		this.freq = Math.max(0.1f, freq);
		return this;
	}

	public ShakeInstance noDecay() {
		this.decay = false;
		return this;
	}

	public void tick() {
		if (ticksLeft > 0) {
			ticksLeft--;
		}
	}

	public boolean isFinished() {
		return ticksLeft <= 0;
	}

	public float getTrauma(float partialTick) {
		if (isFinished()) {
			return 0.0f;
		}
		if (!decay) {
			return intensity;
		}

		// fade out faster near the end
		float progress = Math.clamp((ticksLeft - partialTick) / (float) dur, 0.0f, 1.0f);
		return progress * progress * intensity;
	}

	public float intensity() {
		return intensity;
	}

	public int duration() {
		return dur;
	}

	public int durationTicks() {
		return dur;
	}

	public float frequency() {
		return freq;
	}

	public boolean traumaDecay() {
		return decay;
	}

	public int ticksLeft() {
		return ticksLeft;
	}
}
