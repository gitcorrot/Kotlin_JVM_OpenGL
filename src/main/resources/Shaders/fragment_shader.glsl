#version 330 core

in vec2 texturePos;

out vec4 FragCol;

uniform sampler2D myTexture;

void main()
{
    FragCol = texture(myTexture, texturePos);
}
