package at.befri.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.befri.engine.items.GameItem;
import at.befri.engine.items.Sky;
import at.befri.graph.IMaterial;
import at.befri.graph.Mesh;
import at.befri.graph.weather.Fog;

public class Scene {
	private Map<Mesh, List<GameItem>> meshMap;
	private GameItem terrain;
	private Sky sky;
	private SceneLight sceneLight;
	private Fog fog;

	public Scene() {
		meshMap = new HashMap<>();
		fog = new Fog();
	}

	public Map<Mesh, List<GameItem>> getGameMeshes() {
		return meshMap;
	}
	
	public GameItem getTerrain() {
		return terrain;
	}

	public void setGameItems(GameItem[] gameItems) {
		int numGameItems = gameItems != null ? gameItems.length : 0;
		for (int i = 0; i < numGameItems; i++) {
			GameItem gameItem = gameItems[i];
			Mesh mesh = gameItem.getMesh();
			IMaterial iMaterial = mesh.getIMaterial();
			if (iMaterial != null && iMaterial.isMultilayered()) { // this is the terrain, do not put it in the meshMap
				terrain = gameItem;
			} else {
				List<GameItem> list = meshMap.get(mesh);
				if (list == null) {
					list = new ArrayList<>();
					meshMap.put(mesh, list);
				}
				list.add(gameItem);
			}
		}
	}

	public Sky getSky() {
		return sky;
	}

	public void setSky(Sky sky) {
		this.sky = sky;
	}

	public SceneLight getSceneLight() {
		return sceneLight;
	}

	public void setSceneLight(SceneLight sceneLight) {
		this.sceneLight = sceneLight;
	}

	public Fog getFog() {
		return fog;
	}
}
