package org.paifu.graphics3D.lighting;

import org.joml.Vector4f;
import org.paifu.graphics3D.material.Texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowAtlas {

    public static final int SIZE = 4096;

    private final int fbo;
    private final Texture depth;


    private int tilesPerRow = 4;
    private int nextTile = 0;

    public ShadowAtlas() {
        fbo = glGenFramebuffers();

        depth = new Texture(SIZE, SIZE, GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, GL_FLOAT);
        glBindTexture(GL_TEXTURE_2D, depth.getId());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);

        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_TEXTURE_2D,
                depth.getId(),
                0
        );

        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Shadow atlas incomplete");

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    }

    public Texture getDepthTexture() {
        return depth;
    }

    public Vector4f allocateTile() {
        int x = nextTile % tilesPerRow;
        int y = nextTile / tilesPerRow;

        float tileSize = 1.0f / tilesPerRow;

        Vector4f rect = new Vector4f(
                x * tileSize,
                y * tileSize,
                tileSize,
                tileSize
        );

        nextTile++;
        return rect;
    }
}

