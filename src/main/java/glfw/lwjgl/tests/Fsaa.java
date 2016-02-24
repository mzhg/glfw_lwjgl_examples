package glfw.lwjgl.tests;

//========================================================================
//Full screen anti-aliasing test
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
//This test renders two high contrast, slowly rotating quads, one aliased
//and one (hopefully) anti-aliased, thus allowing for visual verification
//of whether FSAA is indeed enabled
//
//========================================================================

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBMultisample.*;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

final class Fsaa extends TestCommon{
	
	static void framebuffer_size_callback(long window, int width, int height)
	{
	    glViewport(0, 0, width, height);
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action != GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW_KEY_SPACE:
	            glfwSetTime(0.0);
	            break;
	    }
	}
	
	public static void main(String[] args) {
		int /*ch, */samples = 8;
	    long window;

//	    while ((ch = getopt(argc, argv, "hs:")) != -1)
//	    {
//	        switch (ch)
//	        {
//	            case 'h':
//	                usage();
//	                exit(EXIT_SUCCESS);
//	            case 's':
//	                samples = atoi(optarg);
//	                break;
//	            default:
//	                usage();
//	                exit(EXIT_FAILURE);
//	        }
//	    }

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    if (samples!=0)
	        printf("Requesting FSAA with %d samples\n", samples);
	    else
	        printf("Requesting that FSAA not be available\n");

	    glfwWindowHint(GLFW_SAMPLES, samples);
	    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

	    window = glfwCreateWindow(800, 400, "Aliasing Detector", 0, 0);
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

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

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    if (glfwExtensionSupported("GL_ARB_multisample") == 0)
	    {
	        printf("GL_ARB_multisample extension not supported\n");

	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    glfwShowWindow(window);

	    samples = glGetInteger(GL_SAMPLES_ARB);
	    if (samples != 0)
	        printf("Context reports FSAA is available with %d samples\n", samples);
	    else
	        printf("Context reports FSAA is unavailable\n");

	    glMatrixMode(GL_PROJECTION);
	    glOrtho(0.f, 1.f, 0.f, 0.5f, 0.f, 1.f);
	    glMatrixMode(GL_MODELVIEW);

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        float time = (float) glfwGetTime();

	        glClear(GL_COLOR_BUFFER_BIT);

	        glLoadIdentity();
	        glTranslatef(0.25f, 0.25f, 0.f);
	        glRotatef(time, 0.f, 0.f, 1.f);

	        glDisable(GL_MULTISAMPLE_ARB);
	        glRectf(-0.15f, -0.15f, 0.15f, 0.15f);

	        glLoadIdentity();
	        glTranslatef(0.75f, 0.25f, 0.f);
	        glRotatef(time, 0.f, 0.f, 1.f);

	        glEnable(GL_MULTISAMPLE_ARB);
	        glRectf(-0.15f, -0.15f, 0.15f, 0.15f);

	        glfwSwapBuffers(window);
	        glfwPollEvents();
	    }

	    glfwTerminate();
	}
}
