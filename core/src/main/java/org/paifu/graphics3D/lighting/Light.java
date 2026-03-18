package org.paifu.graphics3D.lighting;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class Light {

    protected boolean castsShadows = true;

    private Vector3f colour;
    private float intensity;
    protected Vector4f shadowRect;

    public Light(Vector3f colour, float intensity) {
        this.colour = colour;
        this.intensity = intensity;
    }

    public Vector3f getColour() {
        return colour;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public void update() {
        // Space for future
    }

    public boolean castsShadows() {
        return castsShadows;
    }

    public abstract Matrix4f getViewProjectionMatrix();

    public Vector4f getShadowRect() {
        return shadowRect;
    }

    public void setShadowRect(Vector4f shadowRect) {
        this.shadowRect = shadowRect;
    }
}
