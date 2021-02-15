package myGameEngine2D.window;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import myGameEngine2D.gui.ImGuiLayer;
import myGameEngine2D.listener.KeyListener;
import myGameEngine2D.listener.MouseListener;
import myGameEngine2D.render.DebugDraw;
import myGameEngine2D.render.Framebuffer;
import myGameEngine2D.render.PickingTexture;
import myGameEngine2D.render.Renderer;
import myGameEngine2D.scene.LevelEditorScene;
import myGameEngine2D.scene.LevelScene;
import myGameEngine2D.scene.Scene;
import myGameEngine2D.shader.Shader;
import myGameEngine2D.util.AssetPool;

public class Window {
	private int width, height;
	private String title;
	private long glfwWindow;
	private ImGuiLayer imguiLayer;
	private Framebuffer framebuffer;
	private PickingTexture pickingTexture;

	public float r, g, b, a;

	private static Window window = null;

	private static Scene currentScene = null;

	private Window() {
		this.width = 1280;
		this.height = 720;
		this.title = "MyGame";

		r = 0.8f;
		g = 0.8f;
		b = 0.8f;
		a = 0.8f;
	}

	public static void changeScene(int newScene) {
		switch (newScene) {
		case 0:
			currentScene = new LevelEditorScene();
			break;

		case 1:
			currentScene = new LevelScene();
			break;

		default:
			assert false : "Unknown scene '" + newScene + "'";
			break;
		}

		currentScene.load();
		currentScene.init();
		currentScene.start();
	}

	public static Window get() {
		if (Window.window == null) {
			Window.window = new Window();
		}

		return Window.window;
	}

	public static Scene getScene() {
		return Window.currentScene;
	}

	public void run() {
		System.out.println("Hello Lwjgl " + Version.getVersion() + "!");

		init();
		loop();

		// 메모리 해제
		glfwDestroyWindow(glfwWindow);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	public void init() {
		// 에러 콜백 세팅
		GLFWErrorCallback.createPrint(System.err).set();

		// GLFW 초기화
		if (!glfwInit()) {
			throw new IllegalStateException("GLFW 초기화 실패");
		}

		// GLFW 설정
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

		// 윈도우 생성
		glfwWindow = glfwCreateWindow(this.width, this.height, this.title, 0, 0);
		if (glfwWindow == 0) {
			throw new IllegalStateException("윈도우 생성 실패");
		}

		glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
		glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
		glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
		glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
		glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
			Window.setWidth(newWidth);
			Window.setHeight(newHeight);
		});

		// context 생성
		glfwMakeContextCurrent(glfwWindow);
		// v-sync 활성
		glfwSwapInterval(1);

		// 윈도우창 띄우기
		glfwShowWindow(glfwWindow);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabillties instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

		this.framebuffer = new Framebuffer(1920, 1080);
		this.pickingTexture = new PickingTexture(1920, 1080);
		glViewport(0, 0, 1920, 1080);

		this.imguiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
		this.imguiLayer.initGui();
		
		Window.changeScene(0);
	}

	public void loop() {
		// float beginTime = Time.getTime();
		float beginTime = (float) glfwGetTime();
		float endTime;
		float deltaTime = -1.0f;

		Shader defalutShader = AssetPool.getShader("Shaders/default.glsl");
		Shader pickingShader = AssetPool.getShader("Shaders/pickingShader.glsl");
		
		while (!glfwWindowShouldClose(glfwWindow)) {
			glfwPollEvents();

			// Render pass 1. Render to picking texture
			glDisable(GL_BLEND);
			pickingTexture.enableWriting();

			glViewport(0, 0, 1920, 1080);
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			Renderer.bindShader(pickingShader);
			currentScene.render();
			
			pickingTexture.disableWriting();
			glEnable(GL_BLEND);
			
			// Render pass 2. Render actual game

			DebugDraw.beginFrame();

			this.framebuffer.bind();

			glClearColor(r, g, b, a);
			glClear(GL_COLOR_BUFFER_BIT);

			if (deltaTime >= 0) {
				DebugDraw.draw();
				Renderer.bindShader(defalutShader);
				currentScene.Update(deltaTime);
				currentScene.render();
			}
			this.framebuffer.unbind();

			this.imguiLayer.update(deltaTime, currentScene);
			glfwSwapBuffers(glfwWindow);
			MouseListener.endFrame();
			
			// endTime = Time.getTime();
			endTime = (float) glfwGetTime();
			deltaTime = endTime - beginTime;
			beginTime = endTime;
		}

		currentScene.saveExit();
	}

	public static int getWidth() {
		return get().width;
	}

	public static int getHeight() {
		return get().height;
	}

	public static void setWidth(int newWidth) {
		get().width = newWidth;
	}

	public static void setHeight(int newHeight) {
		get().height = newHeight;
	}

	public static Framebuffer getFramebuffer() {
		return get().framebuffer;
	}

	public static float getTargetAspectRatio() {
		return 16.0f / 9.0f;
	}
	
	public static ImGuiLayer getImguiLayer() {
		return get().imguiLayer;
	}
}
