attribute vec3 verPosition;
varying vec2 UV;
void main(){
    UV = vec2(verPosition);
    gl_Position = vec4(verPosition,1.0);
}