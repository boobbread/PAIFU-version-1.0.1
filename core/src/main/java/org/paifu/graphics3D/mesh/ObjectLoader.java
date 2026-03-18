package org.paifu.graphics3D.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.paifu.core.utils.Loader;
import org.paifu.core.utils.VertexKey;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectLoader {

    private List<Integer> vaos = new ArrayList<>();
    private List<Integer> vbos = new ArrayList<>();
    private List<Integer> textures = new ArrayList<>();

    public Model loadOBJModel(String fileName) throws Exception {

        List<String> lines = Loader.readAllLines(fileName);

        List<Vector3f> positions = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        List<Float> finalPositions = new ArrayList<>();
        List<Float> finalTexCoords = new ArrayList<>();
        List<Float> finalNormals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        Map<VertexKey, Integer> vertexMap = new HashMap<>();

        for (String line : lines) {
            String[] tokens = line.split("\\s+");

            switch (tokens[0]) {

                case "v":
                    positions.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])));
                    break;

                case "vt":
                    texCoords.add(new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])));
                    break;

                case "vn":
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])));
                    break;

                case "f":
                    for (int i = 1; i <= 3; i++) {
                        String[] parts = tokens[i].split("/");

                        int posIndex  = Integer.parseInt(parts[0]) - 1;
                        int texIndex  = parts.length > 1 && !parts[1].isEmpty()
                                ? Integer.parseInt(parts[1]) - 1 : -1;
                        int normIndex = parts.length > 2
                                ? Integer.parseInt(parts[2]) - 1 : -1;

                        VertexKey key = new VertexKey(posIndex, texIndex, normIndex);

                        Integer index = vertexMap.get(key);
                        if (index == null) {
                            index = finalPositions.size() / 3;
                            vertexMap.put(key, index);

                            Vector3f pos = positions.get(posIndex);
                            finalPositions.add(pos.x);
                            finalPositions.add(pos.y);
                            finalPositions.add(pos.z);

                            if (texIndex >= 0) {
                                Vector2f uv = texCoords.get(texIndex);
                                finalTexCoords.add(uv.x);
                                finalTexCoords.add(1.0f - uv.y);
                            } else {
                                finalTexCoords.add(0f);
                                finalTexCoords.add(0f);
                            }

                            if (normIndex >= 0) {
                                Vector3f n = normals.get(normIndex);
                                finalNormals.add(n.x);
                                finalNormals.add(n.y);
                                finalNormals.add(n.z);
                            } else {
                                finalNormals.add(0f);
                                finalNormals.add(0f);
                                finalNormals.add(1f);
                            }
                        }

                        indices.add(index);
                    }
                    break;
            }
        }

        float[] verticesArray = toFloatArray(finalPositions);
        float[] texArray = toFloatArray(finalTexCoords);
        float[] normalArray = toFloatArray(finalNormals);
        int[] indexArray = indices.stream().mapToInt(i -> i).toArray();

        return loadModel(verticesArray, texArray, normalArray, indexArray);
    }

    public Model loadModel(float[] vertices, float[] textureCoords, float[] normals, int[] indices) {
        int id = createVAO();
        storeIndicesBuffer(indices);
        storeDataInAttribList(0, 3, vertices);
        storeDataInAttribList(1, 2, textureCoords);
        storeDataInAttribList(2, 3, normals);
        unbind();
        return new Model(id, indices.length);
    }

    public int loadTexture(String filename) throws Exception {
        int width, height;
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);

            buffer = STBImage.stbi_load(filename, w, h, c, 4);

            if (buffer == null) {
                throw new Exception("Failed to load texture: " + filename);
            }

            width = w.get();
            height = h.get();
        }

        int id = GL11.glGenTextures();
        textures.add(id);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        STBImage.stbi_image_free(buffer);
        return id;
    }

    private int createVAO() {
        int id = GL30.glGenVertexArrays();
        vaos.add(id);
        GL30.glBindVertexArray(id);
        return id;
    }

    private void storeIndicesBuffer(int[] indices) {
        int vbo = GL15.glGenBuffers();
        vbos.add(vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
        IntBuffer buffer = Loader.storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private void storeDataInAttribList(int attribNo, int vertexCount, float[] data) {
        int vbo = GL15.glGenBuffers();
        vbos.add(vbo);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = Loader.storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(attribNo, vertexCount, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(attribNo);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void unbind() {
        GL30.glBindVertexArray(0);
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public void cleanup() {
        for (int vao : vaos) {
            GL30.glDeleteVertexArrays(vao);
        }
        for (int vbo : vbos) {
            GL30.glDeleteBuffers(vbo);
        }
        for (int texture : textures) {
            GL30.glDeleteTextures(texture);
        }
    }

}
