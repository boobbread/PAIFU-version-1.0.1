package org.paifu.graphics3D.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.*;

/**
 * A simple quad rendered to the screen. Used for visualising flat images eg. the depth texture of the scene.
 */
public class ScreenQuad {

    private int vao;
    private int vbo;
    private int ebo;

    public void init() {
        float[] vertices = {
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            1f,  1f, 1f, 1f,
            -1f,  1f, 0f, 1f
        };

        int[] indices = { 0, 1, 2, 2, 3, 0 };

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex buffer
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Element buffer
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Vertex attributes
        glEnableVertexAttribArray(0); // position
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);

        glEnableVertexAttribArray(1); // texCoords
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vao);
        int vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        if (vao == 0) {
            throw new IllegalStateException("No VAO bound before glDrawElements!");
        }

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
    }

    public int getVao() {
        return vao;
    }
}

