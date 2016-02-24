package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwExtensionSupported;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.ARBMultisample.GL_SAMPLES_ARB;
import static org.lwjgl.opengl.GL11.GL_ALPHA_BITS;
import static org.lwjgl.opengl.GL11.GL_BLUE_BITS;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BITS;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_GREEN_BITS;
import static org.lwjgl.opengl.GL11.GL_RED_BITS;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BITS;
import static org.lwjgl.opengl.GL11.GL_STEREO;
import static org.lwjgl.opengl.GL11.glGetInteger;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

final class Defaults extends TestCommon{
	
	static AttribGL gl_attribs[] =
		{
		    new AttribGL( GL_RED_BITS, null, "red bits" ),
		    new AttribGL( GL_GREEN_BITS, null, "green bits" ),
		    new AttribGL( GL_BLUE_BITS, null, "blue bits" ),
		    new AttribGL( GL_ALPHA_BITS, null, "alpha bits" ),
		    new AttribGL( GL_DEPTH_BITS, null, "depth bits" ),
		    new AttribGL( GL_STENCIL_BITS, null, "stencil bits" ),
		    new AttribGL( GL_STEREO, null, "stereo" ),
		    new AttribGL( GL_SAMPLES_ARB, "GL_ARB_multisample", "FSAA samples" ),
		};

		static AttribGLFW glfw_attribs[] =
		{
		    new AttribGLFW( GLFW_CONTEXT_VERSION_MAJOR, "Context version major" ),
		    new AttribGLFW( GLFW_CONTEXT_VERSION_MINOR, "Context version minor" ),
		    new AttribGLFW( GLFW_OPENGL_FORWARD_COMPAT, "OpenGL forward compatible" ),
		    new AttribGLFW( GLFW_OPENGL_DEBUG_CONTEXT, "OpenGL debug context" ),
		    new AttribGLFW( GLFW_OPENGL_PROFILE, "OpenGL profile" ),
		};
		
		public static void main(String[] args) {
			int i, width, height;
		    long window;
		    IntBuffer buf_width, buf_height;

//		    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

		    if (glfwInit() == 0)
		        System.exit(0);

		    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

		    window = glfwCreateWindow(640, 480, "Defaults", 0, 0);
		    if (window == 0)
		    {
		        glfwTerminate();
		        System.exit(0);
		    }

		    buf_width = BufferUtils.createIntBuffer(1);
		    buf_height = BufferUtils.createIntBuffer(1);
		    glfwMakeContextCurrent(window);
		    GL.createCapabilities();
		    glfwGetFramebufferSize(window, buf_width, buf_height);
		    width = buf_width.get();
		    height = buf_height.get();
		    
		    System.out.printf("framebuffer size: %dx%d\n", width, height);

		    for (i = 0;  i < glfw_attribs.length;   i++)
		    {
		    	System.out.printf("%s: %d\n",
		               glfw_attribs[i].name,
		               glfwGetWindowAttrib(window, glfw_attribs[i].attrib));
		    }

		    for (i = 0;  i < gl_attribs.length;   i++)
		    {
		        int value = 0;

		        if (gl_attribs[i].ext != null)
		        {
		            if (glfwExtensionSupported(gl_attribs[i].ext) == 0)
		                continue;
		        }

		        value = glGetInteger(gl_attribs[i].attrib);

		        System.out.printf("%s: %d\n", gl_attribs[i].name, value);
		    }

		    glfwDestroyWindow(window);
		    window = 0;

		    glfwTerminate();
		}

	static final class AttribGL{
		int attrib;
		String ext;
		String name;
		
		public AttribGL(int attrib, String ext, String name) {
			super();
			this.attrib = attrib;
			this.ext = ext;
			this.name = name;
		}
	}
	
	static final class AttribGLFW{
		int attrib;
		String name;
		
		public AttribGLFW(int attrib, String name) {
			this.attrib = attrib;
			this.name = name;
		}
	}
}
