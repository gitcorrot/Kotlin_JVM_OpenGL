#version 330 core

in vec2 texturePos;

out vec4 fragmentCol;

uniform sampler2D myTexture;

void main()
{
    fragmentCol = texture(myTexture, texturePos);
}
