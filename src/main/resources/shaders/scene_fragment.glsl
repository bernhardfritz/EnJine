#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

in vec3 mvVertexPos; 
in vec2 outTexCoord;
in vec3 mvVertexNormal;
in mat3 TBN;

out vec4 fragColor;

struct Attenuation {
	float constant;
	float linear;
	float exponent;
};

struct PointLight {
	vec3 position; // Light position is assumed to be in view coordinates
	vec3 color;
	float intensity;
	Attenuation att;
};

struct SpotLight {
	PointLight pl;
	vec3 conedir;
	float cutoff;
};

struct DirectionalLight {
	vec3 direction;
	vec3 color;
	float intensity;
};

struct Material {
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	int hasDiffuseMap;
	int hasNormalMap;
	float reflectance;
};

struct Fog {
	int activeFog;
	vec3 color;
	float density;
};

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;
uniform Fog fog;

vec4 ambientC;
vec4 diffuseC;
vec4 specularC;

void setupColors(Material material, vec2 texCoord) {
	if (material.hasDiffuseMap == 1) {
		ambientC = texture(diffuseMap, texCoord);
		diffuseC = ambientC;
		specularC = ambientC;
	} else {
		ambientC = material.ambient;
		diffuseC = material.diffuse;
		specularC = material.specular;
	}
}

vec3 toTangentSpace(vec3 v) {
	return TBN * v;
}

vec4 calcLightColor(vec3 light_color, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal) {
	vec4 diffuseColor = vec4(0, 0, 0, 0);
	vec4 specularColor = vec4(0, 0, 0, 0);

	// Diffuse light
	float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
	diffuseColor = diffuseC * vec4(light_color, 1.0) * light_intensity * diffuseFactor;
	
	// Specular light	
	vec3 camera_direction = normalize(toTangentSpace(-position));
	vec3 from_light_dir = -to_light_dir;
	vec3 reflected_light = normalize(reflect(from_light_dir, normal));
	float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
	specularFactor = pow(specularFactor, specularPower); 
	specularColor = specularC * specularFactor * material.reflectance * vec4(light_color, 1.0);

	return (diffuseColor + specularColor);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal) {
	vec3 light_direction = light.position - position;
	vec3 to_light_dir = normalize(toTangentSpace(light_direction));
	vec4 light_color = calcLightColor(light.color, light.intensity, position, to_light_dir, normal);
	
	// Apply Attenuation
	float distance = length(light_direction);
	float attenuationInv = light.att.constant + light.att.linear * distance + light.att.exponent * distance * distance;
	return light_color / attenuationInv;
	
}

vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal) {
	vec3 light_direction = light.pl.position - position;
	vec3 to_light_dir = normalize(toTangentSpace(light_direction));
	vec3 from_light_dir = -to_light_dir;
	float spot_alpha = dot(from_light_dir, normalize(toTangentSpace(light.conedir)));
	
	vec4 color = vec4(0, 0, 0, 0);
	
	if (spot_alpha > light.cutoff) {
		color = calcPointLight(light.pl, position, normal);
		color *= (1.0 - (1.0 - spot_alpha) / (1.0 - light.cutoff));
	}
	
	return color;
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal) {
	return calcLightColor(light.color, light.intensity, position, normalize(toTangentSpace(light.direction)), normal);
}

vec4 calcFog(vec3 pos, vec4 color, Fog fog, vec3 ambientLight, DirectionalLight dirLight) {
	vec3 fogColor = fog.color * (ambientLight + dirLight.color * dirLight.intensity);
	float distance = length(pos);
	float fogFactor = 1.0 / exp((distance * fog.density) * (distance * fog.density));
	fogFactor = clamp(fogFactor, 0.0, 1.0);
	
	vec3 resultColor = mix(fogColor, color.xyz, fogFactor);
	return vec4(resultColor.xyz, color.w);
}

vec3 calcNormal(Material material, vec3 normal, vec2 texCoord) {
	vec3 newNormal = normal;
	if (material.hasNormalMap == 1) {
		newNormal = texture(normalMap, texCoord).rgb;
		newNormal = normalize(newNormal * 2 - 1);
	}
	return newNormal;
}

void main() {
	setupColors(material, outTexCoord);
	
	vec3 currNormal = calcNormal(material, mvVertexNormal, outTexCoord);
	
	vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, mvVertexPos, currNormal); 
	
	for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
		if (pointLights[i].intensity > 0) {
			diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, currNormal);
		}
	}
	
	for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
		if (spotLights[i].pl.intensity > 0) {
			diffuseSpecularComp += calcSpotLight(spotLights[i], mvVertexPos, currNormal);
		}
	}
	
	fragColor = clamp(ambientC * vec4(ambientLight, 1) + diffuseSpecularComp, 0, 1);
	
	if (fog.density > 0) {
		fragColor = calcFog(mvVertexPos, fragColor, fog, ambientLight, directionalLight);
	}
}