package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

//========================================================================
//Context sharing test program
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
//This program is used to test sharing of objects between contexts
//
//========================================================================
final class Sharing extends TestCommon{

	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;
	private static final int OFFSET = 50;
	
	private static final long[] windows = new long[2];
	
	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action == GLFW_PRESS && key == GLFW_KEY_ESCAPE)
	        glfwSetWindowShouldClose(window, GL_TRUE);
	}

	static long open_window(String title, long share, int posX, int posY)
	{
	    long window;

	    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
	    window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, share);
	    if (window == 0)
	        return NULL;

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);
	    glfwSetWindowPos(window, posX, posY);
	    glfwShowWindow(window);

	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    return window;
	}

	static int create_texture()
	{
	    int x, y;
	    ByteBuffer pixels = BufferUtils.createUnalignedByteBuffer(256 * 256);
	    int texture;

	    texture = glGenTextures();
	    glBindTexture(GL_TEXTURE_2D, texture);

	    for (y = 0;  y < 256;  y++)
	    {
	        for (x = 0;  x < 256;  x++)
//	            pixels[y * 256 + x] = rand() % 256;
	        	pixels.put((byte)(Math.random() * 256));
	    }
        pixels.flip();
	    
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 256, 256, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

	    return texture;
	}

	static void draw_quad(int texture)
	{
	    glfwGetFramebufferSize(glfwGetCurrentContext(), x_buf, y_buf);

	    glViewport(0, 0, x_buf.get(0), y_buf.get(0));

	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    glOrtho(0.f, 1.f, 0.f, 1.f, 0.f, 1.f);

	    glEnable(GL_TEXTURE_2D);
	    glBindTexture(GL_TEXTURE_2D, texture);
	    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

	    glBegin(GL_QUADS);

	    glTexCoord2f(0.f, 0.f);
	    glVertex2f(0.f, 0.f);

	    glTexCoord2f(1.f, 0.f);
	    glVertex2f(1.f, 0.f);

	    glTexCoord2f(1.f, 1.f);
	    glVertex2f(1.f, 1.f);

	    glTexCoord2f(0.f, 1.f);
	    glVertex2f(0.f, 1.f);

	    glEnd();
	}
	
	public static void main(String[] args){
		int x, y, width;
	    int texture;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    windows[0] = open_window("First", NULL, OFFSET, OFFSET);
	    if (windows[0]==0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    // This is the one and only time we create a texture
	    // It is created inside the first context, created above
	    // It will then be shared with the second context, created below
	    texture = create_texture();

	    glfwGetWindowPos(windows[0], x_buf, y_buf);
	    x = x_buf.get(0); y = y_buf.get(0);
	    glfwGetWindowSize(windows[0], x_buf, y_buf);
	    width = x_buf.get(0);

	    // Put the second window to the right of the first one
	    windows[1] = open_window("Second", windows[0], x + width + OFFSET, y);
	    if (windows[1] == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    // Set drawing color for both contexts
	    glfwMakeContextCurrent(windows[0]);
	    GL.createCapabilities();
	    glColor3f(0.6f, 0.f, 0.6f);
	    glfwMakeContextCurrent(windows[1]);
	    GL.createCapabilities();
	    glColor3f(0.6f, 0.6f, 0.f);

	    glfwMakeContextCurrent(windows[0]);

	    while (glfwWindowShouldClose(windows[0]) == 0 &&
	           glfwWindowShouldClose(windows[1]) == 0)
	    {
	        glfwMakeContextCurrent(windows[0]);
	        draw_quad(texture);

	        glfwMakeContextCurrent(windows[1]);
	        draw_quad(texture);

	        glfwSwapBuffers(windows[0]);
	        glfwSwapBuffers(windows[1]);

	        glfwWaitEvents();
	    }

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}
}
