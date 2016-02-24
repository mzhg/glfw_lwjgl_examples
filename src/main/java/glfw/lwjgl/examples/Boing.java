package glfw.lwjgl.examples;

/*****************************************************************************
 * Title:   GLBoing
 * Desc:    Tribute to Amiga Boing.
 * Author:  Jim Brooks  <gfx@jimbrooks.org>
 *          Original Amiga authors were R.J. Mical and Dale Luck.
 *          GLFW conversion by Marcus Geelnard
 * Notes:   - 360' = 2*PI [radian]
 *
 *          - Distances between objects are created by doing a relative
 *            Z translations.
 *
 *          - Although OpenGL enticingly supports alpha-blending,
 *            the shadow of the original Boing didn't affect the color
 *            of the grid.
 *
 *          - [Marcus] Changed timing scheme from interval driven to frame-
 *            time based animation steps (which results in much smoother
 *            movement)
 *
 * History of Amiga Boing:
 *
 * Boing was demonstrated on the prototype Amiga (codenamed "Lorraine") in
 * 1985. According to legend, it was written ad-hoc in one night by
 * R. J. Mical and Dale Luck. Because the bouncing ball animation was so fast
 * and smooth, attendees did not believe the Amiga prototype was really doing
 * the rendering. Suspecting a trick, they began looking around the booth for
 * a hidden computer or VCR.
 *****************************************************************************/

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL;

import glfw.lwjgl.tests.TestCommon;


final class Boing extends TestCommon{

	private static final float RADIUS = 70.f;
	private static final float STEP_LONGITUDE = 22.5f /* 22.5 makes 8 bands like original Boing */;
	private static final float STEP_LATITUDE = 22.5f;
	private static final float DIST_BALL = (RADIUS * 2.f + RADIUS * 0.1f);
	private static final float VIEW_SCENE_DIST = (DIST_BALL * 3.f + 200.f)/* distance from viewer to middle of boing area */;
	private static final float GRID_SIZE = (RADIUS * 4.5f) /* length (width) of grid */;
	private static final float BOUNCE_HEIGHT = (RADIUS * 2.1f);
	private static final float BOUNCE_WIDTH = (RADIUS * 2.1f);
	private static final float SHADOW_OFFSET_X = -20.f;
	private static final float SHADOW_OFFSET_Y = 10.f;
	private static final float SHADOW_OFFSET_Z = 0.f;
	private static final float WALL_L_OFFSET = 0.f;
	private static final float WALL_R_OFFSET = 5.f

	/* Animation speed (50.0 mimics the original GLUT demo speed) */;
	private static final float ANIMATION_SPEED = 50.f

	/* Maximum allowed delta time per physics iteration */;
	private static final float MAX_DELTA_T = 0.02f;

	/* Draw ball, or its shadow */
	private static final int DRAW_BALL = 0,
					DRAW_BALL_SHADOW   = 1;
	
	/* Random number generator */
	private static final int RAND_MAX = 4096;
	
	private static boolean BOING_DEBUG = false;
	
	/* Global vars */
	static int width, height;
	static float deg_rot_y       = 0.f;
	static float deg_rot_y_inc   = 2.f;
	static boolean override_pos  = false;
	static float cursor_x        = 0.f;
	static float cursor_y        = 0.f;
	static float ball_x          = -RADIUS;
	static float ball_y          = -RADIUS;
	static float ball_x_inc      = 1.f;
	static float ball_y_inc      = 2.f;
	static int drawBallHow;
	static double  t;
	static double  t_old = 0.f;
	static double  dt;
	
	/*****************************************************************************
	 * Truncate a degree.
	 *****************************************************************************/
	static float TruncateDeg(float deg )
	{
	   if ( deg >= 360.f )
	      return (deg - 360.f);
	   else
	      return deg;
	}

	/*****************************************************************************
	 * Convert a degree (360-based) into a radian.
	 * 360' = 2 * PI
	 *****************************************************************************/
	static double deg2rad( double deg )
	{
	   return deg / 360 * (2 * Math.PI);
	}

	/*****************************************************************************
	 * 360' sin().
	 *****************************************************************************/
	static double sin_deg( double deg )
	{
	   return Math.sin( deg2rad( deg ) );
	}

	/*****************************************************************************
	 * 360' cos().
	 *****************************************************************************/
	static double cos_deg( double deg )
	{
	   return Math.cos( deg2rad( deg ) );
	}
	
	/*****************************************************************************
	 * Compute a cross product (for a normal vector).
	 *
	 * c = a x b
	 *****************************************************************************/
	static void crossProduct( Vertex a, Vertex b, Vertex c, Vertex n )
	{
	   float u1, u2, u3;
	   float v1, v2, v3;

	   u1 = b.x - a.x;
	   u2 = b.y - a.y;
	   u3 = b.y - a.z;

	   v1 = c.x - a.x;
	   v2 = c.y - a.y;
	   v3 = c.z - a.z;

	   n.x = u2 * v3 - v2 * v3;
	   n.y = u3 * v1 - v3 * u1;
	   n.z = u1 * v2 - v1 * u2;
	}

	/*****************************************************************************
	 * Calculate the angle to be passed to gluPerspective() so that a scene
	 * is visible.  This function originates from the OpenGL Red Book.
	 *
	 * Parms   : size
	 *           The size of the segment when the angle is intersected at "dist"
	 *           (ie at the outermost edge of the angle of vision).
	 *
	 *           dist
	 *           Distance from viewpoint to scene.
	 *****************************************************************************/
	static float PerspectiveAngle( float size, float dist )
	{
	   float radTheta, degTheta;

	   radTheta = 2.f * (float) Math.atan2( size / 2.f, dist );
	   degTheta = (180.f * radTheta) / (float) Math.PI;
	   return degTheta;
	}
	
	/*****************************************************************************
	 * init()
	 *****************************************************************************/
	static void init()
	{
	   /*
	    * Clear background.
	    */
	   glClearColor( 0.55f, 0.55f, 0.55f, 0.f );

	   glShadeModel( GL_FLAT );
	}


	/*****************************************************************************
	 * display()
	 *****************************************************************************/
	static void display()
	{
	   glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
	   glPushMatrix();

	   drawBallHow = DRAW_BALL_SHADOW;
	   DrawBoingBall();

	   DrawGrid();

	   drawBallHow = DRAW_BALL;
	   DrawBoingBall();

	   glPopMatrix();
	   glFlush();
	}


	/*****************************************************************************
	 * reshape()
	 *****************************************************************************/
	static void reshape( long window, int w, int h )
	{
	   glViewport( 0, 0, w, h );

	   glMatrixMode( GL_PROJECTION );
	   glLoadIdentity();

	   gluPerspective( PerspectiveAngle( RADIUS * 2, 200 ),
	                   (float)w / (float)h,
	                   1.0f,
	                   VIEW_SCENE_DIST );

	   glMatrixMode( GL_MODELVIEW );
	   glLoadIdentity();

	   gluLookAt( 0.0f, 0.0f, VIEW_SCENE_DIST,/* eye */
	              0.0f, 0.0f, 0.0f,            /* center of vision */
	              0.0f, -1.0f, 0.0f );         /* up vector */
	}

	static void key_callback( long window, int key, int scancode, int action, int mods )
	{
	    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
	        glfwSetWindowShouldClose(window, GL_TRUE);
	}

	static void set_ball_pos ( float x, float y )
	{
	   ball_x = (width / 2) - x;
	   ball_y = y - (height / 2);
	}

	static void mouse_button_callback( long window, int button, int action, int mods )
	{
	   if (button != GLFW_MOUSE_BUTTON_LEFT)
	      return;

	   if (action == GLFW_PRESS)
	   {
	      override_pos = true;
	      set_ball_pos(cursor_x, cursor_y);
	   }
	   else
	   {
	      override_pos = false;
	   }
	}

	static void cursor_position_callback( long window, double x, double y )
	{
	   cursor_x = (float) x;
	   cursor_y = (float) y;

	   if ( override_pos )
	      set_ball_pos(cursor_x, cursor_y);
	}

	/*****************************************************************************
	 * Draw the Boing ball.
	 *
	 * The Boing ball is sphere in which each facet is a rectangle.
	 * Facet colors alternate between red and white.
	 * The ball is built by stacking latitudinal circles.  Each circle is composed
	 * of a widely-separated set of points, so that each facet is noticably large.
	 *****************************************************************************/
	static void DrawBoingBall( )
	{
	   float lon_deg;     /* degree of longitude */
	   double dt_total, dt2;

	   glPushMatrix();
	   glMatrixMode( GL_MODELVIEW );

	  /*
	   * Another relative Z translation to separate objects.
	   */
	   glTranslatef( 0.0f, 0.0f, DIST_BALL );

	   /* Update ball position and rotation (iterate if necessary) */
	   dt_total = dt;
	   while( dt_total > 0.0 )
	   {
	       dt2 = dt_total > MAX_DELTA_T ? MAX_DELTA_T : dt_total;
	       dt_total -= dt2;
	       BounceBall( dt2 );
	       deg_rot_y = TruncateDeg( deg_rot_y + deg_rot_y_inc*((float)dt2*ANIMATION_SPEED) );
	   }

	   /* Set ball position */
	   glTranslatef( ball_x, ball_y, 0.0f );

	  /*
	   * Offset the shadow.
	   */
	   if ( drawBallHow == DRAW_BALL_SHADOW )
	   {
	      glTranslatef( SHADOW_OFFSET_X,
	                    SHADOW_OFFSET_Y,
	                    SHADOW_OFFSET_Z );
	   }

	  /*
	   * Tilt the ball.
	   */
	   glRotatef( -20.0f, 0.0f, 0.0f, 1.0f );

	  /*
	   * Continually rotate ball around Y axis.
	   */
	   glRotatef( deg_rot_y, 0.0f, 1.0f, 0.0f );

	  /*
	   * Set OpenGL state for Boing ball.
	   */
	   glCullFace( GL_FRONT );
	   glEnable( GL_CULL_FACE );
	   glEnable( GL_NORMALIZE );

	  /*
	   * Build a faceted latitude slice of the Boing ball,
	   * stepping same-sized vertical bands of the sphere.
	   */
	   for ( lon_deg = 0;
	         lon_deg < 180;
	         lon_deg += STEP_LONGITUDE )
	   {
	     /*
	      * Draw a latitude circle at this longitude.
	      */
	      DrawBoingBallBand( lon_deg,
	                         lon_deg + STEP_LONGITUDE );
	   }

	   glPopMatrix();

	   return;
	}


	/*****************************************************************************
	 * Bounce the ball.
	 *****************************************************************************/
	static void BounceBall( double delta_t )
	{
	   float sign;
	   float deg;

	   if ( override_pos )
	     return;

	   /* Bounce on walls */
	   if ( ball_x >  (BOUNCE_WIDTH/2 + WALL_R_OFFSET ) )
	   {
	      ball_x_inc = -0.5f - 0.75f * (float)rand() / (float)RAND_MAX;
	      deg_rot_y_inc = -deg_rot_y_inc;
	   }
	   if ( ball_x < -(BOUNCE_HEIGHT/2 + WALL_L_OFFSET) )
	   {
	      ball_x_inc =  0.5f + 0.75f * (float)rand() / (float)RAND_MAX;
	      deg_rot_y_inc = -deg_rot_y_inc;
	   }

	   /* Bounce on floor / roof */
	   if ( ball_y >  BOUNCE_HEIGHT/2      )
	   {
	      ball_y_inc = -0.75f - 1.f * (float)rand() / (float)RAND_MAX;
	   }
	   if ( ball_y < -BOUNCE_HEIGHT/2*0.85 )
	   {
	      ball_y_inc =  0.75f + 1.f * (float)rand() / (float)RAND_MAX;
	   }

	   /* Update ball position */
	   ball_x += ball_x_inc * ((float)delta_t*ANIMATION_SPEED);
	   ball_y += ball_y_inc * ((float)delta_t*ANIMATION_SPEED);

	  /*
	   * Simulate the effects of gravity on Y movement.
	   */
	   if ( ball_y_inc < 0 ) sign = -1; else sign = 1;

	   deg = (ball_y + BOUNCE_HEIGHT/2) * 90 / BOUNCE_HEIGHT;
	   if ( deg > 80 ) deg = 80;
	   if ( deg < 10 ) deg = 10;

	   ball_y_inc = sign * 4.f * (float) sin_deg( deg );
	}


	 static boolean colorToggle = false;
	/*****************************************************************************
	 * Draw a faceted latitude band of the Boing ball.
	 *
	 * Parms:   long_lo, long_hi
	 *          Low and high longitudes of slice, resp.
	 *****************************************************************************/
	static void DrawBoingBallBand( float long_lo,
	                        float long_hi )
	{
	   Vertex vert_ne = new Vertex();  /* "ne" means south-east, so on */
	   Vertex vert_nw = new Vertex();
	   Vertex vert_sw = new Vertex();
	   Vertex vert_se = new Vertex();
	   Vertex vert_norm = new Vertex();
	   float  lat_deg;

	  /*
	   * Iterate thru the points of a latitude circle.
	   * A latitude circle is a 2D set of X,Z points.
	   */
	   for ( lat_deg = 0;
	         lat_deg <= (360 - STEP_LATITUDE);
	         lat_deg += STEP_LATITUDE )
	   {
	     /*
	      * Color this polygon with red or white.
	      */
	      if ( colorToggle )
	         glColor3f( 0.8f, 0.1f, 0.1f );
	      else
	         glColor3f( 0.95f, 0.95f, 0.95f );
//	#if 0
//	      if ( lat_deg >= 180 )
//	         if ( colorToggle )
//	            glColor3f( 0.1f, 0.8f, 0.1f );
//	         else
//	            glColor3f( 0.5f, 0.5f, 0.95f );
//	#endif
	      colorToggle = ! colorToggle;

	     /*
	      * Change color if drawing shadow.
	      */
	      if ( drawBallHow == DRAW_BALL_SHADOW )
	         glColor3f( 0.35f, 0.35f, 0.35f );

	     /*
	      * Assign each Y.
	      */
	      vert_ne.y = vert_nw.y = (float) cos_deg(long_hi) * RADIUS;
	      vert_sw.y = vert_se.y = (float) cos_deg(long_lo) * RADIUS;

	     /*
	      * Assign each X,Z with sin,cos values scaled by latitude radius indexed by longitude.
	      * Eg, long=0 and long=180 are at the poles, so zero scale is sin(longitude),
	      * while long=90 (sin(90)=1) is at equator.
	      */
	      vert_ne.x = (float) cos_deg( lat_deg                 ) * (RADIUS * (float) sin_deg( long_lo + STEP_LONGITUDE ));
	      vert_se.x = (float) cos_deg( lat_deg                 ) * (RADIUS * (float) sin_deg( long_lo                  ));
	      vert_nw.x = (float) cos_deg( lat_deg + STEP_LATITUDE ) * (RADIUS * (float) sin_deg( long_lo + STEP_LONGITUDE ));
	      vert_sw.x = (float) cos_deg( lat_deg + STEP_LATITUDE ) * (RADIUS * (float) sin_deg( long_lo                  ));

	      vert_ne.z = (float) sin_deg( lat_deg                 ) * (RADIUS * (float) sin_deg( long_lo + STEP_LONGITUDE ));
	      vert_se.z = (float) sin_deg( lat_deg                 ) * (RADIUS * (float) sin_deg( long_lo                  ));
	      vert_nw.z = (float) sin_deg( lat_deg + STEP_LATITUDE ) * (RADIUS * (float) sin_deg( long_lo + STEP_LONGITUDE ));
	      vert_sw.z = (float) sin_deg( lat_deg + STEP_LATITUDE ) * (RADIUS * (float) sin_deg( long_lo                  ));

	     /*
	      * Draw the facet.
	      */
	      glBegin( GL_POLYGON );

	      crossProduct( vert_ne, vert_nw, vert_sw, vert_norm );
	      glNormal3f( vert_norm.x, vert_norm.y, vert_norm.z );

	      glVertex3f( vert_ne.x, vert_ne.y, vert_ne.z );
	      glVertex3f( vert_nw.x, vert_nw.y, vert_nw.z );
	      glVertex3f( vert_sw.x, vert_sw.y, vert_sw.z );
	      glVertex3f( vert_se.x, vert_se.y, vert_se.z );

	      glEnd();

//	#if BOING_DEBUG
	if(BOING_DEBUG)
	{
	      printf( "----------------------------------------------------------- \n" );
	      printf( "lat = %f  long_lo = %f  long_hi = %f \n", lat_deg, long_lo, long_hi );
	      printf( "vert_ne  x = %.8f  y = %.8f  z = %.8f \n", vert_ne.x, vert_ne.y, vert_ne.z );
	      printf( "vert_nw  x = %.8f  y = %.8f  z = %.8f \n", vert_nw.x, vert_nw.y, vert_nw.z );
	      printf( "vert_se  x = %.8f  y = %.8f  z = %.8f \n", vert_se.x, vert_se.y, vert_se.z );
	      printf( "vert_sw  x = %.8f  y = %.8f  z = %.8f \n", vert_sw.x, vert_sw.y, vert_sw.z );
	}
//	#endif

	   }

	  /*
	   * Toggle color so that next band will opposite red/white colors than this one.
	   */
	   colorToggle = ! colorToggle;

	  /*
	   * This circular band is done.
	   */
	   return;
	}


	/*****************************************************************************
	 * Draw the purple grid of lines, behind the Boing ball.
	 * When the Workbench is dropped to the bottom, Boing shows 12 rows.
	 *****************************************************************************/
	static void DrawGrid()
	{
	   int              row, col;
	   final int        rowTotal    = 12;                   /* must be divisible by 2 */
	   final int        colTotal    = rowTotal;             /* must be same as rowTotal */
	   final float    widthLine   = 2.0f;                  /* should be divisible by 2 */
	   final float    sizeCell    = GRID_SIZE / rowTotal;
	   final float    z_offset    = -40.0f;
	   float          xl, xr;
	   float          yt, yb;

	   glPushMatrix();
	   glDisable( GL_CULL_FACE );

	  /*
	   * Another relative Z translation to separate objects.
	   */
	   glTranslatef( 0, 0, DIST_BALL );

	  /*
	   * Draw vertical lines (as skinny 3D rectangles).
	   */
	   for ( col = 0; col <= colTotal; col++ )
	   {
	     /*
	      * Compute co-ords of line.
	      */
	      xl = -GRID_SIZE / 2 + col * sizeCell;
	      xr = xl + widthLine;

	      yt =  GRID_SIZE / 2;
	      yb = -GRID_SIZE / 2 - widthLine;

	      glBegin( GL_POLYGON );

	      glColor3f( 0.6f, 0.1f, 0.6f );               /* purple */

	      glVertex3f( xr, yt, z_offset );       /* NE */
	      glVertex3f( xl, yt, z_offset );       /* NW */
	      glVertex3f( xl, yb, z_offset );       /* SW */
	      glVertex3f( xr, yb, z_offset );       /* SE */

	      glEnd();
	   }

	  /*
	   * Draw horizontal lines (as skinny 3D rectangles).
	   */
	   for ( row = 0; row <= rowTotal; row++ )
	   {
	     /*
	      * Compute co-ords of line.
	      */
	      yt = GRID_SIZE / 2 - row * sizeCell;
	      yb = yt - widthLine;

	      xl = -GRID_SIZE / 2;
	      xr =  GRID_SIZE / 2 + widthLine;

	      glBegin( GL_POLYGON );

	      glColor3f( 0.6f, 0.1f, 0.6f );               /* purple */

	      glVertex3f( xr, yt, z_offset );       /* NE */
	      glVertex3f( xl, yt, z_offset );       /* NW */
	      glVertex3f( xl, yb, z_offset );       /* SW */
	      glVertex3f( xr, yb, z_offset );       /* SE */

	      glEnd();
	   }

	   glPopMatrix();

	   return;
	}


	/*======================================================================*
	 * main()
	 *======================================================================*/

//	int main( void )
	public static void main(String[] args)
	{
	   long window;

	   /* Init GLFW */
	   if( glfwInit() == 0 )
	      exit( EXIT_FAILURE );

	   glfwWindowHint(GLFW_DEPTH_BITS, 16);

	   window = glfwCreateWindow( 400, 400, "Boing (classic Amiga demo)", NULL, NULL );
	   if (window == 0)
	   {
	       glfwTerminate();
	       exit( EXIT_FAILURE );
	   }

	   glfwSetFramebufferSizeCallback(window, safe(new GLFWFramebufferSizeCallback() {
		public void invoke(long window, int width, int height) {
			reshape(window, width, height);
		}
	   }));
	   glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
		public void invoke(long window, int key, int scancode, int action, int mods) {
			key_callback(window, key, scancode, action, mods);
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

	   glfwMakeContextCurrent(window);
//	   GLContext.createFromCurrent();
	   GL.createCapabilities();
	   glfwSwapInterval( 1 );

	   glfwGetFramebufferSize(window, x_buf, y_buf);
	   width = x_buf.get(0); height = y_buf.get(0);
	   reshape(window, width, height);

	   glfwSetTime( 0.0 );

	   init();

	   /* Main loop */
	   for (;;)
	   {
	       /* Timing */
	       t = glfwGetTime();
	       dt = t - t_old;
	       t_old = t;

	       /* Draw one frame */
	       display();

	       /* Swap buffers */
	       glfwSwapBuffers(window);
	       glfwPollEvents();

	       /* Check if we are still running */
	       if (glfwWindowShouldClose(window) != 0)
	           break;
	   }

	   glfwTerminate();
	   exit( EXIT_SUCCESS );
	}
	
	private static final class Vertex{
		float x, y, z;
	}
}
