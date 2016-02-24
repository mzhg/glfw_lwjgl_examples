package glfw.lwjgl.tests;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

final class CursorAnim{

	static final int SIZE = 64;	  // cursor size (width & height)
	static final int N    = 60;   // number of frames
	
	static ByteBuffer buffer = BufferUtils.createUnalignedByteBuffer(4 * SIZE * SIZE);
	static GLFWImage image;
	static float star(int x, int y, float t)
	{
	    float c = SIZE / 2.0f;

	    float i = (0.25f * (float)Math.sin(2.0f * 3.1415926f * t) + 0.75f);
	    float k = SIZE * 0.046875f * i;

	    float dist = (float)Math.sqrt((x - c) * (x - c) + (y - c) * (y - c));

	    float salpha = 1.0f - dist / c;
	    float xalpha = (float)x == c ? c : k / (float)Math.abs(x - c);
	    float yalpha = (float)y == c ? c : k / (float)Math.abs(y - c);

	    return Math.max(0.0f, Math.min(1.0f, i * salpha * 0.2f + salpha * xalpha * yalpha));
	}
	
	static long load_frame(float t){
		int x, y;
		buffer.clear();
		if(image == null){
//			image = new GLFWimage();
//			image.setWidth(SIZE);
//			image.setHeight(SIZE);
//			image.setPixels(buffer);
			image = GLFWImage.create();
			image.width(SIZE);
			image.height(SIZE);
		}
		
		for(y = 0; y < SIZE; y++){
			for (x = 0;  x < SIZE;  x++){
				buffer.put((byte) 255);
				buffer.put((byte) 255);
				buffer.put((byte) 255);
				buffer.put((byte) (255 * star(x, y, t)));
			}
		}
		buffer.flip();
		image.pixels(buffer);
		
		return GLFW.glfwCreateCursor(image, 0, 0);
	}
	
	public static void main(String[] args) {
		double t0, t1, frameTime = 0.0;

	    long window;
	    long[] frames = new long[N];

	    if (GLFW.glfwInit() == 0)
	        System.exit(0);

	    window = GLFW.glfwCreateWindow(640, 480, "Cursor animation", 0, 0);

	    if (window == 0)
	    {
	    	GLFW.glfwTerminate();
	    	System.exit(0);
	    }

	    GLFW.glfwMakeContextCurrent(window);
	    GL.createCapabilities();
	    
	    GLFW.glfwSwapInterval(1);

	    for (int i = 0; i < N; i++)
	        frames[i] = load_frame(i / (float)N);

	    t0 = GLFW.glfwGetTime();

	    int i = 0;
	    while (GLFW.glfwWindowShouldClose(window) == 0)
	    {
	        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	        GLFW.glfwSetCursor(window, frames[i]);
	        GLFW.glfwSwapBuffers(window);
	        GLFW.glfwPollEvents();

	        t1 = GLFW.glfwGetTime();
	        frameTime += t1 - t0;
	        t0 = t1;

	        while (frameTime > 1.0 / (double)N)
	        {
	            i = (i + 1) % N;
	            frameTime -= 1.0 / (double)N;
	        }
	    }

	    GLFW.glfwTerminate();
	}
}
