package myGameEngine2D.editor;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import imgui.ImGui;
import myGameEngine2D.components.NonPickable;
import myGameEngine2D.game.GameObject;
import myGameEngine2D.listener.MouseListener;
import myGameEngine2D.render.PickingTexture;
import myGameEngine2D.scene.Scene;

public class PropertiesWindow {
	protected GameObject activeGameObject = null;
	private PickingTexture pickingTexture;

	private float debounce = 0.2f;

	public PropertiesWindow(PickingTexture pickingTexture) {

		this.pickingTexture = pickingTexture;
	}

	public void update(float deltaTime, Scene currentScene) {
		debounce -= deltaTime;

		if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
			int x = (int) MouseListener.getScreenX();
			int y = (int) MouseListener.getScreenY();
			int gameObjectId = pickingTexture.readPixel(x, y);
			GameObject pickedObj = currentScene.getGameObject(gameObjectId);
			if (pickedObj != null && pickedObj.getComponent(NonPickable.class) == null) {
				activeGameObject = pickedObj;
			} else if (pickedObj == null && !MouseListener.isDragging()) {
				activeGameObject = null;
			}
			this.debounce = 0.2f;
		}

	}

	public void imgui() {
		if (activeGameObject != null) {
			ImGui.begin("Properties");
			activeGameObject.imgui();
			ImGui.end();
		}
	}

	public GameObject getActiveGameObject() {
		return this.activeGameObject;
	}
}
