package glfw.lwjgl.examples;

//========================================================================
//Simple GLFW example
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
//! [code]
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import glfw.lwjgl.tests.TestCommon;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

final class Simple extends TestCommon{

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
	        glfwSetWindowShouldClose(window, GL_TRUE);
	}

//	int main()
	public static void main(String[] args)
	{
	    long window;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    window = glfwCreateWindow(640, 480, "Simple example", NULL, NULL);
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    glfwMakeContextCurrent(window);
//	    GLContext.createFromCurrent();
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        float ratio;
	        int width, height;

	        glfwGetFramebufferSize(window, x_buf, y_buf);
	        width = x_buf.get(0); height = y_buf.get(0);
	        ratio = width / (float) height;

	        glViewport(0, 0, width, height);
	        glClear(GL_COLOR_BUFFER_BIT);

	        glMatrixMode(GL_PROJECTION);
	        glLoadIdentity();
	        glOrtho(-ratio, ratio, -1.f, 1.f, 1.f, -1.f);
	        glMatrixMode(GL_MODELVIEW);

	        glLoadIdentity();
	        glRotatef((float) glfwGetTime() * 50.f, 0.f, 0.f, 1.f);

	        glBegin(GL_TRIANGLES);
	        glColor3f(1.f, 0.f, 0.f);
	        glVertex3f(-0.6f, -0.4f, 0.f);
	        glColor3f(0.f, 1.f, 0.f);
	        glVertex3f(0.6f, -0.4f, 0.f);
	        glColor3f(0.f, 0.f, 1.f);
	        glVertex3f(0.f, 0.6f, 0.f);
	        glEnd();

	        glfwSwapBuffers(window);
	        glfwPollEvents();
	    }

	    glfwDestroyWindow(window);

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}
}
