#version 420 core

#define MAX_POINT_LIGHTS 64
#define MAX_SPOT_LIGHTS 64
#define MAX_DIRECTIONAL_LIGHTS 64

in vec2 TexCoords;
out vec4 FragColour;

struct PointLight { // total 96 bytes
    vec4 position; // xyz = position, w = buffer : 16b
    vec4 colour; // xyz = rgb, w = buffer : 16b
    vec4 params; // x = intensity, y = constant, z = linear, w = exponential : 16b
    vec4 fRect; // direct transfer : 16b
    vec4 bRect; // direct transfer : 16b
    vec4 farPlane; // x = farPlane, yzw = reserved : 16b
};

layout(std140, binding = 1) uniform PointLightBlock { // total 6160 bytes
    ivec3 header; // x = numPointLights, yzw = padding : 16b
    PointLight lights[MAX_POINT_LIGHTS]; // 96 * 64 = 6144 bytes
} pointLights;

struct SpotLight { // total 160 bytes
    vec4 position; // xyz = position, w = buffer : 16b
    vec4 colour; // xyz = rgb, w = buffer : 16b
    vec4 params; // x = intensity, y = constant, z = linear, w = exponential : 16b
    vec4 rect; // direct transfer : 16b
    vec4 direction; // xyz = direction, w = buffer : 16b
    mat4 lightSpaceMatrix; // 64b
    vec4 cutoff; // x = cutoff, yzw = reserved : 16b
};

layout(std140, binding = 2) uniform SpotLightBlock { // total 10256 bytes
    ivec3 header; // x = numSpotLights, yzw = padding : 16b
    SpotLight lights[MAX_SPOT_LIGHTS]; // 160 * 64 = 10240 bytes = ~10KB
} spotLights;

struct DirectionLight { // total 128 bytes
    vec4 direction; // xyz = direction, w = buffer : 16b
    vec4 colour; // xyz = rgb, w = buffer : 16b
    vec4 intensity; // x = intensity, yzw = reserved : 16b
    vec4 rect; // direct transfer : 16b
    mat4 lightSpaceMatrix; // 64b
};

layout(std140, binding = 3) uniform DirectionLightBlock { // total 8208 bytes
    ivec3 header; // x = numDirectionLights, yzw = padding : 16b
    DirectionLight lights[MAX_DIRECTIONAL_LIGHTS]; // 128 * 64 = 8192 bytes = ~8KB
} dirLights;


uniform sampler2D shadowAtlas;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedoSpec;

// Helper functions

float sampleShadow(vec2 uv, float depth, vec4 rect, float bias) {
    vec2 texelSize = 1.0 / vec2(textureSize(shadowAtlas, 0));
    float shadow = 0.0;

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 offset = vec2(x, y) * texelSize;
            vec2 atlasUV = clamp(
            rect.xy + uv * rect.zw + offset,
            rect.xy + texelSize,
            rect.xy + rect.zw - texelSize
            );

            float d = texture(shadowAtlas, atlasUV).r;
            shadow += (depth - bias > d) ? 1.0 : 0.0;
        }
    }
    return shadow / 9.0;
}

float PointShadowCalculation(vec3 fragPos, vec3 lightPos, vec4 frontRect, vec4 backRect, float farPlane, float bias) {
    vec3 L = fragPos - lightPos;
    float dist = length(L);
    vec3 dir = normalize(L);

    const float seamWidth = 0.005;

    float frontW = smoothstep(-seamWidth, seamWidth, dir.z);
    float backW  = 1.0 - frontW;

    float shadow = 0.0;

    {
        vec3 d = dir;
        d.z = abs(d.z);

        float denom = 1.0 + d.z;
        vec2 uv = d.xy / denom * 0.5 + 0.5;

        if (uv.x >= 0.0 && uv.x <= 1.0 &&
        uv.y >= 0.0 && uv.y <= 1.0) {
            shadow += frontW *
            sampleShadow(uv, dist / farPlane, frontRect, bias);
        }
    }

    {
        vec3 d = dir;
        d.z = abs(d.z);

        float denom = 1.0 + d.z;
        vec2 uv = d.xy / denom * 0.5 + 0.5;

        if (uv.x >= 0.0 && uv.x <= 1.0 &&
        uv.y >= 0.0 && uv.y <= 1.0) {
            shadow += backW *
            sampleShadow(uv, dist / farPlane, backRect, bias);
        }
    }

    return shadow;
}

void main() {
    vec3 FragPos = texture(gPosition, TexCoords).rgb;
    vec3 Normal = normalize(texture(gNormal, TexCoords).rgb);
    vec3 Albedo = texture(gAlbedoSpec, TexCoords).rgb;
    float SpecularStrength = texture(gAlbedoSpec, TexCoords).a;

    vec3 lighting = vec3(0.0);

    // Directional lights
    for (int i = 0; i < dirLights.header.x; i++) {
        DirectionLight light = dirLights.lights[i];

        vec3 lightDir = normalize(-light.direction.xyz);

        float bias = max(0.005 * (1.0 - dot(Normal, lightDir)), 0.005);

        float diff = max(dot(Normal, lightDir), 0.0);
        vec3 diffuse = diff * light.colour.xyz * light.intensity.x;

        vec4 fragPosLightSpace = light.lightSpaceMatrix * vec4(FragPos, 1.0);

        vec3 proj = fragPosLightSpace.xyz / fragPosLightSpace.w;
        proj = proj * 0.5 + 0.5;

        float shadow = 0.0;
        if (proj.z <= 1.0 &&
        proj.x >= 0.0 && proj.x <= 1.0 &&
        proj.y >= 0.0 && proj.y <= 1.0) {

            shadow = sampleShadow(proj.xy, proj.z, light.rect, bias);
        }

        lighting += (1.0 - shadow) * diffuse;
    }

    // Point lights
    for (int i = 0; i < pointLights.header.x; ++i) {
        PointLight light = pointLights.lights[i];

        vec3 lightDir = light.position.xyz - FragPos;
        float distance = length(lightDir);
        lightDir = normalize(lightDir);

        float bias = max(0.005 * (1.0 - dot(Normal, lightDir)), 0.0005);
        bias *= distance / light.farPlane.x;

        float diff = max(dot(Normal, lightDir), 0.0);

        float attenuation = 1.0 /(light.params.y + light.params.z * distance + light.params.w * distance * distance);

        float shadow = PointShadowCalculation(FragPos, light.position.xyz, light.fRect, light.bRect, light.farPlane.x, bias);

        vec3 diffuse = diff * light.colour.xyz * light.params.x * attenuation;

        lighting += (1.0 - shadow) * diffuse;
    }

    // Spot lights
    for (int i = 0; i < spotLights.header.x; ++i) {
        SpotLight light = spotLights.lights[i];

        vec3 lightDir = light.position.xyz - FragPos;
        float distance = length(lightDir);
        lightDir = normalize(lightDir);

        float bias = max(0.0005 * (1.0 - dot(Normal, lightDir)), 0.0005);

        float diff = max(dot(Normal, lightDir), 0.0);

        float theta = dot(lightDir, normalize(-light.direction.xyz));
        float epsilon = 0.1;
        float intensity = clamp((theta - light.cutoff.x) / epsilon, 0.0, 1.0);

        float attenuation = 1.0 / (light.params.y +
        light.params.z * distance +
        light.params.w * distance * distance);

        vec3 diffuse = diff * light.colour.xyz * light.params.x * attenuation * intensity;

        vec4 fragPosLightSpace = light.lightSpaceMatrix * vec4(FragPos, 1.0);

        vec3 proj = fragPosLightSpace.xyz / fragPosLightSpace.w;
        proj = proj * 0.5 + 0.5;

        float shadow = 0.0;
        if (proj.z <= 1.0 &&
        proj.x >= 0.0 && proj.x <= 1.0 &&
        proj.y >= 0.0 && proj.y <= 1.0) {

            shadow = sampleShadow(proj.xy, proj.z, light.rect, bias);
        }

        lighting += (1.0 - shadow) * diffuse;
    }

    vec3 ambient = 0.05 * Albedo;

    FragColour = vec4(ambient + lighting * Albedo, 1.0);

}