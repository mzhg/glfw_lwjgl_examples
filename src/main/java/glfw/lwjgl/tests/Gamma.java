package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

final class Gamma extends TestCommon{
	
	static final float STEP_SIZE = 0.1f;
	static float gamma_value = 1.0f;

	static void set_gamma(long window, float value)
	{
	    long monitor = glfwGetWindowMonitor(window);
	    if (monitor == 0)
	        monitor = glfwGetPrimaryMonitor();

	    gamma_value = value;
	    printf("Gamma: %f\n", gamma_value);
	    glfwSetGamma(monitor, gamma_value);
	}
	
	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action != GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW_KEY_ESCAPE:
	        {
	            glfwSetWindowShouldClose(window, GL_TRUE);
	            break;
	        }

	        case GLFW_KEY_KP_ADD:
	        case GLFW_KEY_Q:
	        {
	            set_gamma(window, gamma_value + STEP_SIZE);
	            break;
	        }

	        case GLFW_KEY_KP_SUBTRACT:
	        case GLFW_KEY_W:
	        {
	            if (gamma_value - STEP_SIZE > 0.f)
	                set_gamma(window, gamma_value - STEP_SIZE);

	            break;
	        }
	    }
	}

	static void framebuffer_size_callback(long window, int width, int height)
	{
	    glViewport(0, 0, width, height);
	}
	
	public static void main(String[] args) {
		int width, height;
	    long monitor = 0;
	    long window;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

//	    while ((ch = getopt(argc, argv, "fh")) != -1)
//	    {
//	        switch (ch)
//	        {
//	            case 'h':
//	                usage();
//	                exit(EXIT_SUCCESS);
//
//	            case 'f':
//	                monitor = glfwGetPrimaryMonitor();
//	                break;
//
//	            default:
//	                usage();
//	                exit(EXIT_FAILURE);
//	        }
//	    }

	    if (monitor != 0)
	    {
	        final GLFWVidMode mode = glfwGetVideoMode(monitor);

	        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
	        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
	        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
	        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());

	        width = mode.width();
	        height = mode.height();
	    }
	    else
	    {
	        width = 200;
	        height = 200;
	    }

	    window = glfwCreateWindow(width, height, "Gamma Test", monitor, 0);
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    set_gamma(window, 1.f);

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));
	    glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				framebuffer_size_callback(window, width, height);
			}
		}));

	    glMatrixMode(GL_PROJECTION);
	    glOrtho(-1.f, 1.f, -1.f, 1.f, -1.f, 1.f);
	    glMatrixMode(GL_MODELVIEW);

	    glClearColor(0.5f, 0.5f, 0.5f, 0);
	    
	    while (glfwWindowShouldClose(window) == 0)
	    {
	        glClear(GL_COLOR_BUFFER_BIT);

	        glColor3f(0.8f, 0.2f, 0.4f);
	        glRectf(-0.5f, -0.5f, 0.5f, 0.5f);

	        glfwSwapBuffers(window);
	        glfwWaitEvents();
	    }

	    glfwTerminate();
	}
}
