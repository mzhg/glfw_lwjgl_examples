package glfw.lwjgl.examples;

/*
 * 3-D gear wheels.  This program is in the public domain.
 *
 * Command line options:
 *    -info      print GL implementation information
 *    -exit      automatically exit after 30 seconds
 *
 *
 * Brian Paul
 *
 *
 * Marcus Geelnard:
 *   - Conversion to GLFW
 *   - Time based rendering (frame rate independent)
 *   - Slightly modified camera that should work better for stereo viewing
 *
 *
 * Camilla Berglund:
 *   - Removed FPS counter (this is not a benchmark)
 *   - Added a few comments
 *   - Enabled vsync
 */
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import glfw.lwjgl.tests.TestCommon;

final class Gears extends TestCommon{
	
	static final float _pos[] = {5.f, 5.f, 10.f, 0.f};
	static final float _red[] = {0.8f, 0.1f, 0.f, 1.f};
	static final float _green[] = {0.f, 0.8f, 0.2f, 1.f};
	static final float _blue[] = {0.2f, 0.2f, 1.f, 1.f};
	
	static final FloatBuffer pos = BufferUtils.createFloatBuffer(4);
	static final FloatBuffer red = BufferUtils.createFloatBuffer(4);
	static final FloatBuffer green = BufferUtils.createFloatBuffer(4);
	static final FloatBuffer blue = BufferUtils.createFloatBuffer(4);
	
	static{
		pos.put(_pos).flip();
		red.put(_red).flip();
		green.put(_green).flip();
		blue.put(_blue).flip();
	}

	/* If non-zero, the program exits after that many seconds
	 */
	static int autoexit = 0;

	/**

	  Draw a gear wheel.  You'll probably want to call this function when
	  building a display list since we do a lot of trig here.

	  Input:  inner_radius - radius of hole at center
	          outer_radius - radius at center of teeth
	          width - width of gear teeth - number of teeth
	          tooth_depth - depth of tooth

	 **/

	static void
	gear(float inner_radius, float outer_radius, float width,
	  int teeth, float tooth_depth)
	{
	  int i;
	  float r0, r1, r2;
	  float angle, da;
	  float u, v, len;

	  r0 = inner_radius;
	  r1 = outer_radius - tooth_depth / 2.f;
	  r2 = outer_radius + tooth_depth / 2.f;

	  da = 2.f * (float) Math.PI / teeth / 4.f;

	  glShadeModel(GL_FLAT);

	  glNormal3f(0.f, 0.f, 1.f);

	  /* draw front face */
	  glBegin(GL_QUAD_STRIP);
	  for (i = 0; i <= teeth; i++) {
	    angle = i * 2.f * (float) Math.PI / teeth;
	    glVertex3f(r0 * (float) cos(angle), r0 * (float) sin(angle), width * 0.5f);
	    glVertex3f(r1 * (float) cos(angle), r1 * (float) sin(angle), width * 0.5f);
	    if (i < teeth) {
	      glVertex3f(r0 * (float) cos(angle), r0 * (float) sin(angle), width * 0.5f);
	      glVertex3f(r1 * (float) cos(angle + 3 * da), r1 * (float) sin(angle + 3 * da), width * 0.5f);
	    }
	  }
	  glEnd();

	  /* draw front sides of teeth */
	  glBegin(GL_QUADS);
	  da = 2.f * (float) Math.PI / teeth / 4.f;
	  for (i = 0; i < teeth; i++) {
	    angle = i * 2.f * (float) Math.PI / teeth;

	    glVertex3f(r1 * (float) cos(angle), r1 * (float) sin(angle), width * 0.5f);
	    glVertex3f(r2 * (float) cos(angle + da), r2 * (float) sin(angle + da), width * 0.5f);
	    glVertex3f(r2 * (float) cos(angle + 2 * da), r2 * (float) sin(angle + 2 * da), width * 0.5f);
	    glVertex3f(r1 * (float) cos(angle + 3 * da), r1 * (float) sin(angle + 3 * da), width * 0.5f);
	  }
	  glEnd();

	  glNormal3f(0.0f, 0.0f, -1.0f);

	  /* draw back face */
	  glBegin(GL_QUAD_STRIP);
	  for (i = 0; i <= teeth; i++) {
	    angle = i * 2.f * (float) Math.PI / teeth;
	    glVertex3f(r1 * (float) cos(angle), r1 * (float) sin(angle), -width * 0.5f);
	    glVertex3f(r0 * (float) cos(angle), r0 * (float) sin(angle), -width * 0.5f);
	    if (i < teeth) {
	      glVertex3f(r1 * (float) cos(angle + 3 * da), r1 * (float) sin(angle + 3 * da), -width * 0.5f);
	      glVertex3f(r0 * (float) cos(angle), r0 * (float) sin(angle), -width * 0.5f);
	    }
	  }
	  glEnd();

	  /* draw back sides of teeth */
	  glBegin(GL_QUADS);
	  da = 2.f * (float) Math.PI / teeth / 4.f;
	  for (i = 0; i < teeth; i++) {
	    angle = i * 2.f * (float) Math.PI / teeth;

	    glVertex3f(r1 * (float) cos(angle + 3 * da), r1 * (float) sin(angle + 3 * da), -width * 0.5f);
	    glVertex3f(r2 * (float) cos(angle + 2 * da), r2 * (float) sin(angle + 2 * da), -width * 0.5f);
	    glVertex3f(r2 * (float) cos(angle + da), r2 * (float) sin(angle + da), -width * 0.5f);
	    glVertex3f(r1 * (float) cos(angle), r1 * (float) sin(angle), -width * 0.5f);
	  }
	  glEnd();

	  /* draw outward faces of teeth */
	  glBegin(GL_QUAD_STRIP);
	  for (i = 0; i < teeth; i++) {
	    angle = i * 2.f * (float) Math.PI / teeth;

	    glVertex3f(r1 * (float) cos(angle), r1 * (float) sin(angle), width * 0.5f);
	    glVertex3f(r1 * (float) cos(angle), r1 * (float) sin(angle), -width * 0.5f);
	    u = r2 * (float) cos(angle + da) - r1 * (float) cos(angle);
	    v = r2 * (float) sin(angle + da) - r1 * (float) sin(angle);
	    len = (float) Math.sqrt(u * u + v * v);
	    u /= len;
	    v /= len;
	    glNormal3f(v, -u, 0);
	    glVertex3f(r2 * (float) cos(angle + da), r2 * (float) sin(angle + da), width * 0.5f);
	    glVertex3f(r2 * (float) cos(angle + da), r2 * (float) sin(angle + da), -width * 0.5f);
	    glNormal3f((float) cos(angle), (float) sin(angle), 0.f);
	    glVertex3f(r2 * (float) cos(angle + 2 * da), r2 * (float) sin(angle + 2 * da), width * 0.5f);
	    glVertex3f(r2 * (float) cos(angle + 2 * da), r2 * (float) sin(angle + 2 * da), -width * 0.5f);
	    u = r1 * (float) cos(angle + 3 * da) - r2 * (float) cos(angle + 2 * da);
	    v = r1 * (float) sin(angle + 3 * da) - r2 * (float) sin(angle + 2 * da);
	    glNormal3f(v, -u, 0.f);
	    glVertex3f(r1 * (float) cos(angle + 3 * da), r1 * (float) sin(angle + 3 * da), width * 0.5f);
	    glVertex3f(r1 * (float) cos(angle + 3 * da), r1 * (float) sin(angle + 3 * da), -width * 0.5f);
	    glNormal3f((float) cos(angle), (float) sin(angle), 0.f);
	  }

	  glVertex3f(r1 * (float) cos(0), r1 * (float) sin(0), width * 0.5f);
	  glVertex3f(r1 * (float) cos(0), r1 * (float) sin(0), -width * 0.5f);

	  glEnd();

	  glShadeModel(GL_SMOOTH);

	  /* draw inside radius cylinder */
	  glBegin(GL_QUAD_STRIP);
	  for (i = 0; i <= teeth; i++) {
	    angle = i * 2.f * (float) Math.PI / teeth;
	    glNormal3f(-(float) cos(angle), -(float) sin(angle), 0.f);
	    glVertex3f(r0 * (float) cos(angle), r0 * (float) sin(angle), -width * 0.5f);
	    glVertex3f(r0 * (float) cos(angle), r0 * (float) sin(angle), width * 0.5f);
	  }
	  glEnd();

	}


	static float view_rotx = 20.f, view_roty = 30.f, view_rotz = 0.f;
	static int gear1, gear2, gear3;
	static float angle = 0.f;

	/* OpenGL draw function & timing */
	static void draw()
	{
	  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	  glPushMatrix();
	    glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
	    glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
	    glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

	    glPushMatrix();
	      glTranslatef(-3.0f, -2.0f, 0.0f);
	      glRotatef(angle, 0.0f, 0.0f, 1.0f);
	      glCallList(gear1);
	    glPopMatrix();

	    glPushMatrix();
	      glTranslatef(3.1f, -2.f, 0.f);
	      glRotatef(-2.f * angle - 9.f, 0.f, 0.f, 1.f);
	      glCallList(gear2);
	    glPopMatrix();

	    glPushMatrix();
	      glTranslatef(-3.1f, 4.2f, 0.f);
	      glRotatef(-2.f * angle - 25.f, 0.f, 0.f, 1.f);
	      glCallList(gear3);
	    glPopMatrix();

	  glPopMatrix();
	}


	/* update animation parameters */
	static void animate()
	{
	  angle = 100.f * (float) glfwGetTime();
	}


	/* change view angle, exit upon ESC */
	static void key( long window, int k, int s, int action, int mods )
	{
	  if( action != GLFW_PRESS ) return;

	  switch (k) {
	  case GLFW_KEY_Z:
	    if( (mods & GLFW_MOD_SHIFT)!=0 )
	      view_rotz -= 5.0;
	    else
	      view_rotz += 5.0;
	    break;
	  case GLFW_KEY_ESCAPE:
	    glfwSetWindowShouldClose(window, GL_TRUE);
	    break;
	  case GLFW_KEY_UP:
	    view_rotx += 5.0;
	    break;
	  case GLFW_KEY_DOWN:
	    view_rotx -= 5.0;
	    break;
	  case GLFW_KEY_LEFT:
	    view_roty += 5.0;
	    break;
	  case GLFW_KEY_RIGHT:
	    view_roty -= 5.0;
	    break;
	  default:
	    return;
	  }
	}


	/* new window size */
	static void reshape( long window, int width, int height )
	{
	  float h = (float) height / (float) width;
	  float xmax, znear, zfar;

	  znear = 5.0f;
	  zfar  = 30.0f;
	  xmax  = znear * 0.5f;

	  glViewport( 0, 0, (int) width, (int) height );
	  glMatrixMode( GL_PROJECTION );
	  glLoadIdentity();
	  glFrustum( -xmax, xmax, -xmax*h, xmax*h, znear, zfar );
	  glMatrixMode( GL_MODELVIEW );
	  glLoadIdentity();
	  glTranslatef( 0.0f, 0.0f, -20.0f );
	}

	

	/* program & OpenGL initialization */
	static void init(/*int argc, char *argv[]*/)
	{
	  glLightfv(GL_LIGHT0, GL_POSITION, pos);
	  glEnable(GL_CULL_FACE);
	  glEnable(GL_LIGHTING);
	  glEnable(GL_LIGHT0);
	  glEnable(GL_DEPTH_TEST);

	  /* make the gears */
	  gear1 = glGenLists(1);
	  glNewList(gear1, GL_COMPILE);
	  glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, red);
	  gear(1.f, 4.f, 1.f, 20, 0.7f);
	  glEndList();

	  gear2 = glGenLists(1);
	  glNewList(gear2, GL_COMPILE);
	  glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, green);
	  gear(0.5f, 2.f, 2.f, 10, 0.7f);
	  glEndList();

	  gear3 = glGenLists(1);
	  glNewList(gear3, GL_COMPILE);
	  glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, blue);
	  gear(1.3f, 2.f, 0.5f, 10, 0.7f);
	  glEndList();

	  glEnable(GL_NORMALIZE);

//	  for ( i=1; i<argc; i++ ) {
//	    if (strcmp(argv[i], "-info")==0) {
//	      printf("GL_RENDERER   = %s\n", (char *) glGetString(GL_RENDERER));
//	      printf("GL_VERSION    = %s\n", (char *) glGetString(GL_VERSION));
//	      printf("GL_VENDOR     = %s\n", (char *) glGetString(GL_VENDOR));
//	      printf("GL_EXTENSIONS = %s\n", (char *) glGetString(GL_EXTENSIONS));
//	    }
//	    else if ( strcmp(argv[i], "-exit")==0) {
//	      autoexit = 30;
//	      printf("Auto Exit after %i seconds.\n", autoexit );
//	    }
//	  }
	}


	/* program entry */
//	int main(int argc, char *argv[])
	public static void main(String[] argv)
	{
	    long window;
	    int width, height;

	    if( glfwInit()  == 0)
	    {
	        fprintf("Failed to initialize GLFW\n" );
	        exit( EXIT_FAILURE );
	    }

	    glfwWindowHint(GLFW_DEPTH_BITS, 16);

	    window = glfwCreateWindow( 300, 300, "Gears", NULL, NULL );
	    if (window == 0)
	    {
	        fprintf("Failed to open GLFW window\n" );
	        glfwTerminate();
	        exit( EXIT_FAILURE );
	    }

	    // Set callback functions
	    glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				reshape(window, width, height);
			}
		}));
	    
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action,
					int mods) {
				key(window, key, scancode, action, mods);
			}
		}));

	    glfwMakeContextCurrent(window);
//	    GLContext.createFromCurrent();
	    GL.createCapabilities();
	    glfwSwapInterval( 1 );

	    glfwGetFramebufferSize(window, x_buf, y_buf);
	    width = x_buf.get(0);  height = y_buf.get(0);
	    reshape(window, width, height);

	    // Parse command-line options
	    init();

	    // Main loop
	    while(glfwWindowShouldClose(window) == 0)
	    {
	        // Draw gears
	        draw();

	        // Update animation
	        animate();

	        // Swap buffers
	        glfwSwapBuffers(window);
	        glfwPollEvents();
	    }

	    // Terminate GLFW
	    glfwTerminate();

	    // Exit program
	    exit( EXIT_SUCCESS );
	}
}
