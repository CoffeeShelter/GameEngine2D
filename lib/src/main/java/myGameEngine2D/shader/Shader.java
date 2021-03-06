package myGameEngine2D.shader;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;

	// shader program 상에서 position 과 color에 해당하는 attrib index
	public int positionAttribIndex;
	public int colorAttribIndex;
	
	public int getProgramID() {
		return programID;
	}

	private String vertexSource;
	private String fragmentSource;
	private String filepath;
	
	public Shader(String filepath) {
		this.filepath = filepath;
		try {
			String source = new String(Files.readAllBytes(Paths.get(filepath)));
			String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

			int index = source.indexOf("#type") + 6;
			int eol = source.indexOf("\r\n",index);
			String firstPattern = source.substring(index,eol).trim();

			index = source.indexOf("#type", eol) + 6;
			eol = source.indexOf("\r\n",index);
			String secondPattern = source.substring(index,eol).trim();
			
			if(firstPattern.equals("vertex")) {
				vertexSource = splitString[1];
			}else if(firstPattern.equals("fragment")) {
				fragmentSource = splitString[1];
			}else {
				throw new IOException("Unexpected token '" + firstPattern + "'");
			}
			
			if(secondPattern.equals("vertex")) {
				vertexSource = splitString[2];
			}else if(secondPattern.equals("fragment")) {
				fragmentSource = splitString[2];
			}else {
				throw new IOException("Unexpected token '" + secondPattern + "'");
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			assert false : "쉐이더 파일을 찾을 수 없습니다. " + filepath;
		}
		
		compile();
		
		programID = GL20.glCreateProgram();

		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);

		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);

		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		
		
	}
	
	public Shader(String vertexShaderFile, String fragmentShaderFile) {
		// 1. create two type of shader from txt file.
		vertexShaderID = loadShaderFromFile(vertexShaderFile, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShaderFromFile(fragmentShaderFile, GL20.GL_FRAGMENT_SHADER);

		// 2. create program and attach shader to program.
		programID = GL20.glCreateProgram();

		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);

		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);

		// 3. delete shader -> no more need.
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);

	}

	public static int loadShaderFromFile(String fileName, int type) {
		StringBuilder shaderSource = new StringBuilder();

		// read each line and store source in string.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Could not read file!");
			e.printStackTrace();
			System.exit(-1);
		}

		// create particular type of shader.
		int shaderID = GL20.glCreateShader(type);

		// attach source
		GL20.glShaderSource(shaderID, shaderSource);

		// check compile
		GL20.glCompileShader(shaderID);
		if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader.");
			System.exit(-1);
		}

		return shaderID;
	}
	
	public void compile() {
		// create particular type of shader.
		vertexShaderID = GL20.glCreateShader(GL_VERTEX_SHADER);

		// attach source
		GL20.glShaderSource(vertexShaderID, vertexSource);

		// check compile
		GL20.glCompileShader(vertexShaderID);
		if (GL20.glGetShaderi(vertexShaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(vertexShaderID, 500));
			System.err.println("Could not compile VertexShader.");
			System.exit(-1);
		}
		
		// create particular type of shader.
		fragmentShaderID = GL20.glCreateShader(GL_FRAGMENT_SHADER);

		// attach source
		GL20.glShaderSource(fragmentShaderID, fragmentSource);

		// check compile
		GL20.glCompileShader(fragmentShaderID);
		if (GL20.glGetShaderi(fragmentShaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(fragmentShaderID, 500));
			System.err.println("Could not compile FragmentShader.");
			System.exit(-1);
		}
	}

	public void start() {
		GL20.glUseProgram(programID);
	}

	public void stop() {
		GL20.glUseProgram(0);
	}

	public void cleanUp() {
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}

	// register uniform varible in shader code.
	public void setFloat(String name, float value) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform1f(loc, value);
	}
	
	public void setInteger(String name, int value) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform1i(loc, value);
	}
	
	public void setInteger(String name, boolean value) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform1i(loc, value ? 1 : 0);
	}
	
	public void setIntArray(String name, int[] value) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform1iv(loc, value);
	}
	
	public void setVector2f(String name, float x, float y) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform2f(loc, x, y);
	}
	
	public void setVector2f(String name, Vector2f vec) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform2f(loc, vec.x, vec.y);
	}
	
	public void setVector3f(String name, Vector3f vec) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform3f(loc, vec.x, vec.y, vec.z);
	}
	
	public void setVector4f(String name, Vector4f vec) {
		int loc = GL20.glGetUniformLocation(programID, name);
		GL20.glUniform4f(loc, vec.x, vec.y, vec.z, vec.w);
	}
	
	// Matrix를 넘겨주기 위한 배열.
	private float[] matrix4fArr = new float[16];
	
	public void setMatrix4f(String name, Matrix4f matrix) {
		int loc = GL20.glGetUniformLocation(programID, name);
	
		matrix.get(matrix4fArr);
		GL20.glUniformMatrix4fv(loc, false, matrix4fArr);
	
	}
	
	public String getFilepath() {
		return this.filepath;
	}
}
