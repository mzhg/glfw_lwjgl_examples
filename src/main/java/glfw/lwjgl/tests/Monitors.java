package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

final class Monitors extends TestCommon{

	private static final int LIST_MODE = 0;
	private static final int TEST_MODE = 1;
	
	static void usage()
	{
	    printf("Usage: monitors [-t]\n");
	    printf("       monitors -h\n");
	}

	static String format_mode(GLFWVidMode mode)
	{
		int width = mode.width();
		int height = mode.height();
		int redBits = mode.redBits();
		int greenBits = mode.greenBits();
		int blueBits = mode.blueBits();
		int refreshRate = mode.refreshRate();

	    return String.format("%d x %d x %d (%d %d %d) %d Hz",
	            width, height,
	            redBits + greenBits + blueBits,
	            redBits, greenBits, blueBits,
	            refreshRate);
	}
	
	static void framebuffer_size_callback(long window, int width, int height)
	{
	    printf("Framebuffer resized to %dx%d\n", width, height);

	    glViewport(0, 0, width, height);
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (key == GLFW_KEY_ESCAPE)
	        glfwSetWindowShouldClose(window, GL_TRUE);
	}

	static void list_modes(long monitor)
	{
	    int count, x, y, widthMM, heightMM, dpi, i;
	    IntBuffer count_buf = x_buf;
	    
	    final GLFWVidMode mode = glfwGetVideoMode(monitor);
	    final ByteBuffer modes = glfwGetVideoModes(monitor, count_buf);
	    count = count_buf.get(0);

	    glfwGetMonitorPos(monitor, x_buf, y_buf);
	    x = x_buf.get(0); y = y_buf.get(0);
	    
	    glfwGetMonitorPhysicalSize(monitor, x_buf, y_buf);
	    widthMM = x_buf.get(0);  heightMM = y_buf.get(0);

	    printf("Name: %s (%s)\n",
	           glfwGetMonitorName(monitor),
	           glfwGetPrimaryMonitor() == monitor ? "primary" : "secondary");
	    printf("Current mode: %s\n", format_mode(mode));
	    printf("Virtual position: %d %d\n", x, y);

	    dpi = (int) ((float) mode.width() * 25.4f / (float) widthMM);
	    printf("Physical size: %d x %d mm (%d dpi)\n", widthMM, heightMM, dpi);

	    printf("Modes:\n");

	    for (i = 0;  i < count;  i++)
	    {
	    	modes.position(i * GLFWVidMode.SIZEOF);
	    	GLFWVidMode _mode = new GLFWVidMode(modes);
	        printf("%d: %s", i, format_mode(_mode));

	        if (modecmp(mode, _mode))
	            printf(" (current mode)");

//	        putchar('\n');
	        printf("\n");
	    }
	}
	
	private static ByteBuffer glfwGetVideoModes(long monitor, IntBuffer count_buf) {
		// TODO Auto-generated method stub
		return null;
	}

	static boolean modecmp(GLFWVidMode mode1, GLFWVidMode mode2){
		if(mode1.width() != mode2.width()) return false;
		if(mode1.height() != mode2.height()) return false;
		if(mode1.redBits() != mode2.redBits()) return false;
		if(mode1.greenBits() != mode2.greenBits()) return false;
		if(mode1.blueBits() != mode2.blueBits()) return false;
		if(mode1.refreshRate() != mode2.refreshRate()) return false;
		return true;
	}
	
	static void test_modes(long monitor)
	{
	    int i, count;
	    long window;
	    final ByteBuffer modes = glfwGetVideoModes(monitor, x_buf);
	    count = x_buf.get(0);

	    for (i = 0;  i < count;  i++)
	    {
//	        const GLFWvidmode* mode = modes + i;
	    	modes.position(GLFWVidMode.SIZEOF * i);
//	        GLFWvidmode current;
	    	int width;
			int height;
			int redBits;
			int greenBits;
			int blueBits;

			GLFWVidMode _mode = new GLFWVidMode(modes);
	        glfwWindowHint(GLFW_RED_BITS, _mode.redBits());
	        glfwWindowHint(GLFW_GREEN_BITS, _mode.greenBits());
	        glfwWindowHint(GLFW_BLUE_BITS, _mode.blueBits());
	        glfwWindowHint(GLFW_REFRESH_RATE, _mode.refreshRate());

	        printf("Testing mode %d on monitor %s: %s\n",
	                i,
	               glfwGetMonitorName(monitor),
	               format_mode(_mode));

	        window = glfwCreateWindow(_mode.width(), _mode.height(),
	                                  "Video Mode Test",
	                                  glfwGetPrimaryMonitor(),
	                                  0);
	        if (window == 0)
	        {
	            printf("Failed to enter mode %u: %s\n",
	                    i,
	                   format_mode(_mode));
	            continue;
	        }

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

	        glfwMakeContextCurrent(window);
	        GL.createCapabilities();
	        glfwSwapInterval(1);

	        glfwSetTime(0.0);

	        while (glfwGetTime() < 5.0)
	        {
	            glClear(GL_COLOR_BUFFER_BIT);
	            glfwSwapBuffers(window);
	            glfwPollEvents();

	            if (glfwWindowShouldClose(window) != 0)
	            {
	                printf("User terminated program\n");

	                glfwTerminate();
	                exit(EXIT_SUCCESS);
	            }
	        }

	        redBits = glGetInteger(GL_RED_BITS);
	        greenBits = glGetInteger(GL_GREEN_BITS);
	        blueBits = glGetInteger(GL_BLUE_BITS);

	        glfwGetWindowSize(window, x_buf, y_buf);
	        width = x_buf.get(0); height = y_buf.get(0);

	        if (redBits != _mode.redBits() ||
	            greenBits != _mode.greenBits() ||
	            blueBits != _mode.blueBits())
	        {
	            printf("*** Color bit mismatch: (%d %d %d) instead of (%d %d %d)\n",
	                   redBits, greenBits, blueBits,
	                   _mode.redBits(), _mode.greenBits(), _mode.blueBits());
	        }

	        if (width != _mode.width() || height != _mode.height())
	        {
	            printf("*** Size mismatch: %dx%d instead of %dx%d\n",
	                   width, height,
	                   _mode.width(), _mode.height());
	        }

	        printf("Closing window\n");

	        glfwDestroyWindow(window);
	        window = 0;

	        glfwPollEvents();
	    }
	}
	
	public static void main(String[] args) {
		int /*ch, */i, count, mode = LIST_MODE;
	    PointerBuffer monitors;

//	    while ((ch = getopt(argc, argv, "th")) != -1)
//	    {
//	        switch (ch)
//	        {
//	            case 'h':
//	                usage();
//	                exit(EXIT_SUCCESS);
//	            case 't':
//	                mode = TEST_MODE;
//	                break;
//	            default:
//	                usage();
//	                exit(EXIT_FAILURE);
//	        }
//	    }
	    
	    try {
			Options options = new Options();
			options.addOption("h", "Lists short help");
			options.addOption("t", "Enable Test Mode");
			
			CommandLineParser parser = new DefaultParser(); 
			CommandLine cmd = parser.parse(options, args); 

			if(cmd.hasOption("h")) {
				usage();
				exit(EXIT_SUCCESS);
			}else if(cmd.hasOption("t")){
				mode = TEST_MODE;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    monitors = glfwGetMonitors();
	    count = monitors.remaining();

	    for (i = 0;  i < count;  i++)
	    {
	        if (mode == LIST_MODE)
	            list_modes(monitors.get(i));
	        else if (mode == TEST_MODE)
	            test_modes(monitors.get(i));
	    }

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}
}
