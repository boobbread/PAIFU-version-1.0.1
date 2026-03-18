#version 400 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D shadowAtlas;

void main() {
    float depth = texture(shadowAtlas, TexCoords).r;
    FragColor = vec4(vec3(depth), 1.0);

}

