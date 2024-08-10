precision highp float;
uniform sampler2D Texture;
uniform sampler2D ShadowTexture;
varying vec2 lightCoord;
varying vec2 verTextureCoord;
varying vec3 LightIntensity;
varying float nowDepth;
void main() {
    float depth = texture2D(ShadowTexture, lightCoord).g;
    if (depth - 0.005 < nowDepth){
        gl_FragColor = vec4(LightIntensity+vec3(0.2,0.2,0.2), 0.0) * texture2D(Texture,verTextureCoord);
    } else {
        gl_FragColor = vec4(0.2, 0.2, 0.2, 0.0) * texture2D(Texture,verTextureCoord);
    }
}