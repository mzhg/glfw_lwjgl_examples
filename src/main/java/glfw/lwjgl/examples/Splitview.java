package glfw.lwjgl.examples;

//========================================================================
//This is an example program for the GLFW library
//
//The program uses a "split window" view, rendering four views of the
//same scene in one window (e.g. uesful for 3D modelling software). This
//demo uses scissors to separete the four different rendering areas from
//each other.
//
//(If the code seems a little bit strange here and there, it may be
//because I am not a friend of orthogonal projections)
//========================================================================

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.opengl.GL;

import glfw.lwjgl.tests.TestCommon;

final class Splitview extends TestCommon{

	//========================================================================
	// Global variables
	//========================================================================

	// Mouse position
	static double xpos = 0, ypos = 0;

	// Window size
	static int width, height;

	// Active view: 0 = none, 1 = upper left, 2 = upper right, 3 = lower left,
	// 4 = lower right
	static int active_view = 0;

	// Rotation around each axis
	static int rot_x = 0, rot_y = 0, rot_z = 0;

	// Do redraw?
	static boolean do_redraw = true;

	static final float model_diffuse[]  = {1.0f, 0.8f, 0.8f, 1.0f};
	static final float model_specular[] = {0.6f, 0.6f, 0.6f, 1.0f};
	static final float model_shininess   = 20.0f;
	
	static final float light_position[] = {0.0f, 8.0f, 8.0f, 1.0f};
	static final float light_diffuse[]  = {1.0f, 1.0f, 1.0f, 1.0f};
	static final float light_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
	static final float light_ambient[]  = {0.2f, 0.2f, 0.3f, 1.0f};
	
	static final FloatBuffer buf = BufferUtils.createFloatBuffer(4);
	//========================================================================
	// Draw a solid torus (use a display list for the model)
	//========================================================================

	static final float TORUS_MAJOR   =  1.5f;
	static final float TORUS_MINOR   =  0.5f;
	static final float TORUS_MAJOR_RES = 32;
	static final float TORUS_MINOR_RES = 32;
	
	static int torus_list = 0;
	
	static FloatBuffer wrap(float[] array){
		buf.clear();
		buf.put(array).flip();
		return buf;
	}

	static void drawTorus()
	{
	    int    i, j, k;
	    double s, t, x, y, z, nx, ny, nz, scale, twopi;

	    if (torus_list == 0)
	    {
	        // Start recording displaylist
	        torus_list = glGenLists(1);
	        glNewList(torus_list, GL_COMPILE_AND_EXECUTE);

	        // Draw torus
	        twopi = 2.0 * Math.PI;
	        for (i = 0;  i < TORUS_MINOR_RES;  i++)
	        {
	            glBegin(GL_QUAD_STRIP);
	            for (j = 0;  j <= TORUS_MAJOR_RES;  j++)
	            {
	                for (k = 1;  k >= 0;  k--)
	                {
	                    s = (i + k) % TORUS_MINOR_RES + 0.5;
	                    t = j % TORUS_MAJOR_RES;

	                    // Calculate point on surface
	                    x = (TORUS_MAJOR + TORUS_MINOR * cos(s * twopi / TORUS_MINOR_RES)) * cos(t * twopi / TORUS_MAJOR_RES);
	                    y = TORUS_MINOR * sin(s * twopi / TORUS_MINOR_RES);
	                    z = (TORUS_MAJOR + TORUS_MINOR * cos(s * twopi / TORUS_MINOR_RES)) * sin(t * twopi / TORUS_MAJOR_RES);

	                    // Calculate surface normal
	                    nx = x - TORUS_MAJOR * cos(t * twopi / TORUS_MAJOR_RES);
	                    ny = y;
	                    nz = z - TORUS_MAJOR * sin(t * twopi / TORUS_MAJOR_RES);
	                    scale = 1.0 / Math.sqrt(nx*nx + ny*ny + nz*nz);
	                    nx *= scale;
	                    ny *= scale;
	                    nz *= scale;

	                    glNormal3f((float) nx, (float) ny, (float) nz);
	                    glVertex3f((float) x, (float) y, (float) z);
	                }
	            }

	            glEnd();
	        }

	        // Stop recording displaylist
	        glEndList();
	    }
	    else
	    {
	        // Playback displaylist
	        glCallList(torus_list);
	    }
	}


	//========================================================================
	// Draw the scene (a rotating torus)
	//========================================================================

	static void drawScene()
	{
	    glPushMatrix();

	    // Rotate the object
	    glRotatef((float) rot_x * 0.5f, 1.0f, 0.0f, 0.0f);
	    glRotatef((float) rot_y * 0.5f, 0.0f, 1.0f, 0.0f);
	    glRotatef((float) rot_z * 0.5f, 0.0f, 0.0f, 1.0f);

	    // Set model color (used for orthogonal views, lighting disabled)
	    glColor4fv(wrap(model_diffuse));

	    // Set model material (used for perspective view, lighting enabled)
	    glMaterialfv(GL_FRONT, GL_DIFFUSE, wrap(model_diffuse));
	    glMaterialfv(GL_FRONT, GL_SPECULAR, wrap(model_specular));
	    glMaterialf(GL_FRONT, GL_SHININESS, model_shininess);

	    // Draw torus
	    drawTorus();

	    glPopMatrix();
	}


	//========================================================================
	// Draw a 2D grid (used for orthogonal views)
	//========================================================================

	static void drawGrid(float scale, int steps)
	{
	    int i;
	    float x, y;

	    glPushMatrix();

	    // Set background to some dark bluish grey
	    glClearColor(0.05f, 0.05f, 0.2f, 0.0f);
	    glClear(GL_COLOR_BUFFER_BIT);

	    // Setup modelview matrix (flat XY view)
	    glLoadIdentity();
	    gluLookAt(0.0f, 0.0f, 1.0f,
	              0.0f, 0.0f, 0.0f,
	              0.0f, 1.0f, 0.0f);

	    // We don't want to update the Z-buffer
	    glDepthMask(false);

	    // Set grid color
	    glColor3f(0.0f, 0.5f, 0.5f);

	    glBegin(GL_LINES);

	    // Horizontal lines
	    x = scale * 0.5f * (float) (steps - 1);
	    y = -scale * 0.5f * (float) (steps - 1);
	    for (i = 0;  i < steps;  i++)
	    {
	        glVertex3f(-x, y, 0.0f);
	        glVertex3f(x, y, 0.0f);
	        y += scale;
	    }

	    // Vertical lines
	    x = -scale * 0.5f * (float) (steps - 1);
	    y = scale * 0.5f * (float) (steps - 1);
	    for (i = 0;  i < steps;  i++)
	    {
	        glVertex3f(x, -y, 0.0f);
	        glVertex3f(x, y, 0.0f);
	        x += scale;
	    }

	    glEnd();

	    // Enable Z-buffer writing again
	    glDepthMask(true);

	    glPopMatrix();
	}


	//========================================================================
	// Draw all views
	//========================================================================

	static void drawAllViews()
	{
	    double aspect;

	    // Calculate aspect of window
	    if (height > 0)
	        aspect = (double) width / (double) height;
	    else
	        aspect = 1.0;

	    // Clear screen
	    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	    // Enable scissor test
	    glEnable(GL_SCISSOR_TEST);

	    // Enable depth test
	    glEnable(GL_DEPTH_TEST);
	    glDepthFunc(GL_LEQUAL);

	    // ** ORTHOGONAL VIEWS **

	    // For orthogonal views, use wireframe rendering
	    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

	    // Enable line anti-aliasing
	    glEnable(GL_LINE_SMOOTH);
	    glEnable(GL_BLEND);
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	    // Setup orthogonal projection matrix
	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    glOrtho(-3.0 * aspect, 3.0 * aspect, -3.0, 3.0, 1.0, 50.0);

	    // Upper left view (TOP VIEW)
	    glViewport(0, height / 2, width / 2, height / 2);
	    glScissor(0, height / 2, width / 2, height / 2);
	    glMatrixMode(GL_MODELVIEW);
	    glLoadIdentity();
	    gluLookAt(0.0f, 10.0f, 1e-3f,   // Eye-position (above)
	              0.0f, 0.0f, 0.0f,     // View-point
	              0.0f, 1.0f, 0.0f);   // Up-vector
	    drawGrid(0.5f, 12);
	    drawScene();

	    // Lower left view (FRONT VIEW)
	    glViewport(0, 0, width / 2, height / 2);
	    glScissor(0, 0, width / 2, height / 2);
	    glMatrixMode(GL_MODELVIEW);
	    glLoadIdentity();
	    gluLookAt(0.0f, 0.0f, 10.0f,    // Eye-position (in front of)
	              0.0f, 0.0f, 0.0f,     // View-point
	              0.0f, 1.0f, 0.0f);   // Up-vector
	    drawGrid(0.5f, 12);
	    drawScene();

	    // Lower right view (SIDE VIEW)
	    glViewport(width / 2, 0, width / 2, height / 2);
	    glScissor(width / 2, 0, width / 2, height / 2);
	    glMatrixMode(GL_MODELVIEW);
	    glLoadIdentity();
	    gluLookAt(10.0f, 0.0f, 0.0f,    // Eye-position (to the right)
	              0.0f, 0.0f, 0.0f,     // View-point
	              0.0f, 1.0f, 0.0f);   // Up-vector
	    drawGrid(0.5f, 12);
	    drawScene();

	    // Disable line anti-aliasing
	    glDisable(GL_LINE_SMOOTH);
	    glDisable(GL_BLEND);

	    // ** PERSPECTIVE VIEW **

	    // For perspective view, use solid rendering
	    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

	    // Enable face culling (faster rendering)
	    glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);

	    // Setup perspective projection matrix
	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    gluPerspective(65.0f, (float)aspect, 1.0f, 50.0f);

	    // Upper right view (PERSPECTIVE VIEW)
	    glViewport(width / 2, height / 2, width / 2, height / 2);
	    glScissor(width / 2, height / 2, width / 2, height / 2);
	    glMatrixMode(GL_MODELVIEW);
	    glLoadIdentity();
	    gluLookAt(3.0f, 1.5f, 3.0f,     // Eye-position
	              0.0f, 0.0f, 0.0f,     // View-point
	              0.0f, 1.0f, 0.0f);   // Up-vector

	    // Configure and enable light source 1
	    glLightfv(GL_LIGHT1, GL_POSITION, wrap(light_position));
	    glLightfv(GL_LIGHT1, GL_AMBIENT, wrap(light_ambient));
	    glLightfv(GL_LIGHT1, GL_DIFFUSE, wrap(light_diffuse));
	    glLightfv(GL_LIGHT1, GL_SPECULAR, wrap(light_specular));
	    glEnable(GL_LIGHT1);
	    glEnable(GL_LIGHTING);

	    // Draw scene
	    drawScene();

	    // Disable lighting
	    glDisable(GL_LIGHTING);

	    // Disable face culling
	    glDisable(GL_CULL_FACE);

	    // Disable depth test
	    glDisable(GL_DEPTH_TEST);

	    // Disable scissor test
	    glDisable(GL_SCISSOR_TEST);

	    // Draw a border around the active view
	    if (active_view > 0 && active_view != 2)
	    {
	        glViewport(0, 0, width, height);

	        glMatrixMode(GL_PROJECTION);
	        glLoadIdentity();
	        glOrtho(0.0, 2.0, 0.0, 2.0, 0.0, 1.0);

	        glMatrixMode(GL_MODELVIEW);
	        glLoadIdentity();
	        glTranslatef((float) ((active_view - 1) & 1), (float) (1 - (active_view - 1) / 2), 0.0f);

	        glColor3f(1.0f, 1.0f, 0.6f);

	        glBegin(GL_LINE_STRIP);
	        glVertex2i(0, 0);
	        glVertex2i(1, 0);
	        glVertex2i(1, 1);
	        glVertex2i(0, 1);
	        glVertex2i(0, 0);
	        glEnd();
	    }
	}


	//========================================================================
	// Framebuffer size callback function
	//========================================================================

	static void framebufferSizeFun(long window, int w, int h)
	{
	    width  = w;
	    height = h > 0 ? h : 1;
	    do_redraw = true;
	}


	//========================================================================
	// Window refresh callback function
	//========================================================================

	static void windowRefreshFun(long window)
	{
	    drawAllViews();
	    glfwSwapBuffers(window);
	    do_redraw = false;
	}


	//========================================================================
	// Mouse position callback function
	//========================================================================

	static void cursorPosFun(long window, double x, double y)
	{
	    // Depending on which view was selected, rotate around different axes
	    switch (active_view)
	    {
	        case 1:
	            rot_x += (int) (y - ypos);
	            rot_z += (int) (x - xpos);
	            do_redraw = true;
	            break;
	        case 3:
	            rot_x += (int) (y - ypos);
	            rot_y += (int) (x - xpos);
	            do_redraw = true;
	            break;
	        case 4:
	            rot_y += (int) (x - xpos);
	            rot_z += (int) (y - ypos);
	            do_redraw = true;
	            break;
	        default:
	            // Do nothing for perspective view, or if no view is selected
	            break;
	    }

	    // Remember cursor position
	    xpos = x;
	    ypos = y;
	}


	//========================================================================
	// Mouse button callback function
	//========================================================================

	static void mouseButtonFun(long window, int button, int action, int mods)
	{
	    if ((button == GLFW_MOUSE_BUTTON_LEFT) && action == GLFW_PRESS)
	    {
	        // Detect which of the four views was clicked
	        active_view = 1;
	        if (xpos >= width / 2)
	            active_view += 1;
	        if (ypos >= height / 2)
	            active_view += 2;
	    }
	    else if (button == GLFW_MOUSE_BUTTON_LEFT)
	    {
	        // Deselect any previously selected view
	        active_view = 0;
	    }

	    do_redraw = true;
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
	        glfwSetWindowShouldClose(window, GL_TRUE);
	}


	//========================================================================
	// main
	//========================================================================

//	int main()
	public static void main(String[] args)
	{
	    long window;

	    // Initialise GLFW
	    if (glfwInit() == 0)
	    {
	        fprintf("Failed to initialize GLFW\n");
	        exit(EXIT_FAILURE);
	    }

	    // Open OpenGL window
	    window = glfwCreateWindow(500, 500, "Split view demo", NULL, NULL);
	    if (window == 0)
	    {
	        fprintf("Failed to open GLFW window\n");

	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    // Set callback functions
	    glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				framebufferSizeFun(window, width, height);
			}
		}));
	    glfwSetWindowRefreshCallback(window, safe(new GLFWWindowRefreshCallback() {
			public void invoke(long window) {
				windowRefreshFun(window);
			}
		}));
	    glfwSetCursorPosCallback(window, safe(new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				cursorPosFun(window, xpos, ypos);
			}
		}));
	    glfwSetMouseButtonCallback(window, safe(new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) {
				mouseButtonFun(window, button, action, mods);
			}
		}));
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    // Enable vsync
	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    glfwGetFramebufferSize(window, x_buf, y_buf);
	    width = x_buf.get(0); height = y_buf.get(0);
	    framebufferSizeFun(window, width, height);

	    // Main loop
	    for (;;)
	    {
	        // Only redraw if we need to
	        if (do_redraw)
	            windowRefreshFun(window);

	        // Wait for new events
	        glfwWaitEvents();

	        // Check if the window should be closed
	        if (glfwWindowShouldClose(window) != 0)
	            break;
	    }

	    // Close OpenGL window and terminate GLFW
	    glfwTerminate();

	    exit(EXIT_SUCCESS);
	}
}
