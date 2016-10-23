package nullEngine.loading;

import nullEngine.control.Application;
import nullEngine.exception.UnsupportedFeatureException;
import nullEngine.gl.font.Font;
import nullEngine.gl.font.Glyph;
import nullEngine.gl.framebuffer.Framebuffer3D;
import nullEngine.gl.model.Model;
import nullEngine.gl.texture.Texture2D;
import nullEngine.loading.filesys.FileFormatException;
import nullEngine.loading.filesys.ResourceLoader;
import nullEngine.loading.model.NLMLoader;
import nullEngine.loading.model.OBJLoader;
import nullEngine.loading.texture.PNGLoader;
import nullEngine.object.wrapper.HeightMap;
import nullEngine.util.Buffers;
import nullEngine.util.logs.Logs;
import org.lwjgl.opengl.*;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Loader {

	private Application application;
	private GLCapabilities capabilities;

	private float anisotropyAmount = 0;
	private boolean anisotropyEnabled = false;
	private float lodBias = 0;

	private ArrayList<Integer> vaos = new ArrayList<Integer>();
	private ArrayList<Integer> vbos = new ArrayList<Integer>();

	public Loader(Application application) {
		this.application = application;
		capabilities = application.getWindow().getGLCapabilities();
	}

	public void setAnisotropyEnabled(boolean anisotropyEnabled) {
		this.anisotropyEnabled = anisotropyEnabled;
	}

	public void setAnisotropyAmount(float anisotropyAmount) {
		if (capabilities.GL_EXT_texture_filter_anisotropic) {
			this.anisotropyAmount = Math.min(anisotropyAmount, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
		}
	}

	public void setLodBias(float lodBias) {
		this.lodBias = Math.min(GL11.glGetFloat(GL14.GL_MAX_TEXTURE_LOD_BIAS), lodBias);
	}

	private int createVAO() {
		int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		return vao;
	}

	private int dataToAttrib(int attrib, float[] data, int size) {
		FloatBuffer buf = Buffers.createBuffer(data);
		int vbo = GL15.glGenBuffers();
		vbos.add(vbo);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attrib, size, GL11.GL_FLOAT, false, 0, 0);
		return vbo;
	}

	public int createVBO(float[] data) {
		FloatBuffer buf = Buffers.createBuffer(data);
		int vbo = GL15.glGenBuffers();
		vbos.add(vbo);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		return vbo;
	}

	private int indexBuffer(int[] indices) {
		IntBuffer buf = Buffers.createBuffer(indices);
		int ibo = GL15.glGenBuffers();
		vbos.add(ibo);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		return ibo;
	}

	public Model loadModel(float[] vertices, float[] texCoords, float[] normals, int[] indices, int[] vertexCounts) {
		int vao = createVAO();
		int ibo = indexBuffer(indices);
		vaos.add(vao);
		int vertexVBO = dataToAttrib(0, vertices, 3);
		int texCoordVBO = dataToAttrib(1, texCoords, 2);
		int normalVBO = dataToAttrib(2, normals, 3);
		GL30.glBindVertexArray(0);

		int[] vertexOffsets = new int[vertexCounts.length];
		vertexOffsets[0] = 0;

		for (int i = 1; i < vertexCounts.length; i++) {
			vertexOffsets[i] = vertexCounts[i - 1] + vertexOffsets[i - 1];
		}

		float biggestRadius = 0;
		for (int i = 0; i < vertices.length / 3; i++) {
			float x = vertices[i * 3];
			float y = vertices[i * 3 + 1];
			float z = vertices[i * 3 + 2];
			float radius = x * x + y * y + z * z;
			if (radius > biggestRadius)
				biggestRadius = radius;
		}
		return new Model(vao, vertexCounts, vertexOffsets, ibo, vertexVBO, texCoordVBO, normalVBO, (float) Math.sqrt(biggestRadius));
	}

	public Model loadModel(int vertexVBO, float radius, int texCoordVBO, int normalVBO, int[] indices) {
		int vao = createVAO();
		int ibo = indexBuffer(indices);
		vaos.add(vao);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBO);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texCoordVBO);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVBO);
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
		GL30.glBindVertexArray(0);

		return new Model(vao, new int[]{indices.length}, new int[]{0}, ibo, vertexVBO, texCoordVBO, normalVBO, radius);
	}

	public Model loadModel(float[] vertices, float[] texCoords, float[] normals, int[] indices) {
		return loadModel(vertices, texCoords, normals, indices, new int[]{indices.length});
	}

	public Model loadModel(String name) {
		name = name.lastIndexOf('/') < name.lastIndexOf('.') ? name : name + ".nlm";
		if (name.endsWith(".obj")) {
			return OBJLoader.loadModel(this, name);
		} else if (name.endsWith(".nlm")) {
			return NLMLoader.loadModel(this, name);
		}
		Logs.f(new FileFormatException("Unknown model format"));
		return null;
	}

	public float[] toFloatArray(ArrayList<Float> list) {
		float[] ret = new float[list.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	public int[] toIntArray(ArrayList<Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	public Texture2D loadTexture(String file) throws IOException {
		return loadTextureCustomPath("res/textures/" + file, false);
	}

	public Texture2D loadTexture(String file, boolean forceUnique) throws IOException {
		return loadTextureCustomPath("res/textures/" + file, forceUnique);
	}

	private Texture2D loadTextureCustomPath(String file, boolean forceUnique) throws IOException {
		return PNGLoader.loadTexture(file + ".png", lodBias, anisotropyEnabled && capabilities.GL_EXT_texture_filter_anisotropic, anisotropyAmount, forceUnique);
	}

	public Font loadFont(String name, int padding) throws IOException {
		Scanner scanner = new Scanner(ResourceLoader.getResource("res/fonts/" + name + ".fnt"));

		String line = scanner.nextLine();
		line = line.replaceAll("\\s+", " ");
		String[] tokens = line.split(" ");
		if (!line.startsWith("info ") && tokens[10].startsWith("padding="))
			throw new FileFormatException("Not a valid font file");

		String[] padStr = tokens[10].substring(8).replaceAll("\\s", "").split(",");
		int[] paddingArr = new int[padStr.length];
		for (int i = 0; i < padStr.length; i++) {
			paddingArr[i] = Integer.parseInt(padStr[i]);
		}


		line = scanner.nextLine();
		line = line.replaceAll("\\s+", " ");
		if (!line.startsWith("common "))
			throw new FileFormatException("Not a valid font file");

		tokens = line.split(" ");
		if (!tokens[1].startsWith("lineHeight=") || !tokens[3].startsWith("scaleW=") || !tokens[4].startsWith("scaleH=") || !tokens[5].startsWith("pages="))
			throw new FileFormatException("Not a valid font file");

		float lineHeight = Integer.parseInt(tokens[1].substring(11)) - paddingArr[0] - paddingArr[1];
		float width = Integer.parseInt(tokens[3].substring(7));
		float height = Integer.parseInt(tokens[4].substring(7));
		int pages = Integer.parseInt(tokens[5].substring(6));

		if (pages != 1)
			throw new UnsupportedFeatureException("Multi page fonts not supported");

		line = scanner.nextLine();
		line = line.replaceAll("\\s+", " ");
		if (!line.startsWith("page "))
			throw new FileFormatException("Not a valid font file");

		tokens = line.split(" ");

		if (!tokens[2].startsWith("file=\"") || !tokens[2].endsWith(".png\""))
			throw new FileFormatException("Invlaid texture file");

		String textureFile = tokens[2].substring(6, tokens[2].length() - 5);
		if (name.contains("/")) {
			textureFile = name.substring(0, name.lastIndexOf("/") + 1) + textureFile;
		}

		Texture2D texture = loadTextureCustomPath("res/fonts/" + textureFile, false);

		line = scanner.nextLine();
		line = line.replaceAll("\\s+", " ");
		if (!line.startsWith("chars "))
			throw new FileFormatException("Not a valid font file");

		tokens = line.split(" ");

		if (!tokens[1].startsWith("count="))
			throw new FileFormatException("Not a valid font file");

		int charCount = Integer.parseInt(tokens[1].substring(6));
		HashMap<Character, Glyph> glyphs = new HashMap<Character, Glyph>(charCount);
		for (int i = 0; i < charCount; i++) {
			line = scanner.nextLine();
			line = line.replaceAll("\\s+", " ");
			if (!line.startsWith("char "))
				throw new FileFormatException("Not a valid font file");

			tokens = line.split(" ");
			if (!tokens[1].startsWith("id=") || !tokens[2].startsWith("x=") || !tokens[3].startsWith("y=") ||
					!tokens[4].startsWith("width=") || !tokens[5].startsWith("height=") || !tokens[6].startsWith("xoffset=") ||
					!tokens[7].startsWith("yoffset=") || !tokens[8].startsWith("xadvance="))
				throw new FileFormatException("Not a valid font file");

			char character = (char) Short.parseShort(tokens[1].substring(3));

			Glyph glyph = new Glyph();

			glyph.texCoordX = Integer.parseInt(tokens[2].substring(2)) + paddingArr[3];
			glyph.texCoordMaxY = (Integer.parseInt(tokens[3].substring(2)) - paddingArr[0]);

			glyph.texCoordMaxX = glyph.texCoordX + (Integer.parseInt(tokens[4].substring(6)) - paddingArr[1]);
			glyph.texCoordY = glyph.texCoordMaxY + (Integer.parseInt(tokens[5].substring(7)) + paddingArr[2]);

			glyph.width = Integer.parseInt(tokens[4].substring(6)) - paddingArr[3] - paddingArr[2];
			glyph.height = Integer.parseInt(tokens[5].substring(7)) - paddingArr[1] - paddingArr[0];

			glyph.xOffset = Integer.parseInt(tokens[6].substring(8)) + paddingArr[3];
			glyph.yOffset = lineHeight - (Integer.parseInt(tokens[7].substring(8)) + glyph.height + paddingArr[0]);

			glyph.xAdvance = Integer.parseInt(tokens[8].substring(9)) - paddingArr[2] - paddingArr[3];

			glyph.texCoordX /= width;
			glyph.texCoordY /= height;
			glyph.texCoordMaxX /= width;
			glyph.texCoordMaxY /= height;

			glyph.width /= lineHeight;
			glyph.height /= lineHeight;
			glyph.xOffset /= lineHeight;
			glyph.yOffset /= lineHeight;
			glyph.xAdvance /= lineHeight;

			glyph.character = character;

			glyphs.put(character, glyph);
		}

		line = scanner.nextLine();
		line = line.replaceAll("\\s+", " ");
		if (!line.startsWith("kernings "))
			throw new FileFormatException("Not a valid font file");

		tokens = line.split(" ");

		if (!tokens[1].startsWith("count="))
			throw new FileFormatException("Not a valid font file");
		int kerningCount = Integer.parseInt(tokens[1].substring(6));

		for (int i = 0; i < kerningCount; i++) {
			line = scanner.nextLine();
			line = line.replaceAll("\\s+", " ");
			if (!line.startsWith("kerning "))
				throw new FileFormatException("Not a valid font file");

			tokens = line.split(" ");
			if (!tokens[1].startsWith("first=") || !tokens[2].startsWith("second=") || !tokens[3].startsWith("amount="))
				throw new FileFormatException("Not a valid font file");

			char first = (char) Short.parseShort(tokens[1].substring(6));
			char second = (char) Short.parseShort(tokens[2].substring(7));
			float amount = Integer.parseInt(tokens[3].substring(7)) / lineHeight;

			Glyph glyph = glyphs.get(first);
			glyph.kerning.put(second, amount);
		}

		return new Font(this, glyphs, texture, padding, width, lineHeight, (lineHeight - paddingArr[0] - paddingArr[1]) / lineHeight);
	}

	public void cleanup() {
		Logs.d("Loader cleaning up");
		for (int vbo : vbos)
			GL15.glDeleteBuffers(vbo);
		for (int vao : vaos)
			GL30.glDeleteVertexArrays(vao);
	}

	public void preContextChange() {
		Logs.d("Cleaning up vertex arrays");
		for (int vao : vaos)
			GL30.glDeleteVertexArrays(vao);
		vaos.clear();

		Framebuffer3D.preContextChange();
	}

	public void postContextChange() {
		Model.contextChanged(vaos);
		Framebuffer3D.contextChanged();
	}


	public HeightMap generateHeightMap(String name, float maxHeight) throws IOException {
		return new HeightMap(ImageIO.read(ResourceLoader.getResource("res/textures/" + name + ".png")), maxHeight);
	}
}
