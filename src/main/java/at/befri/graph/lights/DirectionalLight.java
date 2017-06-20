package at.befri.graph.lights;

import org.joml.Vector3f;

public class DirectionalLight {
	public static class OrthoCoords {
		public float left;
		public float right;
		public float bottom;
		public float top;
		public float near;
		public float far;
	}
	
	private Vector3f direction;
	private Vector3f color;
	private float intensity;
	private OrthoCoords orthoCoords;
	private float shadowPosMult;
	
	public DirectionalLight(Vector3f direction, Vector3f color, float intensity) {
		this.direction = direction;
		this.color = color;
		this.intensity = intensity;
		this.orthoCoords = new OrthoCoords();
		this.shadowPosMult = 1f;
	}
	
	public DirectionalLight(DirectionalLight that) {
		this(new Vector3f(that.direction), new Vector3f(that.color), that.intensity);
	}

	public Vector3f getDirection() {
		return direction;
	}

	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}

	public Vector3f getColor() {
		return color;
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}

	public float getIntensity() {
		return intensity;
	}

	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}
	
	public OrthoCoords getOrthoCoords() {
		return orthoCoords;
	}
	
	public DirectionalLight setOrthoCoords(float left, float right, float bottom, float top, float near, float far) {
		orthoCoords.left = left;
		orthoCoords.right = right;
		orthoCoords.bottom = bottom;
		orthoCoords.top = top;
		orthoCoords.near = near;
		orthoCoords.far = far;
		return this;
	}

	public float getShadowPosMult() {
		return shadowPosMult;
	}
	
	public DirectionalLight setShadowPosMult(float shadowPosMult) {
		this.shadowPosMult = shadowPosMult;
		return this;
	}
}
