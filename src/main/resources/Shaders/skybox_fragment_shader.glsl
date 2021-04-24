#version 330 core

in vec3 texturePos;

out vec4 fragmentCol;

uniform samplerCube skybox;

void main()
{
    fragmentCol = texture(skybox, texturePos);
}
