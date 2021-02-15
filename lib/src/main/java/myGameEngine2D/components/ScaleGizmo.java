package myGameEngine2D.components;

import myGameEngine2D.editor.PropertiesWindow;
import myGameEngine2D.listener.MouseListener;

public class ScaleGizmo extends Gizmo{
	public ScaleGizmo(Sprite scaleSprite, PropertiesWindow propertiesWindow) {
		super(scaleSprite, propertiesWindow);
	}


	@Override
	public void update(float deltaTime) {
		
		if(activeGameObject != null) {
			if(xAxisActive && !yAxisActive) {
				activeGameObject.transform.scale.x -= MouseListener.getWorldDx();
			}else if (yAxisActive) {
				activeGameObject.transform.scale.y -= MouseListener.getWorldDy();
			}
		}
		
		super.update(deltaTime);
		
	}
}
