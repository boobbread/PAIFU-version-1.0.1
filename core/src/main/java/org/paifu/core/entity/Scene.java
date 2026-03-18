package org.paifu.core.entity;

import org.paifu.graphics3D.camera.Camera;

import java.util.List;

public class Scene {

    Camera camera;
    Entity entity;
    List<Entity> entities;

    public Scene(Camera camera){
        this.camera = camera;
    }
}
