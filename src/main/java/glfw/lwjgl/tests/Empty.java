package glfw.lwjgl.tests;

//========================================================================
//Empty event test
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
//This test is intended to verify that posting of empty events works
//
//========================================================================
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwHideWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPostEmptyEvent;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

final class Empty extends TestCommon{
	
	static volatile boolean running = true;

	private static final Runnable thread_main = new Runnable() {
		public void run() {
			while(running){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				
				glfwPostEmptyEvent();
			}
		}
	};
	
	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
	        glfwSetWindowShouldClose(window, GL_TRUE);
	}
	
	public static void main(String[] args) {
	    Thread thread = new Thread(thread_main);
	    IntBuffer width_buf = BufferUtils.createIntBuffer(1);
	    IntBuffer height_buf = BufferUtils.createIntBuffer(1);
	    long window;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        System.exit(0);

	    window = glfwCreateWindow(640, 480, "Empty Event Test", 0, 0);
	    if (window == 0)
	    {
	        glfwTerminate();
	        System.exit(0);
	    }

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

//	    if (thrd_create(&thread, thread_main, NULL) != thrd_success)
//	    {
//	        fprintf(stderr, "Failed to create secondary thread\n");
//
//	        glfwTerminate();
//	        exit(EXIT_FAILURE);
//	    }
	    thread.start();
	    

	    while (running)
	    {
	        int width, height;
	        float r = (float)Math.random(), g = (float)Math.random(), b = (float)Math.random();
	        float l = (float) Math.sqrt(r * r + g * g + b * b);
	        
	        width_buf.clear(); height_buf.clear();
	        glfwGetFramebufferSize(window, width_buf, height_buf);
	        width = width_buf.get();
	        height = height_buf.get();

	        glViewport(0, 0, width, height);
	        glClearColor(r / l, g / l, b / l, 1.f);
	        glClear(GL_COLOR_BUFFER_BIT);
	        glfwSwapBuffers(window);

	        glfwWaitEvents();
	        
	        if (glfwWindowShouldClose(window) != 0)
	            running = false;
	    }

	    glfwHideWindow(window);
//	    thrd_join(thread, &result);
	    try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	    glfwDestroyWindow(window);

	    glfwTerminate();
	}
}
