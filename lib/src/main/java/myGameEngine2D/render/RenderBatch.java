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

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL15;

import myGameEngine2D.components.SpriteComponent;
import myGameEngine2D.shader.Shader;
import myGameEngine2D.textures.Texture;
import myGameEngine2D.util.AssetPool;
import myGameEngine2D.window.Window;

public class RenderBatch implements Comparable<RenderBatch>{
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

	public RenderBatch(int maxBatchSize, int zIndex){
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
		// vao ����
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);

		// vertices �Ҵ�
		vboID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

		// index ���� ����
		int eboID = GL15.glGenBuffers();
		int[] indices = generatorIndices();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

		// ���� �Ӽ� Ȱ��ȭ
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
		// ��������Ʈ �߰�
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
        
        for (int i=0; i < numSprites; i++) {
            SpriteComponent spr = sprites[i];
            if (spr.isDirty()) {
                loadVertexProperties(i);
                spr.setClean();
                rebufferData = true;
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
        for (int i=0; i < textures.size(); i++) {
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

        for (int i=0; i < textures.size(); i++) {
            textures.get(i).unbind();
        }
        shader.stop();
	}

	private void loadVertexProperties(int index) {
		SpriteComponent sprite = this.sprites[index];

		// Find Offset within array ( 4 vertices per sprite )
		int offset = index * 4 * VERTEX_SIZE;

		Vector4f color = sprite.getColor();
		Vector2f[] texCoords = sprite.getTexCoords();

		// �ؽ�ó Id 0 �� �ؽ�ó ���� ���� ���
		int texId = 0;
        if (sprite.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i).equals(sprite.getTexture())) {
                    texId = i + 1;
                    break;
                }
            }
        }

		// Add vertice with the appropriate properties
		float xAdd = 1.0f;
		float yAdd = 1.0f;
		for (int i = 0; i < 4; i++) {
			if (i == 1) {
				yAdd = 0.0f;
			} else if (i == 2) {
				xAdd = 0.0f;
			} else if (i == 3) {
				yAdd = 1.0f;
			}

			// ��ġ
			vertices[offset] = sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x);
			vertices[offset + 1] = sprite.gameObject.transform.position.y
					+ (yAdd * sprite.gameObject.transform.scale.y);

			// ����
			vertices[offset + 2] = color.x;
			vertices[offset + 3] = color.y;
			vertices[offset + 4] = color.z;
			vertices[offset + 5] = color.w;

			// �ؽ�ó ��ǥ
			vertices[offset + 6] = texCoords[i].x;
			vertices[offset + 7] = texCoords[i].y;

			// �ؽ�ó ID
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