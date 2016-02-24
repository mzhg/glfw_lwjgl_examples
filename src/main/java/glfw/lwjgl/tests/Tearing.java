package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

final class Tearing extends TestCommon{

	static int swap_interval;
	static double frame_rate;
	
	static void update_window_title(long window)
	{
	    String title = String.format("Tearing detector (interval %d, %.1f Hz)",
	            swap_interval, frame_rate);

	    glfwSetWindowTitle(window, title);
	}

	static void set_swap_interval(long window, int interval)
	{
	    swap_interval = interval;
	    glfwSwapInterval(swap_interval);
	    update_window_title(window);
	}

	static void error_callback(int error, String description)
	{
	    printf("Error: %s\n", description);
	}

	static void framebuffer_size_callback(long window, int width, int height)
	{
	    glViewport(0, 0, width, height);
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (key == GLFW_KEY_SPACE && action == GLFW_PRESS)
	        set_swap_interval(window, 1 - swap_interval);
	}
	
	public static void main(String[] args){
		float position;
	    long frame_count = 0;
	    double last_time, current_time;
	    long window;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    window = glfwCreateWindow(640, 480, "", NULL, NULL);
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    set_swap_interval(window, 0);

	    last_time = glfwGetTime();
	    frame_rate = 0.0;

	    glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				framebuffer_size_callback(window, width, height);
			}
		}));
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    glMatrixMode(GL_PROJECTION);
	    glOrtho(-1.f, 1.f, -1.f, 1.f, 1.f, -1.f);
	    glMatrixMode(GL_MODELVIEW);

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        glClear(GL_COLOR_BUFFER_BIT);

	        position = (float) (Math.cos((float) glfwGetTime() * 4.f) * 0.75f);
	        glRectf(position - 0.25f, -1.f, position + 0.25f, 1.f);

	        glfwSwapBuffers(window);
	        glfwPollEvents();

	        frame_count++;

	        current_time = glfwGetTime();
	        if (current_time - last_time > 1.0)
	        {
	            frame_rate = frame_count / (current_time - last_time);
	            frame_count = 0;
	            last_time = current_time;
	            update_window_title(window);
	        }
	    }

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}
}
