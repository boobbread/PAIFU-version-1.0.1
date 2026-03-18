package org.paifu.graphics3D.rendering;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.paifu.graphics3D.material.Texture;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.*;

public class GBuffer {

    // needs a position, diffuse, normal and specular texture
    private Texture positionTexture;
    private Texture normalTexture;
    private Texture diffuseSpecTexture;

    private int fbo;
    private int depthRBO;

    private final int width, height;

    /**
     * A wrapper class for the three textures used for the geometry pass of deferred shading;
     * the position texture captures the world-space coordinates of each vertex, the normal texture captures
     * the normals of each visible fragment, and the diffuse-specular texture captures the diffuse colour and
     * specularity of the objects in the scene.
     * @param width The width of the screen (usually WINDOW_WIDTH)
     * @param height The height of the screen (usually WINDOW_HEIGHT)
     */
    public GBuffer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Creates the textures, FBOs and RBOs required for the GeometryRenderer to render the appropriate data
     * to the buffer.
     */
    public void init() {
        fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        positionTexture = new Texture(width, height, GL30.GL_RGB16F, GL30.GL_RGB, GL30.GL_FLOAT);
        GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, positionTexture.getId(), 0);

        normalTexture = new Texture(width, height, GL30.GL_RGB16F, GL30.GL_RGB, GL30.GL_FLOAT);
        GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, normalTexture.getId(), 0);

        diffuseSpecTexture = new Texture(width, height, GL30.GL_RGBA16F, GL30.GL_RGBA, GL30.GL_FLOAT);
        GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, diffuseSpecTexture.getId(), 0);

        depthRBO = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthRBO);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT24, width, height);
        GL30.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRBO);

        int[] attachments = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};
        GL20.glDrawBuffers(attachments);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("GBuffer framebuffer not complete!");
        }

        GL30.glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }

    public void bind() {
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    }

    public void unbind() {
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Texture getPositionTexture() { return positionTexture; }
    public Texture getNormalTexture() { return normalTexture; }
    public Texture getDiffuseSpecTexture() { return diffuseSpecTexture; }

    public void cleanup() {
        positionTexture.cleanup();
        normalTexture.cleanup();
        diffuseSpecTexture.cleanup();

        glDeleteFramebuffers(fbo);
    }
}
