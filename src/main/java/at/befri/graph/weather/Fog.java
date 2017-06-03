package at.befri.graph.weather;

import org.joml.Vector3f;

public class Fog {
	private Vector3f color;
	private float density;
	
	public Fog() {
		color = new Vector3f(0f, 0f, 0f);
		density = 0f;
	}
	
	public Vector3f getColor() {
		return color;
	}
	
	public Fog setColor(Vector3f color) {
		this.color = color;
		return this;
	}
	
	public float getDensity() {
		return density;
	}
	
	public Fog setDensity(float density) {
		this.density = density;
		return this;
	}
}
