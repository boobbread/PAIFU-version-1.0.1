#version 400 core

uniform int paraboloidSide;

in vec3 vPos;
in float vDepth;

void main() {
    if (float(paraboloidSide) * vPos.z < 0.0)
    discard;

    gl_FragDepth = vDepth;
}
