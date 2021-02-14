package myGameEngine2D.components;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DECIMAL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

import org.joml.Vector2f;

import myGameEngine2D.camera.Camera;
import myGameEngine2D.listener.KeyListener;
import myGameEngine2D.listener.MouseListener;

public class EditorCamera extends Component {

	private float dragDebounce = 0.032f;

	private Camera levelEditorCamera;
	private Vector2f clickOrigin;
	private boolean reset = false;
	
	private float lerpTime = 0.0f;
	private float dragSensitivity = 30.0f;
	private float scrollSensitivity = 0.1f;

	public EditorCamera(Camera levelEditorCamera) {
		this.levelEditorCamera = levelEditorCamera;
		this.clickOrigin = new Vector2f();
	}

	@Override
	public void update(float deltaTime) {
		if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE) && dragDebounce > 0) {
			this.clickOrigin = new Vector2f(MouseListener.getOrthoX(), MouseListener.getOrthoY());
			dragDebounce -= deltaTime;
			return;
		} else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
			Vector2f mousePos = new Vector2f(MouseListener.getOrthoX(), MouseListener.getOrthoY());
			Vector2f delta = new Vector2f(mousePos).sub(this.clickOrigin);
			levelEditorCamera.position.sub(delta.mul(deltaTime).mul(dragSensitivity));
			this.clickOrigin.lerp(mousePos, deltaTime);
		}
		
		if(dragDebounce <= 0.0f && !MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
			dragDebounce = 0.1f;
		}
		
		if(MouseListener.getScrollY() != 0.0f) {
			float addValue = (float)Math.pow(Math.abs(MouseListener.getScrollY() * scrollSensitivity),
					1/ levelEditorCamera.getZoom());
			addValue *= -Math.signum(MouseListener.getScrollY());
			levelEditorCamera.addZoom(addValue);
		}
		
		if(KeyListener.isKeyPressed(GLFW_KEY_KP_DECIMAL)) {
			reset = true;
		}
		
		if(reset) {
			levelEditorCamera.position.lerp(new Vector2f(), lerpTime);
			levelEditorCamera.setZoom(this.levelEditorCamera.getZoom() + 
					((1.0f - levelEditorCamera.getZoom()) * lerpTime));
			this.lerpTime += 0.1f * deltaTime;
			
			if(Math.abs(levelEditorCamera.position.x) <= 5.0f &&
					Math.abs(levelEditorCamera.position.y) <= 5.0f) {
				this.lerpTime = 0.0f;
				levelEditorCamera.position.set(0f, 0f);
				this.levelEditorCamera.setZoom(1.0f);
				reset = false;
			}
		}
	}
}
