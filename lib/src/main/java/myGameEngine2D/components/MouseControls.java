package myGameEngine2D.components;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import myGameEngine2D.game.GameObject;
import myGameEngine2D.listener.MouseListener;
import myGameEngine2D.util.Settings;
import myGameEngine2D.window.Window;

public class MouseControls extends Component {
	GameObject holdingObject = null;

	public void pickupObject(GameObject go) {
		this.holdingObject = go;
		Window.getScene().addGameObjectToScene(go);
	}

	public void place() {
		this.holdingObject = null;
	}

	@Override
	public void update(float deltaTime) {
		if (holdingObject != null) {
			holdingObject.transform.position.x = MouseListener.getOrthoX();
			holdingObject.transform.position.y = MouseListener.getOrthoY();
			holdingObject.transform.position.x = (int) (holdingObject.transform.position.x / Settings.GRID_WIDTH)
					* Settings.GRID_WIDTH;
			holdingObject.transform.position.y = (int) (holdingObject.transform.position.y / Settings.GRID_HEIGHT)
					* Settings.GRID_HEIGHT;

			if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
				place();
			}
		}
	}
}
