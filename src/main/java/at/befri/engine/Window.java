package at.befri.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class Window {
	public static final float FOV = (float) Math.toRadians(60.0f); // Field of View in Radians
	public static final float Z_NEAR = 0.01f; // Distance to the near plane
    public static final float Z_FAR = 2_000_000f; // Distance to the far plane
	
	private final String title;
	private int width;
	private int height;
	private boolean vSync;
	
	private long windowHandle;
	private boolean resized;
	private Matrix4f projectionMatrix;
	
	public Window(String title, int width, int height, boolean vSync) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.vSync = vSync;
		this.resized = false;
		this.projectionMatrix = new Matrix4f();
	}
	
	public void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();
		
		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
		
		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
		
		// macOS compatibility code
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        
        boolean maximized = false;
        // If no size has been specified set it to maximized state
        if (width == 0 || height == 0) {
            // Set up a fixed width and height so window initialization does not fail
            width = 100;
            height = 100;
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            maximized = true;
        }
        
        // Create the window
     	windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
     	if (windowHandle == NULL) {     		
     		throw new RuntimeException("Failed to create the GLFW window");
     	}
     	
     	// Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.resized = true;
        });
        
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
     	glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
     		if (GLFW_KEY_ESCAPE == key && GLFW_RELEASE == action) {
     			glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
     		}
     	});
     	
     	if (!maximized) {
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center our window
            glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );
        }

		// Make the OpenGL context current
		glfwMakeContextCurrent(windowHandle);
		
		if (vSync) {
			// Enable v-sync
			glfwSwapInterval(1);
		}
		
		// Make the window visible
		glfwShowWindow(windowHandle);
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		
		// Set the clear color
        glClearColor(0f, 0f, 0f, 0f);
        
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        
        // Enable wireframe mode
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        
        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
	}
	
	public void update() {
		glfwSwapBuffers(windowHandle);
		glfwPollEvents();
	}
	
	public void setClearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
	}
	
	public boolean isKeyPressed(int keyCode) {
		return GLFW_PRESS == glfwGetKey(windowHandle, keyCode);
	}
	
	public boolean windowShouldClose() {
		return glfwWindowShouldClose(windowHandle);
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean isvSync() {
		return vSync;
	}
	
	public void setvSync(boolean vSync) {
		this.vSync = vSync;
	}
	
	public long getWindowHandle() {
		return windowHandle;
	}
	
	public boolean isResized() {
		return resized;
	}
	
	public void setResized(boolean resized) {
		this.resized = resized;
	}

	public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f updateProjectionMatrix() {
        float aspectRatio = (float) width / (float) height;
        return projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    public static Matrix4f updateProjectionMatrix(Matrix4f matrix, int width, int height) {
        float aspectRatio = (float) width / (float) height;
        return matrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }
}
