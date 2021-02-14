package myGameEngine2D.editor;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import imgui.ImGui;
import myGameEngine2D.game.GameObject;
import myGameEngine2D.listener.MouseListener;
import myGameEngine2D.render.PickingTexture;
import myGameEngine2D.scene.Scene;

public class PropertiesWindow {
	protected GameObject activeGameObject = null;
	private PickingTexture pickingTexture;
	
	public PropertiesWindow(PickingTexture pickingTexture) {
		
		this.pickingTexture = pickingTexture;
	}
	
	public void update(float deltaTime, Scene currentScene) {
		if(MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
			int x = (int) MouseListener.getScreenX();
			int y = (int) MouseListener.getScreenY();
			int gameObjectId = pickingTexture.readPixel(x, y);
			activeGameObject = currentScene.getGameObject(gameObjectId);
		}
		
	}
	
	public void imgui() {
		if (activeGameObject != null) {
			ImGui.begin("Properties");
			activeGameObject.imgui();
			ImGui.end();
		}
	}
}
