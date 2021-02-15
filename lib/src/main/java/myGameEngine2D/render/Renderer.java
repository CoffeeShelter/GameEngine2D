package myGameEngine2D.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import myGameEngine2D.components.SpriteComponent;
import myGameEngine2D.game.GameObject;
import myGameEngine2D.shader.Shader;
import myGameEngine2D.textures.Texture;

public class Renderer {
	private final int MAX_BATCH_SIZE = 1000;
	private List<RenderBatch> batches;
	private static Shader currentShader;
	
	public Renderer() {
		this.batches = new ArrayList<>();
	}

	public void add(GameObject go) {
		SpriteComponent spr = go.getComponent(SpriteComponent.class);
		if (spr != null) {
			add(spr);
		}
	}

	public void add(SpriteComponent sprite) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.hasRoom() && batch.zIndex() == sprite.gameObject.transform.zIndex) {
                Texture tex = sprite.getTexture();
                if (tex == null || (batch.hasTexture(tex) || batch.hasTextureRoom())) {
                    batch.addSprite(sprite);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.transform.zIndex);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(sprite);
            Collections.sort(batches);
        }
	}

	public static void bindShader(Shader shader) {
		currentShader = shader;
	}
	
	public static Shader getBoundShader() {
		return currentShader;
	}
	
	public void render() {
		currentShader.start();
		for (RenderBatch batch : batches) {
			batch.render();
		}
	}
}
