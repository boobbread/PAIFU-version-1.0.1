package org.paifu.graphics3D.lighting;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.logging.Logger;

public class PointLight extends Light {

    private Vector3f position;
    private float constant, linear, exponent;
    private Vector4f frontRect;
    private Vector4f backRect;


    public PointLight(Vector3f colour, Vector3f position, float intensity, float constant, float linear, float exponent) {
        super(colour, intensity);
        this.position = position;
        this.constant = constant;
        this.linear = linear;
        this.exponent = exponent;

        this.castsShadows = true;
    }

    public float getExponent() {
        return exponent;
    }

    public void setExponent(float exponent) {
        this.exponent = exponent;
    }

    public float getLinear() {
        return linear;
    }

    public void setLinear(float linear) {
        this.linear = linear;
    }

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    @Override
    public Matrix4f getViewProjectionMatrix() {
        return null;
    }

    public float getNearPlane() {
        return 0.1f;
    }
    public float getFarPlane() {
        float q = exponent;   // quadratic
        float l = linear;
        float c = constant;

        float rhs = 1.0f / 0.02f;

        float A = q;
        float B = l;
        float C = c - rhs;

        float discriminant = B * B - 4.0f * A * C;

        if (discriminant < 0.0f) {
            return Float.POSITIVE_INFINITY;
        }

        return (-B + (float)Math.sqrt(discriminant)) / (2.0f * A);
    }

    public Vector4f getFrontRect() {
        return frontRect;
    }

    public void setFrontRect(Vector4f frontRect) {
        this.frontRect = frontRect;
    }

    public Vector4f getBackRect() {
        return backRect;
    }

    public void setBackRect(Vector4f backRect) {
        this.backRect = backRect;
    }
}
