package org.paifu.graphics3D.rendering.renderer;

import org.paifu.core.Constants;
import org.paifu.core.entity.Scene;
import org.paifu.core.managers.ShaderManager;
import org.paifu.core.utils.Loader;
import org.paifu.graphics3D.lighting.DirectionLight;
import org.paifu.graphics3D.lighting.PointLight;
import org.paifu.graphics3D.lighting.SpotLight;
import org.paifu.graphics3D.rendering.ScreenQuad;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

public class LightingRenderer {
    private ShaderManager shader;
    private ShaderManager debug;
    private ScreenQuad quad;

    private int pointLightsUBO;
    private int spotLightsUBO;
    private int dirLightsUBO;

    /**
     * Used for the final pass of the deferred shading pipeline -
     * renders everything to the screen, bringing together the GBuffer textures,
     * shadows and the lighting.
     * @throws Exception Throws if the shader fails to be created
     */
    public LightingRenderer() throws Exception {
        shader = new ShaderManager();
        debug = new ShaderManager();
    }

    /**
     * Creates the shader programs and their respective uniforms and UBOs.
     * @throws Exception Throws if the shader fails to compile
     */
    public void init() throws Exception {

        shader.createVertexShader(Loader.loadShader("/shader/lighting_pass.vsh"));
        shader.createFragmentShader(Loader.loadShader("/shader/lighting_pass.fsh"));
        shader.link();

        // Shadow atlas texture uniform
        shader.createUniform("shadowAtlas");

        // GBuffer texture uniforms
        shader.createUniform("gPosition");
        shader.createUniform("gNormal");
        shader.createUniform("gAlbedoSpec");

        // Lighting UBOs
        pointLightsUBO = shader.createUBO(6160, 1);

        spotLightsUBO = shader.createUBO(10256, 2);

        dirLightsUBO = shader.createUBO(8208, 3);

        int program = shader.getProgramID();

        int dirBlock   = glGetUniformBlockIndex(program, "DirectionLightBlock"); // = 2
        int pointBlock = glGetUniformBlockIndex(program, "PointLightBlock");     // = 1
        int spotBlock  = glGetUniformBlockIndex(program, "SpotLightBlock");      // = 0

        glUniformBlockBinding(program, dirBlock,   3);
        glUniformBlockBinding(program, pointBlock, 1);
        glUniformBlockBinding(program, spotBlock,  2);

        // Shadow atlas debug view
        debug.createVertexShader(Loader.loadShader("/shader/debug.vsh"));
        debug.createFragmentShader(Loader.loadShader("/shader/debug.fsh"));
        debug.link();

        debug.createUniform("shadowAtlas");

        quad.init();
    }

    public void render(Scene scene, GeometryRenderer geometryRenderer, ShadowRenderer shadowRenderer) {
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        if (Constants.DEBUG) {
            debug.bind();
            glBindTexture(GL_TEXTURE_2D, shadowRenderer.getAtlas().getDepthTexture().getId());
            debug.setUniform("shadowAtlas", 0);

            quad.render();
            debug.unbind();
        } else {
            shader.bind();

            shadowRenderer.getAtlas().getDepthTexture().bind(3);
            shader.setUniform("shadowAtlas", 3);

            geometryRenderer.bindGBufferTextures(0, 1, 2);
            shader.setUniform("gPosition", 0);
            shader.setUniform("gNormal", 1);
            shader.setUniform("gAlbedoSpec", 2);

            // Directional lights
            DirectionLight[] dirLights = scene.getDirectionalLights().toArray(new DirectionLight[0]);
            shader.setDirectionLightUBO(dirLights, dirLightsUBO);

            // Point lights
            PointLight[] pointLights = scene.getPointLights().toArray(new PointLight[0]);
            shader.setPointLightUBO(pointLights, pointLightsUBO);

            // Spotlights
            SpotLight[] spotLights = scene.getSpotLights().toArray(new SpotLight[0]);
            shader.setSpotLightUBO(spotLights, spotLightsUBO);

            quad.render();
            shader.unbind();
        }
    }

    public void cleanup() {
        shader.cleanup();
        quad.cleanup();
    }
}
