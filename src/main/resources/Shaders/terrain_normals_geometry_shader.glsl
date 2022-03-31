#version 330 core

layout (triangles) in;
layout (line_strip, max_vertices=2) out;

in VS_OUT {
    vec3 _fragmentPos;
    mat4 _projection;
} gs_in[];

// https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
vec3 calculateFaceNormal() {
    // U = p2 - p1
    // V = p3 - p1
    vec3 u = gs_in[1]._fragmentPos.xyz - gs_in[0]._fragmentPos.xyz;
    vec3 v = gs_in[2]._fragmentPos.xyz - gs_in[0]._fragmentPos.xyz;

    // Nx = UyVz - UzVy
    // Ny = UzVx - UxVz
    // Nz = UxVy - UyVx
    return normalize(vec3((u.x*v.z)-(u.z*v.y), (u.z*v.x)-(u.x*v.z), (u.x*v.y)-(u.y*v.x)));
}

// Geometry shader that calculates terrain mesh normals and display them
void main()
{
    vec3 faceNormal = calculateFaceNormal();

    vec4 centerPosition = vec4(
        (gl_in[0].gl_Position.xyz + gl_in[1].gl_Position.xyz + gl_in[2].gl_Position.xyz) / 3.0f,
        1.0f
    );

    gl_Position = gs_in[0]._projection * centerPosition;
    EmitVertex();

    gl_Position = gs_in[0]._projection * (centerPosition + (vec4(faceNormal, 0f)));
    EmitVertex();

    EndPrimitive();
}
