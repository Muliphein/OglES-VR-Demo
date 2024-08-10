precision mediump float;
varying vec2 UV;
varying float rateF;
varying float biasF;
uniform sampler2D LeftTexture;
uniform sampler2D RightTexture;
void main() {
    if (UV.x > 0.0){
//        gl_FragColor = vec4((UV*vec2(1.0, 0.5) +vec2(0.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(-biasF, 0) , 0.0, 1.0);
        gl_FragColor = texture2D( RightTexture, (UV*vec2(1.0, 0.5) +vec2(0.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(-biasF, 0));
    } else {

//        gl_FragColor = vec4((UV*vec2(1.0, 0.5) +vec2(1.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(+biasF,0) , 0.0, 1.0);
        gl_FragColor = texture2D( LeftTexture, (UV*vec2(1.0, 0.5) +vec2(1.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(+biasF, 0));
    }
}