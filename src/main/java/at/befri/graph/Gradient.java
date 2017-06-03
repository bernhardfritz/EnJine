package at.befri.graph;

import java.nio.ByteBuffer;

import org.joml.Vector3f;

import de.matthiasmann.twl.utils.PNGDecoder;

public class Gradient {
	private static final float FACTOR = 1f / 255f;
	private int width;
	private ByteBuffer byteBuffer;
	
	public Gradient(String gradientFile) throws Exception {
		PNGDecoder decoder = new PNGDecoder(getClass().getResourceAsStream(gradientFile));
		width = decoder.getWidth();
		byteBuffer = ByteBuffer.allocateDirect(width * 3);
		decoder.decode(byteBuffer, width * 3, PNGDecoder.Format.RGB);
		byteBuffer.flip();
	}
	
	public Vector3f getColor(float t) {
		int i = (int) lerp(0, width - 1, t);
		byte r = byteBuffer.get(i * 3 + 0);
		byte g = byteBuffer.get(i * 3 + 1);
		byte b = byteBuffer.get(i * 3 + 2);
		return new Vector3f(Byte.toUnsignedInt(r), Byte.toUnsignedInt(g), Byte.toUnsignedInt(b)).mul(FACTOR);
	}
	
	private static float lerp(float v0, float v1, float t) {
		return (1 - t) * v0 + t * v1;
	}
}
