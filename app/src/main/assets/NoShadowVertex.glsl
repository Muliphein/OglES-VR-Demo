attribute vec3 verPosition;
attribute vec3 verNormal;
attribute vec2 verTextureCoordinates;

varying vec3 LightIntensity;
varying vec2 verTextureCoord;

uniform vec3 LightPosition;
uniform vec3 LightAttribute;
uniform vec3 DiffuesLight;
uniform float LightStrength;

uniform mat4 viewMatrix;
uniform mat4 projectMatrix;
uniform mat4 rotateMatrix;
uniform mat4 normalMatrix;

void main(){
    vec3 TrueNormal = normalize(vec3(normalMatrix *vec4(verNormal,1.0)));
    vec3 TruePosition = vec3(rotateMatrix*vec4(verPosition,1.0));
    vec3 lightVec = normalize(LightPosition);
    float lightLength = length(LightPosition - TruePosition);
    LightIntensity = LightAttribute * DiffuesLight * max(dot(lightVec, TrueNormal), 0.0)/lightLength*LightStrength;
    verTextureCoord = verTextureCoordinates;
    gl_Position  = projectMatrix*viewMatrix*rotateMatrix*vec4(verPosition, 1.0);
}