#version 330 core

layout (points) in;
layout (points, max_vertices = 5) out;

out vec4 color;

uniform float time; // seconds
uniform float aspectRatio;

const float PI = 3.141592;
const float radious = 0.1f; // fraction of full screen - 1 is fullscreen
const float speed = 0.75f; // full spins per 1s
const float pointSize = 15.0f;

vec4 calculatePosition(float _time)
{
    float progress = _time * speed * 2.0f * PI;

    float x = (sin(progress)) * radious;
    float y = (cos(progress)) * radious * aspectRatio;

    return vec4(x, y, 0.0f, 1.0f);
}

void main()
{
    for (int i = 0; i < 5; i++) {
        gl_Position = calculatePosition(time - 0.15f * i);
        gl_PointSize = pointSize;
        color = vec4(0.6f/i, 1.0f, 0.8f, 1.0f);
        EmitVertex();
        EndPrimitive();
    }
}
