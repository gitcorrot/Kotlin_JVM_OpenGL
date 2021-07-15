#version 330 core

in vec2 texturePos;

out vec4 fragmentCol;

uniform sampler2D colorPaletteTexture;

void main()
{
    fragmentCol = texture(colorPaletteTexture, texturePos);
}
