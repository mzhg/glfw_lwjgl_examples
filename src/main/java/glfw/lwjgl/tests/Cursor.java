package glfw.lwjgl.tests;

//========================================================================
//Cursor & input mode tests
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
//System cursors and input modes tests.
//
//========================================================================
import java.awt.Dimension;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

final class Cursor extends TestCommon{

	static int W = 640;
	static int H = 480;
	static boolean delay = false;

	static long windows[] = { 0, 0 };
	static long activeWindow = 0;
	static long cursor = 0;
	
	static Command[] commands = {
		new Command(GLFW.GLFW_KEY_H, 0),
		new Command(GLFW.GLFW_KEY_C, 0),
		new Command(GLFW.GLFW_KEY_D, 0),
		new Command(GLFW.GLFW_KEY_S, 0),
		new Command(GLFW.GLFW_KEY_N, 0),
		new Command(GLFW.GLFW_KEY_1, 0),
		new Command(GLFW.GLFW_KEY_2, 0),
		new Command(GLFW.GLFW_KEY_3, 0),
	};
	
	private static final class Command{
		int key;
		double time;
		
		public Command(int key, double time) {
			this.key = key;
			this.time = time;
		}
	}
	
	static Dimension[] cursorSize = {
		new Dimension(24, 24),
		new Dimension(13, 37),
		new Dimension(5, 53),
		new Dimension(43, 64),
		new Dimension(300, 300),
	};
	
	static int currentSize = 0;
	
	static void command_callback(int key)
	{
	    switch (key)
	    {
	        case GLFW.GLFW_KEY_H:
	        {
	            printf("H: show this help\n");
	            printf("C: call glfwCreateCursor()\n");
	            printf("D: call glfwDestroyCursor()\n");
	            printf("S: call glfwSetCursor()\n");
	            printf("N: call glfwSetCursor() with 0\n");
	            printf("1: set GLFW_CURSOR_NORMAL\n");
	            printf("2: set GLFW_CURSOR_HIDDEN\n");
	            printf("3: set GLFW_CURSOR_DISABLED\n");
	            printf("T: enable 3s delay for all previous commands\n");
	        }
	        break;

	        case GLFW.GLFW_KEY_C:
	        {
	            int x, y;
	            ByteBuffer image;

	            if (cursor != 0)
	              break;

	            int width = cursorSize[currentSize].width;
	            int height = cursorSize[currentSize].height;
	            GLFWImage instance = GLFWImage.create();
	            instance.width(width);
	            instance.height(height);
	            
	            printf("Cursor: width = %d, height = %d\n",  width, height);

//	            image = malloc(4 * image.width * image.height);
//	            image.pixels = pixels;
	            image = BufferUtils.createUnalignedByteBuffer(4 * width * height);
//	            image = instance.pixels(4 * width * height);

	            for (y = 0;  y < height;  y++)
	            {
	                for (x =  0;  x < width;  x++)
	                {
	                	image.put((byte) 0xFF);
	                	image.put((byte) 0);
	                	image.put((byte) (255 * y / height));
	                	image.put((byte) (255 * x / width));
	                }
	            }

	            image.flip();
//	            image = GLFWImage.malloc(width, height, image);
	            instance.pixels(image);
	            
	            cursor = GLFW.glfwCreateCursor(instance, 0, 0);
	            if(cursor == 0){
	            	printf("Create cursor failed\n");
	            }
	            currentSize = (currentSize + 1) % cursorSize.length;
	            break;
	        }

	        case GLFW.GLFW_KEY_D:
	        {
	            if (cursor != 0)
	            {
	            	GLFW.glfwDestroyCursor(cursor);
	                cursor = 0;
	            }

	            break;
	        }

	        case GLFW.GLFW_KEY_S:
	        {
	            if (cursor != 0)
	            	GLFW.glfwSetCursor(activeWindow, cursor);
	            else
	                printf("The cursor is not created\n");

	            break;
	        }

	        case GLFW.GLFW_KEY_N:
	        	GLFW.glfwSetCursor(activeWindow, 0);
	            break;

	        case GLFW.GLFW_KEY_1:
	        	GLFW.glfwSetInputMode(activeWindow, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
	            break;

	        case GLFW.GLFW_KEY_2:
	        	GLFW.glfwSetInputMode(activeWindow, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
	            break;

	        case GLFW.GLFW_KEY_3:
	        	GLFW.glfwSetInputMode(activeWindow, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
	            break;
	    }
	}
	
	static void framebuffer_size_callback(long window, int width, int height)
	{
	    W = width;
	    H = height;

	    GL11.glViewport(0, 0, W, H);
	}

	static void refresh_callback(long window)
	{
	    GLFW.glfwMakeContextCurrent(window);
	    GL11.glClearColor(0.0f, window == activeWindow ? 0.8f : 0.0f, 0.0f, 1.0f);
	    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	    GLFW.glfwSwapBuffers(window);
	}
	
	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action != GLFW.GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW.GLFW_KEY_ESCAPE:
	        	GLFW.glfwSetWindowShouldClose(window, 1);
	            break;

	        case GLFW.GLFW_KEY_T:
	            delay = !delay;
	            printf("Delay %s.\n", delay ? "enabled" : "disabled");
	            break;

	        default:
	        {
	            if (delay)
	            {
	                int i = 0;

	                while (i < commands.length && commands[i].key != key)
	                    i++;

	                if (i < commands.length)
	                    commands[i].time = GLFW.glfwGetTime();
	            }
	            else
	            {
	                command_callback(key);
	            }
	        }
	        break;
	    }
	}
	
	static void focus_callback(long window, boolean focused)
	{
	    if (focused)
	    {
	        activeWindow = window;
	        refresh_callback(windows[0]);
	        refresh_callback(windows[1]);
	    }
	}
	
	public static void main(String[] args) {
		int i;
	    boolean running = true;

//	    GLFW.glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (GLFW.glfwInit() == 0)
	        System.exit(0);

	    for (i = 0; i < 2; i++)
	    {
	        windows[i] = GLFW.glfwCreateWindow(W, H, "Cursor testing", 0, 0);

	        if (windows[i] == 0)
	        {
	        	GLFW.glfwTerminate();
	            System.exit(0);
	        }

	        GLFW.glfwSetWindowPos(windows[i], 100 + (i & 1) * (W + 50), 100);

	        GLFW.glfwSetWindowRefreshCallback(windows[i], safe(new GLFWWindowRefreshCallback() {
				@Override
				public void invoke(long window) {
					refresh_callback(window);
				}
			}));
	        GLFW.glfwSetFramebufferSizeCallback(windows[i], safe(new GLFWFramebufferSizeCallback() {
				
				@Override
				public void invoke(long window, int width, int height) {
					framebuffer_size_callback(window, width, height);
				}
			}));
	        GLFW.glfwSetKeyCallback(windows[i], safe(new GLFWKeyCallback() {
				public void invoke(long window, int key, int scancode, int action, int mods) {
					key_callback(window, key, scancode, action, mods);
				}
			}));
	        GLFW.glfwSetWindowFocusCallback(windows[i],safe( new GLFWWindowFocusCallback() {
				public void invoke(long window, int focused) {
					focus_callback(window, focused != 0);
				}
			}));

	        GLFW.glfwMakeContextCurrent(windows[i]);
	        GL.createCapabilities();
	        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	        GLFW.glfwSwapBuffers(windows[i]);
	    }
	    
	    activeWindow = windows[0];

	    key_callback(0, GLFW.GLFW_KEY_H, 0, GLFW.GLFW_PRESS, 0);

	    while (running)
	    {
	        if (delay)
	        {
	            double t = GLFW.glfwGetTime();

	            for (i = 0; i < commands.length; i++)
	            {
	                if (commands[i].time != 0 && t - commands[i].time >= 3.0)
	                {
	                    command_callback(commands[i].key);
	                    commands[i].time = 0;
	                }
	            }
	        }

	        running = !(GLFW.glfwWindowShouldClose(windows[0]) != 0 || GLFW.glfwWindowShouldClose(windows[1]) != 0);

	        GLFW.glfwPollEvents();
	    }
	    GLFW.glfwTerminate();
	}
}
