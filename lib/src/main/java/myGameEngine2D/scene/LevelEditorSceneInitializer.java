package myGameEngine2D.scene;

import org.joml.Vector2f;

import imgui.ImGui;
import imgui.ImVec2;
import myGameEngine2D.camera.Camera;
import myGameEngine2D.components.EditorCamera;
import myGameEngine2D.components.GizmoSystem;
import myGameEngine2D.components.GridLines;
import myGameEngine2D.components.MouseControls;
import myGameEngine2D.components.Sprite;
import myGameEngine2D.components.SpriteComponent;
import myGameEngine2D.components.Spritesheet;
import myGameEngine2D.game.GameObject;
import myGameEngine2D.render.Prefabs;
import myGameEngine2D.render.Transform;
import myGameEngine2D.util.AssetPool;

public class LevelEditorSceneInitializer extends SceneInitializer {

	// private GameObject obj1;
	private Spritesheet sprites;
	SpriteComponent obj1Sprite;

	private GameObject levelEditorStuff;

	public LevelEditorSceneInitializer() {

	}

	@Override
	public void init(Scene scene) {
		sprites = AssetPool.getSpritesheet("res/spritesheets/decorationsAndBlocks.png");
		Spritesheet gizmos = AssetPool.getSpritesheet("res/spritesheets/gizmos.png");
		
		levelEditorStuff = scene.createGameObject("LevelEditor");
		levelEditorStuff.setNoSerialize();
		levelEditorStuff.addComponent(new MouseControls());
		levelEditorStuff.addComponent(new GridLines());
		levelEditorStuff.addComponent(new EditorCamera(scene.camera()));
		levelEditorStuff.addComponent(new GizmoSystem(gizmos));
		scene.addGameObjectToScene(levelEditorStuff);
	}
	
	@Override
	public void loadResources(Scene scene) {
		AssetPool.getShader("shaders/default.glsl");

		AssetPool.addSpritesheet("res/spritesheets/decorationsAndBlocks.png",
				new Spritesheet(AssetPool.getTexture("res/spritesheets/decorationsAndBlocks.png"), 16, 16, 81, 0));
		AssetPool.addSpritesheet("res/spritesheets/gizmos.png",
				new Spritesheet(AssetPool.getTexture("res/spritesheets/gizmos.png"), 24, 48, 3, 0));
		AssetPool.getTexture("res/blendImage2.png");

		for (GameObject g : scene.getGameObjects()) {
			if (g.getComponent(SpriteComponent.class) != null) {
				SpriteComponent spr = g.getComponent(SpriteComponent.class);
				if (spr.getTexture() != null) {
					spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilepath()));
				}
			}
		}
	}

	@Override
	public void imgui() {
		ImGui.begin("Level Editor Stuff");
		levelEditorStuff.imgui();
		ImGui.end();

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
				GameObject object = Prefabs.generateSpriteObject(sprite, 0.25f, 0.25f);
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