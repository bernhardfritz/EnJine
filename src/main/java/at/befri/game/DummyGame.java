package at.befri.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;

import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import at.befri.engine.IGameLogic;
import at.befri.engine.MouseInput;
import at.befri.engine.Scene;
import at.befri.engine.SceneLight;
import at.befri.engine.Window;
import at.befri.engine.items.GameItem;
import at.befri.engine.items.Sky;
import at.befri.graph.Camera;
import at.befri.graph.Gradient;
import at.befri.graph.Terrain;
import at.befri.graph.Material;
import at.befri.graph.Mesh;
import at.befri.graph.MultilayeredMaterial;
import at.befri.graph.OBJLoader;
import at.befri.graph.ProceduralSphere;
import at.befri.graph.Texture;
import at.befri.graph.lights.DirectionalLight;
import at.befri.graph.lights.PointLight;
import at.befri.graph.lights.SpotLight;

public class DummyGame implements IGameLogic {
	private final Renderer renderer;
	private final Camera camera;
	private Scene scene;
	private Gradient gradient;

	private static final float MOUSE_SENSITIVITY = 0.2f;
	private static final float CAMERA_POS_STEP = 0.05f;

	private final Vector3f cameraInc;
	private float spotAngle = 0;
	private float spotInc = 1;
	private float azimuth = 0.0f;
	private boolean firstTime;
	private boolean sceneChanged;

	public DummyGame() {
		renderer = new Renderer();
		camera = new Camera();
		cameraInc = new Vector3f(0, 0, 0);
		gradient = null;
		try {
			gradient = new Gradient("/textures/gradient2.png");
		} catch (Exception e) {
		}
		firstTime = true;
	}

	@Override
	public void init(Window window) throws Exception {
		renderer.init(window);

		scene = new Scene();
		
		// Shadows
        scene.setRenderShadows(true);

		// Setup game items
		// float reflectance = 1f;
		// Mesh mesh = OBJLoader.loadMesh("/models/bunny.obj");
		// Vector4f color = new Vector4f(0.2f, 0.5f, 0.5f, 1f);
		// Material material = new Material()
		// .setAmbientColor(color)
		// .setDiffuseColor(color)
		// .setSpecularColor(color)
		// .setReflectance(reflectance)
		// ;

//		Mesh mesh = OBJLoader.loadMesh("/models/cube.obj");
//		mesh.setIMaterial(new Material().setDiffuseMap(new Texture("/textures/rock.png"))
//				.setNormalMap(new Texture("/textures/rock_normals.png")).setReflectance(0.5f));
//
//		GameItem gameItem = new GameItem(mesh);
//		gameItem.setScale(0.5f);
//		gameItem.setPosition(0, 0, -2);
		// gameItem.setPosition(0, 0, -2);
		// gameItem.setScale(0.1f);
		// gameItem.setPosition(0, 0, -2);
		// gameItem.setPosition(0, 0, -0.2f);

		 Mesh sphere = ProceduralSphere.generate(1.0f, 80, 60, 0f, (float)Math.PI * 2f, 0f, (float)Math.PI, true)
			 .setIMaterial(new Material()
				.setDiffuseMap(new Texture("/textures/terrain/layer3/diffuse.png"))
				.setNormalMap(new Texture("/textures/terrain/layer3/normal.png"))
				.setReflectance(0.5f)
			 )
		;
		 GameItem gameItem = new GameItem(sphere);
		 gameItem.setScale(0.5f);
		 gameItem.setPosition(0, 0, -1);
		Mesh terrainMesh = Terrain.generate(-0.1f, 0.1f, getClass().getResourceAsStream("/textures/heightmap.png"), 40);
		terrainMesh.setIMaterial(new MultilayeredMaterial()
				.setDiffuseMaps(new Texture[] { new Texture("/textures/terrain/layer0/diffuse.png"),
						new Texture("/textures/terrain/layer1/diffuse.png"),
						new Texture("/textures/terrain/layer2/diffuse.png"),
						new Texture("/textures/terrain/layer3/diffuse.png"),
						new Texture("/textures/terrain/layer4/diffuse.png"), })
				.setNormalMaps(new Texture[] { new Texture("/textures/terrain/layer0/normal.png"),
						new Texture("/textures/terrain/layer1/normal.png"),
						new Texture("/textures/terrain/layer2/normal.png"),
						new Texture("/textures/terrain/layer3/normal.png"),
						new Texture("/textures/terrain/layer4/normal.png"), })
				.setRgbaMap(new Texture("/textures/terrain/rgba_new.png")).setReflectance(0.0f));
		GameItem gameItem3 = new GameItem(terrainMesh);
		gameItem3.setScale(25);
		
		Mesh cubeMesh = OBJLoader.loadMesh("/models/cube.obj");
        cubeMesh.setIMaterial(
        		new Material()
        			.setDiffuseMap(new Texture("/textures/terrain/layer3/diffuse.png"))
				.setNormalMap(new Texture("/textures/terrain/layer3/normal.png"))
        			.setReflectance(1f)
        		)
        	;
        GameItem cubeGameItem = new GameItem(cubeMesh);
        cubeGameItem.setPosition(0, 0, 0);
        cubeGameItem.setScale(0.5f);
		
		Mesh quadMesh = OBJLoader.loadMesh("/models/plane.obj");
		quadMesh.setIMaterial(
			new Material()
				.setDiffuseMap(new Texture("/textures/terrain/layer3/diffuse.png"))
				.setNormalMap(new Texture("/textures/terrain/layer3/normal.png"))
				.setReflectance(1f)
		);
		GameItem quadGameItem = new GameItem(quadMesh);
		quadGameItem.setPosition(0, -1, 0);
		quadGameItem.setScale(2.5f);
		
		GameItem[] gameItems = new GameItem[] {gameItem, gameItem3, quadGameItem};
		scene.setGameItems(gameItems);

		scene.getFog().setColor(new Vector3f(0.5f, 0.5f, 0.5f)).setDensity(0.025f);

		// Setup sky
		Sky sky = new Sky();
		scene.setSky(sky);

		// Setup lights
		setupLights();
	}

	private void setupLights() {
		SceneLight sceneLight = new SceneLight();
		scene.setSceneLight(sceneLight);

		// Ambient light
		sceneLight.setAmbientLight(new Vector3f(0.5f, 0.5f, 0.5f));

		// Point light
		Vector3f lightPosition = new Vector3f(0, 0, 1f);
		Vector3f lightColor = new Vector3f(1f, 0f, 0f);
		float lightIntensity = 1f;
		PointLight pointLight = new PointLight(lightPosition, lightColor, lightIntensity);
		PointLight.Attenuation att = new PointLight.Attenuation(0f, 0f, 1f);
		pointLight.setAttenuation(att);
		sceneLight.setPointLightList(new PointLight[] { pointLight });

		// Spot light
		lightPosition = new Vector3f(0f, 0f, 10f);
		lightColor = new Vector3f(1f, 1f, 1f);
		pointLight = new PointLight(lightPosition, lightColor, lightIntensity);
		att = new PointLight.Attenuation(0f, 0f, 0.02f);
		pointLight.setAttenuation(att);
		Vector3f coneDir = new Vector3f(0, 0, -1);
		float cutOff = (float) Math.cos(Math.toRadians(140));
		SpotLight spotLight = new SpotLight(pointLight, coneDir, cutOff);
		sceneLight.setSpotLightList(new SpotLight[] { spotLight, new SpotLight(spotLight) });

		Vector3f lightDirection = new Vector3f(0, 1, 1);
		lightColor = new Vector3f(1, 1, 1);
		sceneLight.setDirectionalLight(
			new DirectionalLight(lightDirection, lightColor, lightIntensity)
				.setShadowPosMult(5f)
				.setOrthoCoords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f)
		);
	}

	@Override
	public void input(Window window, MouseInput mouseInput) {
		sceneChanged = false;
		cameraInc.set(0, 0, 0);
		if (window.isKeyPressed(GLFW_KEY_W)) {
			sceneChanged = true;
			cameraInc.z = -1;
		} else if (window.isKeyPressed(GLFW_KEY_S)) {
			sceneChanged = true;
			cameraInc.z = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_A)) {
			sceneChanged = true;
			cameraInc.x = -1;
		} else if (window.isKeyPressed(GLFW_KEY_D)) {
			sceneChanged = true;
			cameraInc.x = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_Z)) {
			sceneChanged = true;
			cameraInc.y = -1;
		} else if (window.isKeyPressed(GLFW_KEY_X)) {
			sceneChanged = true;
			cameraInc.y = 1;
		}
		SceneLight sceneLight = scene.getSceneLight();
		SpotLight[] spotLightList = sceneLight.getSpotLightList();
		if (window.isKeyPressed(GLFW_KEY_N)) {
			sceneChanged = true;
			spotLightList[0].getPointLight().getPosition().z += 0.1f;
			azimuth += 0.001f;
			if (azimuth > 1.0f) {
				azimuth = 0.0f;
			}
		} else if (window.isKeyPressed(GLFW_KEY_M)) {
			sceneChanged = true;
			spotLightList[0].getPointLight().getPosition().z -= 0.1f;
			azimuth -= 0.001f;
			if (azimuth < 0.0f) {
				azimuth = 1.0f;
			}
		}
	}

	@Override
	public void update(float interval, MouseInput mouseInput) {
		// Update camera position
		camera.setPosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP,
				false);

		// Update camera based on mouse
		if (mouseInput.isRightButtonPressed()) {
			Vector2f rotVec = mouseInput.getDisplVec();
			camera.setRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0, false);
		}

		// Update sky
		Sky sky = scene.getSky();
		sky.setInclination(0.0f);
		sky.setAzimuth(azimuth);

		// Update spot light direction
		spotAngle += spotInc * 0.05f;
		if (spotAngle > 2) {
			spotInc = -1;
		} else if (spotAngle < -2) {
			spotInc = 1;
		}
		double spotAngleRad = Math.toRadians(spotAngle);
		SceneLight sceneLight = scene.getSceneLight();
		SpotLight[] spotLightList = sceneLight.getSpotLightList();
		Vector3f coneDir = spotLightList[0].getConeDirection();
		coneDir.y = (float) Math.sin(spotAngleRad);

		// Update directional light direction, intensity and color
		DirectionalLight directionalLight = sceneLight.getDirectionalLight();
		// lightAngle += 1.1f;
		// if (lightAngle > 90) {
		// directionalLight.setIntensity(0);
		// if (lightAngle >= 360) {
		// lightAngle = -90;
		// }
		// } else if (lightAngle <= -80 || lightAngle >= 80) {
		// float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
		// directionalLight.setIntensity(factor);
		// directionalLight.getColor().y = Math.max(factor, 0.9f);
		// directionalLight.getColor().z = Math.max(factor, 0.5f);
		// } else {
		// directionalLight.setIntensity(1);
		// directionalLight.getColor().x = 1;
		// directionalLight.getColor().y = 1;
		// directionalLight.getColor().z = 1;
		// }
		// double angRad = Math.toRadians(lightAngle);
		// directionalLight.getDirection().x = (float) Math.sin(angRad);
		// directionalLight.getDirection().y = (float) Math.cos(angRad);
		directionalLight.getDirection().x = sky.getSunPosition().x;
		directionalLight.getDirection().y = sky.getSunPosition().y;
		directionalLight.getDirection().z = sky.getSunPosition().z;
		directionalLight.getDirection().normalize();
		if (azimuth > 0.5) {
			directionalLight.getDirection().negate();
		}
		directionalLight.setColor(gradient.getColor(azimuth));
		
		// Update view matrix
        camera.updateViewMatrix();
	}

	@Override
	public void render(Window window) {
		if (firstTime) {
            sceneChanged = true;
            firstTime = false;
        }
		renderer.render(window, camera, scene, sceneChanged);
	}

	@Override
	public void cleanup() {
		renderer.cleanup();
		Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (Mesh mesh : mapMeshes.keySet()) {
			mesh.cleanUp();
		}
		GameItem terrain = scene.getTerrain();
		if (terrain != null) {
			Mesh terrainMesh = terrain.getMesh();
			if (terrainMesh != null) {
				terrainMesh.cleanUp();
			}
		}
	}
}
