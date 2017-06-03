package at.befri.graph.lights;

import org.joml.Vector3f;

public class PointLight {
	public static class Attenuation {
		private float constant;
		private float linear;
		private float exponent;
		
		public Attenuation(float constant, float linear, float exponent) {
			this.constant = constant;
			this.linear = linear;
			this.exponent = exponent;
		}
		
		public float getConstant() {
			return constant;
		}
		
		public void setConstant(float constant) {
			this.constant = constant;
		}
		
		public float getLinear() {
			return linear;
		}
		
		public void setLinear(float linear) {
			this.linear = linear;
		}
		
		public float getExponent() {
			return exponent;
		}
		
		public void setExponent(float exponent) {
			this.exponent = exponent;
		}
	}
	
	private Vector3f position;
	private Vector3f color;
	private float intensity;
	private Attenuation attenuation;
	
	public PointLight(Vector3f position, Vector3f color, float intensity) {
		this.position = position;
		this.color = color;
		this.intensity = intensity;
		attenuation = new Attenuation(1f, 0f, 0f);
	}
	
	public PointLight(PointLight pointLight) {
		this(new Vector3f(pointLight.getPosition()), new Vector3f(pointLight.getColor()), pointLight.getIntensity());
		this.setAttenuation(pointLight.getAttenuation());
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void setPosition(Vector3f position) {
		this.position = position;
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
	
	public Attenuation getAttenuation() {
		return attenuation;
	}
	
	public PointLight setAttenuation(Attenuation attenuation) {
		this.attenuation = attenuation;
		return this;
	}
}
