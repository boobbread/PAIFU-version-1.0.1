package org.paifu.graphics3D.lighting;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DirectionLight extends Light {

    private Vector3f direction;

    public DirectionLight(Vector3f colour, Vector3f direction, float intensity) {
        super(colour, intensity);
        this.direction = direction;

        this.castsShadows = true;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public Matrix4f getViewProjectionMatrix() {
        Vector3f sceneCenter = new Vector3f(0, 0, 0);
        float sceneRadius = 10f;

        Vector3f direction = new Vector3f(getDirection()).normalize();
        Vector3f lightPos = new Vector3f(sceneCenter).sub(new Vector3f(direction).mul(sceneRadius * 2.0f));
        Vector3f up = Math.abs(direction.y) > 0.99f ? new Vector3f(0, 0, -1) : new Vector3f(0,1,0);

        Matrix4f lightViewMatrix = new Matrix4f().lookAt(lightPos, sceneCenter, up);

        float left = -sceneRadius;
        float right = sceneRadius;
        float bottom = -sceneRadius;
        float top = sceneRadius;
        float near = 1f;
        float far = sceneRadius * 4.0f;

        Matrix4f lightProjectionMatrix = new Matrix4f().ortho(left, right, bottom, top, near, far);

        return new Matrix4f(lightProjectionMatrix).mul(lightViewMatrix);
    }
}
