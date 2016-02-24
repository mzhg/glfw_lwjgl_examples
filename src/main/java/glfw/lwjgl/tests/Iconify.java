package glfw.lwjgl.tests;

//========================================================================
//Iconify/restore test program
//Copyright (c) Camilla Berglund <elmindreda@elmindreda.org>
//
//This software is provided 'as-is', without any express or implied
//warranty. In no event will the authors be held liable for any damages
//arising from the use of this software.
//
//Permission is granted to anyone to use this software for any purpose,
//including commercial applications, and to alter it and redistribute it
//freely, subject to the following restrictions:
//
//1. The origin of this software must not be misrepresented; you must not
// claim that you wrote the original software. If you use this software
// in a product, an acknowledgment in the product documentation would
// be appreciated but is not required.
//
//2. Altered source versions must be plainly marked as such, and must not
// be misrepresented as being the original software.
//
//3. This notice may not be removed or altered from any source
// distribution.
//
//========================================================================
//
//This program is used to test the iconify/restore functionality for
//both full screen and windowed mode windows
//
//========================================================================
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

final class Iconify extends TestCommon{

	static final IntBuffer width_buf = x_buf;
	static final IntBuffer height_buf = y_buf;
	
	static void usage()
	{
	    printf("Usage: iconify [-h] [-f [-a] [-n]]\n");
	    printf("Options:\n");
	    printf("  -a create windows for all monitors\n");
	    printf("  -f create full screen window(s)\n");
	    printf("  -h show this help\n");
	    printf("  -n no automatic iconification of full screen windows\n");
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    printf("%.2f Key %s\n",
	           glfwGetTime(),
	           action == GLFW_PRESS ? "pressed" : "released");

	    if (action != GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW_KEY_SPACE:
	            glfwIconifyWindow(window);
	            break;
	        case GLFW_KEY_ESCAPE:
	            glfwSetWindowShouldClose(window, GL_TRUE);
	            break;
	    }
	}

	static void window_size_callback(long window, int width, int height)
	{
	    printf("%.2f Window resized to %dx%d\n", glfwGetTime(), width, height);
	}

	static void framebuffer_size_callback(long window, int width, int height)
	{
	    printf("%.2f Framebuffer resized to %dx%d\n", glfwGetTime(), width, height);

	    glViewport(0, 0, width, height);
	}

	static void window_focus_callback(long window, int focused)
	{
	    printf("%.2f Window %s\n",
	           glfwGetTime(),
	           focused != 0 ? "focused" : "defocused");
	}

	static void window_iconify_callback(long window, int iconified)
	{
	    printf("%.2f Window %s\n",
	           glfwGetTime(),
	           iconified != 0 ? "iconified" : "restored");
	}

	static void window_refresh_callback(long window)
	{
	    int width, height;
	    glfwGetFramebufferSize(window, width_buf, height_buf);
	    width = width_buf.get(0); height = height_buf.get(0);

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glEnable(GL_SCISSOR_TEST);

	    glScissor(0, 0, width, height);
	    glClearColor(0, 0, 0, 0);
	    glClear(GL_COLOR_BUFFER_BIT);

	    glScissor(0, 0, 640, 480);
	    glClearColor(1, 1, 1, 0);
	    glClear(GL_COLOR_BUFFER_BIT);

	    glfwSwapBuffers(window);
	}

	static long create_window(long monitor)
	{
	    int width, height;
	    long window;

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
	        width = 640;
	        height = 480;
	    }

	    window = glfwCreateWindow(width, height, "Iconify", monitor, 0);
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    return window;
	}
	
	public static void main(String[] args) {
		int i, window_count;
	    boolean auto_iconify = true, fullscreen = false, all_monitors = false;
	    long[] windows;

//	    while ((ch = getopt(argc, argv, "afhn")) != -1)
//	    {
//	        switch (ch)
//	        {
//	            case 'a':
//	                all_monitors = GL_TRUE;
//	                break;
//
//	            case 'h':
//	                usage();
//	                exit(EXIT_SUCCESS);
//
//	            case 'f':
//	                fullscreen = GL_TRUE;
//	                break;
//
//	            case 'n':
//	                auto_iconify = GL_FALSE;
//	                break;
//
//	            default:
//	                usage();
//	                exit(EXIT_FAILURE);
//	        }
//	    }
	    try {
			Options options = new Options();
			options.addOption("a", "all monitors");
			options.addOption("h", "Lists short help");
			options.addOption("f", "Enable the fullscreen mode.");
			options.addOption("n", "auto_iconify.");
			
			CommandLineParser parser = new DefaultParser(); 
			CommandLine cmd = parser.parse(options, args); 

			System.out.println("args = " + Arrays.toString(args));
			if(cmd.hasOption("a")){
				all_monitors = true;
			}if(cmd.hasOption("h")) {
				usage();
				exit(EXIT_SUCCESS);
			}else if(cmd.hasOption('f')){
				System.out.println("fullscreeen");
				fullscreen = true;
			}else if(cmd.hasOption('n')){
				auto_iconify = false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    glfwWindowHint(GLFW_AUTO_ICONIFY, auto_iconify ? 1 : 0);

	    if (fullscreen && all_monitors)
	    {
	        int monitor_count;
//	        GLFWmonitor** monitors = glfwGetMonitors(&monitor_count);
	        PointerBuffer monitors = glfwGetMonitors();
	        monitor_count = monitors.remaining();

	        window_count = monitor_count;
//	        windows = calloc(window_count, sizeof(GLFWwindow*));
	        windows = new long[window_count];

	        for (i = 0;  i < monitor_count;  i++)
	        {
	            windows[i] = create_window(monitors.get());
	            if (windows[i] == 0)
	                break;
	        }
	    }
	    else
	    {
	        long monitor = 0;

	        if (fullscreen)
	            monitor = glfwGetPrimaryMonitor();

	        window_count = 1;
//	        windows = calloc(window_count, sizeof(GLFWwindow*));
	        windows = new long[window_count];
	        windows[0] = create_window(monitor);
	    }

	    for (i = 0;  i < window_count;  i++)
	    {
	        glfwSetKeyCallback(windows[i], safe(new GLFWKeyCallback() {
				public void invoke(long window, int key, int scancode, int action, int mods) {
					key_callback(window, key, scancode, action, mods);
				}
			}));
	        glfwSetFramebufferSizeCallback(windows[i], safe(new GLFWFramebufferSizeCallback() {
				public void invoke(long window, int width, int height) {
					framebuffer_size_callback(window, width, height);
				}
			}));
	        glfwSetWindowSizeCallback(windows[i], safe(new GLFWWindowSizeCallback() {
				public void invoke(long window, int width, int height) {
					window_size_callback(window, width, height);
				}
			}));
	        glfwSetWindowFocusCallback(windows[i], safe(new GLFWWindowFocusCallback() {
				public void invoke(long window, int focused) {
					window_focus_callback(window, focused);
				}
			}));
	        glfwSetWindowIconifyCallback(windows[i], safe(new GLFWWindowIconifyCallback() {
				public void invoke(long window, int iconified) {
					window_iconify_callback(window, iconified);
				}
			}));
	        glfwSetWindowRefreshCallback(windows[i], safe(new GLFWWindowRefreshCallback() {
				public void invoke(long window) {
					window_refresh_callback(window);
				}
			}));

	        window_refresh_callback(windows[i]);

	        printf("Window is %s and %s\n",
	            glfwGetWindowAttrib(windows[i], GLFW_ICONIFIED) != 0? "iconified" : "restored",
	            glfwGetWindowAttrib(windows[i], GLFW_FOCUSED) != 0 ? "focused" : "defocused");
	    }

	    for (;;)
	    {
	        glfwPollEvents();

	        for (i = 0;  i < window_count;  i++)
	        {
	            if (glfwWindowShouldClose(windows[i]) != 0)
	                break;
	        }

	        if (i < window_count)
	            break;
	    }

	    glfwTerminate();
	}
}
