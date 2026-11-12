package dev.nova.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
	private final double min;
	private final double max;
	private final double step;

	public NumberSetting(String name, double value, double min, double max, double step) {
		super(name, value);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	@Override
	public void set(Double value) {
		double snapped = Math.round(value / step) * step;
		this.value = Math.max(min, Math.min(max, snapped));
	}

	public float getFloat() {
		return value.floatValue();
	}

	public int getInt() {
		return (int) Math.round(value);
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	/** Position of the current value between min and max, 0..1. */
	public double getProgress() {
		return (value - min) / (max - min);
	}

	public void setProgress(double progress) {
		set(min + (max - min) * Math.max(0, Math.min(1, progress)));
	}

	public String format() {
		if (step >= 1) return Integer.toString(getInt());
		return String.format(java.util.Locale.ROOT, step >= 0.1 ? "%.1f" : "%.2f", value);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(value);
	}

	@Override
	public void fromJson(JsonElement json) {
		set(json.getAsDouble());
	}
}
