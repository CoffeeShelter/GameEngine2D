package myGameEngine2D.render;

import org.joml.Vector2f;

import myGameEngine2D.components.Sprite;
import myGameEngine2D.components.SpriteComponent;
import myGameEngine2D.game.GameObject;

public class Prefabs {
	public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {
		GameObject block = new GameObject("Sprite_Object_Gen",new Transform(new Vector2f(), new Vector2f(sizeX, sizeY)),0);
		SpriteComponent renderer = new SpriteComponent();
		renderer.setSprite(sprite);
		block.addComponent(renderer);
		
		return block;
	}
}
