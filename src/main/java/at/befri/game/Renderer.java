package at.befri.game;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
import at.befri.graph.Transformation;
import at.befri.graph.lights.DirectionalLight;
import at.befri.graph.lights.PointLight;
import at.befri.graph.lights.SpotLight;
import at.befri.graph.shadow.ShadowCascade;
import at.befri.graph.shadow.ShadowRenderer;

public class Renderer {
	private static final int MAX_POINT_LIGHTS = 5;
	private static final int MAX_SPOT_LIGHTS = 5;
	
	private ShadowRenderer shadowRenderer;
	
	private ShaderProgram skyShaderProgram;
	private ShaderProgram terrainShaderProgram;
	private ShaderProgram sceneShaderProgram;
	private Transformation transformation;
	private float specularPower;
	
	public Renderer() {
		transformation = new Transformation();
		specularPower = 10f;
		shadowRenderer = new ShadowRenderer();
	}

	public void init(Window window) throws Exception {
		shadowRenderer.init(window);
		setupSkyShader();
		setupTerrainShader();
		setupSceneShader();
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
		
		// Create uniforms for view and projection matrices and texture
		terrainShaderProgram.createUniform("viewMatrix");
		terrainShaderProgram.createUniform("projectionMatrix");
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
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
			terrainShaderProgram.createUniform("shadowMap_" + i);
        }
		terrainShaderProgram.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
		terrainShaderProgram.createUniform("modelMatrix");
		terrainShaderProgram.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
		terrainShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
		terrainShaderProgram.createUniform("renderShadow");
	}
	
	private void setupSceneShader() throws Exception {
		// Create shader
		sceneShaderProgram = new ShaderProgram();
		sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/scene_vertex.glsl"));
		sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/scene_fragment.glsl"));
		sceneShaderProgram.link();
		
		// Create uniforms for view and projection matrices and texture
		sceneShaderProgram.createUniform("viewMatrix");
		sceneShaderProgram.createUniform("projectionMatrix");
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
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            sceneShaderProgram.createUniform("shadowMap_" + i);
        }
        sceneShaderProgram.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
        sceneShaderProgram.createUniform("modelMatrix");
        sceneShaderProgram.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
        sceneShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
        sceneShaderProgram.createUniform("renderShadow");
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void render(Window window, Camera camera, Scene scene, boolean sceneChanged) {
		clear();
		
		// Render depth map before view ports has been set up
        if (scene.isRenderShadows() && sceneChanged) {
            shadowRenderer.render(window, scene, camera, transformation, this);
        }
		
		glViewport(0, 0, window.getWidth(), window.getHeight());
		
		// Update projectionMatrix
		window.updateProjectionMatrix();
		
		renderScene(window, camera, scene);
		
		if (scene.getTerrain() != null) {
			renderTerrain(window, camera, scene);
		}
		
		renderSky(window, camera, scene);
	}
	
	private void renderSky(Window window, Camera camera, Scene scene) {
		skyShaderProgram.bind();
		
		Sky sky = scene.getSky();
		
		Matrix4f projectionMatrix = window.getProjectionMatrix();
		skyShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		Matrix4f modelMatrix = transformation.buildModelMatrix(sky);
		skyShaderProgram.setUniform("modelMatrix", modelMatrix);
		Matrix4f viewMatrix = camera.getViewMatrix();
		float m30 = viewMatrix.m30();
        viewMatrix.m30(0);
        float m31 = viewMatrix.m31();
        viewMatrix.m31(0);
        float m32 = viewMatrix.m32();
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
		
		viewMatrix.m30(m30);
        viewMatrix.m31(m31);
        viewMatrix.m32(m32);
		
		skyShaderProgram.unbind();
	}
	
	private void renderTerrain(Window window, Camera camera, Scene scene) {
		terrainShaderProgram.bind();
		
		Matrix4f viewMatrix = camera.getViewMatrix();
		sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
		Matrix4f projectionMatrix = window.getProjectionMatrix();
		sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		
		List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            terrainShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            terrainShaderProgram.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            terrainShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }
		
		// Update light uniforms
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(terrainShaderProgram, viewMatrix, sceneLight);
		
		int start = GL_TEXTURE0 + ShadowRenderer.NUM_CASCADES;
		
		for (int i = 0; i < MultilayeredMaterial.MAX_LAYERS; i++) {
			terrainShaderProgram.setUniform("diffuseMaps[" + i + "]", start + i * 2 + 0);
			terrainShaderProgram.setUniform("normalMaps[" + i + "]", start + i * 2 + 1);
		}
		terrainShaderProgram.setUniform("rgbaMap", start + MultilayeredMaterial.MAX_LAYERS * 2);
		
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
        		terrainShaderProgram.setUniform("shadowMap_" + i, GL_TEXTURE0 + i);
        }
        terrainShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);
		
		terrainShaderProgram.setUniform("fog", scene.getFog());
		
		// Render terrain mesh
		GameItem terrain = scene.getTerrain();
		Mesh terrainMesh = terrain.getMesh();
		terrainShaderProgram.setUniform("multilayeredMaterial", terrainMesh.getIMaterial());
		shadowRenderer.bindTextures(GL_TEXTURE0);
		// Set model matrix
		Matrix4f modelMatrix = transformation.buildModelMatrix(terrain);
		terrainShaderProgram.setUniform("modelMatrix", modelMatrix);
		terrainMesh.render();
		
		terrainShaderProgram.unbind();
	}
	
	private void renderScene(Window window, Camera camera, Scene scene) {
		sceneShaderProgram.bind();
		
		Matrix4f viewMatrix = camera.getViewMatrix();
		sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
		Matrix4f projectionMatrix = window.getProjectionMatrix();
		sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		
		List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            sceneShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            sceneShaderProgram.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            sceneShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }
		
		// Update light uniforms
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(sceneShaderProgram, viewMatrix, sceneLight);
		
		int start = GL_TEXTURE0 + ShadowRenderer.NUM_CASCADES;
		sceneShaderProgram.setUniform("diffuseMap", start + 0);
		sceneShaderProgram.setUniform("normalMap", start + 1);
		
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            sceneShaderProgram.setUniform("shadowMap_" + i, GL_TEXTURE0 + i);
        }
        sceneShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);
		
		sceneShaderProgram.setUniform("fog", scene.getFog());
		
		renderMeshes(scene);
		
		sceneShaderProgram.unbind();
	}

	private void renderMeshes(Scene scene) {
		// Render each mesh with the associated game items
		Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (Mesh mesh : mapMeshes.keySet()) {
			sceneShaderProgram.setUniform("material", mesh.getIMaterial());
			shadowRenderer.bindTextures(GL_TEXTURE0);
			mesh.renderList(mapMeshes.get(mesh), gameItem -> {
				// Set model matrix for this gameItem
				Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
	            sceneShaderProgram.setUniform("modelMatrix", modelMatrix);
			});
		}
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
