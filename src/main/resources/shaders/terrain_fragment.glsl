#version 330

const int MAX_LAYERS = 5;
const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

in vec3 mvVertexPos; 
in vec2 outTexCoord;
in vec3 mvVertexNormal;
in mat3 TBN;
in vec4 mlightviewVertexPos;

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

struct MultilayeredMaterial {
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	int hasDiffuseMaps;
	int hasNormalMaps;
	float reflectance;
};

struct Fog {
	int activeFog;
	vec3 color;
	float density;
};

uniform sampler2D diffuseMaps[MAX_LAYERS];
uniform sampler2D normalMaps[MAX_LAYERS];
uniform sampler2D rgbaMap;
uniform sampler2D shadowMap;
uniform vec3 ambientLight;
uniform float specularPower;
uniform MultilayeredMaterial multilayeredMaterial;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;
uniform Fog fog;

vec4 ambientC;
vec4 diffuseC;
vec4 specularC;

void setupColors(MultilayeredMaterial multilayeredMaterial, vec2 texCoord) {
	if (multilayeredMaterial.hasDiffuseMaps == 1) {
		vec4 rgba = texture(rgbaMap, texCoord / 40);
		if (rgba.a == 1) {
			ambientC = texture(diffuseMaps[4], texCoord);
		} else if (rgba.b == 1) {
			ambientC = texture(diffuseMaps[3], texCoord);
		} else if (rgba.g == 1) {
			ambientC = texture(diffuseMaps[2], texCoord);
		} else if (rgba.r == 1) {
			ambientC = texture(diffuseMaps[1], texCoord);
		} else if (rgba.r == 0 && rgba.g == 0 && rgba.b == 0 && rgba.a == 0) {
			ambientC = texture(diffuseMaps[0], texCoord);
		} else {
			vec4 color0 = texture(diffuseMaps[0], texCoord);
			vec4 color1 = texture(diffuseMaps[1], texCoord);
			vec4 color2 = texture(diffuseMaps[2], texCoord);
			vec4 color3 = texture(diffuseMaps[3], texCoord);
			vec4 color4 = texture(diffuseMaps[4], texCoord);
			ambientC = mix(mix(mix(mix(color0, color1, rgba.r), color2, rgba.g), color3, rgba.b), color4, rgba.a);
		}
		diffuseC = ambientC;
		specularC = ambientC;
	} else {
		ambientC = multilayeredMaterial.ambient;
		diffuseC = multilayeredMaterial.diffuse;
		specularC = multilayeredMaterial.specular;
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
	specularColor = specularC * specularFactor * multilayeredMaterial.reflectance * vec4(light_color, 1.0);

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

vec3 calcNormal(MultilayeredMaterial multilayeredMaterial, vec3 normal, vec2 texCoord) {
	vec3 newNormal = normal;
	if (multilayeredMaterial.hasNormalMaps == 1) {
		vec4 rgba = texture(rgbaMap, texCoord / 40);
		if (rgba.a == 1) {
			newNormal = texture(normalMaps[4], texCoord).rgb;
		} else if (rgba.b == 1) {
			newNormal = texture(normalMaps[3], texCoord).rgb;
		} else if (rgba.g == 1) {
			newNormal = texture(normalMaps[2], texCoord).rgb;
		} else if (rgba.r == 1) {
			newNormal = texture(normalMaps[1], texCoord).rgb;
		} else if (rgba.r == 0 && rgba.g == 0 && rgba.b == 0 && rgba.a == 0) {
			newNormal = texture(normalMaps[0], texCoord).rgb;
		} else {
			vec3 normal0 = texture(normalMaps[0], texCoord).rgb;
			vec3 normal1 = texture(normalMaps[1], texCoord).rgb;
			vec3 normal2 = texture(normalMaps[2], texCoord).rgb;
			vec3 normal3 = texture(normalMaps[3], texCoord).rgb;
			vec3 normal4 = texture(normalMaps[4], texCoord).rgb;
			newNormal = mix(mix(mix(mix(normal0, normal1, rgba.r), normal2, rgba.g), normal3, rgba.b), normal4, rgba.a);
		}
		newNormal = normalize(newNormal * 2 - 1);
	}
	return newNormal;
}

float calcShadow(vec4 position) {
    vec3 projCoords = position.xyz;
    // Transform from screen coordinates to texture coordinates
    projCoords = projCoords * 0.5 + 0.5;
    float bias = 0.05;

    float shadowFactor = 0.0;
    vec2 inc = 1.0 / textureSize(shadowMap, 0);
    for (int row = -1; row <= 1; ++row) {
        for (int col = -1; col <= 1; ++col) {
            float textDepth = texture(shadowMap, projCoords.xy + vec2(row, col) * inc).r; 
            shadowFactor += projCoords.z - bias > textDepth ? 1.0 : 0.0;        
        }    
    }
    shadowFactor /= 9.0;

    if (projCoords.z > 1.0) {
        shadowFactor = 1.0;
    }

    return 1 - shadowFactor;
}

void main() {
	setupColors(multilayeredMaterial, outTexCoord);
	
	vec3 currNormal = calcNormal(multilayeredMaterial, mvVertexNormal, outTexCoord);
	
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
	
	float shadow = calcShadow(mlightviewVertexPos);
	fragColor = clamp(ambientC * vec4(ambientLight, 1) + diffuseSpecularComp * shadow, 0, 1);
	
	if (fog.density > 0) {
		fragColor = calcFog(mvVertexPos, fragColor, fog, ambientLight, directionalLight);
	}
}