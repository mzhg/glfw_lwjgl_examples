package glfw.lwjgl.tests;

//========================================================================
//Mouse cursor accuracy test
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
//This test came about as the result of bug #1867804
//
//No sign of said bug has so far been detected
//
//========================================================================
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

final class Accuracy extends TestCommon{
	
	static double cursor_x = 0.0, cursor_y = 0.0;
	static int window_width = 640, window_height = 480;
	static int swap_interval = 1;
	
	static void set_swap_interval(long window, int interval)
	{
	    swap_interval = interval;
	    GLFW.glfwSwapInterval(swap_interval);

	    String title = String.format("Cursor Inaccuracy Detector (interval %d)", swap_interval);

	    GLFW.glfwSetWindowTitle(window, title);
	}
	
	static void framebuffer_size_callback(long window, int width, int height)
	{
	    window_width = width;
	    window_height = height;

	    GL11.glViewport(0, 0, window_width, window_height);

	    GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
	    GL11.glOrtho(0.f, window_width, 0.f, window_height, 0.f, 1.f);
	}
	
	public static void main(String[] args) {
		long window;
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		
//		GLFW.glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));
		
		if(GLFW.glfwInit() == 0)
			System.exit(0);
		
		window = GLFW.glfwCreateWindow(window_width, window_height, "", 0, 0);
		if(window == 0){
			GLFW.glfwTerminate();
			System.out.println("window is null!");
			System.exit(0);
		}
		
		GLFW.glfwSetCursorPosCallback(window, safe(new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				cursor_x = xpos;
				cursor_y = ypos;
			}
		}));
		
		GLFW.glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			
			@Override
			public void invoke(long window, int width, int height) {
				framebuffer_size_callback(window, width, height);
			}
		}));
		
		GLFW.glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_PRESS)
			        set_swap_interval(window, 1 - swap_interval);
			}
		}));
		
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities();
		
		GLFW.glfwGetFramebufferSize(window, width, height);
		
		framebuffer_size_callback(window, width.get(), height.get());
		set_swap_interval(window, swap_interval);
		
		while(GLFW.glfwWindowShouldClose(window) == 0)
		{
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2d(0.f, (window_height - cursor_y));
			GL11.glVertex2d(window_width, (window_height - cursor_y));
			GL11.glVertex2d(cursor_x, 0.f);
			GL11.glVertex2d(cursor_x, window_height);
			GL11.glEnd();

	        GLFW.glfwSwapBuffers(window);
	        GLFW.glfwPollEvents();
		}
		
		GLFW.glfwTerminate();
	}
}
