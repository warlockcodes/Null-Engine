#VS
#version 150 core

in vec3 inPosition;

out vec4 worldPosition;
out vec3 localPosition;

uniform mat4 modelMatrix;
uniform mat4 mvp;

void main() {
	worldPosition = modelMatrix * vec4(inPosition, 1);
	localPosition = inPosition;
	gl_Position = mvp * vec4(inPosition, 1);
}
#VS

#FS
#version 150 core

in vec4 worldPosition;
in vec3 localPosition;

out vec4 outColor;
out vec4 outWorldPosition;
out vec4 outLocalPosition;

uniform vec4 color;

void main() {
	outColor = color;
	outWorldPosition = worldPosition;
	outLocalPosition = vec4(localPosition, 1);
}
#FS
