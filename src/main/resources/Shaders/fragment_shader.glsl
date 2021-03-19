#version 330 core

in vec2 texturePos;

out vec4 FragCol;

uniform sampler2D myTexture;

void main()
{
    FragCol = vec4(1.0f, 1.0f, 1.0f, 1.0f);
    FragCol = texture(myTexture, texturePos);
}
