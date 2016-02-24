package glfw.lwjgl.tests;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

public class TestCommon {

	protected static final int EXIT_SUCCESS =  0;
	protected static final int EXIT_FAILURE = -1;
	
	protected static final long NULL = 0;

	protected static final List<Object> callbacks = new ArrayList<>();
	
	protected static final IntBuffer x_buf = BufferUtils.createIntBuffer(1);
	protected static final IntBuffer y_buf = BufferUtils.createIntBuffer(1);

	protected static <T> T safe(T obj) {
		callbacks.add(obj);
		return obj;
	}

	protected static synchronized void printf(String pat, Object... args) {
		System.out.printf(pat, args);
	}
	
	protected static synchronized void fprintf(String pat, Object... args) {
		System.err.printf(pat, args);
	}

	protected static void exit(int status) {
		System.exit(status);
	}
	
	protected static int memcmp(ByteBuffer src, ByteBuffer dst, int size){
		final int src_pos = src.position();
		final int dst_pos = dst.position();
		
		int actualSize = Math.min(src.remaining(), Math.min(dst.remaining(), size));
		int _size = actualSize;
		while(_size > 0){
			byte src_byte = src.get();
			byte dst_byte = dst.get();
			
			int diff = (src_byte & 0xFF) - (dst_byte & 0xFF);
			if(diff < 0)
				return -1;
			else if(diff > 0)
				return 1;
			
			_size--;
		}
		
		src.position(src_pos);
		dst.position(dst_pos);
		
		if(actualSize == size)
			return 0;
		else
			return src.remaining() - dst.remaining();
	}
	
	public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
		Project.gluPerspective(fovy, aspect, zNear, zFar);
	}
	
	public static void gluLookAt(
			float eyex,
			float eyey,
			float eyez,
			float centerx,
			float centery,
			float centerz,
			float upx,
			float upy,
			float upz){
		Project.gluLookAt(eyex, eyey, eyez, centerx, centery, centerz, upx, upy, upz);
	}
	
	protected static int rand(){
		return (int) (Math.random() * Short.MAX_VALUE);
	}
	
	public static double cos(double angle){
		return Math.cos(angle);
	}
	
	public static double sin(double angle){
		return Math.sin(angle);
	}
}
