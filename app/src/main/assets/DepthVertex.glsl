attribute vec3 verPosition;
uniform mat4 viewMatrix;
uniform mat4 projectMatrix;
uniform mat4 rotateMatrix;
uniform float aNear;
uniform float aFar;
varying float depth;
varying vec4 Position;
varying vec3 TruePosition;
void main(){
    TruePosition = vec3(rotateMatrix*vec4(verPosition,1.0));
    Position = viewMatrix*rotateMatrix*vec4(verPosition, 1.0);
    depth =  1.0 - (-Position.z - aNear) / (aFar - aNear);
    gl_Position  = projectMatrix*viewMatrix*rotateMatrix*vec4(verPosition, 1.0);
}