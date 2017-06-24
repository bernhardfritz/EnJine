package at.befri.graph;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	private final Vector3f position;
	private final Vector3f rotation;
	private Matrix4f viewMatrix;
	
	public Camera() {
		position = new Vector3f(0, 0, 0);
		rotation = new Vector3f(0, 0, 0);
		viewMatrix = new Matrix4f();
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Camera setPosition(float x, float y, float z, boolean absolute) {
		if (absolute) {
			position.x = x;
			position.y = y;
			position.z = z;			
		} else {
			if (x != 0) {
				position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * x;
				position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * x;
			}
			position.y += y;
			if (z != 0) {
				position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * z;
				position.z += (float) Math.cos(Math.toRadians(rotation.y)) * z;
			}
		}
		return this;
	}
	
	public Vector3f getRotation() {
		return rotation;
	}
	
	public Camera setRotation(float x, float y, float z, boolean absolute) {
		if (absolute) {
			rotation.x = x;
			rotation.y = y;
			rotation.z = z;			
		} else {
			rotation.x += x;
			rotation.y += y;
			rotation.z += z;
		}
		return this;
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	public Matrix4f updateViewMatrix() {
		return Transformation.updateGenericViewMatrix(position, rotation, viewMatrix);
	}
}
