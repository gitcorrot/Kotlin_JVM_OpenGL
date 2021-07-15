#version 330 core

struct LightAmbient {
    vec3 color;
};

struct LightDirectional {
    vec3 color;
    vec3 direction;
};

struct LightPoint {
    vec3 color;
    vec3 position;
    float kc;
    float kl;
    float kq;
};

struct LightSpot {
    vec3 color;
    vec3 position;
    vec3 direction;
    float outerAngle;
    float innerAngle;
};

in vec2 texturePos;

uniform sampler2D gPos;
uniform sampler2D gNor;
uniform sampler2D gCol;

uniform LightDirectional directionalLight;
uniform LightAmbient ambientLight;
uniform LightPoint pointLights[8];
uniform LightSpot spotLights[8];

uniform int noPointLights;
uniform int noSpotLights;

out vec4 fragmentCol;


void main()
{
    vec3 fragmentPos = texture(gPos, texturePos).rgb;
    vec3 normal = texture(gNor, texturePos).rgb;
    vec3 outputColor = texture(gCol, texturePos).rgb;

    // Calculate ambient light
    outputColor = outputColor * ambientLight.color;

    // Calculate directional light
    float diffuse = max((dot(normal, normalize(-directionalLight.direction))), 0.0f);
    outputColor += directionalLight.color * diffuse;

    // Calculate point lights
    for (int i = 0; i < noPointLights; i++) {
        float distance = length(pointLights[i].position - fragmentPos);
        float attenuation = 1.0f /  (pointLights[i].kc +
                                    (pointLights[i].kl * distance) +
                                    (pointLights[i].kq * (distance * distance)));
        // Ambient
//        outputColor += pointLights[i].color * attenuation;

        // Diffuse
        vec3 lightDirection = normalize(pointLights[i].position - fragmentPos);
        diffuse = max((dot(normal, lightDirection)), 0.0f);
        outputColor += diffuse * pointLights[i].color * attenuation;
    }

    // Calculate spot lights
    for (int i = 0; i < noSpotLights; i++) {
        vec3 lightDirection = normalize(spotLights[i].position - fragmentPos);
        float theta = dot(spotLights[i].direction, -lightDirection);
        if (theta > spotLights[i].outerAngle) {
            diffuse = max((dot(normal, lightDirection)), 0.0f);

            float gain = 1.0f;
            if (theta > spotLights[i].outerAngle && theta < spotLights[i].innerAngle) {
                gain = (theta - spotLights[i].outerAngle) / (spotLights[i].innerAngle - spotLights[i].outerAngle);
            }

            outputColor += spotLights[i].color * diffuse * gain; // * attenuation;
        }
    }

    fragmentCol = vec4(outputColor, 1.0f);
}
