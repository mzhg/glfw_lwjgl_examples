package glfw.lwjgl.examples;

//========================================================================
//Heightmap example program using OpenGL 3 core profile
//Copyright (c) 2010 Olivier Delannoy
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
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import glfw.lwjgl.tests.TestCommon;

final class HeightMap extends TestCommon{

	/* = Map height updates */;
	static final float MAX_CIRCLE_SIZE = (5.0f);
	static final float MAX_DISPLACEMENT = (1.0f);
	static final float DISPLACEMENT_SIGN_LIMIT = (0.3f);
	static final int MAX_ITER = (200);
	static final int NUM_ITER_AT_A_TIME = (1)

	/* Map general information */;
	static final float MAP_SIZE = (10.0f);
	static final int MAP_NUM_VERTICES = (80);
	static final int MAP_NUM_TOTAL_VERTICES = (MAP_NUM_VERTICES*MAP_NUM_VERTICES);
	static final int MAP_NUM_LINES = (3* (MAP_NUM_VERTICES - 1) * (MAP_NUM_VERTICES - 1) + 2 * (MAP_NUM_VERTICES - 1));
	
	static boolean DEBUG_ENABLED = false;
	/**********************************************************************
	 * Default shader programs
	 *********************************************************************/
	static final String vertex_shader_text =
	"#version 150\n"+
	"uniform mat4 project;\n"+
	"uniform mat4 modelview;\n"+
	"in float x;\n"+
	"in float y;\n"+
	"in float z;\n"+
	"\n"+
	"void main()\n"+
	"{\n"+
	"   gl_Position = project * modelview * vec4(x, y, z, 1.0);\n"+
	"}\n";

	static final String fragment_shader_text =
	"#version 150\n"+
	"out vec4 color;\n"+
	"void main()\n"+
	"{\n"+
	"    color = vec4(0.2, 1.0, 0.2, 1.0); \n"+
	"}\n";

	/**********************************************************************
	 * Values for shader uniforms
	 *********************************************************************/

	/* Frustum configuration */
	static float view_angle = 45.0f;
	static float aspect_ratio = 4.0f/3.0f;
	static float z_near = 1.0f;
	static float z_far = 100.f;

	/* Projection matrix */
	static float projection_matrix[] = {
	    1.0f, 0.0f, 0.0f, 0.0f,
	    0.0f, 1.0f, 0.0f, 0.0f,
	    0.0f, 0.0f, 1.0f, 0.0f,
	    0.0f, 0.0f, 0.0f, 1.0f
	};

	/* Model view matrix */
	static float modelview_matrix[] = {
	    1.0f, 0.0f, 0.0f, 0.0f,
	    0.0f, 1.0f, 0.0f, 0.0f,
	    0.0f, 0.0f, 1.0f, 0.0f,
	    0.0f, 0.0f, 0.0f, 1.0f
	};

	/**********************************************************************
	 * Heightmap vertex and index data
	 *********************************************************************/

	static final float[][] map_vertices = new float[3][MAP_NUM_TOTAL_VERTICES];
	static final int[]  map_line_indices= new int[2*MAP_NUM_LINES];
	
	static final IntBuffer map_line_indices_buf = BufferUtils.createIntBuffer(2*MAP_NUM_LINES);
	static final FloatBuffer map_vertices_buf = BufferUtils.createFloatBuffer(3 * MAP_NUM_TOTAL_VERTICES);

	/* Store uniform location for the shaders
	 * Those values are setup as part of the process of creating
	 * the shader program. They should not be used before creating
	 * the program.
	 */
	static int mesh;
	static final int[] mesh_vbo = new int[4];

	/**********************************************************************
	 * OpenGL helper functions
	 *********************************************************************/

	/* Creates a shader object of the specified type using the specified text
	 */
	static int make_shader(int type, String text)
	{
	    int shader;
	    int shader_ok;
	    shader = glCreateShader(type);
	    if (shader != 0)
	    {
	    	glShaderSource(shader, text);
	        glCompileShader(shader);
	        shader_ok = glGetShaderi(shader, GL_COMPILE_STATUS);
	        if (shader_ok != GL_TRUE)
	        {
	            fprintf("ERROR: Failed to compile %s shader\n", (type == GL_FRAGMENT_SHADER) ? "fragment" : "vertex" );
//	            glGetShaderInfoLog(shader, 8192, &log_length,info_log);
	            String info_log = glGetShaderInfoLog(shader);
	            fprintf("ERROR: \n%s\n\n", info_log);
	            glDeleteShader(shader);
	            shader = 0;
	        }
	    }
	    return shader;
	}

	/* Creates a program object using the specified vertex and fragment text
	 */
	static int make_shader_program(String vs_text,String fs_text)
	{
	    int program = 0;
	    int program_ok;
	    int vertex_shader = 0;
	    int fragment_shader = 0;
	    vertex_shader = make_shader(GL_VERTEX_SHADER, vs_text);
	    if (vertex_shader != 0)
	    {
	        fragment_shader = make_shader(GL_FRAGMENT_SHADER, fs_text);
	        if (fragment_shader != 0)
	        {
	            /* make the program that connect the two shader and link it */
	            program = glCreateProgram();
	            if (program != 0)
	            {
	                /* attach both shader and link */
	                glAttachShader(program, vertex_shader);
	                glAttachShader(program, fragment_shader);
	                glLinkProgram(program);
	                program_ok = glGetProgrami(program, GL_LINK_STATUS);

	                if (program_ok != GL_TRUE)
	                {
	                    fprintf("ERROR, failed to link shader program\n");
//	                    glGetProgramInfoLog(program, 8192, &log_length, info_log);
	                    String info_log = glGetProgramInfoLog(program);
	                    fprintf("ERROR: \n%s\n\n", info_log);
	                    glDeleteProgram(program);
	                    glDeleteShader(fragment_shader);
	                    glDeleteShader(vertex_shader);
	                    program = 0;
	                }
	            }
	        }
	        else
	        {
	            fprintf("ERROR: Unable to load fragment shader\n");
	            glDeleteShader(vertex_shader);
	        }
	    }
	    else
	    {
	        fprintf("ERROR: Unable to load vertex shader\n");
	    }
	    return program;
	}

	/**********************************************************************
	 * Geometry creation functions
	 *********************************************************************/

	/* Generate vertices and indices for the heightmap
	 */
	static void init_map()
	{
	    int i;
	    int j;
	    int k;
	    float step = MAP_SIZE / (MAP_NUM_VERTICES - 1);
	    float x = 0.0f;
	    float z = 0.0f;
	    /* Create a flat grid */
	    k = 0;
	    for (i = 0 ; i < MAP_NUM_VERTICES ; ++i)
	    {
	        for (j = 0 ; j < MAP_NUM_VERTICES ; ++j)
	        {
	            map_vertices[0][k] = x;
	            map_vertices[1][k] = 0.0f;
	            map_vertices[2][k] = z;
	            z += step;
	            ++k;
	        }
	        x += step;
	        z = 0.0f;
	    }
	    
	    map_vertices_buf.clear();
	    map_vertices_buf.put(map_vertices[0]);
	    map_vertices_buf.put(map_vertices[1]);
	    map_vertices_buf.put(map_vertices[2]);
	    map_vertices_buf.flip();
	if(DEBUG_ENABLED)
	{
	    for (i = 0 ; i < MAP_NUM_TOTAL_VERTICES ; ++i)
	    {
	        printf ("Vertice %d (%f, %f, %f)\n",
	                i, map_vertices[0][i], map_vertices[1][i], map_vertices[2][i]);

	    }
	}
	    /* create indices */
	    /* line fan based on i
	     * i+1
	     * |  / i + n + 1
	     * | /
	     * |/
	     * i --- i + n
	     */

	    /* close the top of the square */
	    k = 0;
	    for (i = 0 ; i < MAP_NUM_VERTICES  -1 ; ++i)
	    {
	        map_line_indices[k++] = (i + 1) * MAP_NUM_VERTICES -1;
	        map_line_indices[k++] = (i + 2) * MAP_NUM_VERTICES -1;
	    }
	    /* close the right of the square */
	    for (i = 0 ; i < MAP_NUM_VERTICES -1 ; ++i)
	    {
	        map_line_indices[k++] = (MAP_NUM_VERTICES - 1) * MAP_NUM_VERTICES + i;
	        map_line_indices[k++] = (MAP_NUM_VERTICES - 1) * MAP_NUM_VERTICES + i + 1;
	    }

	    for (i = 0 ; i < (MAP_NUM_VERTICES - 1) ; ++i)
	    {
	        for (j = 0 ; j < (MAP_NUM_VERTICES - 1) ; ++j)
	        {
	            int ref = i * (MAP_NUM_VERTICES) + j;
	            map_line_indices[k++] = ref;
	            map_line_indices[k++] = ref + 1;

	            map_line_indices[k++] = ref;
	            map_line_indices[k++] = ref + MAP_NUM_VERTICES;

	            map_line_indices[k++] = ref;
	            map_line_indices[k++] = ref + MAP_NUM_VERTICES + 1;
	        }
	    }

	if( DEBUG_ENABLED)
	    for (k = 0 ; k < 2 * MAP_NUM_LINES ; k += 2)
	    {
	        int beg, end;
	        beg = map_line_indices[k];
	        end = map_line_indices[k+1];
	        printf ("Line %d: %d -> %d (%f, %f, %f) -> (%f, %f, %f)\n",
	                k / 2, beg, end,
	                map_vertices[0][beg], map_vertices[1][beg], map_vertices[2][beg],
	                map_vertices[0][end], map_vertices[1][end], map_vertices[2][end]);
	    }
	
	map_line_indices_buf.clear();
	map_line_indices_buf.put(map_line_indices).flip();
	}

	static void generate_heightmap__circle(float[] out/*float* center_x, float* center_y,
	        float* size, float* displacement*/)
	{
	    float sign;
	    /* random value for element in between [0-1.0] */
//	    *center_x = (MAP_SIZE * rand()) / (1.0f * RAND_MAX);
//	    *center_y = (MAP_SIZE * rand()) / (1.0f * RAND_MAX);
//	    *size = (MAX_CIRCLE_SIZE * rand()) / (1.0f * RAND_MAX);
//	    sign = (1.0f * rand()) / (1.0f * RAND_MAX);
//	    sign = (sign < DISPLACEMENT_SIGN_LIMIT) ? -1.0f : 1.0f;
//	    *displacement = (sign * (MAX_DISPLACEMENT * rand())) / (1.0f * RAND_MAX);
	    
	    out[0] = MAP_SIZE * (float)Math.random();
	    out[1] = MAP_SIZE * (float)Math.random();
	    out[2] = MAX_CIRCLE_SIZE * (float)Math.random();
	    sign = (float)Math.random();
	    sign = (sign < DISPLACEMENT_SIGN_LIMIT) ? -1.0f : 1.0f;
	    out[3] = sign * MAX_DISPLACEMENT * (float)Math.random();
	}

	/* Run the specified number of iterations of the generation process for the
	 * heightmap
	 */
	static void update_map(int num_iter)
	{
	    assert(num_iter > 0);
	    float[] out = new float[4];
	    while(num_iter != 0)
	    {
	        /* center of the circle */
	        float center_x;
	        float center_z;
	        float circle_size;
	        float disp;
	        int ii;
	        generate_heightmap__circle(out/*&center_x, &center_z, &circle_size, &disp*/);
	        center_x = out[0]; center_z = out[1];
	        circle_size = out[2]; disp = out[3];
	        
	        disp = disp / 2.0f;
	        for (ii = 0 ; ii < MAP_NUM_TOTAL_VERTICES ; ++ii)
	        {
	            float dx = center_x - map_vertices[0][ii];
	            float dz = center_z - map_vertices[2][ii];
	            float pd = (float) ((2.0 * Math.sqrt((dx * dx) + (dz * dz))) / circle_size);
	            if (Math.abs(pd) <= 1.0f)
	            {
	                /* tx,tz is within the circle */
	                float new_height = disp + (float) (cos(pd*3.14f)*disp);
	                map_vertices[1][ii] += new_height;
	            }
	        }
	        --num_iter;
	    }
	    
	    map_vertices_buf.clear();
	    map_vertices_buf.put(map_vertices[0]);
	    map_vertices_buf.put(map_vertices[1]);
	    map_vertices_buf.put(map_vertices[2]);
	    map_vertices_buf.flip();
	}

	/**********************************************************************
	 * OpenGL helper functions
	 *********************************************************************/

	/* Create VBO, IBO and VAO objects for the heightmap geometry and bind them to
	 * the specified program object
	 */
	static void make_mesh(int program)
	{
	    int attrloc;

	    mesh = glGenVertexArrays();
//	    glGenBuffers(4, mesh_vbo);
	    mesh_vbo[0] = glGenBuffers();
	    mesh_vbo[1] = glGenBuffers();
	    mesh_vbo[2] = glGenBuffers();
	    mesh_vbo[3] = glGenBuffers();
	    glBindVertexArray(mesh);
	    /* Prepare the data for drawing through a buffer inidices */
	    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh_vbo[3]);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, map_line_indices_buf, GL_STATIC_DRAW);

	    /* Prepare the attributes for rendering */
	    attrloc = glGetAttribLocation(program, "x");
	    glBindBuffer(GL_ARRAY_BUFFER, mesh_vbo[0]);
	    map_vertices_buf.position(0).limit(MAP_NUM_TOTAL_VERTICES);
	    glBufferData(GL_ARRAY_BUFFER, /*4 * MAP_NUM_TOTAL_VERTICES,*/ map_vertices_buf, GL_STATIC_DRAW);
	    glEnableVertexAttribArray(attrloc);
	    glVertexAttribPointer(attrloc, 1, GL_FLOAT, false, 0, 0);

	    attrloc = glGetAttribLocation(program, "z");
	    glBindBuffer(GL_ARRAY_BUFFER, mesh_vbo[2]);
	    map_vertices_buf.clear();
	    map_vertices_buf.position(MAP_NUM_TOTAL_VERTICES * 2).limit(MAP_NUM_TOTAL_VERTICES * 3);
	    glBufferData(GL_ARRAY_BUFFER, /*4 * MAP_NUM_TOTAL_VERTICES, &*/map_vertices_buf, GL_STATIC_DRAW);
	    glEnableVertexAttribArray(attrloc);
	    glVertexAttribPointer(attrloc, 1, GL_FLOAT, false, 0, 0);

	    attrloc = glGetAttribLocation(program, "y");
	    glBindBuffer(GL_ARRAY_BUFFER, mesh_vbo[1]);
	    map_vertices_buf.clear();
	    map_vertices_buf.position(MAP_NUM_TOTAL_VERTICES).limit(MAP_NUM_TOTAL_VERTICES * 2);
	    glBufferData(GL_ARRAY_BUFFER, /*4 * MAP_NUM_TOTAL_VERTICES,*/ map_vertices_buf, GL_DYNAMIC_DRAW);
	    glEnableVertexAttribArray(attrloc);
	    glVertexAttribPointer(attrloc, 1, GL_FLOAT, false, 0, 0);
	}

	/* Update VBO vertices from source data
	 */
	static void update_mesh()
	{
		map_vertices_buf.clear();
		map_vertices_buf.position(MAP_NUM_TOTAL_VERTICES).limit(MAP_NUM_TOTAL_VERTICES * 2);
	    glBufferSubData(GL_ARRAY_BUFFER, 0, map_vertices_buf);
	}

	/**********************************************************************
	 * GLFW callback functions
	 *********************************************************************/

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    switch(key)
	    {
	        case GLFW_KEY_ESCAPE:
	            /* Exit program on Escape */
	            glfwSetWindowShouldClose(window, GL_TRUE);
	            break;
	    }
	}

//	int main(int argc, char** argv)
	public static void main(String[] argv)
	{
	    long window;
	    int iter;
	    double dt;
	    double last_update_time;
	    float f;
	    int uloc_modelview;
	    int uloc_project;

	    int shader_program;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
	    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
	    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
	    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

	    window = glfwCreateWindow(800, 600, "GLFW OpenGL3 Heightmap demo", NULL, NULL);
	    if (window ==0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    /* Register events callback */
	    glfwSetKeyCallback(window, safe(new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				key_callback(window, key, scancode, action, mods);
			}
		}));

	    glfwMakeContextCurrent(window);
//	    GLContext.createFromCurrent();
	    GL.createCapabilities();
//	    gladLoadGLLoader((GLADloadproc) glfwGetProcAddress);

	    /* Prepare opengl resources for rendering */
	    shader_program = make_shader_program(vertex_shader_text, fragment_shader_text);

	    if (shader_program == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    glUseProgram(shader_program);
	    uloc_project   = glGetUniformLocation(shader_program, "project");
	    uloc_modelview = glGetUniformLocation(shader_program, "modelview");
	    
	    FloatBuffer mat_buf = BufferUtils.createFloatBuffer(16);
	    /* Compute the projection matrix */
	    f = (float) (1.0 / Math.tan(view_angle / 2.0));
	    projection_matrix[0]  = f / aspect_ratio;
	    projection_matrix[5]  = f;
	    projection_matrix[10] = (z_far + z_near)/ (z_near - z_far);
	    projection_matrix[11] = -1.0f;
	    projection_matrix[14] = 2.0f * (z_far * z_near) / (z_near - z_far);
	    mat_buf.put(projection_matrix).flip();
	    glUniformMatrix4fv(uloc_project, false, mat_buf);

	    /* Set the camera position */
	    modelview_matrix[12]  = -5.0f;
	    modelview_matrix[13]  = -5.0f;
	    modelview_matrix[14]  = -20.0f;
	    mat_buf.clear();
	    mat_buf.put(modelview_matrix).flip();
	    glUniformMatrix4fv(uloc_modelview, false, mat_buf);

	    /* Create mesh data */
	    init_map();
	    make_mesh(shader_program);

	    /* Create vao + vbo to store the mesh */
	    /* Create the vbo to store all the information for the grid and the height */

	    /* setup the scene ready for rendering */
	    glViewport(0, 0, 800, 600);
	    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

	    iter = 0;
	    last_update_time = glfwGetTime();

	    while (glfwWindowShouldClose(window) == 0)
	    {
	        /* render the next frame */
	        glClear(GL_COLOR_BUFFER_BIT);
	        glDrawElements(GL_LINES, 2* MAP_NUM_LINES , GL_UNSIGNED_INT, 0);

	        /* display and process events through callbacks */
	        glfwSwapBuffers(window);
	        glfwPollEvents();
	        /* Check the frame rate and update the heightmap if needed */
	        dt = glfwGetTime();
	        if ((dt - last_update_time) > 0.2)
	        {
	            /* generate the next iteration of the heightmap */
	            if (iter < MAX_ITER)
	            {
	                update_map(NUM_ITER_AT_A_TIME);
	                update_mesh();
	                iter += NUM_ITER_AT_A_TIME;
	            }
	            last_update_time = dt;
	        }
	    }

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}
}
