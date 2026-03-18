#version 400 core
layout (location = 0) in vec3 aPos;

uniform mat4 model;
uniform mat4 lightView;
uniform int paraboloidSide;
uniform float nearPlane;
uniform float farPlane;

out float vDepth;
out vec3 vPos;

void main() {
    vec4 lightSpacePos = lightView * model * vec4(aPos, 1.0);
    vec3 pos = lightSpacePos.xyz;
    vPos = pos;

    // Standard paraboloid projection formula
    const float hemiBias = 0.005;
    pos.z -= float(paraboloidSide) * hemiBias;

    float len = length(pos);
    float z = paraboloidSide * pos.z;
    float denom = len + z;
    vec2 proj = pos.xy / denom;

    // Calculate normalized depth [0, 1]
    // Using linear depth based on distance from origin
    vDepth = (len - nearPlane) / (farPlane - nearPlane);
    vDepth = clamp(vDepth, 0.0, 1.0);

    // Output position with depth
    gl_Position = vec4(proj, 0.0, 1.0);
}