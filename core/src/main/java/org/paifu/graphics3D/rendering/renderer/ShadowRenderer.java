package org.paifu.graphics3D.rendering.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.paifu.core.entity.Entity;
import org.paifu.core.entity.Scene;
import org.paifu.core.managers.ShaderManager;
import org.paifu.core.utils.Loader;
import org.paifu.graphics3D.lighting.Light;
import org.paifu.graphics3D.lighting.PointLight;
import org.paifu.graphics3D.lighting.ShadowAtlas;
import org.paifu.graphics3D.lighting.SpotLight;
import org.paifu.graphics3D.mesh.Model;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.paifu.core.Constants.WINDOW_HEIGHT;
import static org.paifu.core.Constants.WINDOW_WIDTH;

public class ShadowRenderer {
    private ShaderManager shader;
    private ShaderManager pointLightShader;

    private ShadowAtlas atlas;

    /**
     * Used for the second pass of the deferred shading pipeline -
     * renders the geometry of the scene from the perspective of each light
     * to the shadow atlas.
     * @throws Exception Throws if the shader fails to be created
     */
    public ShadowRenderer() throws Exception {
        shader = new ShaderManager();
        pointLightShader = new ShaderManager();
    }

    /**
     * Creates the shader programs and their respective uniforms.
     * @throws Exception Throws if the shader fails to compile
     */
    public void init() throws Exception {

        shader.createVertexShader(Loader.loadShader("/shader/shadow_pass.vsh"));
        shader.createFragmentShader(Loader.loadShader("/shader/shadow_pass.fsh"));
        shader.link();

        shader.createUniform("lightSpaceMatrix");
        shader.createUniform("model");

        pointLightShader.createVertexShader(Loader.loadShader("/shader/pointlight/point_shadow.vsh"));
        pointLightShader.createFragmentShader(Loader.loadShader("/shader/pointlight/point_shadow.fsh"));

        pointLightShader.link();

        pointLightShader.createUniform("model");
        pointLightShader.createUniform("lightView");
        pointLightShader.createUniform("paraboloidSide");
        pointLightShader.createUniform("farPlane");
        pointLightShader.createUniform("nearPlane");

        atlas = new ShadowAtlas();
    }

    /**
     * Performs the shadow pass, rendering the scene to the shadow atlas.
     * @param scene The scene to render.
     */
    public void render(Scene scene) {

        atlas.bind();
        shader.bind();
        glClear(GL_DEPTH_BUFFER_BIT);

        for (Light light : scene.getLights()) {
            if (!light.castsShadows()) continue;
            if (light instanceof PointLight && !(light instanceof SpotLight)) {
                renderPointLightShadow(scene, (PointLight) light);
                continue;
            }

            Matrix4f lightSpaceMatrix = light.getViewProjectionMatrix();
            Vector4f r = light.getShadowRect();

            glViewport(
                    (int)(r.x * ShadowAtlas.SIZE),
                    (int)(r.y * ShadowAtlas.SIZE),
                    (int)(r.z * ShadowAtlas.SIZE),
                    (int)(r.w * ShadowAtlas.SIZE)
            );

            shader.setUniform("lightSpaceMatrix", lightSpaceMatrix);

            renderEntities(scene, shader);

        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /**
     * A utility method for rendering specifically point light shadows, which require
     * a slightly different pipeline, using dual-paraboloid projection.
     */
    private void renderPointLightShadow(Scene scene, PointLight light) {

        shader.unbind();

        pointLightShader.bind();
        pointLightShader.setUniform("farPlane", light.getFarPlane());
        pointLightShader.setUniform("nearPlane", light.getNearPlane());

        Vector4f frontRect = light.getFrontRect();
        Vector4f backRect = light.getBackRect();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);

        glCullFace(GL_BACK);
        renderHemisphere(scene, frontRect, 1, light.getPosition());
        glCullFace(GL_FRONT);
        renderHemisphere(scene, backRect, -1, light.getPosition());
        glCullFace(GL_BACK);
        shader.bind();
    }

    /**
     * A utility method for rendering each hemisphere of the point light's dual-paraboloid
     */
    private void renderHemisphere(Scene scene, Vector4f r, int hemi, Vector3f lightPos) {
        int x = Math.round(r.x * ShadowAtlas.SIZE);
        int y = Math.round(r.y * ShadowAtlas.SIZE);
        int w = Math.round(r.z * ShadowAtlas.SIZE);
        int h = Math.round(r.w * ShadowAtlas.SIZE);

        glViewport(x, y, w, h);

        Matrix4f view = new Matrix4f().identity();
        view.translate(-lightPos.x, -lightPos.y, -lightPos.z);

        pointLightShader.setUniform("paraboloidSide", hemi);
        pointLightShader.setUniform("lightView", view);

        renderEntities(scene, pointLightShader);
    }

    private void renderEntities(Scene scene, ShaderManager shader) {
        for (Entity e : scene.getRenderQueue().keySet()) {
            Matrix4f modelMatrix = scene.getRenderQueue().get(e).first;
            Model model = scene.getRenderQueue().get(e).second;

            shader.setUniform("model", modelMatrix);

            glBindVertexArray(model.getId());
            glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }
    }

    public ShadowAtlas getAtlas() {
        return atlas;
    }

    public void cleanup() {
        shader.cleanup();
        pointLightShader.cleanup();
    }
}
