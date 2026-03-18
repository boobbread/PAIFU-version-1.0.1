package org.paifu.graphics3D.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private Vector3f position;
    private Vector3f rotation;
    private boolean perspective = true;
    private float zoom = 60f;

    //Constructor
    public Camera(Vector3f position, Vector3f rotation){
        this.position = position;
        this.rotation = rotation;
    }

    //Incrementing position
    public void incPos(Vector3f value){
        this.position.add(value);
    }

    public void incPos(float x, float y, float z){
        this.position.x += x;
        this.position.y += y;
        this.position.z += z;
    }

    //Incrementing rotation
    public void incRot(Vector3f value){
        this.rotation.add(value);
    }

    public void incRot(float x, float y, float z){
        this.rotation.x += x;
        this.rotation.y += y;
        this.rotation.z += z;
    }

    //Calculate FOV
    public Matrix4f calcFov(){
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0)).
                rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0)).
                rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
        matrix.translate(-position.x, -position.y, -position.z);

        return matrix;
    }

    //OpenGL shader stuff
    //Pitch = up/down
    //Yaw = left/right
    public Vector3f getForward(){
        float pitch = (float)Math.toRadians(rotation.x);
        float yaw = (float)Math.toRadians(rotation.y);

        Vector3f forward = new Vector3f(
            (float)(Math.cos(pitch) * Math.sin(yaw)),
            (float)(Math.sin(pitch)),
            (float)(Math.cos(pitch) * Math.cos(yaw))
        );

        return forward.normalize();
    }

    //Getters and Setters
    public Vector3f getPos() {
        return position;
    }

    public void setPos(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRot() {
        return rotation;
    }

    public void setRot(Vector3f rotation) {
        this.rotation = rotation;
    }

    public boolean isPerspective() {
        return perspective;
    }

    public void setPerspective(boolean perspective) {
        this.perspective = perspective;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
}
