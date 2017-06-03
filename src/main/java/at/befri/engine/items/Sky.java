package at.befri.engine.items;

import org.joml.Vector3f;

import at.befri.graph.ProceduralSphere;

public class Sky extends GameItem {
	float turbidity = 10f;
	float rayleigh = 2f;
	float mieCoefficient = 0.005f;
	float mieDirectionalG = 0.8f;
	float luminance = 1f;
	float inclination = 0.49f; // elevation / inclination
	float azimuth = 0.25f; // Facing front
	boolean sun = !true;
	
	float distance = 400_000f;
	
	public Sky() {
		super(ProceduralSphere.generate(450_000f, 32, 15, 0f, (float)Math.PI * 2f, 0f, (float)Math.PI, false));
	}
	
	public float getTurbidity() {
		return turbidity;
	}
	
	public void setTurbidity(float turbidity) {
		this.turbidity = turbidity;
	}
	
	public float getRayleigh() {
		return rayleigh;
	}
	
	public void setRayleigh(float rayleigh) {
		this.rayleigh = rayleigh;
	}
	
	public float getMieCoefficient() {
		return mieCoefficient;
	}
	
	public float getMieDirectionalG() {
		return mieDirectionalG;
	}
	
	public float getLuminance() {
		return luminance;
	}
	
	public void setLuminance(float luminance) {
		this.luminance = luminance;
	}
	
	public float getInclination() {
		return inclination;
	}
	
	public void setInclination(float inclination) {
		this.inclination = inclination;
	}
	
	public float getAzimuth() {
		return azimuth;
	}
	
	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}
	
	public boolean isSun() {
		return sun;
	}
	
	public void setSun(boolean sun) {
		this.sun = sun;
	}
	
	public Vector3f getSunPosition() {
		float theta = (float)Math.PI * (inclination - 0.5f);
		float phi = 2f * (float)Math.PI * (azimuth - 0.5f);
		
		Vector3f sunPosition = new Vector3f();
		sunPosition.x = distance * (float)Math.cos(phi);
		sunPosition.y = distance * (float)Math.sin(phi) * (float)Math.sin(theta);
		sunPosition.z = distance * (float)Math.sin(phi) * (float)Math.cos(theta);
		
		return sunPosition;
	}
}
