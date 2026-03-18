package org.paifu.core.managers;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;
import org.paifu.graphics3D.lighting.DirectionLight;
import org.paifu.graphics3D.lighting.PointLight;
import org.paifu.graphics3D.lighting.SpotLight;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * A utility and management class that handles shader creation, buffer management,
 * and the creation and assignment of uniforms and ubos
 */
public class ShaderManager {
    // Fields
    private final int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private int geometryShaderID;
    private final Map<String, Integer> uniforms;

    // Constructor
    public ShaderManager() throws Exception {
        programID = GL20.glCreateProgram();
        if (programID == 0) {
            throw new Exception("Could not create program");
        }

        uniforms = new HashMap<>();
    }

    // Shader creation and management
    /**
     * Creates a vertex shader program
     * @param shaderCode Use in conjunction with Loader.loadShader()
     * @throws Exception Throws if either shader cannot be created or compiled
     */
    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderID = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
    }

    /**
     * Creates a fragment shader program
     * @param shaderCode Use in conjunction with Loader.loadShader()
     * @throws Exception Throws if either shader cannot be created or compiled
     */
    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderID = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
    }

    private int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderID = GL20.glCreateShader(shaderType);
        if (shaderID == 0) {
            throw new Exception("Could not create shader of type: " + shaderType);
        }

        GL20.glShaderSource(shaderID, shaderCode);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == 0) {
            throw new Exception("Could not compile shader of type: " + shaderType
                    + "\n" + GL20.glGetShaderInfoLog(shaderID, 1024));
        }

        GL20.glAttachShader(programID, shaderID);

        return shaderID;
    }

    /**
     * Links the current shader program to the current OpenGL instance to be bound later.
     * @throws Exception Throws if the link is unsuccessful.
     */
    public void link() throws Exception {

        glLinkProgram(programID);
        int status = glGetProgrami(programID, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String log = glGetProgramInfoLog(programID);
            throw new RuntimeException("Could not link shader program: " + log);
        }

        if (vertexShaderID != 0) {
            GL20.glDetachShader(programID, vertexShaderID);
        }

        if (fragmentShaderID != 0) {
            GL20.glDetachShader(programID, fragmentShaderID);
        }

        if (geometryShaderID != 0) {
            GL20.glDetachShader(programID, geometryShaderID);
        }

        GL20.glValidateProgram(programID);
        if (glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == 0) {
            throw new Exception("Could not validate program: " + glGetProgramInfoLog(programID, 1024));
        }
    }

    /**
     * Binds the shader to the current OpenGL instance
     */
    public void bind() {
        GL20.glUseProgram(programID);
    }

    /**
     * Unbinds the shader from the current OpenGL instance
     */
    public void unbind() {
        GL20.glUseProgram(0);
    }

    public int getProgramID() {
        return programID;
    }

    // Uniform creation and management
    public void createUniform(String uniformName) {
        int uniformLocation = GL20.glGetUniformLocation(programID, uniformName);

        if (uniformLocation < 0) {
            System.err.println("Could not get uniform " + uniformName);
        }

        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GL20.glUniformMatrix4fv(uniforms.get(uniformName), false,
                    value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String uniformName, int value) {
        GL20.glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, Vector3f value) {
        GL20.glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, Vector4f value) {
        GL20.glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    public void setUniform(String uniformName, boolean value) {
        float res = 0;
        if (value) {
            res = 1;
        }
        GL20.glUniform1f(uniforms.get(uniformName), res);
    }

    public void setUniform(String uniformName, float value) {
        GL20.glUniform1f(uniforms.get(uniformName), value);
    }

    // UBO management
    public int createUBO(int size, int bindingIndex) {
        if (size <= 0) {
            throw new IllegalArgumentException("UBO size must be positive and non-zero");
        }

        if (size % 16 != 0) {
            throw new IllegalArgumentException("UBO size must be a multiple of 16 bytes");
        }

        int ubo = glGenBuffers();

        glBindBuffer(GL_UNIFORM_BUFFER, ubo);
        glBufferData(GL_UNIFORM_BUFFER, size, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, bindingIndex, ubo);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return ubo;
    }

    public void setDirectionLightUBO(DirectionLight[] directionLights, int ubo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numDirLights = Math.min(directionLights.length, 64);

            ByteBuffer buffer = stack.malloc(8208);

            // Header
            buffer.putInt(numDirLights);
            buffer.putInt(0); // Padding
            buffer.putInt(0);
            buffer.putInt(0);

            // Lights
            for (int i = 0; i < numDirLights; i++) {
                DirectionLight l = directionLights[i];

                // vec4 direction
                buffer.putFloat(l.getDirection().x);
                buffer.putFloat(l.getDirection().y);
                buffer.putFloat(l.getDirection().z);
                buffer.putFloat(0);

                // vec4 colour
                buffer.putFloat(l.getColour().x);
                buffer.putFloat(l.getColour().y);
                buffer.putFloat(l.getColour().z);
                buffer.putFloat(0);

                // vec4 intensity
                buffer.putFloat(l.getIntensity());
                buffer.putFloat(0);
                buffer.putFloat(0);
                buffer.putFloat(0);

                // vec4 rect
                buffer.putFloat(l.getShadowRect().x);
                buffer.putFloat(l.getShadowRect().y);
                buffer.putFloat(l.getShadowRect().z);
                buffer.putFloat(l.getShadowRect().w);

                Matrix4f matrix = l.getViewProjectionMatrix();

                float[] matrixArray = new float[16];
                matrix.get(matrixArray);

                buffer.putFloat(matrixArray[0]);
                buffer.putFloat(matrixArray[1]);
                buffer.putFloat(matrixArray[2]);
                buffer.putFloat(matrixArray[3]);

                buffer.putFloat(matrixArray[4]);
                buffer.putFloat(matrixArray[5]);
                buffer.putFloat(matrixArray[6]);
                buffer.putFloat(matrixArray[7]);

                buffer.putFloat(matrixArray[8]);
                buffer.putFloat(matrixArray[9]);
                buffer.putFloat(matrixArray[10]);
                buffer.putFloat(matrixArray[11]);

                buffer.putFloat(matrixArray[12]);
                buffer.putFloat(matrixArray[13]);
                buffer.putFloat(matrixArray[14]);
                buffer.putFloat(matrixArray[15]);
            }

            buffer.flip();

            glBindBuffer(GL_UNIFORM_BUFFER, ubo);
            glBufferSubData(GL_UNIFORM_BUFFER, 0, buffer);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
    }

    public void setPointLightUBO(PointLight[] pointLights, int ubo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numPointLights = Math.min(pointLights.length, 64);

            ByteBuffer buffer = stack.malloc(6160);

            // Header
            buffer.putInt(numPointLights);
            buffer.putInt(0);
            buffer.putInt(0);
            buffer.putInt(0);

            // Lights
            for (int i = 0; i < numPointLights; i++) {
                PointLight l = pointLights[i];

                // vec4 pos
                buffer.putFloat(l.getPosition().x);
                buffer.putFloat(l.getPosition().y);
                buffer.putFloat(l.getPosition().z);
                buffer.putFloat(0);

                // vec4 colour
                buffer.putFloat(l.getColour().x);
                buffer.putFloat(l.getColour().y);
                buffer.putFloat(l.getColour().z);
                buffer.putFloat(0);

                // vec4 params
                buffer.putFloat(l.getIntensity());
                buffer.putFloat(l.getConstant());
                buffer.putFloat(l.getLinear());
                buffer.putFloat(l.getExponent());

                // vec4 frontRect
                buffer.putFloat(l.getFrontRect().x);
                buffer.putFloat(l.getFrontRect().y);
                buffer.putFloat(l.getFrontRect().z);
                buffer.putFloat(l.getFrontRect().w);

                // vec4 backRect
                buffer.putFloat(l.getBackRect().x);
                buffer.putFloat(l.getBackRect().y);
                buffer.putFloat(l.getBackRect().z);
                buffer.putFloat(l.getBackRect().w);

                // vec4 farPlane
                buffer.putFloat(l.getFarPlane());
                buffer.putFloat(0);
                buffer.putFloat(0);
                buffer.putFloat(0);
            }

            buffer.flip();

            glBindBuffer(GL_UNIFORM_BUFFER, ubo);
            glBufferSubData(GL_UNIFORM_BUFFER, 0, buffer);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
    }

    public void setSpotLightUBO(SpotLight[] spotLights, int ubo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numSpotLights = Math.min(spotLights.length, 64);

            ByteBuffer buffer = stack.malloc(10256);

            buffer.putInt(numSpotLights);
            buffer.putInt(0);
            buffer.putInt(0);
            buffer.putInt(0);

            for (int i = 0; i < numSpotLights; i++) {
                SpotLight l = spotLights[i];

                // vec4 position
                buffer.putFloat(l.getPosition().x);
                buffer.putFloat(l.getPosition().y);
                buffer.putFloat(l.getPosition().z);
                buffer.putFloat(0);

                // vec4 colour
                buffer.putFloat(l.getColour().x);
                buffer.putFloat(l.getColour().y);
                buffer.putFloat(l.getColour().z);
                buffer.putFloat(0);

                // vec4 params
                buffer.putFloat(l.getIntensity());
                buffer.putFloat(l.getConstant());
                buffer.putFloat(l.getLinear());
                buffer.putFloat(l.getExponent());

                // vec4 rect
                buffer.putFloat(l.getShadowRect().x);
                buffer.putFloat(l.getShadowRect().y);
                buffer.putFloat(l.getShadowRect().z);
                buffer.putFloat(l.getShadowRect().w);

                // vec4 direction
                buffer.putFloat(l.getConeDirection().x);
                buffer.putFloat(l.getConeDirection().y);
                buffer.putFloat(l.getConeDirection().z);
                buffer.putFloat(0);

                // mat4 lightSpaceMatrix
                Matrix4f matrix = l.getViewProjectionMatrix();

                float[] matrixArray = new float[16];
                matrix.get(matrixArray);

                buffer.putFloat(matrixArray[0]);
                buffer.putFloat(matrixArray[1]);
                buffer.putFloat(matrixArray[2]);
                buffer.putFloat(matrixArray[3]);

                buffer.putFloat(matrixArray[4]);
                buffer.putFloat(matrixArray[5]);
                buffer.putFloat(matrixArray[6]);
                buffer.putFloat(matrixArray[7]);

                buffer.putFloat(matrixArray[8]);
                buffer.putFloat(matrixArray[9]);
                buffer.putFloat(matrixArray[10]);
                buffer.putFloat(matrixArray[11]);

                buffer.putFloat(matrixArray[12]);
                buffer.putFloat(matrixArray[13]);
                buffer.putFloat(matrixArray[14]);
                buffer.putFloat(matrixArray[15]);

                // vec4 cutoff
                buffer.putFloat(l.getCutoff());
                buffer.putFloat(0);
                buffer.putFloat(0);
                buffer.putFloat(0);
            }

            buffer.flip();

            glBindBuffer(GL_UNIFORM_BUFFER, ubo);
            glBufferSubData(GL_UNIFORM_BUFFER, 0, buffer);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
    }

    public void cleanup() {
        unbind();
        if (programID != 0) {
            GL20.glDeleteProgram(programID);
        }
    }
}
