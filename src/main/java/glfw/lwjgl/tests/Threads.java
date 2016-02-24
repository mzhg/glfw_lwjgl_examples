package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.opengl.GL;

//========================================================================
//Multi-threading test
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
//This test is intended to verify whether the OpenGL context part of
//the GLFW API is able to be used from multiple threads
//
//========================================================================
final class Threads extends TestCommon{

	private static volatile boolean running = true;
	
	public static void main(String[] args) {
		int i;
	    _Thread threads[] =
	    {
	        new _Thread( NULL, "Red", 1.f, 0.f, 0.f),
	        new _Thread( NULL, "Green", 0.f, 1.f, 0.f),
	        new _Thread( NULL, "Blue", 0.f, 0.f, 1.f)
	    };
	    final int count = threads.length;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

	    for (i = 0;  i < count;  i++)
	    {
	        threads[i].window = glfwCreateWindow(200, 200,
	                                             threads[i].title,
	                                             NULL, NULL);
	        if (threads[i].window == 0)
	        {
	            glfwTerminate();
	            exit(EXIT_FAILURE);
	        }

	        glfwSetWindowPos(threads[i].window, 200 + 250 * i, 200);
	        glfwShowWindow(threads[i].window);

//	        if (thrd_create(&threads[i].id, thread_main, threads + i) !=
//	            thrd_success)
//	        {
//	            fprintf(stderr, "Failed to create secondary thread\n");
//
//	            glfwTerminate();
//	            exit(EXIT_FAILURE);
//	        }
	        
	        new ThreadMain(threads[i]);
	    }

	    while (running)
	    {
	        glfwWaitEvents();

	        for (i = 0;  i < count;  i++)
	        {
	            if (glfwWindowShouldClose(threads[i].window) != 0)
	                running = false;
	        }
	    }

	    for (i = 0;  i < count;  i++)
	        glfwHideWindow(threads[i].window);

	    for (i = 0;  i < count;  i++)
			try {
				threads[i].id.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

	    exit(EXIT_SUCCESS);
	}
	
	private final static class ThreadMain implements Runnable {
		
		private _Thread thread;
		public ThreadMain(_Thread thread) {
			this.thread = thread;
			
			thread.id = new Thread(this);
			thread.id.start();
		}
		
		@Override
		public void run() {
			glfwMakeContextCurrent(thread.window);
			GL.createCapabilities();
		    glfwSwapInterval(1);

		    while (running)
		    {
		        final float v = (float) Math.abs(Math.sin(glfwGetTime() * 2.f));
		        glClearColor(thread.r * v, thread.g * v, thread.b * v, 0.f);

		        glClear(GL_COLOR_BUFFER_BIT);
		        glfwSwapBuffers(thread.window);
		    }

		    glfwMakeContextCurrent(NULL);
		}
	};
	
	private static final class _Thread{
		long window;
		String title;
		float r,g,b;
		Thread id;
		
		public _Thread(long window, String title, float r, float g, float b) {
			this.window = window;
			this.title = title;
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}
}
