#version 330 core

layout (triangles) in;
layout (triangle_strip, max_vertices=3) out;

in VS_OUT {
    vec3 fragmentPos;
    vec3 color;
} gs_in[];

out vec3 fragmentPos2;
out vec3 modelNormal2;
out vec3 color2;

// https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
vec3 calculateNormal() {
    // U = p2 - p1
    // V = p3 - p1
    vec3 u = gs_in[1].fragmentPos.xyz - gs_in[0].fragmentPos.xyz;
    vec3 v = gs_in[2].fragmentPos.xyz - gs_in[0].fragmentPos.xyz;

    // Nx = UyVz - UzVy
    // Ny = UzVx - UxVz
    // Nz = UxVy - UyVx
    return normalize(vec3((u.x*v.z)-(u.z*v.y), (u.z*v.x)-(u.x*v.z), (u.x*v.y)-(u.y*v.x)));
}

// Geometry shader that calculates terrain mesh normals
void main()
{
    for (int i = 0; i < gl_in.length(); i++)
    {
        gl_Position = gl_in[i].gl_Position;
        fragmentPos2 = gs_in[i].fragmentPos;
        modelNormal2 = calculateNormal();
        color2 = gs_in[i].color;

        EmitVertex();
    }
    EndPrimitive();
}
