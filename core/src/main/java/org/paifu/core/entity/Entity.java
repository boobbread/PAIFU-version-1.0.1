package org.paifu.core.entity;

import org.joml.Vector3f;

import java.util.Vector;

public class Entity {

    public Vector3f position;
    public Vector3f rotation;
    public String type;


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
}
