package org.paifu.graphics3D.lighting;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SpotLight extends PointLight {

    private Vector3f coneDirection;
    private float cutoff;

    private PointLight pointLight;

    public SpotLight(PointLight pointLight, Vector3f coneDirection, float cutoff) {
        super(pointLight.getColour(), pointLight.getPosition(), pointLight.getIntensity(),
                pointLight.getConstant(), pointLight.getLinear(), pointLight.getExponent());
        this.cutoff = cutoff;
        this.coneDirection = coneDirection;
        this.pointLight = pointLight;

        this.castsShadows = true;
    }

    public Vector3f getConeDirection() {
        return coneDirection;
    }

    public void setConeDirection(Vector3f coneDirection) {
        this.coneDirection = coneDirection;
    }

    public float getCutoff() {
        return cutoff;
    }

    public void setCutoff(float cutoff) {
        this.cutoff = cutoff;
    }

    public PointLight getPointLight() {
        return this.pointLight;
    }

    public Matrix4f getViewProjectionMatrix() {
        Vector3f coneDir = new Vector3f(coneDirection).normalize();
        Vector3f lightPos = new Vector3f(getPosition());
        float fovy = 2.0f * (float) Math.acos(cutoff);

        Vector3f up = Math.abs(coneDir.y) > 0.99f ? new Vector3f(0, 0, -1) : new Vector3f(0,1,0);
        Matrix4f lightViewMatrix = new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add(new Vector3f(coneDir)), up);
        Matrix4f lightProjectionMatrix = new Matrix4f().perspective(fovy, 1f, 0.01f, this.pointLight.getFarPlane());

        return new Matrix4f(lightProjectionMatrix).mul(lightViewMatrix);
    }
}
