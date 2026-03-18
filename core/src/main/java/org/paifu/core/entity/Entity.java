package org.paifu.core.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.paifu.graphics3D.mesh.Model;

import java.util.Vector;

public class Entity {

    public Vector3f position;
    public Vector3f rotation;
    public float scale;
    public String type;
    public Matrix4f modelMatrix = new Matrix4f();
    public Model model;

    //Constructors
    public Entity(Vector3f position, Vector3f rotation, String type){
        this.position = position;
        this.rotation = rotation;
        this.type = type;
    }

    public Entity(Vector3f position, Vector3f rotation){
        this.position = position;
        this.rotation = rotation;
        this.type = "generic";
    }

    public Entity(Vector3f position) {
        this.position = position;
        this.rotation = new Vector3f(0, 0, 0);
        this.type = "generic";
    }

    //Moving entity
    public void move(Vector3f position){
        this.position.add(position);
    }

    public void move(float x, float z){
        this.position.x += x;
        this.position.z += z;
    }

    //Rotating entity
    public void rotate(Vector3f rotation){
        float pitch = (float)Math.toRadians(rotation.x);
        float yaw = (float)Math.toRadians(rotation.z);

        this.rotation.x = (float)(Math.cos(pitch) * Math.sin(yaw));
        this.rotation.z = (float)(Math.cos(pitch) * Math.cos(yaw));
    }

    // Updating model matrix
    public void updateModelMatrix() {

        modelMatrix.identity().translate(position).
                rotateX((float) Math.toRadians(rotation.x)).
                rotateY((float) Math.toRadians(rotation.y)).
                rotateZ((float) Math.toRadians(rotation.z)).
                scale(scale);

    }

    //Getters and Setters
    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public void setModelMatrix(Matrix4f modelMatrix) {
        this.modelMatrix = modelMatrix;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
