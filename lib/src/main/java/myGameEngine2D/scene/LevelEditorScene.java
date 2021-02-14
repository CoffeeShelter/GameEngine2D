package myGameEngine2D.scene;

import org.joml.Vector2f;

import imgui.ImGui;
import imgui.ImVec2;
import myGameEngine2D.camera.Camera;
import myGameEngine2D.components.EditorCamera;
import myGameEngine2D.components.GridLines;
import myGameEngine2D.components.MouseControls;
import myGameEngine2D.components.Sprite;
import myGameEngine2D.components.SpriteComponent;
import myGameEngine2D.components.Spritesheet;
import myGameEngine2D.game.GameObject;
import myGameEngine2D.render.Prefabs;
import myGameEngine2D.render.Transform;
import myGameEngine2D.util.AssetPool;

public class LevelEditorScene extends Scene {

	// private GameObject obj1;
	private Spritesheet sprites;
	SpriteComponent obj1Sprite;

	GameObject levelEditorStuff = new GameObject("LevelEditor", new Transform(new Vector2f()), 0);

	public LevelEditorScene() {

	}

	@Override
	public void init() {
		this.camera = new Camera(new Vector2f(-250, 0));
		
		levelEditorStuff.addComponent(new MouseControls());
		levelEditorStuff.addComponent(new GridLines());
		levelEditorStuff.addComponent(new EditorCamera(this.camera));

		loadResources();
		
		sprites = AssetPool.getSpritesheet("res/spritesheets/decorationsAndBlocks.png");

//		obj1 = new GameObject("Object 1", new Transform(new Vector2f(200, 100), new Vector2f(256, 256)), 2);
//		obj1Sprite = new SpriteComponent();
//		obj1Sprite.setColor(new Vector4f(1, 0, 0, 1));
//		// obj1.addComponent(new Rigidbody());
//		obj1.addComponent(obj1Sprite);
//		this.addGameObjectToScene(obj1);
//		this.activeGameObject = obj1;
//
//		GameObject obj2 = new GameObject("Object 2", new Transform(new Vector2f(400, 100), new Vector2f(256, 256)), 3);
//		SpriteComponent obj2SpriteComponent = new SpriteComponent();
//		Sprite obj2Sprite = new Sprite();
//		obj2Sprite.setTexture(AssetPool.getTexture("res/blendImage2.png"));
//		obj2SpriteComponent.setSprite(obj2Sprite);
//		obj2.addComponent(obj2SpriteComponent);
//		this.addGameObjectToScene(obj2);
	}

	private void loadResources() {
		AssetPool.getShader("shaders/default.glsl");

		AssetPool.addSpritesheet("res/spritesheets/decorationsAndBlocks.png",
				new Spritesheet(AssetPool.getTexture("res/spritesheets/decorationsAndBlocks.png"), 16, 16, 81, 0));

		AssetPool.getTexture("res/blendImage2.png");

		for (GameObject g : gameObjects) {
			if (g.getComponent(SpriteComponent.class) != null) {
				SpriteComponent spr = g.getComponent(SpriteComponent.class);
				if (spr.getTexture() != null) {
					spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilepath()));
				}
			}
		}
	}

	@Override
	public void Update(float deltaTime) {
		levelEditorStuff.update(deltaTime);
		this.camera.adjustProjection();
		
		for (GameObject go : this.gameObjects) {
			go.update(deltaTime);
		}
	}

	@Override
	public void render() {
		this.renderer.render();
	}
	
	@Override
	public void imgui() {
		ImGui.begin("Test Window");

		ImVec2 windowPos = new ImVec2();
		ImGui.getWindowPos(windowPos);
		ImVec2 windowSize = new ImVec2();
		ImGui.getWindowSize(windowSize);
		ImVec2 itemSpacing = new ImVec2();
		ImGui.getStyle().getItemSpacing(itemSpacing);

		float windowX2 = windowPos.x + windowSize.x;
		for (int i = 0; i < sprites.size(); i++) {
			Sprite sprite = sprites.getSprite(i);
			float spriteWidth = sprite.getWidth() * 2;
			float spriteHeight = sprite.getHeight() * 2;
			int id = sprite.getTexId();
			Vector2f[] texCoords = sprite.getTexCoords();

			ImGui.pushID(i);
			if (ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x,
					texCoords[2].y)) {
				GameObject object = Prefabs.generateSpriteObject(sprite, 32, 32);
				levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);
			}
			ImGui.popID();

			ImVec2 lastButtonPos = new ImVec2();
			ImGui.getItemRectMax(lastButtonPos);
			float lastButtonX2 = lastButtonPos.x;
			float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
			if (i + 1 < sprites.size() && nextButtonX2 < windowX2) {
				ImGui.sameLine();
			}
		}

		ImGui.end();
	}

}