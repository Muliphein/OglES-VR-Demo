precision mediump float;
uniform sampler2D Texture;
varying vec2 verTextureCoord;
varying vec3 LightIntensity;
void main() {
    gl_FragColor = vec4(LightIntensity+vec3(0.2,0.2,0.2), 0.0) * texture2D(Texture,verTextureCoord);
}