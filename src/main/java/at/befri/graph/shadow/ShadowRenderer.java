package at.befri.graph.shadow;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import at.befri.engine.Scene;
import at.befri.engine.SceneLight;
import at.befri.engine.Utils;
import at.befri.engine.Window;
import at.befri.engine.items.GameItem;
import at.befri.game.Renderer;
import at.befri.graph.Camera;
import at.befri.graph.Mesh;
import at.befri.graph.ShaderProgram;
import at.befri.graph.Transformation;
import at.befri.graph.lights.DirectionalLight;

public class ShadowRenderer {
	public static final int NUM_CASCADES = 3;
    public static final float[] CASCADE_SPLITS = new float[]{Window.Z_FAR / 20.0f, Window.Z_FAR / 10.0f, Window.Z_FAR};
	private List<ShadowCascade> shadowCascades;
	private ShadowBuffer shadowBuffer;
	private ShaderProgram depthShaderProgram;
	
	public void init(Window window) throws Exception {
		shadowBuffer = new ShadowBuffer();
		shadowCascades = new ArrayList<>();
		
		setupDepthShader();
		
		float zNear = Window.Z_NEAR;
        for (int i = 0; i < NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = new ShadowCascade(zNear, CASCADE_SPLITS[i]);
            shadowCascades.add(shadowCascade);
            zNear = CASCADE_SPLITS[i];
        }
	}
	
	public List<ShadowCascade> getShadowCascades() {
        return shadowCascades;
    }
	
	public void bindTextures(int start) {
        this.shadowBuffer.bindTextures(start);
    }
	
	private void setupDepthShader() throws Exception {
		depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.glsl"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.glsl"));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("modelMatrix");
        depthShaderProgram.createUniform("lightViewMatrix");
        depthShaderProgram.createUniform("orthoProjectionMatrix");
	}
    
    public void update(Window window, Matrix4f viewMatrix, Scene scene) {
    		SceneLight sceneLight = scene.getSceneLight();
    		DirectionalLight directionalLight = sceneLight != null ? sceneLight.getDirectionalLight() : null;
    		for (int i = 0; i < NUM_CASCADES; i++) {
    			ShadowCascade shadowCascade = shadowCascades.get(i);
    			shadowCascade.update(window, viewMatrix, directionalLight);
    		}
    }
    
    public void render(Window window, Scene scene, Camera camera, Transformation transformation, Renderer renderer) {
    		update(window, camera.getViewMatrix(), scene);
    		
    		// Setup view port to match the texture size
    		glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
    		glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);
        
        depthShaderProgram.bind();
        
        // Render scene for each cascade map
        for (int i = 0; i < NUM_CASCADES; i++) {
        		ShadowCascade shadowCascade = shadowCascades.get(i);
        		
        		depthShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix());
        		depthShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix());
            
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIds()[i], 0);
            glClear(GL_DEPTH_BUFFER_BIT);
            
            renderMeshes(scene, transformation);
        }
        
        // Unbind
        depthShaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    private void renderMeshes(Scene scene, Transformation transformation) {
    		Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (Mesh mesh : mapMeshes.keySet()) {
			mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
				Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
				depthShaderProgram.setUniform("modelMatrix", modelMatrix);
			});
		}
    }
    
    public void cleanup() {
        if (shadowBuffer != null) {
            shadowBuffer.cleanup();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanup();
        }
    }
}
