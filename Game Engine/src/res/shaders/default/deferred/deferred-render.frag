#version 150 core

in vec2 texCoords;
in vec3 normal;
in vec4 position;

out vec4 outColor;
out vec4 outPosition;
out vec4 outNormal;
out vec4 outSpecular;

uniform sampler2D diffuse;
uniform float reflectivity;
uniform float shineDamper;

void main() {
	outColor = texture(diffuse, texCoords);
	outPosition = position;
	outNormal = vec4(normalize(normal), 1);
	outSpecular = vec4(reflectivity, shineDamper, 0, 1);
}
