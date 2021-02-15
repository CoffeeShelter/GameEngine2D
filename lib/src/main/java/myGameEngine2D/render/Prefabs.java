package myGameEngine2D.render;

import org.joml.Vector2f;

import myGameEngine2D.components.Sprite;
import myGameEngine2D.components.SpriteComponent;
import myGameEngine2D.game.GameObject;
import myGameEngine2D.window.Window;

public class Prefabs {
	public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {
		GameObject block = Window.getScene().createGameObject("Sprite_Object_Gen");
		block.transform.scale.x = sizeX;
		block.transform.scale.y = sizeY;
		SpriteComponent renderer = new SpriteComponent();
		renderer.setSprite(sprite);
		block.addComponent(renderer);
		
		return block;
	}
}
