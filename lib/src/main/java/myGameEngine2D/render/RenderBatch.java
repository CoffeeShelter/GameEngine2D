package myGameEngine2D.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL15;

import myGameEngine2D.components.SpriteComponent;
import myGameEngine2D.game.GameObject;
import myGameEngine2D.shader.Shader;
import myGameEngine2D.textures.Texture;
import myGameEngine2D.window.Window;

public class RenderBatch implements Comparable<RenderBatch> {
	// Vertex
	// ======
	// Pos Color tex Coords tex id
	// float, float, float, float, float, float, float, float, float
	private final int POS_SIZE = 2;
	private final int COLOR_SIZE = 4;
	private final int TEX_COORDS_SIZE = 2;
	private final int TEX_ID_SIZE = 1;
	private final int ENTITY_ID_SIZE = 1;

	private final int POS_OFFSET = 0;
	private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
	private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
	private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
	private final int ENTITY_ID_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;
	private final int VERTEX_SIZE = 10;
	private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

	private SpriteComponent[] sprites;
	private int numSprites;
	private boolean hasRoom;
	private float[] vertices;
	private int[] texSlots = { 0, 1, 2, 3, 4, 5, 6, 7 };

	private List<Texture> textures;
	private int vaoID, vboID;
	private int maxBatchSize;
	private int zIndex;

	private Renderer renderer;
	
	public RenderBatch(int maxBatchSize, int zIndex, Renderer renderer) {
		this.renderer = renderer;
		
		this.zIndex = zIndex;
		this.sprites = new SpriteComponent[maxBatchSize];
		this.maxBatchSize = maxBatchSize;
		
		// 4 vertices quads
		vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

		this.numSprites = 0;
		this.hasRoom = true;
		this.textures = new ArrayList<>();
	}

	public void start() {
		// vao 생성
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);

		// vertices 할당
		vboID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

		// index 버퍼 생성
		int eboID = GL15.glGenBuffers();
		int[] indices = generatorIndices();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

		// 버퍼 속성 활성화
		// position
		glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
		glEnableVertexAttribArray(0);
		// color
		glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		glEnableVertexAttribArray(2);

		glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
		glEnableVertexAttribArray(3);

		glVertexAttribPointer(4, ENTITY_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, ENTITY_ID_OFFSET);
		glEnableVertexAttribArray(4);
	}

	public void addSprite(SpriteComponent spc) {
		// 스프라이트 추가
		int index = this.numSprites;
		this.sprites[index] = spc;
		this.numSprites++;

		if (spc.getTexture() != null) {
			if (!textures.contains(spc.getTexture())) {
				textures.add(spc.getTexture());
			}
		}

		// Add properties to local vertices array
		loadVertexProperties(index);

		if (numSprites >= this.maxBatchSize) {
			this.hasRoom = false;
		}
	}

	public void render() {
		boolean rebufferData = false;

		for (int i = 0; i < numSprites; i++) {
			SpriteComponent spr = sprites[i];
			if (spr.isDirty()) {
				loadVertexProperties(i);
				spr.setClean();
				rebufferData = true;
			}
			
			//TODO: get better solution for this
			if(spr.gameObject.transform.zIndex != this.zIndex) {
				destroyIfExists(spr.gameObject);
				renderer.add(spr.gameObject);
				i --;
			}
		}

		if (rebufferData) {
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices);
		}

		// Use shader
		Shader shader = Renderer.getBoundShader();
		shader.start();
		shader.setMatrix4f("uProjection", Window.getScene().camera().getProjectionMatrix());
		shader.setMatrix4f("uView", Window.getScene().camera().getViewMatrix());
		for (int i = 0; i < textures.size(); i++) {
			GL15.glActiveTexture(GL15.GL_TEXTURE0 + i + 1);
			textures.get(i).bind();
		}
		shader.setIntArray("uTextures", texSlots);

		glBindVertexArray(vaoID);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);

		for (int i = 0; i < textures.size(); i++) {
			textures.get(i).unbind();
		}
		shader.stop();
	}

	public boolean destroyIfExists(GameObject go) {
		SpriteComponent sprite = go.getComponent(SpriteComponent.class);
		for (int i = 0; i < numSprites; i++) {
			if (sprites[i] == sprite) {
				for (int j = i; j < numSprites - 1; j++) {
					sprites[j] = sprites[j + 1];
					sprites[j].setDirty();
				}
				numSprites--;
				return true;
			}
		}
		
		return false;
	}

	private void loadVertexProperties(int index) {
		SpriteComponent sprite = this.sprites[index];

		// Find Offset within array ( 4 vertices per sprite )
		int offset = index * 4 * VERTEX_SIZE;

		Vector4f color = sprite.getColor();
		Vector2f[] texCoords = sprite.getTexCoords();

		// 텍스처 Id 0 은 텍스처 없이 색상만 출력
		int texId = 0;
		if (sprite.getTexture() != null) {
			for (int i = 0; i < textures.size(); i++) {
				if (textures.get(i).equals(sprite.getTexture())) {
					texId = i + 1;
					break;
				}
			}
		}

		boolean isRotated = sprite.gameObject.transform.rotation != 0.0f;
		Matrix4f transformMatrix = new Matrix4f().identity();
		if (isRotated) {
			transformMatrix.translate(sprite.gameObject.transform.position.x, sprite.gameObject.transform.position.y,
					0);
			transformMatrix.rotate((float) Math.toRadians(sprite.gameObject.transform.rotation), 0, 0, 1);
			transformMatrix.scale(sprite.gameObject.transform.scale.x, sprite.gameObject.transform.scale.y, 0);
		}

		// Add vertice with the appropriate properties
		float xAdd = 0.5f;
		float yAdd = 0.5f;
		for (int i = 0; i < 4; i++) {
			if (i == 1) {
				yAdd = -0.5f;
			} else if (i == 2) {
				xAdd = -0.5f;
			} else if (i == 3) {
				yAdd = 0.5f;
			}

			Vector4f currentPos = new Vector4f(
					sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x),
					sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y), 0, 1);
			if (isRotated) {
				currentPos = new Vector4f(xAdd, yAdd, 0, 1).mul(transformMatrix);
			}

			// 위치
			vertices[offset] = currentPos.x;
			vertices[offset + 1] = currentPos.y;

			// 색상
			vertices[offset + 2] = color.x;
			vertices[offset + 3] = color.y;
			vertices[offset + 4] = color.z;
			vertices[offset + 5] = color.w;

			// 텍스처 좌표
			vertices[offset + 6] = texCoords[i].x;
			vertices[offset + 7] = texCoords[i].y;

			// 텍스처 ID
			vertices[offset + 8] = texId;

			// Entity ID
			vertices[offset + 9] = sprite.gameObject.getUid() + 1;

			offset += VERTEX_SIZE;
		}
	}

	private int[] generatorIndices() {
		// 6 indices per quad ( 3 per triangle )
		int[] elements = new int[6 * maxBatchSize];

		for (int i = 0; i < maxBatchSize; i++) {
			loadElementIndices(elements, i);
		}

		return elements;
	}

	private void loadElementIndices(int[] elements, int index) {
		int offsetArrayIndex = 6 * index;
		int offset = 4 * index;

		// 3, 2, 0, 0, 2, 1 7, 6, 4, 4, 6, 5
		// Triangle 1
		elements[offsetArrayIndex] = offset + 3;
		elements[offsetArrayIndex + 1] = offset + 2;
		elements[offsetArrayIndex + 2] = offset + 0;

		elements[offsetArrayIndex + 3] = offset + 0;
		elements[offsetArrayIndex + 4] = offset + 2;
		elements[offsetArrayIndex + 5] = offset + 1;

	}

	public boolean hasRoom() {
		return this.hasRoom;
	}

	public boolean hasTextureRoom() {
		return this.textures.size() < 8;
	}

	public boolean hasTexture(Texture texture) {
		return this.textures.contains(texture);
	}

	public int zIndex() {
		return this.zIndex;
	}

	@Override
	public int compareTo(RenderBatch o) {
		return Integer.compare(this.zIndex, o.zIndex);
	}
}
