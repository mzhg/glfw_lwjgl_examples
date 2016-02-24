package glfw.lwjgl.examples;

//========================================================================
//A simple particle engine with threaded physics
//Copyright (c) Marcus Geelnard
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
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

import glfw.lwjgl.tests.TestCommon;

final class Particles extends TestCommon{
	
	static boolean USE_MULTI_THREAD = true;
	
	static final int GL_LIGHT_MODEL_COLOR_CONTROL_EXT = 0x81F8;
	static final int GL_SINGLE_COLOR_EXT = 0x81F9;
	static final int GL_SEPARATE_SPECULAR_COLOR_EXT = 0x81FA;
	//========================================================================
	// = These are fixed constants that control the particle engine. In a
	// modular world, these values should be variables...
	//========================================================================

	// Maximum number of particles;
	public static final int MAX_PARTICLES = 3000;

	// Life span of a particle (in seconds);
	public static final float LIFE_SPAN = 8.f;

	// A new particle is born every [BIRTH_INTERVAL] second;
	public static final float BIRTH_INTERVAL = (LIFE_SPAN/(float)MAX_PARTICLES);

	// Particle size (meters);
	public static final float PARTICLE_SIZE = 0.7f;

	// Gravitational constant (m/s^2);
	public static final float GRAVITY = 9.8f;

	// Base initial velocity (m/s);
	public static final float VELOCITY = 8.f;

	// Bounce friction (1.0 = no friction, 0.0 = maximum friction);
	public static final float FRICTION = 0.75f;

	// "Fountain" height (m);
	public static final float FOUNTAIN_HEIGHT = 3.f;

	// Fountain radius (m);
	public static final float FOUNTAIN_RADIUS = 1.6f;

	// Minimum delta-time for particle phisics (s);
	public static final float MIN_DELTA_T = (BIRTH_INTERVAL * 0.5f);
	
	//========================================================================
	// Texture declarations (we hard-code them into the source code, since
	// they are so simple)
	//========================================================================

	static final int P_TEX_WIDTH = 8;    // Particle texture dimensions
	static final int P_TEX_HEIGHT= 8;
	static final int F_TEX_WIDTH = 16;   // Floor texture dimensions
	static final int F_TEX_HEIGHT= 16;
	
	static final int BATCH_PARTICLES = 70;  // Number of particles to draw in each batch
    // (70 corresponds to 7.5 KB = will not blow
    // the L1 data cache on most CPUs)
	static final int PARTICLE_VERTS  =4;   // Number of vertices per particle
	
	//========================================================================
	// Program control global variables
	//========================================================================

	// Window dimensions
	static float aspect_ratio;

	// "wireframe" flag (true if we use wireframe view)
	static boolean wireframe;
	
	// Texture object IDs
	static int particle_tex_id, floor_tex_id;
	
	// Particle texture (a simple spot)
	static final short particle_texture[ /*P_TEX_WIDTH * P_TEX_HEIGHT*/ ] = {
	    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	    0x00, 0x00, 0x11, 0x22, 0x22, 0x11, 0x00, 0x00,
	    0x00, 0x11, 0x33, 0x88, 0x77, 0x33, 0x11, 0x00,
	    0x00, 0x22, 0x88, 0xff, 0xee, 0x77, 0x22, 0x00,
	    0x00, 0x22, 0x77, 0xee, 0xff, 0x88, 0x22, 0x00,
	    0x00, 0x11, 0x33, 0x77, 0x88, 0x33, 0x11, 0x00,
	    0x00, 0x00, 0x11, 0x33, 0x22, 0x11, 0x00, 0x00,
	    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
	};

	// Floor texture (your basic checkered floor)
	static final short floor_texture[ /*F_TEX_WIDTH * F_TEX_HEIGHT*/ ] = {
	    0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
	    0xff, 0xf0, 0xcc, 0xf0, 0xf0, 0xf0, 0xff, 0xf0, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
	    0xf0, 0xcc, 0xee, 0xff, 0xf0, 0xf0, 0xf0, 0xf0, 0x30, 0x66, 0x30, 0x30, 0x30, 0x20, 0x30, 0x30,
	    0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xee, 0xf0, 0xf0, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
	    0xf0, 0xf0, 0xf0, 0xf0, 0xcc, 0xf0, 0xf0, 0xf0, 0x30, 0x30, 0x55, 0x30, 0x30, 0x44, 0x30, 0x30,
	    0xf0, 0xdd, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0x33, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
	    0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xff, 0xf0, 0xf0, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x60, 0x30,
	    0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0x33, 0x33, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
	    0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x33, 0x30, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0,
	    0x30, 0x30, 0x30, 0x30, 0x30, 0x20, 0x30, 0x30, 0xf0, 0xff, 0xf0, 0xf0, 0xdd, 0xf0, 0xf0, 0xff,
	    0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x55, 0x33, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xff, 0xf0, 0xf0,
	    0x30, 0x44, 0x66, 0x30, 0x30, 0x30, 0x30, 0x30, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0,
	    0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0xf0, 0xf0, 0xf0, 0xaa, 0xf0, 0xf0, 0xcc, 0xf0,
	    0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0xff, 0xf0, 0xf0, 0xf0, 0xff, 0xf0, 0xdd, 0xf0,
	    0x30, 0x30, 0x30, 0x77, 0x30, 0x30, 0x30, 0x30, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0,
	    0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0,
	};
	
	// Global vectors holding all particles. We use two vectors for double
	// buffering.
	static final Particle[] particles = new Particle[MAX_PARTICLES];
	static final Vertex[] vertex_array = new Vertex[BATCH_PARTICLES * PARTICLE_VERTS];

	// Global variable holding the age of the youngest particle
	static float min_age;

	// Color of latest born particle (used for fountain lighting)
	static final float[] glow_color = new float[4];

	// Position of latest born particle (used for fountain lighting)
	static final float[] glow_pos = new float[4];
	
	static final Object particles_lock = new Object();

	//========================================================================
	// Object material and fog configuration constants
	//========================================================================

	static final float fountain_diffuse[]  = { 0.7f, 1.f,  1.f,  1.f };
	static final float fountain_specular[] = {  1.f, 1.f,  1.f,  1.f };
	static final float fountain_shininess   = 12.f;
	static final float floor_diffuse[]     = { 1.f,  0.6f, 0.6f, 1.f };
	static final float floor_specular[]    = { 0.6f, 0.6f, 0.6f, 1.f };
	static final float floor_shininess      = 18.f;
	static final float fog_color[]         = { 0.1f, 0.1f, 0.1f, 1.f };
	
	static ByteBuffer nativeBuffer;
	static FloatBuffer floatBuffer;
	static IntBuffer intBuffer;
	
	static {
		allocate(10 * 1024 * 1024);  // 10MB
		for(int i= 0; i < particles.length; i++)
			particles[i] = new Particle();
		
		for(int i = 0; i < vertex_array.length; i++)
			vertex_array[i] = new Vertex();
	}
	
	static void allocate(int size){
		nativeBuffer = BufferUtils.createUnalignedByteBuffer(size);
		floatBuffer = nativeBuffer.asFloatBuffer();
		intBuffer = nativeBuffer.asIntBuffer();
	}
	
	//========================================================================
	// Print usage information
	//========================================================================

	static void usage()
	{
	    printf("Usage: particles [-bfhs]\n");
	    printf("Options:\n");
	    printf(" -f   Run in full screen\n");
	    printf(" -h   Display this help\n");
	    printf(" -s   Run program as single thread (default is to use two threads)\n");
	    printf("\n");
	    printf("Program runtime controls:\n");
	    printf(" W    Toggle wireframe mode\n");
	    printf(" Esc  Exit program\n");
	}


	//========================================================================
	// Initialize a new particle
	//========================================================================
	static void init_particle(Particle p, double t)
	{
	    float xy_angle, velocity;

	    // Start position of particle is at the fountain blow-out
	    p.x = 0.f;
	    p.y = 0.f;
	    p.z = FOUNTAIN_HEIGHT;

	    // Start velocity is up (Z)...
	    p.vz = 0.7f + (0.3f / 4096.f) * (float) (rand() & 4095);

	    // ...and a randomly chosen X/Y direction
	    xy_angle = (2.f * (float) Math.PI / 4096.f) * (float) (rand() & 4095);
	    p.vx = 0.4f * (float) cos(xy_angle);
	    p.vy = 0.4f * (float) sin(xy_angle);

	    // Scale velocity vector according to a time-varying velocity
	    velocity = VELOCITY * (0.8f + 0.1f * (float) (sin(0.5 * t) + sin(1.31 * t)));
	    p.vx *= velocity;
	    p.vy *= velocity;
	    p.vz *= velocity;

	    // Color is time-varying
	    p.r = 0.7f + 0.3f * (float) sin(0.34 * t + 0.1);
	    p.g = 0.6f + 0.4f * (float) sin(0.63 * t + 1.1);
	    p.b = 0.6f + 0.4f * (float) sin(0.91 * t + 2.1);

	    // Store settings for fountain glow lighting
	    glow_pos[0] = 0.4f * (float) sin(1.34 * t);
	    glow_pos[1] = 0.4f * (float) sin(3.11 * t);
	    glow_pos[2] = FOUNTAIN_HEIGHT + 1.f;
	    glow_pos[3] = 1.f;
	    glow_color[0] = p.r;
	    glow_color[1] = p.g;
	    glow_color[2] = p.b;
	    glow_color[3] = 1.f;

	    // The particle is new-born and active
	    p.life = 1.f;
	    p.active = true;
	}


	//========================================================================
	// Update a particle
	//========================================================================
	static final float FOUNTAIN_R2 = (FOUNTAIN_RADIUS+PARTICLE_SIZE/2)*(FOUNTAIN_RADIUS+PARTICLE_SIZE/2);

	static void update_particle(Particle p, float dt)
	{
	    // If the particle is not active, we need not do anything
	    if (!p.active)
	        return;

	    // The particle is getting older...
	    p.life -= dt * (1.f / LIFE_SPAN);

	    // Did the particle die?
	    if (p.life <= 0.f)
	    {
	        p.active = false;
	        return;
	    }

	    // Apply gravity
	    p.vz = p.vz - GRAVITY * dt;

	    // Update particle position
	    p.x = p.x + p.vx * dt;
	    p.y = p.y + p.vy * dt;
	    p.z = p.z + p.vz * dt;

	    // Simple collision detection + response
	    if (p.vz < 0.f)
	    {
	        // Particles should bounce on the fountain (with friction)
	        if ((p.x * p.x + p.y * p.y) < FOUNTAIN_R2 &&
	            p.z < (FOUNTAIN_HEIGHT + PARTICLE_SIZE / 2))
	        {
	            p.vz = -FRICTION * p.vz;
	            p.z  = FOUNTAIN_HEIGHT + PARTICLE_SIZE / 2 +
	                    FRICTION * (FOUNTAIN_HEIGHT +
	                    PARTICLE_SIZE / 2 - p.z);
	        }

	        // Particles should bounce on the floor (with friction)
	        else if (p.z < PARTICLE_SIZE / 2)
	        {
	            p.vz = -FRICTION * p.vz;
	            p.z  = PARTICLE_SIZE / 2 +
	                    FRICTION * (PARTICLE_SIZE / 2 - p.z);
	        }
	    }
	}


	//========================================================================
	// The main frame for the particle engine. Called once per frame.
	//========================================================================

	static void particle_engine(double t, float dt)
	{
	    int i;
	    float dt2;

	    // Update particles (iterated several times per frame if dt is too large)
	    while (dt > 0.f)
	    {
	        // Calculate delta time for this iteration
	        dt2 = dt < MIN_DELTA_T ? dt : MIN_DELTA_T;

	        for (i = 0;  i < MAX_PARTICLES;  i++)
	            update_particle(particles[i], dt2);

	        min_age += dt2;

	        // Should we create any new particle(s)?
	        while (min_age >= BIRTH_INTERVAL)
	        {
	            min_age -= BIRTH_INTERVAL;

	            // Find a dead particle to replace with a new one
	            for (i = 0;  i < MAX_PARTICLES;  i++)
	            {
	                if (!particles[i].active)
	                {
	                    init_particle(particles[i], t + min_age);
	                    update_particle(particles[i], min_age);
	                    break;
	                }
	            }
	        }

	        dt -= dt2;
	    }
	}


	//========================================================================
	// Draw all active particles. We use OpenGL 1.1 vertex
	// arrays for this in order to accelerate the drawing.
	//========================================================================

	static final float[] matrix = new float[16];
	static final ThreadSync thread_sync = new ThreadSync();
	
	static ByteBuffer wrap(Vertex[] array){
		int size = array.length * 6 * 4;
		if(size > nativeBuffer.capacity()){
			allocate(size);
		}
		
		nativeBuffer.clear();
		for(Vertex v : array){
			nativeBuffer.putFloat(v.s).putFloat(v.t);
			nativeBuffer.putInt(v.rgba);
			nativeBuffer.putFloat(v.x).putFloat(v.y).putFloat(v.z);
		}
		nativeBuffer.flip();
		return nativeBuffer;
	}
	
	static FloatBuffer wrap(float[] array){
		floatBuffer.clear();
		floatBuffer.put(array).flip();
		return floatBuffer;
	}
	
	static ByteBuffer wrap(short[] array){
		nativeBuffer.clear();
		for(short s : array){
			nativeBuffer.put((byte)s);
		}
		nativeBuffer.flip();
		return nativeBuffer;
	}
	
	static void draw_particles(long window, double t, float dt)
	{
	    int i, particle_count;
//	    Vertex vertex_array[BATCH_PARTICLES * PARTICLE_VERTS];
	    Vertex vptr;
	    float alpha;
	    int rgba;
	    final Vec3 quad_lower_left = new Vec3(), quad_lower_right = new Vec3();
	    float[] mat = matrix;
	    Particle pptr;
	    int vIndex = 0;

	    // Here comes the real trick with flat single primitive objects (s.c.
	    // "billboards"): We must rotate the textured primitive so that it
	    // always faces the viewer (is coplanar with the view-plane).
	    // We:
	    //   1) Create the primitive around origo (0,0,0)
	    //   2) Rotate it so that it is coplanar with the view plane
	    //   3) Translate it according to the particle position
	    // Note that 1) and 2) is the same for all particles (done only once).

	    // Get modelview matrix. We will only use the upper left 3x3 part of
	    // the matrix, which represents the rotation.
	    floatBuffer.clear();
	    glGetFloatv(GL_MODELVIEW_MATRIX, floatBuffer);
	    floatBuffer.get(mat);

	    // 1) & 2) We do it in one swift step:
	    // Although not obvious, the following six lines represent two matrix/
	    // vector multiplications. The matrix is the inverse 3x3 rotation
	    // matrix (i.e. the transpose of the same matrix), and the two vectors
	    // represent the lower left corner of the quad, PARTICLE_SIZE/2 *
	    // (-1,-1,0), and the lower right corner, PARTICLE_SIZE/2 * (1,-1,0).
	    // The upper left/right corners of the quad is always the negative of
	    // the opposite corners (regardless of rotation).
	    quad_lower_left.x = (-PARTICLE_SIZE / 2) * (mat[0] + mat[1]);
	    quad_lower_left.y = (-PARTICLE_SIZE / 2) * (mat[4] + mat[5]);
	    quad_lower_left.z = (-PARTICLE_SIZE / 2) * (mat[8] + mat[9]);
	    quad_lower_right.x = (PARTICLE_SIZE / 2) * (mat[0] - mat[1]);
	    quad_lower_right.y = (PARTICLE_SIZE / 2) * (mat[4] - mat[5]);
	    quad_lower_right.z = (PARTICLE_SIZE / 2) * (mat[8] - mat[9]);

	    // Don't update z-buffer, since all particles are transparent!
	    glDepthMask(false);

	    glEnable(GL_BLEND);
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE);

	    // Select particle texture
	    if (!wireframe)
	    {
	        glEnable(GL_TEXTURE_2D);
	        glBindTexture(GL_TEXTURE_2D, particle_tex_id);
	    }

	    if(!USE_MULTI_THREAD){
	    	particle_engine(t, dt);
	    }
	    	

	    // Wait for particle physics thread to be done
	    synchronized (particles_lock) {
//	    mtx_lock(&thread_sync.particles_lock);
//	    while (glfwWindowShouldClose(window) != 0 &&
//	            thread_sync.p_frame <= thread_sync.d_frame)
//	    {
//	        struct timespec ts;
//	        clock_gettime(CLOCK_REALTIME, &ts);
//	        ts.tv_nsec += 100000000;
//	        cnd_timedwait(&thread_sync.p_done, &thread_sync.particles_lock, &ts);
//	    }

	    // Store the frame time and delta time for the physics thread
	    thread_sync.t = t;
	    thread_sync.dt = dt;

	    // Update frame counter
	    thread_sync.d_frame++;

	    // Loop through all particles and build vertex arrays.
	    particle_count = 0;

	    for (i = 0;  i < MAX_PARTICLES;  i++)
	    {
	    	pptr = particles[i];
	        if (pptr.active)
	        {
	            // Calculate particle intensity (we set it to max during 75%
	            // of its life, then it fades out)
	            alpha =  4.f * pptr.life;
	            if (alpha > 1.f)
	                alpha = 1.f;

	            // Convert color from float to 8-bit (store it in a 32-bit
	            // integer using endian independent type casting)
//	            ((GLubyte*) &rgba)[0] = (GLubyte)(pptr.r * 255.f);
//	            ((GLubyte*) &rgba)[1] = (GLubyte)(pptr.g * 255.f);
//	            ((GLubyte*) &rgba)[2] = (GLubyte)(pptr.b * 255.f);
//	            ((GLubyte*) &rgba)[3] = (GLubyte)(alpha * 255.f);
	            int r = (int)(pptr.r * 255);
	            int g = (int)(pptr.g * 255);
	            int b = (int)(pptr.b * 255);
	            int a = (int)(alpha * 255);
	            
	            rgba = r | (g << 8) | (b << 16)| (a << 24);

	            // 3) Translate the quad to the correct position in modelview
	            // space and store its parameters in vertex arrays (we also
	            // store texture coord and color information for each vertex).

	            // Lower left corner
	            vptr = vertex_array[vIndex++];
	            vptr.s    = 0.f;
	            vptr.t    = 0.f;
	            vptr.rgba = rgba;
	            vptr.x    = pptr.x + quad_lower_left.x;
	            vptr.y    = pptr.y + quad_lower_left.y;
	            vptr.z    = pptr.z + quad_lower_left.z;

	            // Lower right corner
	            vptr = vertex_array[vIndex++];
	            vptr.s    = 1.f;
	            vptr.t    = 0.f;
	            vptr.rgba = rgba;
	            vptr.x    = pptr.x + quad_lower_right.x;
	            vptr.y    = pptr.y + quad_lower_right.y;
	            vptr.z    = pptr.z + quad_lower_right.z;

	            // Upper right corner
	            vptr = vertex_array[vIndex++];
	            vptr.s    = 1.f;
	            vptr.t    = 1.f;
	            vptr.rgba = rgba;
	            vptr.x    = pptr.x - quad_lower_left.x;
	            vptr.y    = pptr.y - quad_lower_left.y;
	            vptr.z    = pptr.z - quad_lower_left.z;

	            // Upper left corner
	            vptr = vertex_array[vIndex++];
	            vptr.s    = 0.f;
	            vptr.t    = 1.f;
	            vptr.rgba = rgba;
	            vptr.x    = pptr.x - quad_lower_right.x;
	            vptr.y    = pptr.y - quad_lower_right.y;
	            vptr.z    = pptr.z - quad_lower_right.z;

	            // Increase count of drawable particles
	            particle_count ++;
	        }

	        // If we have filled up one batch of particles, draw it as a set
	        // of quads using glDrawArrays.
	        if (particle_count >= BATCH_PARTICLES)
	        {
	        	// Set up vertex arrays. We use interleaved arrays, which is easier to
	    	    // handle (in most situations) and it gives a linear memeory access
	    	    // access pattern (which may give better performance in some
	    	    // situations). GL_T2F_C4UB_V3F means: 2 floats for texture coords,
	    	    // 4 ubytes for color and 3 floats for vertex coord (in that order).
	    	    // Most OpenGL cards / drivers are optimized for this format.
	    	    glInterleavedArrays(GL_T2F_C4UB_V3F, 0, wrap(vertex_array));
	            // The first argument tells which primitive type we use (QUAD)
	            // The second argument tells the index of the first vertex (0)
	            // The last argument is the vertex count
	            glDrawArrays(GL_QUADS, 0, PARTICLE_VERTS * particle_count);
	            particle_count = 0;
	            vIndex = 0;
//	            vptr = vertex_array[vIndex++];
	        }

	    }

	    // We are done with the particle data
//	    mtx_unlock(&thread_sync.particles_lock);
//	    cnd_signal(&thread_sync.d_done);
	    
	    particles_lock.notify();
	    }

	 // Set up vertex arrays. We use interleaved arrays, which is easier to
	    // handle (in most situations) and it gives a linear memeory access
	    // access pattern (which may give better performance in some
	    // situations). GL_T2F_C4UB_V3F means: 2 floats for texture coords,
	    // 4 ubytes for color and 3 floats for vertex coord (in that order).
	    // Most OpenGL cards / drivers are optimized for this format.
	    glInterleavedArrays(GL_T2F_C4UB_V3F, 0, wrap(vertex_array));
	    // Draw final batch of particles (if any)
	    glDrawArrays(GL_QUADS, 0, PARTICLE_VERTS * particle_count);

	    // Disable vertex arrays (Note: glInterleavedArrays implicitly called
	    // glEnableClientState for vertex, texture coord and color arrays)
	    glDisableClientState(GL_VERTEX_ARRAY);
	    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
	    glDisableClientState(GL_COLOR_ARRAY);

	    glDisable(GL_TEXTURE_2D);
	    glDisable(GL_BLEND);

	    glDepthMask(true);
	}


	//========================================================================
	// Fountain geometry specification
	//========================================================================

	static final int FOUNTAIN_SIDE_POINTS = 14;
	static final int FOUNTAIN_SWEEP_STEPS = 32;

	static final float fountain_side[/*FOUNTAIN_SIDE_POINTS * 2*/] =
	{
	    1.2f, 0.f,  1.f, 0.2f,  0.41f, 0.3f, 0.4f, 0.35f,
	    0.4f, 1.95f, 0.41f, 2.f, 0.8f, 2.2f,  1.2f, 2.4f,
	    1.5f, 2.7f,  1.55f,2.95f, 1.6f, 3.f,  1.f, 3.f,
	    0.5f, 3.f,  0.f, 3.f
	};

	static final float fountain_normal[/*FOUNTAIN_SIDE_POINTS * 2*/] =
	{
	    1.0000f, 0.0000f,  0.6428f, 0.7660f,  0.3420f, 0.9397f,  1.0000f, 0.0000f,
	    1.0000f, 0.0000f,  0.3420f,-0.9397f,  0.4226f,-0.9063f,  0.5000f,-0.8660f,
	    0.7660f,-0.6428f,  0.9063f,-0.4226f,  0.0000f,1.00000f,  0.0000f,1.00000f,
	    0.0000f,1.00000f,  0.0000f,1.00000f
	};


	//========================================================================
	// Draw a fountain
	//========================================================================
	static int fountain_list = 0;
	static void draw_fountain()
	{
	    double angle;
	    float  x, y;
	    int m, n;

	    // The first time, we build the fountain display list
	    if (fountain_list == 0)
	    {
	        fountain_list = glGenLists(1);
	        glNewList(fountain_list, GL_COMPILE_AND_EXECUTE);

	        glMaterialfv(GL_FRONT, GL_DIFFUSE, wrap(fountain_diffuse));
	        glMaterialfv(GL_FRONT, GL_SPECULAR, wrap(fountain_specular));
	        glMaterialf(GL_FRONT, GL_SHININESS, fountain_shininess);

	        // Build fountain using triangle strips
	        for (n = 0;  n < FOUNTAIN_SIDE_POINTS - 1;  n++)
	        {
	            glBegin(GL_TRIANGLE_STRIP);
	            for (m = 0;  m <= FOUNTAIN_SWEEP_STEPS;  m++)
	            {
	                angle = (double) m * (2.0 * Math.PI / (double) FOUNTAIN_SWEEP_STEPS);
	                x = (float) cos(angle);
	                y = (float) sin(angle);

	                // Draw triangle strip
	                glNormal3f(x * fountain_normal[n * 2 + 2],
	                           y * fountain_normal[n * 2 + 2],
	                           fountain_normal[n * 2 + 3]);
	                glVertex3f(x * fountain_side[n * 2 + 2],
	                           y * fountain_side[n * 2 + 2],
	                           fountain_side[n * 2 +3 ]);
	                glNormal3f(x * fountain_normal[n * 2],
	                           y * fountain_normal[n * 2],
	                           fountain_normal[n * 2 + 1]);
	                glVertex3f(x * fountain_side[n * 2],
	                           y * fountain_side[n * 2],
	                           fountain_side[n * 2 + 1]);
	            }

	            glEnd();
	        }

	        glEndList();
	    }
	    else
	        glCallList(fountain_list);
	}


	//========================================================================
	// Recursive function for building variable tesselated floor
	//========================================================================

	static void tessellate_floor(float x1, float y1, float x2, float y2, int depth)
	{
	    float delta, x, y;

	    // Last recursion?
	    if (depth >= 5)
	        delta = 999999.f;
	    else
	    {
	        x = (float) (Math.abs(x1) < Math.abs(x2) ? Math.abs(x1) : Math.abs(x2));
	        y = (float) (Math.abs(y1) < Math.abs(y2) ? Math.abs(y1) : Math.abs(y2));
	        delta = x*x + y*y;
	    }

	    // Recurse further?
	    if (delta < 0.1f)
	    {
	        x = (x1 + x2) * 0.5f;
	        y = (y1 + y2) * 0.5f;
	        tessellate_floor(x1, y1,  x,  y, depth + 1);
	        tessellate_floor(x, y1, x2,  y, depth + 1);
	        tessellate_floor(x1,  y,  x, y2, depth + 1);
	        tessellate_floor(x,  y, x2, y2, depth + 1);
	    }
	    else
	    {
	        glTexCoord2f(x1 * 30.f, y1 * 30.f);
	        glVertex3f(  x1 * 80.f, y1 * 80.f, 0.f);
	        glTexCoord2f(x2 * 30.f, y1 * 30.f);
	        glVertex3f(  x2 * 80.f, y1 * 80.f, 0.f);
	        glTexCoord2f(x2 * 30.f, y2 * 30.f);
	        glVertex3f(  x2 * 80.f, y2 * 80.f, 0.f);
	        glTexCoord2f(x1 * 30.f, y2 * 30.f);
	        glVertex3f(  x1 * 80.f, y2 * 80.f, 0.f);
	    }
	}


	//========================================================================
	// Draw floor. We build the floor recursively and let the tessellation in the
	// center (near x,y=0,0) be high, while the tessellation around the edges be
	// low.
	//========================================================================
	static int floor_list = 0;
	static void draw_floor()
	{
	    if (!wireframe)
	    {
	        glEnable(GL_TEXTURE_2D);
	        glBindTexture(GL_TEXTURE_2D, floor_tex_id);
	    }

	    // The first time, we build the floor display list
	    if (floor_list == 0)
	    {
	        floor_list = glGenLists(1);
	        glNewList(floor_list, GL_COMPILE_AND_EXECUTE);

	        glMaterialfv(GL_FRONT, GL_DIFFUSE, wrap(floor_diffuse));
	        glMaterialfv(GL_FRONT, GL_SPECULAR, wrap(floor_specular));
	        glMaterialf(GL_FRONT, GL_SHININESS, floor_shininess);

	        // Draw floor as a bunch of triangle strips (high tesselation
	        // improves lighting)
	        glNormal3f(0.f, 0.f, 1.f);
	        glBegin(GL_QUADS);
	        tessellate_floor(-1.f, -1.f, 0.f, 0.f, 0);
	        tessellate_floor( 0.f, -1.f, 1.f, 0.f, 0);
	        tessellate_floor( 0.f,  0.f, 1.f, 1.f, 0);
	        tessellate_floor(-1.f,  0.f, 0.f, 1.f, 0);
	        glEnd();

	        glEndList();
	    }
	    else
	        glCallList(floor_list);

	    glDisable(GL_TEXTURE_2D);

	}


	//========================================================================
	// Position and configure light sources
	//========================================================================

	static void setup_lights()
	{
	    float l1pos[] = new float[4], l1amb[] = new float[4], l1dif[] = new float[4], l1spec[] = new float[4];
	    float l2pos[] = new float[4], l2amb[] = new float[4], l2dif[] = new float[4], l2spec[] = new float[4];

	    // Set light source 1 parameters
	    l1pos[0] =  0.f;  l1pos[1] = -9.f; l1pos[2] =   8.f;  l1pos[3] = 1.f;
	    l1amb[0] = 0.2f;  l1amb[1] = 0.2f;  l1amb[2] = 0.2f;  l1amb[3] = 1.f;
	    l1dif[0] = 0.8f;  l1dif[1] = 0.4f;  l1dif[2] = 0.2f;  l1dif[3] = 1.f;
	    l1spec[0] = 1.f; l1spec[1] = 0.6f; l1spec[2] = 0.2f; l1spec[3] = 0.f;

	    // Set light source 2 parameters
	    l2pos[0] =  -15.f; l2pos[1] =  12.f; l2pos[2] = 1.5f; l2pos[3] =  1.f;
	    l2amb[0] =    0.f; l2amb[1] =   0.f; l2amb[2] =  0.f; l2amb[3] =  1.f;
	    l2dif[0] =   0.2f; l2dif[1] =  0.4f; l2dif[2] = 0.8f; l2dif[3] =  1.f;
	    l2spec[0] =  0.2f; l2spec[1] = 0.6f; l2spec[2] = 1.f; l2spec[3] = 0.f;

	    glLightfv(GL_LIGHT1, GL_POSITION, wrap(l1pos));
	    glLightfv(GL_LIGHT1, GL_AMBIENT, wrap(l1amb));
	    glLightfv(GL_LIGHT1, GL_DIFFUSE, wrap(l1dif));
	    glLightfv(GL_LIGHT1, GL_SPECULAR, wrap(l1spec));
	    glLightfv(GL_LIGHT2, GL_POSITION, wrap(l2pos));
	    glLightfv(GL_LIGHT2, GL_AMBIENT, wrap(l2amb));
	    glLightfv(GL_LIGHT2, GL_DIFFUSE, wrap(l2dif));
	    glLightfv(GL_LIGHT2, GL_SPECULAR, wrap(l2spec));
	    glLightfv(GL_LIGHT3, GL_POSITION, wrap(glow_pos));
	    glLightfv(GL_LIGHT3, GL_DIFFUSE, wrap(glow_color));
	    glLightfv(GL_LIGHT3, GL_SPECULAR, wrap(glow_color));

	    glEnable(GL_LIGHT1);
	    glEnable(GL_LIGHT2);
	    glEnable(GL_LIGHT3);
	}


	//========================================================================
	// Main rendering function
	//========================================================================
	static double t_old = 0.0;
	static void draw_scene(long window, double t)
	{
	    double xpos, ypos, zpos, angle_x, angle_y, angle_z;
	    float dt;

	    // Calculate frame-to-frame delta time
	    dt = (float) (t - t_old);
	    t_old = t;

	    glClearColor(0.1f, 0.1f, 0.1f, 1.f);
	    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    gluPerspective(65.0f, aspect_ratio, 1.0f, 60.0f);

	    // Setup camera
	    glMatrixMode(GL_MODELVIEW);
	    glLoadIdentity();

	    // Rotate camera
	    angle_x = 90.0 - 10.0;
	    angle_y = 10.0 * sin(0.3 * t);
	    angle_z = 10.0 * t;
	    glRotated(-angle_x, 1.0, 0.0, 0.0);
	    glRotated(-angle_y, 0.0, 1.0, 0.0);
	    glRotated(-angle_z, 0.0, 0.0, 1.0);

	    // Translate camera
	    xpos =  15.0 * sin((Math.PI / 180.0) * angle_z) +
	             2.0 * sin((Math.PI / 180.0) * 3.1 * t);
	    ypos = -15.0 * cos((Math.PI / 180.0) * angle_z) +
	             2.0 * cos((Math.PI / 180.0) * 2.9 * t);
	    zpos = 4.0 + 2.0 * cos((Math.PI / 180.0) * 4.9 * t);
	    glTranslated(-xpos, -ypos, -zpos);

	    glFrontFace(GL_CCW);
	    glCullFace(GL_BACK);
	    glEnable(GL_CULL_FACE);

	    setup_lights();
	    glEnable(GL_LIGHTING);

	    glEnable(GL_FOG);
	    glFogi(GL_FOG_MODE, GL_EXP);
	    glFogf(GL_FOG_DENSITY, 0.05f);
	    glFogfv(GL_FOG_COLOR, wrap(fog_color));

	    draw_floor();

	    glEnable(GL_DEPTH_TEST);
	    glDepthFunc(GL_LEQUAL);
	    glDepthMask(true);

	    draw_fountain();

	    glDisable(GL_LIGHTING);
	    glDisable(GL_FOG);

	    // Particles must be drawn after all solid objects have been drawn
	    draw_particles(window, t, dt);

	    // Z-buffer not needed anymore
	    glDisable(GL_DEPTH_TEST);
	}


	//========================================================================
	// Window resize callback function
	//========================================================================

	static void resize_callback(long window, int width, int height)
	{
	    glViewport(0, 0, width, height);
	    aspect_ratio = height != 0 ? width / (float) height : 1.f;
	}


	//========================================================================
	// Key callback functions
	//========================================================================

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    if (action == GLFW_PRESS)
	    {
	        switch (key)
	        {
	            case GLFW_KEY_ESCAPE:
	                glfwSetWindowShouldClose(window, GL_TRUE);
	                break;
	            case GLFW_KEY_W:
	                wireframe = !wireframe;
	                glPolygonMode(GL_FRONT_AND_BACK,
	                              wireframe ? GL_LINE : GL_FILL);
	                break;
	            default:
	                break;
	        }
	    }
	}


	//========================================================================
	// Thread for updating particle physics
	//========================================================================

	static int physics_thread_main(long window)
	{
	    for (;;)
	    {
//	        mtx_lock(&thread_sync.particles_lock);
	    	synchronized (particles_lock) {
	    		
	        // Wait for particle drawing to be done
	        while (glfwWindowShouldClose(window) == 0 &&
	               thread_sync.p_frame > thread_sync.d_frame)
	        {
//	            struct timespec ts;
//	            clock_gettime(CLOCK_REALTIME, &ts);
//	            ts.tv_nsec += 100000000;
//	            cnd_timedwait(&thread_sync.d_done, &thread_sync.particles_lock, &ts);
	        	try {
					particles_lock.wait(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }

	        if (glfwWindowShouldClose(window) != 0)
	            break;

	        // Update particles
	        particle_engine(thread_sync.t, thread_sync.dt);

	        // Update frame counter
	        thread_sync.p_frame++;

	        // Unlock mutex and signal drawing thread
//	        mtx_unlock(&thread_sync.particles_lock);
//	        cnd_signal(&thread_sync.p_done);
	    	}
	    }

	    return 0;
	}


	//========================================================================
	// main
	//========================================================================

//	int main(int argc, char** argv)
	public static void main(String[] argv)
	{
	    int width, height;
	    Thread physics_thread = null;
	    long window;
	    long monitor = NULL;

	    if (glfwInit() == 0)
	    {
	        fprintf("Failed to initialize GLFW\n");
	        exit(EXIT_FAILURE);
	    }

//	    while ((ch = getopt(argc, argv, "fh")) != -1)
//	    {
//	        switch (ch)
//	        {
//	            case 'f':
//	                monitor = glfwGetPrimaryMonitor();
//	                break;
//	            case 'h':
//	                usage();
//	                exit(EXIT_SUCCESS);
//	        }
//	    }

	    if (monitor != NULL)
	    {
	        final GLFWVidMode mode = glfwGetVideoMode(monitor);

	        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
	        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
	        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
	        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());

	        width  = mode.width();
	        height = mode.height();
	    }
	    else
	    {
	        width  = 640;
	        height = 480;
	    }

	    window = glfwCreateWindow(width, height, "Particle Engine", monitor, NULL);
	    if (window == 0)
	    {
	        fprintf("Failed to create GLFW window\n");
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    if (monitor != NULL)
	        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

	    glfwMakeContextCurrent(window);
//	    GLContext.createFromCurrent();
	    GL.createCapabilities();
	    glfwSwapInterval(1);

	    glfwSetWindowSizeCallback(window, safe(new GLFWWindowSizeCallback() {
			public void invoke(long window, int width, int height) {
				resize_callback(window, width, height);
			}
		}));
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    // Set initial aspect ratio
	    glfwGetWindowSize(window, x_buf, y_buf);
	    width = x_buf.get(0); height = y_buf.get(0);
	    resize_callback(window, width, height);

	    // Upload particle texture
	    particle_tex_id = glGenTextures();
	    glBindTexture(GL_TEXTURE_2D, particle_tex_id);
	    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, P_TEX_WIDTH, P_TEX_HEIGHT,
	                 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, wrap(particle_texture));

	    // Upload floor texture
	    floor_tex_id = glGenTextures();
	    glBindTexture(GL_TEXTURE_2D, floor_tex_id);
	    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, F_TEX_WIDTH, F_TEX_HEIGHT,
	                 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, wrap(floor_texture));

	    if (glfwExtensionSupported("GL_EXT_separate_specular_color") != 0)
	    {
	        glLightModeli(GL_LIGHT_MODEL_COLOR_CONTROL_EXT,
	                      GL_SEPARATE_SPECULAR_COLOR_EXT);
	    }

	    // Set filled polygon mode as default (not wireframe)
	    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	    wireframe = false;

	    // Set initial times
	    thread_sync.t  = 0.0;
	    thread_sync.dt = 0.001f;
	    thread_sync.p_frame = 0;
	    thread_sync.d_frame = 0;

//	    mtx_init(&thread_sync.particles_lock, mtx_timed);
//	    cnd_init(&thread_sync.p_done);
//	    cnd_init(&thread_sync.d_done);

//	    if (thrd_create(&physics_thread, physics_thread_main, window) != thrd_success)
//	    {
//	        glfwTerminate();
//	        exit(EXIT_FAILURE);
//	    }
	    
	    if(USE_MULTI_THREAD){
		    final long _window = window;
		    physics_thread = new Thread(new Runnable() {
				public void run() {
					physics_thread_main(_window);
				}
			});
		    physics_thread.start();
	    }

	    glfwSetTime(0.0);

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        draw_scene(window, glfwGetTime());

	        glfwSwapBuffers(window);
	        glfwPollEvents();
	    }

//	    thrd_join(physics_thread, NULL);
	    if(USE_MULTI_THREAD){
		    try {
				physics_thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }

	    glfwDestroyWindow(window);
	    glfwTerminate();

	    exit(EXIT_SUCCESS);
	}

	final static class Vec3{
		float x,y,z;
	}
	
	// This class is used for interleaved vertex arrays (see the
	// draw_particles function)
	//
	// NOTE: This structure SHOULD be packed on most systems. It uses 32-bit fields
	// on 32-bit boundaries, and is a multiple of 64 bits in total (6x32=3x64). If
	// it does not work, try using pragmas or whatever to force the structure to be
	// packed.
	final static class Vertex{
		float s, t;         // Texture coordinates
	    int  rgba;          // Color (four ubytes packed into an uint)
	    float x, y, z;      // Vertex coordinates
	}
	
	// Thread synchronization
	final static class ThreadSync{
		double    t;         // Time (s)
	    float     dt;        // Time since last frame (s)
	    int       p_frame;   // Particle physics frame number
	    int       d_frame;   // Particle draw frame number
	}
	
	//========================================================================
	// Particle system global variables
	//========================================================================

	// This structure holds all state for a single particle
	final static class Particle{
		float x,y,z;     // Position in space
	    float vx,vy,vz;  // Velocity vector
	    float r,g,b;     // Color of particle
	    float life;      // Life of particle (1.0 = newborn, < 0.0 = dead)
	    boolean  active;    // Tells if this particle is active
	}
	
	
}
