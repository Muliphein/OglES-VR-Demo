precision mediump float;
varying vec2 UV;
uniform sampler2D Texture;
void main() {
    gl_FragColor = texture2D( Texture, UV*vec2(0.5,0.5) +vec2(0.5,0.5) );
}

