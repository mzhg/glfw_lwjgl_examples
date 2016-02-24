package glfw.lwjgl.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBRobustness.*;

import java.nio.IntBuffer;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

final class GlfwInfo extends TestCommon{

	private static final String API_OPENGL = "gl";
	private static final String API_OPENGL_ES = "es";

	private static final String PROFILE_NAME_CORE = "core";
	private static final String PROFILE_NAME_COMPAT = "compat";

	private static final String STRATEGY_NAME_NONE = "none";
	private static final String STRATEGY_NAME_LOSE = "lose";

	private static final String BEHAVIOR_NAME_NONE = "none";
	private static final String BEHAVIOR_NAME_FLUSH = "flush";
	
	private static final int GL_CONTEXT_CORE_PROFILE_BIT = 0x1;
	private static final int GL_CONTEXT_COMPATIBILITY_PROFILE_BIT = 0x2;
	private static final int GL_NUM_EXTENSIONS = 0x821d;
	private static final int GL_CONTEXT_FLAGS = 0x821e;
	private static final int GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT = 0x1;
	private static final int GL_CONTEXT_FLAG_DEBUG_BIT = 0x2;
	private static final int GL_CONTEXT_PROFILE_MASK = 0x9126;
	
	static void usage()
	{
	    printf("Usage: glfwinfo [-h] [-a API] [-m MAJOR] [-n MINOR] [-d] [-l] [-f] [-p PROFILE] [-s STRATEGY] [-b BEHAVIOR]\n");
	    printf("Options:\n");
	    printf("  -a the client API to use (" + API_OPENGL + " or " + API_OPENGL_ES + ")\n");
	    printf("  -b the release behavior to use (" + BEHAVIOR_NAME_NONE + " or " + BEHAVIOR_NAME_FLUSH + ")\n");
	    printf("  -d request a debug context\n");
	    printf("  -f require a forward-compatible context\n");
	    printf("  -h show this help\n");
	    printf("  -l list all client API extensions after context creation\n");
	    printf("  -m the major number of the required client API version\n");
	    printf("  -n the minor number of the required client API version\n");
	    printf("  -p the OpenGL profile to use (" + PROFILE_NAME_CORE + " or " + PROFILE_NAME_COMPAT + ")\n");
	    printf("  -s the robustness strategy to use (" + STRATEGY_NAME_NONE + " or " + STRATEGY_NAME_LOSE + ")\n");
	}
	
	static String get_client_api_name(int api)
	{
	    if (api == GLFW_OPENGL_API)
	        return "OpenGL";
	    else if (api == GLFW_OPENGL_ES_API)
	        return "OpenGL ES";
	    
	    return "Unknown API";
	}

	static String get_profile_name_gl(int mask)
	{
	    if ((mask & GL_CONTEXT_COMPATIBILITY_PROFILE_BIT)!=0)
	        return PROFILE_NAME_COMPAT;
	    if ((mask & GL_CONTEXT_CORE_PROFILE_BIT)!=0)
	        return PROFILE_NAME_CORE;

	    return "unknown";
	}

	static String get_profile_name_glfw(int profile)
	{
	    if (profile == GLFW_OPENGL_COMPAT_PROFILE)
	        return PROFILE_NAME_COMPAT;
	    if (profile == GLFW_OPENGL_CORE_PROFILE)
	        return PROFILE_NAME_CORE;

	    return "unknown";
	}

	static String get_strategy_name_gl(int strategy)
	{
	    if (strategy == GL_LOSE_CONTEXT_ON_RESET_ARB)
	        return STRATEGY_NAME_LOSE;
	    if (strategy == GL_NO_RESET_NOTIFICATION_ARB)
	        return STRATEGY_NAME_NONE;

	    return "unknown";
	}

	static String get_strategy_name_glfw(int strategy)
	{
	    if (strategy == GLFW_LOSE_CONTEXT_ON_RESET)
	        return STRATEGY_NAME_LOSE;
	    if (strategy == GLFW_NO_RESET_NOTIFICATION)
	        return STRATEGY_NAME_NONE;

	    return "unknown";
	}

	static void list_extensions(int api, int major, int minor)
	{
	    int i;
	    int count;
	    String extensions;

	    printf("%s context supported extensions:\n", get_client_api_name(api));

	    if (api == GLFW_OPENGL_API && major > 2)
	    {
//	        PFNGLGETSTRINGIPROC glGetStringi = (PFNGLGETSTRINGIPROC) glfwGetProcAddress("glGetStringi");
//	        if (!glGetStringi)
//	        {
//	            glfwTerminate();
//	            exit(EXIT_FAILURE);
//	        }
	    	
	    	if(!GL.getCapabilities().OpenGL30){
	            glfwTerminate();
	            exit(EXIT_FAILURE);
	    	}

	    	count = glGetInteger(GL_NUM_EXTENSIONS);

	        for (i = 0;  i < count;  i++)
	            printf(GL30.glGetStringi(GL_EXTENSIONS, i) + "\n");
	    }
	    else
	    {
	        extensions = glGetString(GL_EXTENSIONS);
//	        while (*extensions != '\0')
//	        {
//	            if (*extensions == ' ')
//	                putchar('\n');
//	            else
//	                putchar(*extensions);
//
//	            extensions++;
//	        }
	        StringTokenizer token = new StringTokenizer(extensions, " ");
	        while(token.hasMoreTokens()){
	        	printf(token.nextToken() + "\n");
	        }
	    }

	    printf("\n");
	}
	

	static boolean valid_version()
	{
		IntBuffer major = x_buf;
		IntBuffer minor = y_buf;
		IntBuffer revision = BufferUtils.createIntBuffer(1);
//	    int major, minor, revision;

	    glfwGetVersion(major, minor, revision);

	    printf("GLFW header version: %d.%d.%d\n",
	           GLFW_VERSION_MAJOR,
	           GLFW_VERSION_MINOR,
	           GLFW_VERSION_REVISION);

	    printf("GLFW library version: %d.%d.%d\n", major.get(0), minor.get(0), revision.get(0));

	    if (major.get(0) != GLFW_VERSION_MAJOR)
	    {
	        printf("*** ERROR: GLFW major version mismatch! ***\n");
	        return false;
	    }

	    if (minor.get(0) != GLFW_VERSION_MINOR || revision.get(0) != GLFW_VERSION_REVISION)
	        printf("*** WARNING: GLFW version mismatch! ***\n");

	    printf("GLFW library version string: \"%s\"\n", glfwGetVersionString());
	    return true;
	}
	
	public static void main(String[] args) {
		int api = 0, profile = 0, strategy = 0, behavior = 0, major = 1, minor = 0, revision;
	    boolean debug = false, forward = false, list = true;
	    int flags, mask;
	    long window;
	    
	    try {
			Options options = new Options();
			options.addOption("a", false, "OpenGL Context(gl or es)");
			options.addOption("b", false, "none or flush");
			options.addOption("d", false, "Create Debug Context? true or false");
			options.addOption("f", false, "forward");
			options.addOption("h", false, "Usage");
			options.addOption("l", false, "list");
			options.addOption("m", false, "major version");
			options.addOption("n", false, "minor version");
			options.addOption("p", false, "OpenGL profile, core or compat");
			options.addOption("s", false, "none or lost");
			
			CommandLineParser parser = new DefaultParser(); 
			CommandLine cmd = parser.parse(options, args); 

			if(cmd.hasOption("a")) {
				String value = cmd.getOptionValue("a");
				if(value.equalsIgnoreCase(API_OPENGL)){
					api = GLFW_OPENGL_API;
				}else if(value.equalsIgnoreCase(API_OPENGL_ES)){
					 api = GLFW_OPENGL_ES_API;
				}else{
                    usage();
                    exit(EXIT_FAILURE);
                }
			}else if(cmd.hasOption("b")){
				String value = cmd.getOptionValue("b");
				if(value.equalsIgnoreCase(BEHAVIOR_NAME_NONE)){
					behavior = GLFW_RELEASE_BEHAVIOR_NONE;
				}else if(value.equalsIgnoreCase(BEHAVIOR_NAME_FLUSH)){
					behavior = GLFW_RELEASE_BEHAVIOR_FLUSH;
				}else{
					usage();
                    exit(EXIT_FAILURE);
				}
			}else if(cmd.hasOption("d")){
				debug = true;
			}else if(cmd.hasOption("f")){
				forward = true;
			}else if(cmd.hasOption("h")){
				usage();
				exit(EXIT_SUCCESS);
			}else if(cmd.hasOption("l")){
				list = true;
			}else if(cmd.hasOption("m")){
				major = Integer.parseInt(cmd.getOptionValue("m"));
			}else if(cmd.hasOption("n")){
				minor = Integer.parseInt(cmd.getOptionValue("n"));
			}else if(cmd.hasOption("p")){
				String value = cmd.getOptionValue("p");
				if(value == null) value = "";
				if(value.equalsIgnoreCase(PROFILE_NAME_CORE)){
					 profile = GLFW_OPENGL_CORE_PROFILE;
				}else if(value.equalsIgnoreCase(PROFILE_NAME_COMPAT)){
					profile = GLFW_OPENGL_COMPAT_PROFILE;
				}else{
					usage();
                    exit(EXIT_FAILURE);
				}
			}else if(cmd.hasOption("s")){
				String value = cmd.getOptionValue("s");
				if(value == null) value = "";
				if(value.equalsIgnoreCase(STRATEGY_NAME_NONE)){
					strategy = GLFW_NO_RESET_NOTIFICATION;
				}else if(value.equalsIgnoreCase(STRATEGY_NAME_LOSE)){
					strategy = GLFW_LOSE_CONTEXT_ON_RESET;
				}else{
					usage();
                    exit(EXIT_FAILURE);
				}
				
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch(NumberFormatException e){
			
		}

//	    while ((ch = getopt(argc, argv, "a:b:dfhlm:n:p:s:")) != -1)
//	    {
//	        switch (ch)
//	        {
//	            case 'a':
//	                if (strcasecmp(optarg, API_OPENGL) == 0)
//	                    api = GLFW_OPENGL_API;
//	                else if (strcasecmp(optarg, API_OPENGL_ES) == 0)
//	                    api = GLFW_OPENGL_ES_API;
//	                else
//	                {
//	                    usage();
//	                    exit(EXIT_FAILURE);
//	                }
//	                break;
//	            case 'b':
//	                if (strcasecmp(optarg, BEHAVIOR_NAME_NONE) == 0)
//	                    behavior = GLFW_RELEASE_BEHAVIOR_NONE;
//	                else if (strcasecmp(optarg, BEHAVIOR_NAME_FLUSH) == 0)
//	                    behavior = GLFW_RELEASE_BEHAVIOR_FLUSH;
//	                else
//	                {
//	                    usage();
//	                    exit(EXIT_FAILURE);
//	                }
//	                break;
//	            case 'd':
//	                debug = GL_TRUE;
//	                break;
//	            case 'f':
//	                forward = GL_TRUE;
//	                break;
//	            case 'h':
//	                usage();
//	                exit(EXIT_SUCCESS);
//	            case 'l':
//	                list = GL_TRUE;
//	                break;
//	            case 'm':
//	                major = atoi(optarg);
//	                break;
//	            case 'n':
//	                minor = atoi(optarg);
//	                break;
//	            case 'p':
//	                if (strcasecmp(optarg, PROFILE_NAME_CORE) == 0)
//	                    profile = GLFW_OPENGL_CORE_PROFILE;
//	                else if (strcasecmp(optarg, PROFILE_NAME_COMPAT) == 0)
//	                    profile = GLFW_OPENGL_COMPAT_PROFILE;
//	                else
//	                {
//	                    usage();
//	                    exit(EXIT_FAILURE);
//	                }
//	                break;
//	            case 's':
//	                if (strcasecmp(optarg, STRATEGY_NAME_NONE) == 0)
//	                    strategy = GLFW_NO_RESET_NOTIFICATION;
//	                else if (strcasecmp(optarg, STRATEGY_NAME_LOSE) == 0)
//	                    strategy = GLFW_LOSE_CONTEXT_ON_RESET;
//	                else
//	                {
//	                    usage();
//	                    exit(EXIT_FAILURE);
//	                }
//	                break;
//	            default:
//	                usage();
//	                exit(EXIT_FAILURE);
//	        }
//	    }

	    // Initialize GLFW and create window

	    if (!valid_version()){
	        exit(EXIT_FAILURE);
	    }

//	    glfwSetErrorCallback(safe(Callbacks.errorCallbackPrint()));

	    if (glfwInit() == 0)
	        exit(EXIT_FAILURE);

	    if (major != 1 || minor != 0)
	    {
	        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, major);
	        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, minor);
	    }

	    if (api != 0)
	        glfwWindowHint(GLFW_CLIENT_API, api);

	    if (debug)
	        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL_TRUE);

	    if (forward)
	        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

	    if (profile != 0)
	        glfwWindowHint(GLFW_OPENGL_PROFILE, profile);

	    if (strategy != 0)
	        glfwWindowHint(GLFW_CONTEXT_ROBUSTNESS, strategy);

	    if (behavior != 0)
	        glfwWindowHint(GLFW_CONTEXT_RELEASE_BEHAVIOR, behavior);

	    glfwWindowHint(GLFW_RED_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_GREEN_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_BLUE_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_ALPHA_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_DEPTH_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_STENCIL_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_ACCUM_RED_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_ACCUM_GREEN_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_ACCUM_BLUE_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_ACCUM_ALPHA_BITS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_AUX_BUFFERS, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_SAMPLES, GLFW_DONT_CARE);
	    glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_DONT_CARE);

	    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

	    window = glfwCreateWindow(200, 200, "Version", NULL, NULL);
	    if (window == 0)
	    {
	        glfwTerminate();
	        exit(EXIT_FAILURE);
	    }

	    glfwMakeContextCurrent(window);
	    GL.createCapabilities();

	    // Report client API version

	    api = glfwGetWindowAttrib(window, GLFW_CLIENT_API);
	    major = glfwGetWindowAttrib(window, GLFW_CONTEXT_VERSION_MAJOR);
	    minor = glfwGetWindowAttrib(window, GLFW_CONTEXT_VERSION_MINOR);
	    revision = glfwGetWindowAttrib(window, GLFW_CONTEXT_REVISION);

	    printf("%s context version string: \"%s\"\n",
	           get_client_api_name(api),
	           glGetString(GL_VERSION));

	    printf("%s context version parsed by GLFW: %d.%d.%d\n",
	           get_client_api_name(api),
	           major, minor, revision);

	    // Report client API context properties

	    if (api == GLFW_OPENGL_API)
	    {
	        if (major >= 3)
	        {
	        	flags = glGetInteger(GL_CONTEXT_FLAGS);
	            printf("%s context flags (0x%08x):", get_client_api_name(api), flags);

	            if ((flags & GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT) != 0)
	                printf(" forward-compatible");
	            if ((flags & GL_CONTEXT_FLAG_DEBUG_BIT) != 0)
	                printf(" debug");
	            if ((flags & GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT_ARB) != 0)
	                printf(" robustness");
	            printf("\n");

	            printf("%s context flags parsed by GLFW:", get_client_api_name(api));

	            if (glfwGetWindowAttrib(window, GLFW_OPENGL_FORWARD_COMPAT) != 0)
	                printf(" forward-compatible");
	            if (glfwGetWindowAttrib(window, GLFW_OPENGL_DEBUG_CONTEXT) != 0)
	                printf(" debug");
	            if (glfwGetWindowAttrib(window, GLFW_CONTEXT_ROBUSTNESS) != GLFW_NO_ROBUSTNESS)
	                printf(" robustness");
	            printf("\n");
	        }

	        if (major > 3 || (major == 3 && minor >= 2))
	        {
	            profile = glfwGetWindowAttrib(window, GLFW_OPENGL_PROFILE);

	            mask = glGetInteger(GL_CONTEXT_PROFILE_MASK);
	            printf("%s profile mask (0x%08x): %s\n",
	                   get_client_api_name(api),
	                   mask,
	                   get_profile_name_gl(mask));

	            printf("%s profile mask parsed by GLFW: %s\n",
	                   get_client_api_name(api),
	                   get_profile_name_glfw(profile));
	        }

	        if (glfwExtensionSupported("GL_ARB_robustness") != 0)
	        {
	            int robustness;
	            strategy = glGetInteger(GL_RESET_NOTIFICATION_STRATEGY_ARB);

	            printf("%s robustness strategy (0x%08x): %s\n",
	                   get_client_api_name(api),
	                   strategy,
	                   get_strategy_name_gl(strategy));

	            robustness = glfwGetWindowAttrib(window, GLFW_CONTEXT_ROBUSTNESS);

	            printf("%s robustness strategy parsed by GLFW: %s\n",
	                   get_client_api_name(api),
	                   get_strategy_name_glfw(robustness));
	        }
	    }

	    printf("%s context renderer string: \"%s\"\n",
	           get_client_api_name(api),
	           glGetString(GL_RENDERER));
	    printf("%s context vendor string: \"%s\"\n",
	           get_client_api_name(api),
	           glGetString(GL_VENDOR));

	    if (major > 1)
	    {
	        printf("%s context shading language version: \"%s\"\n",
	               get_client_api_name(api),
	               glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
	    }

	    // Report client API extensions
	    if (list)
	        list_extensions(api, major, minor);

	    glfwTerminate();
	    exit(EXIT_SUCCESS);
	}
}
