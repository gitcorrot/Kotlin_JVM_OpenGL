#version 330 core

in vec3 color;
in vec2 texturePos;

out vec4 FragCol;

uniform sampler2D myTexture;

void main()
{
//    FragCol = texture(myTexture, texturePos);
    FragCol = texture(myTexture, texturePos) * vec4(color.xyz, 1.0f);
//    FragCol = vec4(color.xyz, 1.0f);
}
