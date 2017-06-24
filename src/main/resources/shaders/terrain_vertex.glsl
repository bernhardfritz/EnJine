#version 330

const int NUM_CASCADES = 3;

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=3) in vec3 vertexTangent;
layout (location=4) in vec3 vertexBitangent;

out vec3 mvVertexPos;
out vec2 outTexCoord;
out vec3 mvVertexNormal;
out mat3 TBN;
out vec4 mlightviewVertexPos[NUM_CASCADES];

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
uniform mat4 lightViewMatrix[NUM_CASCADES];
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];

void main() {
	mat4 modelViewMatrix = viewMatrix * modelMatrix;
	vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPos;
    outTexCoord = texCoord;
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    vec3 mvVertexTangent = normalize(modelViewMatrix * vec4(vertexTangent, 0.0)).xyz;
    vec3 mvVertexBitangent = normalize(modelViewMatrix * vec4(vertexBitangent, 0.0)).xyz;
    TBN = transpose(mat3(mvVertexTangent, mvVertexBitangent, mvVertexNormal));
    mvVertexPos = mvPos.xyz;
    for (int i = 0; i < NUM_CASCADES; i++) {
        mlightviewVertexPos[i] = orthoProjectionMatrix[i] * lightViewMatrix[i] * modelMatrix * vec4(position, 1.0);
    }
}