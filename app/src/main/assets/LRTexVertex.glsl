attribute vec3 verPosition;
uniform float rateV;
uniform float biasV;
varying float rateF;
varying float biasF;
varying vec2 UV;
void main(){
    rateF = rateV;
    biasF = biasV;
    UV = vec2(verPosition);
    gl_Position = vec4(verPosition,1.0);
}