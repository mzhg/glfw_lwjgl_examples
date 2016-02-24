package glfw.lwjgl.examples;

/*****************************************************************************
 * Wave Simulation in OpenGL
 * (C) 2002 Jakob Thomsen
 * http://home.in.tum.de/~thomsen
 * Modified for GLFW by Sylvain Hellegouarch - sh@programmationworld.com
 * Modified for variable frame rate by Marcus Geelnard
 * 2003-Jan-31: Minor cleanups and speedups / MG
 * 2010-10-24: Formatting and cleanup - Camilla Berglund
 *****************************************************************************/
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;

import glfw.lwjgl.tests.TestCommon;

final class Wave extends TestCommon{

	// Maximum delta T to allow for differential calculations
	static final float MAX_DELTA_T = 0.01f;

	// Animation speed (10.0 looks good)
	static final float ANIMATION_SPEED = 10.0f;

	static float alpha = 210.f, beta = -70.f;
	static float zoom = 2.f;

	static final DoubleBuffer cursorX = BufferUtils.createDoubleBuffer(1);
	static final DoubleBuffer cursorY = BufferUtils.createDoubleBuffer(1);

	static final class Vertex
	{
	    float x, y, z;
	    float r, g, b;
	};

	private static final int GRIDW = 50;
	private static final int GRIDH = 50;
	private static final int VERTEXNUM = (GRIDW*GRIDH);
	private static final int QUADW = (GRIDW - 1);
	private static final int QUADH = (GRIDH - 1);
	private static final int QUADNUM = (QUADW*QUADH);

	static final int[] quad = new int[4 * QUADNUM];
	static final Vertex[] vertex = new Vertex[VERTEXNUM];
	
	static final IntBuffer indexBuffer = BufferUtils.createIntBuffer(quad.length);
	static final FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertex.length * 6);
	

	/* The grid will look like this:
	 *
	 *      3   4   5
	 *      *---*---*
	 *      |   |   |
	 *      | 0 | 1 |
	 *      |   |   |
	 *      *---*---*
	 *      0   1   2
	 */

	//========================================================================
	// Initialize grid geometry
	//========================================================================

	static void init_vertices()
	{
	    int x, y, p;

	    // Place the vertices in a grid
	    for (y = 0;  y < GRIDH;  y++)
	    {
	        for (x = 0;  x < GRIDW;  x++)
	        {
	            p = y * GRIDW + x;
	            if(vertex[p] == null)
	            	vertex[p] = new Vertex();

	            vertex[p].x = (float) (x - GRIDW / 2) / (float) (GRIDW / 2);
	            vertex[p].y = (float) (y - GRIDH / 2) / (float) (GRIDH / 2);
	            vertex[p].z = 0;

	            if ((x % 4 < 2) ^ (y % 4 < 2))
	                vertex[p].r = 0.0f;
	            else
	                vertex[p].r = 1.0f;

	            vertex[p].g = (float) y / (float) GRIDH;
	            vertex[p].b = 1.f - ((float) x / (float) GRIDW + (float) y / (float) GRIDH) / 2.f;
	        }
	    }

	    for (y = 0;  y < QUADH;  y++)
	    {
	        for (x = 0;  x < QUADW;  x++)
	        {
	            p = 4 * (y * QUADW + x);

	            quad[p + 0] = y       * GRIDW + x;     // Some point
	            quad[p + 1] = y       * GRIDW + x + 1; // Neighbor at the right side
	            quad[p + 2] = (y + 1) * GRIDW + x + 1; // Upper right neighbor
	            quad[p + 3] = (y + 1) * GRIDW + x;     // Upper neighbor
	        }
	    }
	    
	    indexBuffer.put(quad).flip();
	}

	static double dt;
	static final double p[][] = new double[GRIDW][GRIDH];
//	double vx[GRIDW][GRIDH], vy[GRIDW][GRIDH];
//	double ax[GRIDW][GRIDH], ay[GRIDW][GRIDH];
	
	static final double[][] vx = new double[GRIDW][GRIDW];
	static final double[][] vy = new double[GRIDW][GRIDW];
	static final double[][] ax = new double[GRIDW][GRIDW];
	static final double[][] ay = new double[GRIDW][GRIDW];

	//========================================================================
	// Initialize grid
	//========================================================================

	static void init_grid()
	{
	    int x, y;
	    double dx, dy, d;

	    for (y = 0; y < GRIDH;  y++)
	    {
	        for (x = 0; x < GRIDW;  x++)
	        {
	            dx = (double) (x - GRIDW / 2);
	            dy = (double) (y - GRIDH / 2);
	            d = Math.sqrt(dx * dx + dy * dy);
	            if (d < 0.1 * (double) (GRIDW / 2))
	            {
	                d = d * 10.0;
	                p[x][y] = -cos(d * (Math.PI / (double)(GRIDW * 4))) * 100.0;
	            }
	            else
	                p[x][y] = 0.0;

	            vx[x][y] = 0.0;
	            vy[x][y] = 0.0;
	        }
	    }
	}


	//========================================================================
	// Draw scene
	//========================================================================

	static void draw_scene(long window)
	{
	    // Clear the color and depth buffers
	    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	    // We don't want to modify the projection matrix
	    glMatrixMode(GL_MODELVIEW);
	    glLoadIdentity();

	    // Move back
	    glTranslatef(0.0f, 0.0f, -zoom);
	    // Rotate the view
	    glRotatef(beta, 1.0f, 0.0f, 0.0f);
	    glRotatef(alpha, 0.0f, 0.0f, 1.0f);
	    
	    vertexBuffer.clear();
	    for(Vertex v: vertex){
	    	vertexBuffer.put(v.x);
	    	vertexBuffer.put(v.y);
	    	vertexBuffer.put(v.z);
	    	
	    	vertexBuffer.put(v.r);
	    	vertexBuffer.put(v.g);
	    	vertexBuffer.put(v.b);
	    }
	    vertexBuffer.flip();
	    
	    glVertexPointer(3, GL_FLOAT, 24, vertexBuffer);
	    vertexBuffer.position(3);
	    glColorPointer(3, GL_FLOAT,24, vertexBuffer); // Pointer to the first color

	    glDrawElements(GL_QUADS, indexBuffer);

	    glfwSwapBuffers(window);
	}


	//========================================================================
	// Initialize Miscellaneous OpenGL state
	//========================================================================

	static void init_opengl()
	{
	    // Use Gouraud (smooth) shading
	    glShadeModel(GL_SMOOTH);

	    // Switch on the z-buffer
	    glEnable(GL_DEPTH_TEST);

	    glEnableClientState(GL_VERTEX_ARRAY);
	    glEnableClientState(GL_COLOR_ARRAY);
	    
//	    glVertexPointer(3, GL_FLOAT, sizeof(struct Vertex), vertex);
//	    glColorPointer(3, GL_FLOAT, sizeof(struct Vertex), &vertex[0].r); // Pointer to the first color

	    glPointSize(2.0f);

	    // Background color is black
	    glClearColor(0, 0, 0, 0);
	}


	//========================================================================
	// Modify the height of each vertex according to the pressure
	//========================================================================

	static void adjust_grid()
	{
	    int pos;
	    int x, y;

	    for (y = 0; y < GRIDH;  y++)
	    {
	        for (x = 0;  x < GRIDW;  x++)
	        {
	            pos = y * GRIDW + x;
	            vertex[pos].z = (float) (p[x][y] * (1.0 / 50.0));
	        }
	    }
	}


	//========================================================================
	// Calculate wave propagation
	//========================================================================

	static void calc_grid()
	{
	    int x, y, x2, y2;
	    double time_step = dt * ANIMATION_SPEED;

	    // Compute accelerations
	    for (x = 0;  x < GRIDW;  x++)
	    {
	        x2 = (x + 1) % GRIDW;
	        for(y = 0; y < GRIDH; y++)
	            ax[x][y] = p[x][y] - p[x2][y];
	    }

	    for (y = 0;  y < GRIDH;  y++)
	    {
	        y2 = (y + 1) % GRIDH;
	        for(x = 0; x < GRIDW; x++)
	            ay[x][y] = p[x][y] - p[x][y2];
	    }

	    // Compute speeds
	    for (x = 0;  x < GRIDW;  x++)
	    {
	        for (y = 0;  y < GRIDH;  y++)
	        {
	            vx[x][y] = vx[x][y] + ax[x][y] * time_step;
	            vy[x][y] = vy[x][y] + ay[x][y] * time_step;
	        }
	    }

	    // Compute pressure
	    for (x = 1;  x < GRIDW;  x++)
	    {
	        x2 = x - 1;
	        for (y = 1;  y < GRIDH;  y++)
	        {
	            y2 = y - 1;
	            p[x][y] = p[x][y] + (vx[x2][y] - vx[x][y] + vy[x][y2] - vy[x][y]) * time_step;
	        }
	    }
	}

	//========================================================================
	// Handle key strokes
	//========================================================================

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action != GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW_KEY_ESCAPE:
	            glfwSetWindowShouldClose(window, GL_TRUE);
	            break;
	        case GLFW_KEY_SPACE:
	            init_grid();
	            break;
	        case GLFW_KEY_LEFT:
	            alpha += 5;
	            break;
	        case GLFW_KEY_RIGHT:
	            alpha -= 5;
	            break;
	        case GLFW_KEY_UP:
	            beta -= 5;
	            break;
	        case GLFW_KEY_DOWN:
	            beta += 5;
	            break;
	        case GLFW_KEY_PAGE_UP:
	            zoom -= 0.25f;
	            if (zoom < 0.f)
	                zoom = 0.f;
	            break;
	        case GLFW_KEY_PAGE_DOWN:
	            zoom += 0.25f;
	            break;
	        default:
	            break;
	    }
	}


	//========================================================================
	// Callback function for mouse button events
	//========================================================================

	static void mouse_button_callback(long window, int button, int action, int mods)
	{
	    if (button != GLFW_MOUSE_BUTTON_LEFT)
	        return;

	    if (action == GLFW_PRESS)
	    {
	        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	        glfwGetCursorPos(window, cursorX, cursorY);
	    }
	    else
	        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
	}


	//========================================================================
	// Callback function for cursor motion events
	//========================================================================
	static void cursor_position_callback(long window, double x, double y)
	{
	    if (glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED)
	    {
	        alpha += (float) (x - cursorX.get(0)) / 10.f;
	        beta += (float) (y - cursorY.get(0)) / 10.f;

	        cursorX.put(0, x);
	        cursorY.put(0, y);
	    }
	}


	//========================================================================
	// Callback function for scroll events
	//========================================================================

	static void scroll_callback(long window, double x, double y)
	{
	    zoom += (float) y / 4.f;
	    if (zoom < 0)
	        zoom = 0;
	}


	//========================================================================
	// Callback function for framebuffer resize events
	//========================================================================

	static void framebuffer_size_callback(long window, int width, int height)
	{
	    float ratio = 1.f;

	    if (height > 0)
	        ratio = (float) width / (float) height;

	    // Setup viewport
	    glViewport(0, 0, width, height);

	    // Change to the projection matrix and set our viewing volume
	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    gluPerspective(60.0f, ratio, 1.0f, 1024.0f);
	}


	//========================================================================
	// main
	//========================================================================

//	int main(int argc, char* argv[])
	public static void main(String[] args)
	{
	    long window;
	    double t, dt_total, t_old;
	    int width, height;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    window = glfwCreateWindow(640, 480, "Wave Simulation", NULL, NULL);
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
	    glfwSetMouseButtonCallback(window, safe(new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) {
				mouse_button_callback(window, button, action, mods);
			}
		}));
	    glfwSetCursorPosCallback(window, safe(new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				cursor_position_callback(window, xpos, ypos);
			}
		}));
	    glfwSetScrollCallback(window, safe(new GLFWScrollCallback() {
			public void invoke(long window, double xoffset, double yoffset) {
				scroll_callback(window, xoffset, yoffset);
			}
		}));

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    glfwGetFramebufferSize(window, x_buf, y_buf);
	    width = x_buf.get(0); height = y_buf.get(0);
	    framebuffer_size_callback(window, width, height);

	    // Initialize OpenGL
	    init_opengl();

	    // Initialize simulation
	    init_vertices();
	    init_grid();
	    adjust_grid();

	    // Initialize timer
	    t_old = glfwGetTime() - 0.01;

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        t = glfwGetTime();
	        dt_total = t - t_old;
	        t_old = t;

	        // Safety - iterate if dt_total is too large
	        while (dt_total > 0.f)
	        {
	            // Select iteration time step
	            dt = dt_total > MAX_DELTA_T ? MAX_DELTA_T : dt_total;
	            dt_total -= dt;

	            // Calculate wave propagation
	            calc_grid();
	        }

	        // Compute height of each vertex
	        adjust_grid();

	        // Draw wave grid to OpenGL display
	        draw_scene(window);

	        glfwPollEvents();
	    }

	    exit(EXIT_SUCCESS);
	}
}
