package glfw.lwjgl.tests;
//========================================================================
//Cursor mode test
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
//This test allows you to switch between the various cursor modes
//
//========================================================================
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

final class Peter extends TestCommon{

	static boolean reopen = false;
	static final DoubleBuffer cursor_x = BufferUtils.createDoubleBuffer(1);
	static final DoubleBuffer cursor_y = BufferUtils.createDoubleBuffer(1);
	
	static void cursor_position_callback(long window, double x, double y)
	{
	    printf("%.3f: Cursor position: %f %f (%f %f)\n",
	           glfwGetTime(),
	           x, y, x - cursor_x.get(0), y - cursor_y.get(0));

	    cursor_x.put(0, x);
	    cursor_y.put(0, y);
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action != GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW_KEY_D:
	            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	            printf("(( cursor is disabled ))\n");
	            break;

	        case GLFW_KEY_H:
	            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
	            printf("(( cursor is hidden ))\n");
	            break;

	        case GLFW_KEY_N:
	            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
	            printf("(( cursor is normal ))\n");
	            break;

	        case GLFW_KEY_R:
	            reopen = true;
	            break;
	    }
	}

	static void framebuffer_size_callback(long window, int width, int height)
	{
	    glViewport(0, 0, width, height);
	}

	static long open_window()
	{
	    long window = glfwCreateWindow(640, 480, "Peter Detector", 0, 0);
	    if (window == 0)
	        return 0;

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    glfwGetCursorPos(window, cursor_x, cursor_y);
	    printf("Cursor position: %f %f\n", cursor_x.get(0), cursor_y.get(0));

	    glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				framebuffer_size_callback(window, width, height);
			}
		}));
	    glfwSetCursorPosCallback(window, safe(new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				cursor_position_callback(window, xpos, ypos);
			}
		}));
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    return window;
	}
	
	public static void main(String[] args) {
		long window;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    window = open_window();
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    glClearColor(0.f, 0.f, 0.f, 0.f);

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        glClear(GL_COLOR_BUFFER_BIT);

	        glfwSwapBuffers(window);
	        glfwWaitEvents();

	        if (reopen)
	        {
	            glfwDestroyWindow(window);
	            window = open_window();
	            if (window == 0)
	            {
	                glfwTerminate();
	                exit(EXIT_FAILURE);
	            }

	            reopen = false;
	        }

	        // Workaround for an issue with msvcrt and mintty
//	        fflush(stdout);
	    }

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}
}
