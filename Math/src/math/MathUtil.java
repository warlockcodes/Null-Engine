package math;

public class MathUtil {

	public static final float PI = (float) Math.PI;
	public static final float E = (float) Math.E;

	public static int clamp(int val, int min, int max) {
		return val > min ? (val < max ? val : max) : min;
	}

	public static float clamp(float val, float min, float max) {
		return val > min ? (val < max ? val : max) : min;
	}
}
