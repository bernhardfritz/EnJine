#version 330

layout (location=0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 lightViewMatrix;
uniform mat4 orthoProjectionMatrix;

void main() {
	gl_Position = orthoProjectionMatrix * lightViewMatrix * modelMatrix * vec4(position, 1.0f); 
}