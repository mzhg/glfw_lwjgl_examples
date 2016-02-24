package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;

//========================================================================
//Joystick input test
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
//This test displays the state of every button and axis of every connected
//joystick and/or gamepad
//
//========================================================================
final class Joysticks extends TestCommon{
	
	static final Joystick[] joysticks = new Joystick[GLFW_JOYSTICK_LAST - GLFW_JOYSTICK_1 + 1];
	static int joystick_count = 0;
	
	final static IntBuffer width_buf = BufferUtils.createIntBuffer(1);
	final static IntBuffer height_buf = BufferUtils.createIntBuffer(1);
	
	static{
		for(int i = 0; i < joysticks.length; i++)
			joysticks[i] = new Joystick();
	}
	
	static void framebuffer_size_callback(long window, int width, int height)
	{
	    glViewport(0, 0, width, height);
	}

	static void draw_joystick(Joystick j, int x, int y, int width, int height)
	{
	    int i;
	    final int axis_height = 3 * height / 4;
	    final int button_height = height / 4;

	    if (j.axis_count != 0)
	    {
	        final int axis_width = width / j.axis_count;

	        for (i = 0;  i < j.axis_count;  i++)
	        {
	            float value = j.axes[i] / 2.f + 0.5f;

	            glColor3f(0.3f, 0.3f, 0.3f);
	            glRecti(x + i * axis_width,
	                    y,
	                    x + (i + 1) * axis_width,
	                    y + axis_height);

	            glColor3f(1.f, 1.f, 1.f);
	            glRecti(x + i * axis_width,
	                    y + (int) (value * (axis_height - 5)),
	                    x + (i + 1) * axis_width,
	                    y + 5 + (int) (value * (axis_height - 5)));
	        }
	    }

	    if (j.button_count!=0)
	    {
	        final int button_width = width / j.button_count;

	        for (i = 0;  i < j.button_count;  i++)
	        {
	            if (j.buttons[i]!=0)
	                glColor3f(1.f, 1.f, 1.f);
	            else
	                glColor3f(0.3f, 0.3f, 0.3f);

	            glRecti(x + i * button_width,
	                    y + axis_height,
	                    x + (i + 1) * button_width,
	                    y + axis_height + button_height);
	        }
	    }
	}

	static void draw_joysticks(long window)
	{
	    int i, width, height, offset = 0;

	    glfwGetFramebufferSize(window, width_buf, height_buf);
	    width = width_buf.get(0);  height = height_buf.get(0);

	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    glOrtho(0.f, width, height, 0.f, 1.f, -1.f);
	    glMatrixMode(GL_MODELVIEW);

	    for (i = 0;  i < joysticks.length;  i++)
	    {
	        Joystick j = joysticks[i];

	        if (j.present)
	        {
	            draw_joystick(j,
	                          0, offset * height / joystick_count,
	                          width, height / joystick_count);
	            offset++;
	        }
	    }
	}

	static void refresh_joysticks()
	{
	    int i;

	    for (i = 0;  i < joysticks.length;  i++)
	    {
	        Joystick j = joysticks[i];

	        if (glfwJoystickPresent(GLFW_JOYSTICK_1 + i) != 0)
	        {
	            final FloatBuffer axes;
	            final ByteBuffer buttons;
	            int axis_count, button_count;

//	            free(j.name);
	            j.name = glfwGetJoystickName(GLFW_JOYSTICK_1 + i);

	            axes = glfwGetJoystickAxes(GLFW_JOYSTICK_1 + i);
	            axis_count = axes.remaining();
	            
	            if (axis_count != j.axis_count)
	            {
	                j.axis_count = axis_count;
//	                j.axes = realloc(j.axes, j.axis_count * sizeof(float));
	                j.axes = new float[j.axis_count];
	            }

//	            memcpy(j.axes, axes, axis_count * sizeof(float));
	            axes.get(j.axes);

	            buttons = glfwGetJoystickButtons(GLFW_JOYSTICK_1 + i);
	            button_count = buttons.remaining();
	            
	            if (button_count != j.button_count)
	            {
	                j.button_count = button_count;
//	                j.buttons = realloc(j.buttons, j.button_count);
	                j.buttons = new int[j.button_count];
	            }

//	            memcpy(j.buttons, buttons, button_count * sizeof(unsigned char));
	            for(i = 0; i < button_count; i++){
	            	j.buttons[i] = buttons.get(i) & 0xFF;
	            }

	            if (!j.present)
	            {
	                printf("Found joystick %d named \'%s\' with %d axes, %d buttons\n",
	                       i + 1, j.name, j.axis_count, j.button_count);

	                joystick_count++;
	            }

	            j.present = true;
	        }
	        else
	        {
	            if (j.present)
	            {
	                printf("Lost joystick %d named \'%s\'\n", i + 1, j.name);

//	                free(j.name);
//	                free(j.axes);
//	                free(j.buttons);
//	                memset(j, 0, sizeof(Joystick));
	                j.reset();

	                joystick_count--;
	            }
	        }
	    }
	}
	
	public static void main(String[] args) {
		long window;

//	    memset(joysticks, 0, sizeof(joysticks));

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    window = glfwCreateWindow(640, 480, "Joystick Test", 0, 0);
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				framebuffer_size_callback(window, width, height);
			}
		}));

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        glClear(GL_COLOR_BUFFER_BIT);

	        refresh_joysticks();
	        draw_joysticks(window);

	        glfwSwapBuffers(window);
	        glfwPollEvents();
	    }

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}

	private final static class Joystick{
		boolean present;
	    String name;
	    float[] axes;
	    int[] buttons;
	    int axis_count;
	    int button_count;
	    
	    public void reset(){
	    	present = false;
	    	name = null;
	    	buttons = null;
	    	axes = null;
	    	axis_count = 0;
	    	button_count = 0;
	    }
	}
}
