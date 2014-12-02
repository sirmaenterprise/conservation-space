attribute vec3 vertexPosition;
attribute vec3 vertexNormal;
attribute vec2 textureCoordinate;
uniform mat4 viewMatrix;
uniform mat4 positionMatrix;
uniform mat3 normalMatrix;
uniform vec3 ambientColor;
uniform vec3 lightLocation;
uniform vec3 lightColor;
uniform bool lightEnabled;
varying vec2 vTextureCoord;
varying vec3 vLightWeighting;
void main(void) {
	vec4 mvPosition = viewMatrix * vec4(vertexPosition, 1.0);
	gl_Position = positionMatrix * mvPosition;
	vTextureCoord = textureCoordinate;
	if (lightEnabled) {
		vec3 lightDirection = normalize(lightLocation - mvPosition.xyz);
		vec3 transformedNormal = normalMatrix * vertexNormal;
		float directionalLightWeighting = max(dot(transformedNormal, lightDirection), 0.0);
		vLightWeighting = ambientColor + lightColor * directionalLightWeighting;
	} else {
		vLightWeighting = vec3(1.0, 1.0, 1.0);
	}
}