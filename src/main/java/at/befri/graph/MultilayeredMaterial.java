package at.befri.graph;

import org.joml.Vector4f;

public class MultilayeredMaterial implements IMaterial {
	public static final int MAX_LAYERS = 5;
	private static final Vector4f DEFAULT_COLOR = new Vector4f(1f, 1f, 1f, 1f);
	private Vector4f ambientColor;
	private Vector4f diffuseColor;
	private Vector4f specularColor;
	private Texture[] diffuseMaps;
	private Texture[] normalMaps;
	private Texture rgbaMap;
	private float reflectance;

	public MultilayeredMaterial() {
		ambientColor = DEFAULT_COLOR;
		diffuseColor = DEFAULT_COLOR;
		specularColor = DEFAULT_COLOR;
		diffuseMaps = new Texture[MAX_LAYERS];
		normalMaps = new Texture[MAX_LAYERS];
		reflectance = 0f;
	}

	public Vector4f getAmbientColor() {
		return ambientColor;
	}

	public MultilayeredMaterial setAmbientColor(Vector4f ambientColor) {
		this.ambientColor = ambientColor;
		return this;
	}

	public Vector4f getDiffuseColor() {
		return diffuseColor;
	}

	public MultilayeredMaterial setDiffuseColor(Vector4f diffuseColor) {
		this.diffuseColor = diffuseColor;
		return this;
	}

	public Vector4f getSpecularColor() {
		return specularColor;
	}

	public MultilayeredMaterial setSpecularColor(Vector4f specularColor) {
		this.specularColor = specularColor;
		return this;
	}

	public Texture[] getDiffuseMaps() {
		return diffuseMaps;
	}

	public MultilayeredMaterial setDiffuseMaps(Texture[] diffuseMaps) {
		this.diffuseMaps = diffuseMaps;
		return this;
	}

	public boolean hasDiffuseMaps() {
		return diffuseMaps != null && diffuseMaps.length > 0;
	}

	public float getReflectance() {
		return reflectance;
	}

	public MultilayeredMaterial setReflectance(float reflectance) {
		this.reflectance = reflectance;
		return this;
	}

	public Texture[] getNormalMaps() {
		return normalMaps;
	}

	public MultilayeredMaterial setNormalMaps(Texture[] normalMaps) {
		this.normalMaps = normalMaps;
		return this;
	}

	public boolean hasNormalMaps() {
		return normalMaps != null && normalMaps.length > 0;
	}

	public Texture getRgbaMap() {
		return rgbaMap;
	}

	public MultilayeredMaterial setRgbaMap(Texture rgbaMap) {
		this.rgbaMap = rgbaMap;
		return this;
	}

	public boolean hasRgbaMap() {
		return rgbaMap != null;
	}
	
	@Override
	public boolean isMultilayered() {
		return true;
	}
}
