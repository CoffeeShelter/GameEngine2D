package myGameEngine2D.components;

import myGameEngine2D.editor.PropertiesWindow;
import myGameEngine2D.listener.MouseListener;

public class TranslateGizmo extends Gizmo {

	public TranslateGizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
		super(arrowSprite, propertiesWindow);
	}


	@Override
	public void editorUpdate(float deltaTime) {
		
		if(activeGameObject != null) {
			if(xAxisActive && !yAxisActive) {
				activeGameObject.transform.position.x -= MouseListener.getWorldDx();
			}else if (yAxisActive) {
				activeGameObject.transform.position.y -= MouseListener.getWorldDy();
			}
		}
		
		super.editorUpdate(deltaTime);
		
	}

}
