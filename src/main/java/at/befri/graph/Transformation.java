package at.befri.graph;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import at.befri.engine.items.GameItem;

public class Transformation {
	private final Matrix4f projectionMatrix;
	private final Matrix4f viewMatrix;
	private final Matrix4f modelMatrix;
	private final Matrix4f modelViewMatrix;
	
	public Transformation() {
		projectionMatrix = new Matrix4f();
		viewMatrix = new Matrix4f();
		modelMatrix = new Matrix4f();
		modelViewMatrix = new Matrix4f();
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	public Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
		float aspectRatio = width / height;
		return projectionMatrix
			.identity()
			.perspective(fov, aspectRatio, zNear, zFar)
		;
	}
	
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	public Matrix4f updateViewMatrix(Camera camera) {
		Vector3f position = camera.getPosition();
		Vector3f rotation = camera.getRotation();
		
		return viewMatrix
			.identity()
			.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0)) // First do the rotation so the camera rotates over its position
			.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0))
			.translate(-position.x, -position.y, -position.z) // Then do the translation
		;
	}
	
	public Matrix4f buildModelMatrix(GameItem gameItem) {
        Vector3f rotation = gameItem.getRotation();
        return modelMatrix
        	.identity()
        	.translate(gameItem.getPosition())
        	.rotateX((float)Math.toRadians(-rotation.x))
        	.rotateY((float)Math.toRadians(-rotation.y))
        	.rotateZ((float)Math.toRadians(-rotation.z))
        	.scale(gameItem.getScale())
        ;
    }
	
	public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {
		Vector3f rotation = gameItem.getRotation();
		modelMatrix
			.identity()
			.translate(gameItem.getPosition())
			.rotateX((float) Math.toRadians(-rotation.x))
			.rotateY((float) Math.toRadians(-rotation.y))
			.rotateZ((float) Math.toRadians(-rotation.z))
			.scale(gameItem.getScale())
		;
		modelViewMatrix.set(viewMatrix);
		return modelViewMatrix.mul(modelMatrix);
	}
}
