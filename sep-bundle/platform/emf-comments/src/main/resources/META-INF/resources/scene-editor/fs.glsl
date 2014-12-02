precision mediump float;
varying vec2 vTextureCoord;
varying vec3 vLightWeighting;
uniform sampler2D samplerUniform;
uniform float alphaUniform;
uniform bool transparentUniform;
void main(void) {
	vec4 textureColor = texture2D(samplerUniform, vec2(vTextureCoord.s, vTextureCoord.t));
	if (transparentUniform) {
		gl_FragColor = vec4(textureColor.rgb * vLightWeighting, textureColor.a * alphaUniform);
	} else {
		gl_FragColor = vec4(textureColor.rgb * vLightWeighting, textureColor.a);
	}
}