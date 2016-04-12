package nullEngine.gl.framebuffer;

import nullEngine.control.Application;
import nullEngine.gl.model.Quad;
import nullEngine.util.logs.Logs;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class FramebufferDeferred {
	private int frameBufferID;
	private int colorTextureID;
	private int positionTextureID;
	private int normalTextureID;
	private int specularTextureID;
	private int depthTexutreID;
	private int width, height;

	private static final IntBuffer DRAW_BUFFERS = BufferUtils.createIntBuffer(4);

	static {
		DRAW_BUFFERS.put(GL30.GL_COLOR_ATTACHMENT0).put(GL30.GL_COLOR_ATTACHMENT1).put(GL30.GL_COLOR_ATTACHMENT2).put(GL30.GL_COLOR_ATTACHMENT3);
		DRAW_BUFFERS.flip();
	}

	private static final ArrayList<FramebufferDeferred> framebuffers = new ArrayList<FramebufferDeferred>();

	private static int genTexture(int width,int height) {
		int id = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		return id;
	}

	private static int genTextureDetailed(int width,int height) {
		int id = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		return id;
	}


	public FramebufferDeferred(int width, int height) {
		this.width = width;
		this.height = height;

		depthTexutreID = GL11.glGenTextures();
		frameBufferID = GL30.glGenFramebuffers();

		colorTextureID = genTexture(width, height);
		positionTextureID = genTextureDetailed(width, height);
		normalTextureID = genTextureDetailed(width, height);
		specularTextureID = genTextureDetailed(width, height);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexutreID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT32F, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferID);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTextureID, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, positionTextureID, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, GL11.GL_TEXTURE_2D, normalTextureID, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT3, GL11.GL_TEXTURE_2D, specularTextureID, 0);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexutreID, 0);

		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Frame buffer is not complete");

		GL20.glDrawBuffers(DRAW_BUFFERS);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		framebuffers.add(this);
	}

	public void bind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferID);
		GL11.glViewport(0, 0, width, height);
	}

	public static void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Application.get().getWidth(), Application.get().getHeight());
	}

	public void render() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTextureID);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, positionTextureID);
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalTextureID);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, specularTextureID);
		Quad.get().render();
	}

	public void renderBack() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTextureID);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, positionTextureID);
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalTextureID);
		Quad.back().render();
	}

	public void delete() {
		GL30.glDeleteFramebuffers(frameBufferID);
		GL11.glDeleteTextures(colorTextureID);
		GL11.glDeleteTextures(positionTextureID);
		GL11.glDeleteTextures(normalTextureID);
		GL11.glDeleteTextures(depthTexutreID);
		GL11.glDeleteTextures(specularTextureID);
		framebuffers.remove(this);
	}

	public int getFrameBufferID() {
		return frameBufferID;
	}

	public int getColorTextureID() {
		return colorTextureID;
	}

	public int getPositionTextureID() {
		return positionTextureID;
	}

	public int getNormalTextureID() {
		return normalTextureID;
	}

	public int getDepthTexutreID() {
		return depthTexutreID;
	}

	public int getSpecularTextureID() {
		return specularTextureID;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public static void preContextChange() {
		for (FramebufferDeferred framebuffer : framebuffers)
			GL30.glDeleteFramebuffers(framebuffer.getFrameBufferID());
	}

	public static void contextChanged() {
		for (FramebufferDeferred framebuffer : framebuffers)
			framebuffer.reload();
	}

	private void reload() {
		frameBufferID = GL30.glGenFramebuffers();

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferID);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTextureID, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, positionTextureID, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, GL11.GL_TEXTURE_2D, normalTextureID, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT3, GL11.GL_TEXTURE_2D, specularTextureID, 0);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexutreID, 0);

		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
			Logs.e("Framebuffer error: " + GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER));
			throw new RuntimeException("Frame buffer is not complete");
		}

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
}
