package glfw.lwjgl.tests;

//========================================================================
//Event linter (event spewer)
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
//This test hooks every available callback and outputs their arguments
//
//Log messages go to stdout, error messages to stderr
//
//Every event also gets a (sequential) number to aid discussion of logs
//
//========================================================================
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

final class Events extends TestCommon{
	
	static int counter = 0;
	static Slot[] slots;
	
	static String get_key_name(int key)
	{
	    switch (key)
	    {
	        // Printable keys
	        case GLFW_KEY_A:            return "A";
	        case GLFW_KEY_B:            return "B";
	        case GLFW_KEY_C:            return "C";
	        case GLFW_KEY_D:            return "D";
	        case GLFW_KEY_E:            return "E";
	        case GLFW_KEY_F:            return "F";
	        case GLFW_KEY_G:            return "G";
	        case GLFW_KEY_H:            return "H";
	        case GLFW_KEY_I:            return "I";
	        case GLFW_KEY_J:            return "J";
	        case GLFW_KEY_K:            return "K";
	        case GLFW_KEY_L:            return "L";
	        case GLFW_KEY_M:            return "M";
	        case GLFW_KEY_N:            return "N";
	        case GLFW_KEY_O:            return "O";
	        case GLFW_KEY_P:            return "P";
	        case GLFW_KEY_Q:            return "Q";
	        case GLFW_KEY_R:            return "R";
	        case GLFW_KEY_S:            return "S";
	        case GLFW_KEY_T:            return "T";
	        case GLFW_KEY_U:            return "U";
	        case GLFW_KEY_V:            return "V";
	        case GLFW_KEY_W:            return "W";
	        case GLFW_KEY_X:            return "X";
	        case GLFW_KEY_Y:            return "Y";
	        case GLFW_KEY_Z:            return "Z";
	        case GLFW_KEY_1:            return "1";
	        case GLFW_KEY_2:            return "2";
	        case GLFW_KEY_3:            return "3";
	        case GLFW_KEY_4:            return "4";
	        case GLFW_KEY_5:            return "5";
	        case GLFW_KEY_6:            return "6";
	        case GLFW_KEY_7:            return "7";
	        case GLFW_KEY_8:            return "8";
	        case GLFW_KEY_9:            return "9";
	        case GLFW_KEY_0:            return "0";
	        case GLFW_KEY_SPACE:        return "SPACE";
	        case GLFW_KEY_MINUS:        return "MINUS";
	        case GLFW_KEY_EQUAL:        return "EQUAL";
	        case GLFW_KEY_LEFT_BRACKET: return "LEFT BRACKET";
	        case GLFW_KEY_RIGHT_BRACKET: return "RIGHT BRACKET";
	        case GLFW_KEY_BACKSLASH:    return "BACKSLASH";
	        case GLFW_KEY_SEMICOLON:    return "SEMICOLON";
	        case GLFW_KEY_APOSTROPHE:   return "APOSTROPHE";
	        case GLFW_KEY_GRAVE_ACCENT: return "GRAVE ACCENT";
	        case GLFW_KEY_COMMA:        return "COMMA";
	        case GLFW_KEY_PERIOD:       return "PERIOD";
	        case GLFW_KEY_SLASH:        return "SLASH";
	        case GLFW_KEY_WORLD_1:      return "WORLD 1";
	        case GLFW_KEY_WORLD_2:      return "WORLD 2";

	        // Function keys
	        case GLFW_KEY_ESCAPE:       return "ESCAPE";
	        case GLFW_KEY_F1:           return "F1";
	        case GLFW_KEY_F2:           return "F2";
	        case GLFW_KEY_F3:           return "F3";
	        case GLFW_KEY_F4:           return "F4";
	        case GLFW_KEY_F5:           return "F5";
	        case GLFW_KEY_F6:           return "F6";
	        case GLFW_KEY_F7:           return "F7";
	        case GLFW_KEY_F8:           return "F8";
	        case GLFW_KEY_F9:           return "F9";
	        case GLFW_KEY_F10:          return "F10";
	        case GLFW_KEY_F11:          return "F11";
	        case GLFW_KEY_F12:          return "F12";
	        case GLFW_KEY_F13:          return "F13";
	        case GLFW_KEY_F14:          return "F14";
	        case GLFW_KEY_F15:          return "F15";
	        case GLFW_KEY_F16:          return "F16";
	        case GLFW_KEY_F17:          return "F17";
	        case GLFW_KEY_F18:          return "F18";
	        case GLFW_KEY_F19:          return "F19";
	        case GLFW_KEY_F20:          return "F20";
	        case GLFW_KEY_F21:          return "F21";
	        case GLFW_KEY_F22:          return "F22";
	        case GLFW_KEY_F23:          return "F23";
	        case GLFW_KEY_F24:          return "F24";
	        case GLFW_KEY_F25:          return "F25";
	        case GLFW_KEY_UP:           return "UP";
	        case GLFW_KEY_DOWN:         return "DOWN";
	        case GLFW_KEY_LEFT:         return "LEFT";
	        case GLFW_KEY_RIGHT:        return "RIGHT";
	        case GLFW_KEY_LEFT_SHIFT:   return "LEFT SHIFT";
	        case GLFW_KEY_RIGHT_SHIFT:  return "RIGHT SHIFT";
	        case GLFW_KEY_LEFT_CONTROL: return "LEFT CONTROL";
	        case GLFW_KEY_RIGHT_CONTROL: return "RIGHT CONTROL";
	        case GLFW_KEY_LEFT_ALT:     return "LEFT ALT";
	        case GLFW_KEY_RIGHT_ALT:    return "RIGHT ALT";
	        case GLFW_KEY_TAB:          return "TAB";
	        case GLFW_KEY_ENTER:        return "ENTER";
	        case GLFW_KEY_BACKSPACE:    return "BACKSPACE";
	        case GLFW_KEY_INSERT:       return "INSERT";
	        case GLFW_KEY_DELETE:       return "DELETE";
	        case GLFW_KEY_PAGE_UP:      return "PAGE UP";
	        case GLFW_KEY_PAGE_DOWN:    return "PAGE DOWN";
	        case GLFW_KEY_HOME:         return "HOME";
	        case GLFW_KEY_END:          return "END";
	        case GLFW_KEY_KP_0:         return "KEYPAD 0";
	        case GLFW_KEY_KP_1:         return "KEYPAD 1";
	        case GLFW_KEY_KP_2:         return "KEYPAD 2";
	        case GLFW_KEY_KP_3:         return "KEYPAD 3";
	        case GLFW_KEY_KP_4:         return "KEYPAD 4";
	        case GLFW_KEY_KP_5:         return "KEYPAD 5";
	        case GLFW_KEY_KP_6:         return "KEYPAD 6";
	        case GLFW_KEY_KP_7:         return "KEYPAD 7";
	        case GLFW_KEY_KP_8:         return "KEYPAD 8";
	        case GLFW_KEY_KP_9:         return "KEYPAD 9";
	        case GLFW_KEY_KP_DIVIDE:    return "KEYPAD DIVIDE";
	        case GLFW_KEY_KP_MULTIPLY:  return "KEYPAD MULTPLY";
	        case GLFW_KEY_KP_SUBTRACT:  return "KEYPAD SUBTRACT";
	        case GLFW_KEY_KP_ADD:       return "KEYPAD ADD";
	        case GLFW_KEY_KP_DECIMAL:   return "KEYPAD DECIMAL";
	        case GLFW_KEY_KP_EQUAL:     return "KEYPAD EQUAL";
	        case GLFW_KEY_KP_ENTER:     return "KEYPAD ENTER";
	        case GLFW_KEY_PRINT_SCREEN: return "PRINT SCREEN";
	        case GLFW_KEY_NUM_LOCK:     return "NUM LOCK";
	        case GLFW_KEY_CAPS_LOCK:    return "CAPS LOCK";
	        case GLFW_KEY_SCROLL_LOCK:  return "SCROLL LOCK";
	        case GLFW_KEY_PAUSE:        return "PAUSE";
	        case GLFW_KEY_LEFT_SUPER:   return "LEFT SUPER";
	        case GLFW_KEY_RIGHT_SUPER:  return "RIGHT SUPER";
	        case GLFW_KEY_MENU:         return "MENU";

	        default:                    return "UNKNOWN";
	    }
	}
	
	static String get_action_name(int action)
	{
	    switch (action)
	    {
	        case GLFW_PRESS:
	            return "pressed";
	        case GLFW_RELEASE:
	            return "released";
	        case GLFW_REPEAT:
	            return "repeated";
	    }

	    return "caused unknown action";
	}

	static String get_button_name(int button)
	{
	    switch (button)
	    {
	        case GLFW_MOUSE_BUTTON_LEFT:
	            return "left";
	        case GLFW_MOUSE_BUTTON_RIGHT:
	            return "right";
	        case GLFW_MOUSE_BUTTON_MIDDLE:
	            return "middle";
	        default:
	        {
	            String name = Integer.toString(button);
	            return name;
	        }
	    }
	}

	static String get_mods_name(int mods)
	{
	    StringBuilder name = new StringBuilder();

	    if (mods == 0)
	        return " no mods";

	    if ((mods & GLFW_MOD_SHIFT)!=0)
//	        strcat(name, " shift");
	    	name.append(" shift");
	    if ((mods & GLFW_MOD_CONTROL)!=0)
	    	name.append(" control");
	    if ((mods & GLFW_MOD_ALT)!=0)
	    	name.append(" alt");
	    if ((mods & GLFW_MOD_SUPER)!=0)
	    	name.append(" super");

	    return name.toString();
	}

	static String get_character_string(int codepoint)
	{
	    // This assumes UTF-8, which is stupid
//	    static char result[6 + 1];
//
//	    int length = wctomb(result, codepoint);
//	    if (length == -1)
//	        length = 0;
//
//	    result[length] = '\0';
//	    return result;
		
		return Character.toString((char)codepoint);
	}
	
	static void usage()
	{
	    printf("Usage: events [-f] [-h] [-n WINDOWS]\n");
	    printf("Options:\n");
	    printf("  -f use full screen\n");
	    printf("  -h show this help\n");
	    printf("  -n the number of windows to create\n");
	}

	static void window_pos_callback(long window, int x, int y)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Window position: %d %d\n",
	           counter++, slot.number, glfwGetTime(), x, y);
	}

	static void window_size_callback(long window, int width, int height)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Window size: %d %d\n",
	           counter++, slot.number, glfwGetTime(), width, height);
	}

	static void framebuffer_size_callback(long window, int width, int height)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Framebuffer size: %d %d\n",
	           counter++, slot.number, glfwGetTime(), width, height);

	    glViewport(0, 0, width, height);
	}

	static void window_close_callback(long window)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Window close\n",
	           counter++, slot.number, glfwGetTime());

	    glfwSetWindowShouldClose(window, slot.closeable);
	}

	static void window_refresh_callback(long window)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Window refresh\n",
	           counter++, slot.number, glfwGetTime());

	    glfwMakeContextCurrent(window);
	    glClear(GL_COLOR_BUFFER_BIT);
	    glfwSwapBuffers(window);
	}

	static void window_focus_callback(long window, int focused)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Window %s\n",
	           counter++, slot.number, glfwGetTime(),
	           focused!=0 ? "focused" : "defocused");
	}

	static void window_iconify_callback(long window, int iconified)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Window was %s\n",
	           counter++, slot.number, glfwGetTime(),
	           iconified != 0 ? "iconified" : "restored");
	}

	static void mouse_button_callback(long window, int button, int action, int mods)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Mouse button %d (%s) (with%s) was %s\n",
	           counter++, slot.number, glfwGetTime(), button,
	           get_button_name(button),
	           get_mods_name(mods),
	           get_action_name(action));
	}

	static void cursor_position_callback(long window, double x, double y)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Cursor position: %f %f\n",
	           counter++, slot.number, glfwGetTime(), x, y);
	}

	static void cursor_enter_callback(long window, int entered)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Cursor %s window\n",
	           counter++, slot.number, glfwGetTime(),
	           entered != 0 ? "entered" : "left");
	}

	static void scroll_callback(long window, double x, double y)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Scroll: %.3f %.3f\n",
	           counter++, slot.number, glfwGetTime(), x, y);
	}

	static void key_callback(long window, int key, int scancode, int action, int mods)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];

	    printf("%08x to %d at %.3f: Key 0x%04x Scancode 0x%04x (%s) (with%s) was %s\n",
	           counter++, slot.number, glfwGetTime(), key, scancode,
	           get_key_name(key),
	           get_mods_name(mods),
	           get_action_name(action));

	    if (action != GLFW_PRESS)
	        return;

	    switch (key)
	    {
	        case GLFW_KEY_C:
	        {
	            slot.closeable = 1- slot.closeable;

	            printf("(( closing %s ))\n", slot.closeable!=0 ? "enabled" : "disabled");
	            break;
	        }
	    }
	}

	static void char_callback(long window, int codepoint)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Character 0x%08x (%s) input\n",
	           counter++, slot.number, glfwGetTime(), codepoint,
	           get_character_string(codepoint));
	}

	static void char_mods_callback(long window, int codepoint, int mods)
	{
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];
	    printf("%08x to %d at %.3f: Character 0x%08x (%s) with modifiers (with%s) input\n",
	            counter++, slot.number, glfwGetTime(), codepoint,
	            get_character_string(codepoint),
	            get_mods_name(mods));
	}

	static void drop_callback(long window, int count, String[] paths)
	{
	    int i;
	    Slot slot = slots[(int) glfwGetWindowUserPointer(window)];

	    printf("%08x to %d at %.3f: Drop input\n",
	           counter++, slot.number, glfwGetTime());

	    for (i = 0;  i < count;  i++)
	        printf("  %d: \"%s\"\n", i, paths[i]);
	}

	static final IntBuffer intx = BufferUtils.createIntBuffer(1);
	static final IntBuffer inty = BufferUtils.createIntBuffer(1);
	static void monitor_callback(long monitor, int event)
	{
	    if (event == GLFW_CONNECTED)
	    {
	        int x, y, widthMM, heightMM;
	        final GLFWVidMode mode = glfwGetVideoMode(monitor);
            
	        glfwGetMonitorPos(monitor, intx, inty);  x = intx.get(0); y = inty.get(0);
	        glfwGetMonitorPhysicalSize(monitor, intx, inty); widthMM = intx.get(0); heightMM = inty.get(0);

	        printf("%08x at %.3f: Monitor %s (%dx%d at %dx%d, %dx%d mm) was connected\n",
	               counter++,
	               glfwGetTime(),
	               glfwGetMonitorName(monitor),
	               mode.width(), mode.height(),
	               x, y,
	               widthMM, heightMM);
	    }
	    else
	    {
	        printf("%08x at %.3f: Monitor %s was disconnected\n",
	               counter++,
	               glfwGetTime(),
	               glfwGetMonitorName(monitor));
	    }
	}
	
	public static void main(String[] args) {
		long monitor = 0;
	    int i, width, height, count = 1;

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        System.exit(0);

	    printf("Library initialized\n");

	    glfwSetMonitorCallback(safe(new GLFWMonitorCallback() {
			@Override
			public void invoke(long monitor, int event) {
				monitor_callback(monitor, event);
			}
		}));

//	    while ((ch = getopt(argc, argv, "hfn:")) != -1)
//	    {
//	        switch (ch)
//	        {
//	            case 'h':
//	                usage();
//	                exit(EXIT_SUCCESS);
//
//	            case 'f':
//	                monitor = glfwGetPrimaryMonitor();
//	                break;
//
//	            case 'n':
//	                count = (int) strtol(optarg, NULL, 10);
//	                break;
//
//	            default:
//	                usage();
//	                System.exit(0);
//	        }
//	    }
	    
//	    monitor = glfwGetPrimaryMonitor();
	    count = 2;

	    if (monitor != 0)
	    {
	        final GLFWVidMode mode = glfwGetVideoMode(monitor);

	        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
	        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
	        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
	        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());

	        width = mode.width();
	        height = mode.height();
	    }
	    else
	    {
	        width  = 640;
	        height = 480;
	    }

	    if (count == 0)
	    {
	    	System.err.println("Invalid user");
	        System.exit(0);
	    }

	    slots = new Slot[count];

	    for (i = 0;  i < count;  i++)
	    {
	    	slots[i] = new Slot();
	        slots[i].closeable = GL_TRUE;
	        slots[i].number = i + 1;

	        String title = String.format("Event Linter (Window %d)", slots[i].number);

	        if (monitor!=0)
	        {
	            printf("Creating full screen window %d (%dx%d on %s)\n",
	                   slots[i].number,
	                   width, height,
	                   glfwGetMonitorName(monitor));
	        }
	        else
	        {
	            printf("Creating windowed mode window %d (%dx%d)\n",
	                   slots[i].number,
	                   width, height);
	        }

	        slots[i].window = glfwCreateWindow(width, height, title, monitor, 0);
	        if (slots[i].window == 0)
	        {
	            glfwTerminate();
	            System.exit(0);
	        }

	        glfwSetWindowUserPointer(slots[i].window, i);

	        glfwSetWindowPosCallback(slots[i].window, safe(new GLFWWindowPosCallback() {
				public void invoke(long window, int xpos, int ypos) {
					window_pos_callback(window, xpos, ypos);
				}
			}));
	        glfwSetWindowSizeCallback(slots[i].window, safe(new GLFWWindowSizeCallback() {
				public void invoke(long window, int width, int height) {
					window_size_callback(window, width, height);
				}
			}));
	        glfwSetFramebufferSizeCallback(slots[i].window, safe(new GLFWFramebufferSizeCallback() {
				public void invoke(long window, int width, int height) {
					framebuffer_size_callback(window, width, height);
				}
			}));
	        glfwSetWindowCloseCallback(slots[i].window, safe(new GLFWWindowCloseCallback() {
				public void invoke(long window) {
					
				}
			}));
	        glfwSetWindowRefreshCallback(slots[i].window, safe(new GLFWWindowRefreshCallback() {
				public void invoke(long window) {
					window_refresh_callback(window);
				}
			}));
	        glfwSetWindowFocusCallback(slots[i].window, safe(new GLFWWindowFocusCallback() {
				public void invoke(long window, int focused) {
					window_focus_callback(window, focused);
				}
			}));
	        glfwSetWindowIconifyCallback(slots[i].window, safe(new GLFWWindowIconifyCallback() {
				public void invoke(long window, int iconified) {
					window_iconify_callback(window, iconified);
				}
			}));
	        glfwSetMouseButtonCallback(slots[i].window, safe(new GLFWMouseButtonCallback() {
				public void invoke(long window, int button, int action, int mods) {
					mouse_button_callback(window, button, action, mods);
				}
			}));
	        glfwSetCursorPosCallback(slots[i].window, safe(new GLFWCursorPosCallback() {
				public void invoke(long window, double xpos, double ypos) {
					cursor_position_callback(window, xpos, ypos);
				}
			}));
	        glfwSetCursorEnterCallback(slots[i].window, safe(new GLFWCursorEnterCallback() {
				public void invoke(long window, int entered) {
					cursor_enter_callback(window, entered);
				}
			}));
	        glfwSetScrollCallback(slots[i].window, safe(new GLFWScrollCallback() {
				public void invoke(long window, double xoffset, double yoffset) {
					scroll_callback(window, xoffset, yoffset);
				}
			}));
	        glfwSetKeyCallback(slots[i].window, safe(new GLFWKeyCallback() {
				public void invoke(long window, int key, int scancode, int action, int mods) {
					key_callback(window, key, scancode, action, mods);
				}
			}));
	        glfwSetCharCallback(slots[i].window, safe(new GLFWCharCallback() {
				public void invoke(long window, int codepoint) {
					char_callback(window, codepoint);
				}
			}));
	        glfwSetCharModsCallback(slots[i].window, safe(new GLFWCharModsCallback() {
				public void invoke(long window, int codepoint, int mods) {
					char_mods_callback(window, codepoint, mods);
				}
			}));
	        glfwSetDropCallback(slots[i].window, safe(new GLFWDropCallback() {
				public void invoke(long window, int count, long names) {
//					drop_callback(window, count, Callbacks.dropCallbackNamesString(count,names));  TODO
				}
			}));

	        glfwMakeContextCurrent(slots[i].window);
	        GL.createCapabilities();
	        glfwSwapInterval(1);
	    }

	    printf("Main loop starting\n");

	    for (;;)
	    {
	        for (i = 0;  i < count;  i++)
	        {
	            if (glfwWindowShouldClose(slots[i].window) != 0)
	                break;
	        }

	        if (i < count)
	            break;

	        glfwWaitEvents();

	        // Workaround for an issue with msvcrt and mintty
//	        fflush(stdout);
	    }

//	    free(slots);
	    glfwTerminate();
	}
	
	private static final class Slot{
		long window;
		int number;
		int closeable;
	}
}
