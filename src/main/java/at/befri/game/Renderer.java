package at.befri.game;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import at.befri.engine.Scene;
import at.befri.engine.SceneLight;
import at.befri.engine.Utils;
import at.befri.engine.Window;
import at.befri.engine.items.GameItem;
import at.befri.engine.items.Sky;
import at.befri.graph.Camera;
import at.befri.graph.Mesh;
import at.befri.graph.MultilayeredMaterial;
import at.befri.graph.ShaderProgram;
import at.befri.graph.ShadowMap;
import at.befri.graph.Transformation;
import at.befri.graph.lights.DirectionalLight;
import at.befri.graph.lights.PointLight;
import at.befri.graph.lights.SpotLight;

public class Renderer {
	private static final float FOV = (float) Math.toRadians(60f); // Field of view in radians
	private static final float Z_NEAR = 0.01f;
	private static final float Z_FAR = 2_000_000f;
	private static final int MAX_POINT_LIGHTS = 5;
	private static final int MAX_SPOT_LIGHTS = 5;
	
	private ShadowMap shadowMap;
	
	private ShaderProgram depthShaderProgram;
	private ShaderProgram skyShaderProgram;
	private ShaderProgram terrainShaderProgram;
	private ShaderProgram sceneShaderProgram;
	private Transformation transformation;
	private float specularPower;
	
	public Renderer() {
		transformation = new Transformation();
		specularPower = 10f;
	}

	public void init(Window window) throws Exception {
		shadowMap = new ShadowMap();
		
		setupDepthShader();
		setupSkyShader();
		setupTerrainShader();
		setupSceneShader();
	}
	
	private void setupDepthShader() throws Exception {
		// Create shader
		depthShaderProgram = new ShaderProgram();
		depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.glsl"));
		depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.glsl"));
		depthShaderProgram.link();
		
		// Create uniforms
		depthShaderProgram.createUniform("orthoProjectionMatrix");
		depthShaderProgram.createUniform("modelLightViewMatrix");
	}
	
	private void setupSkyShader() throws Exception {
		// Create shader
		skyShaderProgram = new ShaderProgram();
		skyShaderProgram.createVertexShader(Utils.loadResource("/shaders/sky_vertex.glsl"));
		skyShaderProgram.createFragmentShader(Utils.loadResource("/shaders/sky_fragment.glsl"));
		skyShaderProgram.link();
		
		// Create uniforms
		skyShaderProgram.createUniform("projectionMatrix");
		skyShaderProgram.createUniform("modelMatrix");
		skyShaderProgram.createUniform("modelViewMatrix");
		
		skyShaderProgram.createUniform("sunPosition");
		skyShaderProgram.createUniform("rayleigh");
		skyShaderProgram.createUniform("turbidity");
		skyShaderProgram.createUniform("mieCoefficient");
		
		skyShaderProgram.createUniform("luminance");
		skyShaderProgram.createUniform("mieDirectionalG");
	}
	
	private void setupTerrainShader() throws Exception {
		// Create shader
		terrainShaderProgram = new ShaderProgram();
		terrainShaderProgram.createVertexShader(Utils.loadResource("/shaders/terrain_vertex.glsl"));
		terrainShaderProgram.createFragmentShader(Utils.loadResource("/shaders/terrain_fragment.glsl"));
		terrainShaderProgram.link();
		
		// Create uniforms for modelView and projection matrices and texture
		terrainShaderProgram.createUniform("projectionMatrix");
		terrainShaderProgram.createUniform("modelViewMatrix");
		for (int i = 0; i < MultilayeredMaterial.MAX_LAYERS; i++) {
			terrainShaderProgram.createUniform("diffuseMaps[" + i + "]");
			terrainShaderProgram.createUniform("normalMaps[" + i + "]");
		}
		terrainShaderProgram.createUniform("rgbaMap");
		
		// Create uniform for material
		terrainShaderProgram.createMultilayeredMaterialUniform("multilayeredMaterial");
		
		// Create lightning related uniforms
		terrainShaderProgram.createUniform("specularPower");
		terrainShaderProgram.createUniform("ambientLight");
		terrainShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		terrainShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
		terrainShaderProgram.createDirectionalLightUniform("directionalLight");
		
		// Create fog uniform
		terrainShaderProgram.createFogUniform("fog");
		
		// Create uniforms for shadow mapping
		terrainShaderProgram.createUniform("shadowMap");
		terrainShaderProgram.createUniform("orthoProjectionMatrix");
		terrainShaderProgram.createUniform("modelLightViewMatrix");
	}
	
	private void setupSceneShader() throws Exception {
		// Create shader
		sceneShaderProgram = new ShaderProgram();
		sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/scene_vertex.glsl"));
		sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/scene_fragment.glsl"));
		sceneShaderProgram.link();
		
		// Create uniforms for modelView and projection matrices and texture
		sceneShaderProgram.createUniform("projectionMatrix");
		sceneShaderProgram.createUniform("modelViewMatrix");
		sceneShaderProgram.createUniform("diffuseMap");
		sceneShaderProgram.createUniform("normalMap");

		// Create uniform for material
		sceneShaderProgram.createMaterialUniform("material");
		
		// Create lightning related uniforms
		sceneShaderProgram.createUniform("specularPower");
		sceneShaderProgram.createUniform("ambientLight");
		sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
		sceneShaderProgram.createDirectionalLightUniform("directionalLight");
		
		// Create fog uniform
		sceneShaderProgram.createFogUniform("fog");
		
		// Create uniforms for shadow mapping
		sceneShaderProgram.createUniform("shadowMap");
		sceneShaderProgram.createUniform("orthoProjectionMatrix");
		sceneShaderProgram.createUniform("modelLightViewMatrix");
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void render(Window window, Camera camera, Scene scene) {
		clear();
		
		// Render depth map before view ports has been set up
		renderDepthMap(window, camera, scene);
		
		glViewport(0, 0, window.getWidth(), window.getHeight());
		
//		if (window.isResized()) {
//			glViewport(0, 0, window.getWidth(), window.getHeight());
//			window.setResized(false);
//		}
		
		// Update projectionMatrix
		transformation.updateProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
		
		// Update viewMatrix
		transformation.updateViewMatrix(camera);
		
		renderScene(window, camera, scene);
		
		if (scene.getTerrain() != null) {			
			renderTerrain(window, camera, scene);
		}
		
		renderSky(window, camera, scene);
	}
	
	private void renderDepthMap(Window window, Camera camera, Scene scene) {
		// Setup view port to match the texture size
		glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
		glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
		glClear(GL_DEPTH_BUFFER_BIT);
		
		depthShaderProgram.bind();
		
		DirectionalLight light = scene.getSceneLight().getDirectionalLight();
		Vector3f lightDirection = light.getDirection();
		
		float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z));
		float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x));
		float lightAngleZ = 0;
		Matrix4f lightViewMatrix = transformation.updateLightViewMatrix(new Vector3f(lightDirection).mul(light.getShadowPosMult()), new Vector3f(lightAngleX, lightAngleY, lightAngleZ));
		DirectionalLight.OrthoCoords orthoCoords = light.getOrthoCoords();
		Matrix4f orthoProjMatrix = transformation.updateOrthoProjectionMatrix(orthoCoords.left, orthoCoords.right, orthoCoords.bottom, orthoCoords.top, orthoCoords.near, orthoCoords.far);
		
		depthShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
		Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (Mesh mesh : mapMeshes.keySet()) {
			mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
				Matrix4f modelLightViewMatrix = transformation.buildModelViewMatrix(gameItem, lightViewMatrix);
				depthShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
			});
		}
		
		// Unbind
		depthShaderProgram.unbind();
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	private void renderSky(Window window, Camera camera, Scene scene) {
		skyShaderProgram.bind();
		
		Sky sky = scene.getSky();
		
		Matrix4f projectionMatrix = transformation.getProjectionMatrix();
		skyShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		Matrix4f modelMatrix = transformation.buildModelMatrix(sky);
		skyShaderProgram.setUniform("modelMatrix", modelMatrix);
		Matrix4f viewMatrix = transformation.getViewMatrix();
		viewMatrix.m30(0);
		viewMatrix.m31(0);
		viewMatrix.m32(0);
		Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(sky, viewMatrix);
		skyShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
		
		Vector3f sunPosition = sky.getSunPosition();
		skyShaderProgram.setUniform("sunPosition", sunPosition);
		
		float rayleigh = sky.getRayleigh();
		skyShaderProgram.setUniform("rayleigh", rayleigh);

		float turbidity = sky.getTurbidity();
		skyShaderProgram.setUniform("turbidity", turbidity);
		
		float mieCoefficient = sky.getMieCoefficient();
		skyShaderProgram.setUniform("mieCoefficient", mieCoefficient);
		
		float luminance = sky.getLuminance();
		skyShaderProgram.setUniform("luminance", luminance);
		
		float mieDirectionalG = sky.getMieDirectionalG();
		skyShaderProgram.setUniform("mieDirectionalG", mieDirectionalG);
		
		sky.getMesh().render();
		
		skyShaderProgram.unbind();
	}
	
	private void renderTerrain(Window window, Camera camera, Scene scene) {
		terrainShaderProgram.bind();
		
		Matrix4f projectionMatrix = transformation.getProjectionMatrix();
		terrainShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		Matrix4f orthoProjMatrix = transformation.getOrthoProjectionMatrix();
		terrainShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
		Matrix4f lightViewMatrix = transformation.getLightViewMatrix();
		
		Matrix4f viewMatrix = transformation.getViewMatrix();
		
		// Update light uniforms
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(terrainShaderProgram, viewMatrix, sceneLight);
		
		for (int i = 0; i < MultilayeredMaterial.MAX_LAYERS; i++) {
			terrainShaderProgram.setUniform("diffuseMaps[" + i + "]", i * 2 + 0);
			terrainShaderProgram.setUniform("normalMaps[" + i + "]", i * 2 + 1);
		}
		terrainShaderProgram.setUniform("rgbaMap", MultilayeredMaterial.MAX_LAYERS * 2);
		
		terrainShaderProgram.setUniform("shadowMap", MultilayeredMaterial.MAX_LAYERS * 2 + 1);
		
		terrainShaderProgram.setUniform("fog", scene.getFog());
		
		// Render terrain mesh
		GameItem terrain = scene.getTerrain();
		Mesh terrainMesh = terrain.getMesh();
		terrainShaderProgram.setUniform("multilayeredMaterial", terrainMesh.getIMaterial());
		glActiveTexture(GL_TEXTURE0 + MultilayeredMaterial.MAX_LAYERS * 2 + 1);
		glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());
		// Set model view matrix
		Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(terrain, viewMatrix);
		terrainShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);	
		Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(terrain, lightViewMatrix);
		terrainShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
		terrainMesh.render();
		
		terrainShaderProgram.unbind();
	}
	
	private void renderScene(Window window, Camera camera, Scene scene) {
		sceneShaderProgram.bind();
		
		Matrix4f projectionMatrix = transformation.getProjectionMatrix();
		sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		Matrix4f orthoProjMatrix = transformation.getOrthoProjectionMatrix();
		sceneShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
		Matrix4f lightViewMatrix = transformation.getLightViewMatrix();
		
		Matrix4f viewMatrix = transformation.getViewMatrix();
		
		// Update light uniforms
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(sceneShaderProgram, viewMatrix, sceneLight);
		
		sceneShaderProgram.setUniform("diffuseMap", 0);
		sceneShaderProgram.setUniform("normalMap", 1);
		sceneShaderProgram.setUniform("shadowMap", 2);
		
		sceneShaderProgram.setUniform("fog", scene.getFog());
		
		// Render each mesh with the associated game items
		Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (Mesh mesh : mapMeshes.keySet()) {
			sceneShaderProgram.setUniform("material", mesh.getIMaterial());
			glActiveTexture(GL_TEXTURE2);
			glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());
			mesh.renderList(mapMeshes.get(mesh), gameItem -> {
				// Set model view matrix for this gameItem
				Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
				sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
				Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(gameItem, lightViewMatrix);
				sceneShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
			});
		}
		
		sceneShaderProgram.unbind();
	}

	private void renderLights(ShaderProgram shaderProgram, Matrix4f viewMatrix, SceneLight sceneLight) {
		shaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
		shaderProgram.setUniform("specularPower", specularPower);
		
		// Process point lights
		PointLight[] pointLightList = sceneLight.getPointLightList();
		int numLights = pointLightList != null ? pointLightList.length : 0;
		for (int i = 0; i < numLights; i++) {
			// Get a copy of the light object and transform its position to view coordinates
			PointLight currPointLight = new PointLight(pointLightList[i]);
			Vector3f lightPos = currPointLight.getPosition();
			Vector4f aux = new Vector4f(lightPos, 1);
			aux.mul(viewMatrix);
			lightPos.x = aux.x;
			lightPos.y = aux.y;
			lightPos.z = aux.z;
			shaderProgram.setUniform("pointLights", currPointLight, i);
		}
		
		// Process spot lights
		SpotLight[] spotLightList = sceneLight.getSpotLightList();
		numLights = spotLightList != null ? spotLightList.length : 0;
		for (int i = 0; i < numLights; i++) {
			// Get a copy of the spot light object and transform its position and cone direction to view coordinates
			SpotLight currSpotLight = new SpotLight(spotLightList[i]);
			Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
			dir.mul(viewMatrix);
			currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));
			
			Vector3f lightPos = currSpotLight.getPointLight().getPosition();
			Vector4f aux = new Vector4f(lightPos, 1);
			aux.mul(viewMatrix);
			lightPos.x = aux.x;
			lightPos.y = aux.y;
			lightPos.z = aux.z;
			
			shaderProgram.setUniform("spotLights", currSpotLight, i);
		}
		
		// Get a copy of the directional light object and transform its position to view coordinates
		DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
		Vector4f direction = new Vector4f(currDirLight.getDirection(), 0);
		direction.mul(viewMatrix);
		currDirLight.setDirection(new Vector3f(direction.x, direction.y, direction.z));
		shaderProgram.setUniform("directionalLight", currDirLight);
	}

	public void cleanup() {
		if (skyShaderProgram != null) {
			skyShaderProgram.cleanup();
		}
		
		if (terrainShaderProgram != null) {
			terrainShaderProgram.cleanup();
		}
		
		if (sceneShaderProgram != null) {
			sceneShaderProgram.cleanup();
		}
	}
}
