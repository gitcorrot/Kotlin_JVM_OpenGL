#version 330 core

in vec3 fragmentPos;
in vec3 modelNormal;
in vec2 texturePos;

out vec4 fragmentCol;

uniform sampler2D myTexture;
uniform vec3 cameraPosition;
uniform vec3 lightPosition;

void main()
{
    vec3 lightDirection = normalize(lightPosition - cameraPosition);

    vec3 ambient = vec3(0.5f, 0.5f, 0.5f);
    vec3 diffuse = max((dot(modelNormal, lightDirection)), 0.0f) * vec3(0.8f,0.85f,0.85f);

    vec4 lighting = vec4(ambient + diffuse, 1.0f);

    //    fragmentCol = vec4(1.0f, 1.0f, 1.0f, 1.0f);
//    fragmentCol = texture(myTexture, texturePos);
    fragmentCol = texture(myTexture, texturePos) * lighting;
}
