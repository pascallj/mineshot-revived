package nl.pascalroeleven.minecraft.mineshotrevived.client.wrapper;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Projection {
	public static double offsetX;
	public static double offsetY;
	public static double zoom = 1;

	static final float PI = (float) Math.PI;

	private static final float[] IDENTITY_MATRIX = new float[] { 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f };

	private static final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);

	private static void __gluMakeIdentityf(FloatBuffer m) {
		int oldPos = m.position();
		m.put(IDENTITY_MATRIX);
		m.position(oldPos);
	}

	public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
		float sine, cotangent, deltaZ;
		float radians = fovy / 2 * PI / 180;

		deltaZ = zFar - zNear;
		sine = (float) Math.sin(radians);

		if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
			return;
		}

		cotangent = (float) Math.cos(radians) / sine;

		__gluMakeIdentityf(matrix);

		matrix.put(0 * 4 + 0, cotangent / aspect);
		matrix.put(1 * 4 + 1, cotangent);
		matrix.put(2 * 4 + 2, -(zFar + zNear) / deltaZ);
		matrix.put(2 * 4 + 3, -1);
		matrix.put(3 * 4 + 2, -2 * zNear * zFar / deltaZ);
		matrix.put(3 * 4 + 3, 0);

		GL11.glMultMatrixf(matrix);
	}

	public static void transform() {
		GL11.glTranslated(offsetX, -offsetY, 0);
		GL11.glScaled(zoom, zoom, 1);
	}

	public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar) {
		transform();
		GL11.glOrtho(left, right, bottom, top, zNear, zFar);
	}

	public static void perspective(float fovy, float aspect, float zNear, float zFar) {
		transform();
		gluPerspective(fovy, aspect, zNear, zFar);
	}
}
