#version 330

uniform float uTime;
uniform float uScale;
uniform sampler2D tNoise;

layout (location=0) in vec3 positionStart;
layout (location=1) in float startTime;
layout (location=2) in vec3 velocity;
layout (location=3) in float turbulence;
layout (location=4) in vec3 color;
layout (location=5) in float size;
layout (location=6) in float lifeTime;

out vec4 vColor;
out float lifeLeft;

void main() {
	// unpack things from our attributes
	vColor = vec4(color, 1.0);

	// convert our velocity back into a value we can use
	vec3 newPosition;
	vec3 v;

	float timeElapsed = uTime - startTime;
	lifeLeft = 1.0 - (timeElapsed / lifeTime);
	gl_PointSize = (uScale * size) * lifeLeft;

	v.x = (velocity.x - 0.5) * 3.0;
	v.y = (velocity.y - 0.5) * 3.0;
	v.z = (velocity.z - 0.5) * 3.0;

	newPosition = positionStart + (v * 10.0) * (uTime - startTime);

	vec3 noise = texture2D(tNoise, vec2(newPosition.x * 0.015 + (uTime * 0.05), newPosition.y * 0.02 + (uTime * 0.015))).rgb;
	vec3 noiseVel = (noise.rgb - 0.5) * 30.0;

	newPosition = mix(newPosition, newPosition + vec3(noiseVel * (turbulence * 5.0)), (timeElapsed / lifeTime));

	if(v.y > 0. && v.y < .05) {
		lifeLeft = 0.0;
	}

	if(v.x < - 1.45) {
		lifeLeft = 0.0;
  	}

	if(timeElapsed > 0.0) {
		gl_Position = projectionMatrix * modelViewMatrix * vec4( newPosition, 1.0 );
	} else {
		gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
		lifeLeft = 0.0;
		gl_PointSize = 0.;
  	}
}