package org.paifu.core.entity;

import org.joml.Matrix4f;
import org.paifu.core.utils.Pair;
import org.paifu.graphics3D.camera.Camera;
import org.paifu.graphics3D.lighting.DirectionLight;
import org.paifu.graphics3D.lighting.Light;
import org.paifu.graphics3D.lighting.PointLight;
import org.paifu.graphics3D.lighting.SpotLight;
import org.paifu.graphics3D.mesh.Model;
import org.paifu.graphics3D.rendering.renderer.GeometryRenderer;
import org.paifu.graphics3D.rendering.renderer.LightingRenderer;
import org.paifu.graphics3D.rendering.renderer.ShadowRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.paifu.core.Constants.WINDOW_HEIGHT;
import static org.paifu.core.Constants.WINDOW_WIDTH;

public class Scene {

    Camera camera;
    List<Entity> entities;
    private Map<Entity, Pair<Matrix4f, Model>> renderQueue;
    private Light[] lights;

    private GeometryRenderer geometryRenderer;
    private LightingRenderer lightingRenderer;
    private ShadowRenderer shadowRenderer;

    public Scene(Camera camera) throws Exception {

        this.camera = camera;

        geometryRenderer = new GeometryRenderer(WINDOW_WIDTH, WINDOW_HEIGHT);
        shadowRenderer = new ShadowRenderer();
        lightingRenderer = new LightingRenderer();

        geometryRenderer.init();
        shadowRenderer.init();
        lightingRenderer.init();

    }

    public Camera getCamera() {
        return camera;
    }

    public void render() {
        geometryRenderer.render(this);
        shadowRenderer.render(this);
        lightingRenderer.render(this, geometryRenderer, shadowRenderer);

        renderQueue.clear();
    }

    public Map<Entity, Pair<Matrix4f, Model>> getRenderQueue() {
        return renderQueue;
    }

    public Light[] getLights() {
        return lights;
    }

    public List<DirectionLight> getDirectionalLights() {
        List<DirectionLight> directionLights = new ArrayList<>();

        for (Light light : this.lights) {
            if (light instanceof DirectionLight) {
                directionLights.add((DirectionLight) light);
            }
        }
        return directionLights;
    }

    public List<SpotLight> getSpotLights() {
        List<SpotLight> spotLights = new ArrayList<>();

        for (Light light : this.lights) {
            if (light instanceof SpotLight) {
                spotLights.add((SpotLight) light);
            }
        }
        return spotLights;
    }

    public List<PointLight> getPointLights() {
        List<PointLight> pointLights = new ArrayList<>();

        for (Light light : this.lights) {
            if (light instanceof PointLight && !(light instanceof SpotLight)) {
                pointLights.add((PointLight) light);
            }
        }
        return pointLights;
    }

}
