package myGameEngine2D.textures;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

public class Texture {
	private String filepath = null;
	private transient int texID;
	private int width, height;

	public Texture() {
		texID = -1;
		width = -1;
		height = -1;
	}

	public Texture(int width, int height) {
		this.filepath = "Generated";

		// Generate texture on GPU
		texID = glGenTextures();
		glBindTexture(GL11.GL_TEXTURE_2D, texID);
		
		glTexParameteri(GL_TEXTURE_2D,  GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D,  GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0);
	}

	public void init(String filepath) {
		this.filepath = filepath;

		// Generate texture on GPU
		texID = glGenTextures();
		glBindTexture(GL11.GL_TEXTURE_2D, texID);

		// Set texture parameters
		// Repeat image in both directions
		glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		// When stretching the image, pixelate
		glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		// When shrinking an image, pixelate
		glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer channels = BufferUtils.createIntBuffer(1);
		STBImage.stbi_set_flip_vertically_on_load(true);
		ByteBuffer image = STBImage.stbi_load(filepath, width, height, channels, 0);

		if (image != null) {
			this.width = width.get(0);
			this.height = height.get(0);

			if (channels.get(0) == 3) {
				glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width.get(0), height.get(0), 0, GL11.GL_RGB,
						GL11.GL_UNSIGNED_BYTE, image);
			} else if (channels.get(0) == 4) {
				glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 0, GL11.GL_RGBA,
						GL11.GL_UNSIGNED_BYTE, image);
			} else {
				assert false : "Error: (Texture) Unknown number of channesl '" + channels.get(0) + "'";
			}
		} else {
			assert false : "Error: (Texture) Could not load image '" + filepath + "'";
		}

		STBImage.stbi_image_free(image);
	}

	public void bind() {
		glBindTexture(GL11.GL_TEXTURE_2D, texID);
	}

	public void unbind() {
		glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public int getWidth() {
		return this.width;
	}

	public String getFilepath() {
		return this.filepath;
	}

	public int getHeight() {
		return this.height;
	}

	public int getId() {
		return texID;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Texture))
			return false;
		Texture oTex = (Texture) o;
		return oTex.getWidth() == this.width && oTex.getHeight() == this.height && oTex.getId() == this.texID
				&& oTex.getFilepath() == this.filepath;
	}
}
