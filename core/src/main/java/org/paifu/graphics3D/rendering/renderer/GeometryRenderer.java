package org.paifu.graphics3D.rendering.renderer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.paifu.core.entity.Entity;
import org.paifu.core.entity.Scene;
import org.paifu.core.managers.ShaderManager;
import org.paifu.core.utils.Loader;
import org.paifu.graphics3D.mesh.Model;
import org.paifu.graphics3D.rendering.GBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class GeometryRenderer {
    private GBuffer gBuffer;
    private ShaderManager shader;

    /**
     * Used for the first pass of the deferred shading pipeline -
     * renders the geometry of the scene to the GBuffer
     * @param width The width of the screen (usually WINDOW_WIDTH)
     * @param height The height of the screen (usually WINDOW_HEIGHT)
     * @throws Exception Throws if the shader fails to be created
     */
    public GeometryRenderer(int width, int height) throws Exception {
        gBuffer = new GBuffer(width, height);
        shader = new ShaderManager();
    }

    /**
     * Creates the shader programs and their respective uniforms
     * @throws Exception Throws if the shader fails to compile
     */
    public void init() throws Exception {
        gBuffer.init();
        String srcVert = Loader.loadShader("/shader/geometry_pass.vsh");
        String srcFrag = Loader.loadShader("/shader/geometry_pass.fsh");

        shader.createVertexShader(srcVert);
        shader.createFragmentShader(srcFrag);
        shader.link();

        shader.createUniform("model");
        shader.createUniform("view");
        shader.createUniform("projection");

        shader.createUniform("texture_diffuse1");
        shader.createUniform("materialSpecular");
    }

    /**
     * Performs the geometry pass, rendering the scene to the GBuffer
     * @param scene The scene to render
     */
    public void render(Scene scene) {
        gBuffer.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        shader.bind();

        shader.setUniform("view", scene.getCamera().getViewMatrix());
        shader.setUniform("projection", scene.getCamera().updateProjectionMatrix());

        shader.setUniform("texture_diffuse1", 0);

        for (Entity e : scene.getRenderQueue().keySet()) {

            Matrix4f modelMatrix = scene.getRenderQueue().get(e).first;
            Model model = scene.getRenderQueue().get(e).second;

            shader.setUniform("model", modelMatrix);
            shader.setUniform("materialSpecular", model.getMaterial().getSpecularColour());

            model.getTexture().bind(0);

            glBindVertexArray(model.getId());
            GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }

        gBuffer.unbind();
    }

    public void bindGBufferTextures(int positionUnit, int normalUnit, int albedoSpecUnit) {
        gBuffer.getPositionTexture().bind(positionUnit);
        gBuffer.getNormalTexture().bind(normalUnit);
        gBuffer.getDiffuseSpecTexture().bind(albedoSpecUnit);
    }

    public void cleanup() {
        shader.cleanup();
        gBuffer.cleanup();
    }
}
