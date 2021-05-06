#version 330 core

struct LightDirectional {
    vec3 color;
    vec3 direction;
};

struct LightAmbient {
    vec3 color;
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

in vec3 fragmentPos2;
in vec3 modelNormal2;
in vec3 color2;

out vec4 fragmentCol;

uniform vec3 cameraPosition; // for specular
uniform LightDirectional directionalLight;
uniform LightAmbient ambientLight;
uniform LightPoint pointLights[8];
uniform LightSpot spotLights[8];

// TODO: Implement material properties for objects
void main()
{
    vec3 outputColor = vec3(0.0f, 0.0f, 0.0f);

    // Calculate ambient
    outputColor += ambientLight.color;

    // Calculate directional
    float diffuse = max((dot(modelNormal2, normalize(directionalLight.direction))), 0.0f);
    outputColor += directionalLight.color * diffuse;

    // Calculate point lights
    for (int i = 0; i < 2; i++) { // TODO: loop through all array elements
        float distance = length(pointLights[i].position - fragmentPos2);
        float attenuation = 1.0f / (pointLights[i].kc +
                                   (pointLights[i].kl * distance) +
                                   (pointLights[i].kq * (distance * distance)));
        // Ambient
        outputColor += pointLights[i].color * attenuation;

        // Diffuse
        vec3 lightDirection = normalize(pointLights[i].position - fragmentPos2);
        diffuse = max((dot(modelNormal2, lightDirection)), 0.0f);
        outputColor += pointLights[i].color * diffuse * attenuation;
    }

    // Calculate spot lights
    for (int i = 0; i < 1; i++) { // TODO: loop through all array elements
        vec3 lightDirection = normalize(spotLights[i].position - fragmentPos2);
        float theta = dot(spotLights[i].direction, -lightDirection);
        if (theta > spotLights[i].outerAngle) {
            diffuse = max((dot(modelNormal2, -lightDirection)), 0.0f);

            float gain = 1.0f;
            if (theta > spotLights[i].outerAngle && theta < spotLights[i].innerAngle) {
                gain = (theta - spotLights[i].outerAngle) / (spotLights[i].innerAngle - spotLights[i].outerAngle);
            }

            outputColor += spotLights[i].color * diffuse * gain; // * attenuation;
        }
    }

//     fragmentCol = vec4(1.0f, 1.0f, 1.0f, 1.0f);
    fragmentCol = vec4(color2.xyz, 1.0f) * vec4(outputColor, 0.0f);
//    fragmentCol = vec4(modelNormal2.xyz, 1.0f);
}
