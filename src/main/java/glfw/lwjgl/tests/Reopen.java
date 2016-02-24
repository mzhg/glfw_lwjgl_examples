package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.opengl.GL;

//========================================================================
//Window re-opener (open/close stress test)
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
//This test came about as the result of bug #1262773
//
//It closes and re-opens the GLFW window every five seconds, alternating
//between windowed and full screen mode
//
//It also times and logs opening and closing actions and attempts to separate
//user initiated window closing from its own
//
//========================================================================

final class Reopen extends TestCommon{

	static void framebuffer_size_callback(long window, int width, int height)
	{
	    glViewport(0, 0, width, height);
	}

	static void window_close_callback(long window)
	{
	    printf("Close callback triggered\n");
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action != GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW_KEY_Q:
	        case GLFW_KEY_ESCAPE:
	            glfwSetWindowShouldClose(window, GL_TRUE);
	            break;
	    }
	}

	static long open_window(int width, int height, long monitor)
	{
	    double base;
	    long window;

	    base = glfwGetTime();

	    window = glfwCreateWindow(width, height, "Window Re-opener", monitor, 0);
	    if (window == 0)
	        return 0;

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				framebuffer_size_callback(window, width, height);
			}
		}));
	    glfwSetWindowCloseCallback(window, safe(new GLFWWindowCloseCallback() {
			public void invoke(long window) {
				window_close_callback(window);
			}
		}));
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    if (monitor != 0)
	    {
	        printf("Opening full screen window on monitor %s took %.3f seconds\n",
	               glfwGetMonitorName(monitor),
	               glfwGetTime() - base);
	    }
	    else
	    {
	        printf("Opening regular window took %.3f seconds\n",
	               glfwGetTime() - base);
	    }

	    return window;
	}

	static void close_window(long window)
	{
	    double base = glfwGetTime();
	    glfwDestroyWindow(window);
	    printf("Closing window took %.3f seconds\n", glfwGetTime() - base);
	}
	
	public static void main(String[] args){
		int count = 0;
	    long window;

//	    srand((unsigned int) time(NULL));

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    for (;;)
	    {
	        long monitor = NULL;

	        if (count % 2 == 0)
	        {
	            PointerBuffer monitors = glfwGetMonitors();
	            monitor = monitors.get((int)(Math.random() * Integer.MAX_VALUE) % monitors.remaining());
	        }

	        window = open_window(640, 480, monitor);
	        if (window == 0)
	        {
	            glfwTerminate();
	            exit(EXIT_FAILURE);
	        }

	        glMatrixMode(GL_PROJECTION);
	        glOrtho(-1.f, 1.f, -1.f, 1.f, 1.f, -1.f);
	        glMatrixMode(GL_MODELVIEW);

	        glfwSetTime(0.0);

	        while (glfwGetTime() < 5.0)
	        {
	            glClear(GL_COLOR_BUFFER_BIT);

	            glPushMatrix();
	            glRotatef((float)glfwGetTime() * 100.f, 0.f, 0.f, 1.f);
	            glRectf(-0.5f, -0.5f, 1.f, 1.f);
	            glPopMatrix();

	            glfwSwapBuffers(window);
	            glfwPollEvents();

	            if (glfwWindowShouldClose(window) != 0)
	            {
	                close_window(window);
	                printf("User closed window\n");

	                glfwTerminate();
	                exit(EXIT_SUCCESS);
	            }
	        }

	        printf("Closing window\n");
	        close_window(window);

	        count++;
	    }
	}
}
