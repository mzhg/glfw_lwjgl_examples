package glfw.lwjgl.tests;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

final class Clipboard extends TestCommon{

	static void usage(){
		System.out.println("Usage: clipboard [-h]\n");
	}
	
	static void key_callback(long window, int key, int scancode, int action, int mods){
		if (action != GLFW.GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW.GLFW_KEY_ESCAPE:
	        	GLFW.glfwSetWindowShouldClose(window, 1);
	            break;

	        case GLFW.GLFW_KEY_V:
	            if (mods == GLFW.GLFW_MOD_CONTROL)
	            {
	                String string;

	                string = GLFW.glfwGetClipboardString(window);
	                if (string != null)
	                    System.out.printf("Clipboard contains \"%s\"\n", string);
	                else
	                	System.out.printf("Clipboard does not contain a string\n");
	            }
	            break;

	        case GLFW.GLFW_KEY_C:
	            if (mods == GLFW.GLFW_MOD_CONTROL)
	            {
	                String string = "Hello GLFW World!";
	                GLFW.glfwSetClipboardString(window, string);
	                System.out.printf("Setting clipboard to \"%s\"\n", string);
	            }
	            break;
	    }
	}
	
	static void framebuffer_size_callback(long window, int width, int height)
	{
	    GL11.glViewport(0, 0, width, height);
	}
	
	public static void main(String[] args) {
		long window;
		
//		GLFW.glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));
		
		if(GLFW.glfwInit() == 0){
			System.err.println("Failed to initialize GLFW");
			System.exit(1);
		}
		
		window = GLFW.glfwCreateWindow(200, 200, "Clipboard Test", 0, 0);
		if(window == 0){
			GLFW.glfwTerminate();
			System.err.println("Failed to open GLFW window");
		}
		
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		
		GLFW.glfwSwapInterval(1);
		GLFW.glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));
		
		GLFW.glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				framebuffer_size_callback(window, width, height);
			}
		}));
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glOrtho(-1.f, 1.f, -1.f, 1.f, -1.f, 1.f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glClearColor(0.5f, 0.5f, 0.5f, 0);

	    while (GLFW.glfwWindowShouldClose(window) == 0)
	    {
	    	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

	    	GL11.glColor3f(0.8f, 0.2f, 0.4f);
	    	GL11.glRectf(-0.5f, -0.5f, 0.5f, 0.5f);

	    	GLFW.glfwSwapBuffers(window);
	    	GLFW.glfwWaitEvents();
	    }

	    GLFW.glfwTerminate();
	}
}
