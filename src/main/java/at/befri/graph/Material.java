package at.befri.graph;

import org.joml.Vector4f;

public class Material implements IMaterial {
	private static final Vector4f DEFAULT_COLOR = new Vector4f(1f, 1f, 1f, 1f);
	private Vector4f ambientColor;
	private Vector4f diffuseColor;
	private Vector4f specularColor;
	private Texture diffuseMap;
	private Texture normalMap;
	private float reflectance;
	
	public Material() {
		ambientColor = DEFAULT_COLOR;
		diffuseColor = DEFAULT_COLOR;
		specularColor = DEFAULT_COLOR;
		reflectance = 0f;
	}
	
	public Vector4f getAmbientColor() {
		return ambientColor;
	}
	
	public Material setAmbientColor(Vector4f ambientColor) {
		this.ambientColor = ambientColor;
		return this;
	}
	
	public Vector4f getDiffuseColor() {
		return diffuseColor;
	}
	
	public Material setDiffuseColor(Vector4f diffuseColor) {
		this.diffuseColor = diffuseColor;
		return this;
	}
	
	public Vector4f getSpecularColor() {
		return specularColor;
	}
	
	public Material setSpecularColor(Vector4f specularColor) {
		this.specularColor = specularColor;
		return this;
	}
	
	public Texture getDiffuseMap() {
		return diffuseMap;
	}
	
	public Material setDiffuseMap(Texture diffuseMap) {
		this.diffuseMap = diffuseMap;
		return this;
	}
	
	public boolean hasDiffuseMap() {
		return diffuseMap != null;
	}
	
	public float getReflectance() {
		return reflectance;
	}
	
	public Material setReflectance(float reflectance) {
		this.reflectance = reflectance;
		return this;
	}
	
	public Texture getNormalMap() {
		return normalMap;
	}
	
	public Material setNormalMap(Texture normalMap) {
		this.normalMap = normalMap;
		return this;
	}
	
	public boolean hasNormalMap() {
		return normalMap != null;
	}
}
