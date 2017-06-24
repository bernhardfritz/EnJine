package at.befri.engine.items;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import at.befri.graph.Mesh;

public class GameItem {
	private final Mesh mesh;
	private final Vector3f position;
	private final Quaternionf rotation;
	private float scale;
	
	public GameItem(Mesh mesh) {
		this.mesh = mesh;
		position = new Vector3f(0, 0, 0);
		rotation = new Quaternionf();
		scale = 1;
	}
	
	public Mesh getMesh() {
		return mesh;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}
	
	public Quaternionf getRotation() {
		return rotation;
	}
	
	public final void setRotation(Quaternionf q) {
        this.rotation.set(q);
    }
	
	public float getScale() {
		return scale;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}
}
