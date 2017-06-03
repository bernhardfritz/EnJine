package at.befri.graph.lights;

import org.joml.Vector3f;

public class DirectionalLight {
	private Vector3f direction;
	private Vector3f color;
	private float intensity;
	
	public DirectionalLight(Vector3f direction, Vector3f color, float intensity) {
		this.direction = direction;
		this.color = color;
		this.intensity = intensity;
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
}
