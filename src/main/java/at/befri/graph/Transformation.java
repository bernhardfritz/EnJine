package at.befri.graph;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import at.befri.engine.items.GameItem;

public class Transformation {
	private final Matrix4f projectionMatrix;
	private final Matrix4f viewMatrix;
	private final Matrix4f modelMatrix;
	private final Matrix4f modelViewMatrix;
	private final Matrix4f modelLightMatrix;
	private final Matrix4f modelLightViewMatrix;
	private final Matrix4f orthoProjMatrix;
	private final Matrix4f lightViewMatrix;
	
	public Transformation() {
		projectionMatrix = new Matrix4f();
		viewMatrix = new Matrix4f();
		modelMatrix = new Matrix4f();
		modelViewMatrix = new Matrix4f();
		modelLightMatrix = new Matrix4f();
		modelLightViewMatrix = new Matrix4f();
		orthoProjMatrix = new Matrix4f();
		lightViewMatrix = new Matrix4f();
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
	
	public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f matrix) {
		Vector3f rotation = gameItem.getRotation();
		modelMatrix
			.identity()
			.translate(gameItem.getPosition())
			.rotateX((float) Math.toRadians(-rotation.x))
			.rotateY((float) Math.toRadians(-rotation.y))
			.rotateZ((float) Math.toRadians(-rotation.z))
			.scale(gameItem.getScale())
		;
		modelViewMatrix.set(matrix);
		return modelViewMatrix.mul(modelMatrix);
	}
	
	public Matrix4f getLightViewMatrix() {
		return lightViewMatrix;
	}
	
	public void setLightViewMatrix(Matrix4f lightViewMatrix) {
		this.lightViewMatrix.set(lightViewMatrix);
	}

	public Matrix4f updateLightViewMatrix(Vector3f position, Vector3f rotation) {
		return updateGenericViewMatrix(position, rotation, lightViewMatrix);
	}
	
	private Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
		matrix.identity();
		// First do the rotation so camera rotates over its position
		matrix
			.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
			.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0))
		;
		// Then do the translation
		matrix.translate(-position.x, -position.y, -position.z);
		return matrix;
	}
	
	public Matrix4f getOrthoProjectionMatrix() {
		return orthoProjMatrix;
	}
	
	public Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar) {
		orthoProjMatrix.identity();
		orthoProjMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
		return orthoProjMatrix;
	}

	public Matrix4f buildModelLightViewMatrix(GameItem gameItem, Matrix4f matrix) {
		Vector3f rotation = gameItem.getRotation();
		modelLightMatrix
			.identity()
			.translate(gameItem.getPosition())
			.rotateX((float) Math.toRadians(-rotation.x))
			.rotateY((float) Math.toRadians(-rotation.y))
			.rotateZ((float) Math.toRadians(-rotation.z))
			.scale(gameItem.getScale())
		;
		modelLightViewMatrix.set(matrix);
		return modelLightViewMatrix.mul(modelLightMatrix);
	}
}
