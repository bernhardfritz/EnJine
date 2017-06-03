package at.befri.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class Window {
	private final String title;
	private int width;
	private int height;
	private boolean vSync;
	
	private long windowHandle;
	private boolean resized;
	
	public Window(String title, int width, int height, boolean vSync) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.vSync = vSync;
		this.resized = false;
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
     	
     	// Get the resolution of the primary monitor
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		// Center the window
		glfwSetWindowPos(
			windowHandle,
			(vidmode.width() - width) / 2,
			(vidmode.height() - height) / 2
		);
		
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
}
