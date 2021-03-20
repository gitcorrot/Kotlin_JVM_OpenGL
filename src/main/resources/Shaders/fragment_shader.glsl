#version 330 core

in vec3 position;
in vec3 normal;
in vec2 texturePos;

out vec4 FragCol;

uniform sampler2D myTexture;

void main()
{
    vec3 lightPosition = vec3(0.0f, 5.0f, 55.0f);
    vec3 lightDirection = normalize(lightPosition - position);

    vec3 ambient = vec3(0.2f, 0.2f, 0.2f);
    vec3 diffuse = max((dot(normal, lightDirection)), 0.0f) * vec3(0.8f,0.85f,0.85f);

    vec4 lighting = vec4(ambient + diffuse, 1.0f);

    //    FragCol = vec4(1.0f, 1.0f, 1.0f, 1.0f);
//    FragCol = texture(myTexture, texturePos);
    FragCol = texture(myTexture, texturePos) * lighting;
}
