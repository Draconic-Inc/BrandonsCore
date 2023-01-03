#version 150

#moj_import <fog.glsl>
#moj_import <brandonscore:math.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec4 BaseColor;
uniform float Time;
uniform float Transition;
uniform float Hue;

in vec3 fPos;
in vec3 vPos;
in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;
in vec4 vNorm;

out vec4 fragColor;

void main() {
    vec4 texCol = texture(Sampler0, texCoord0) * ColorModulator;
    if (texCol.a <= 0 || texCol.a > Transition) {
        discard;
    }

    if (texCol.a > Transition * 0.85) {
        fragColor = vec4(0, 0, 0, 1);
        return;
    }

    vec3 coord = vec3(texCoord0, 0.0);
    vec4 baseColour = BaseColor;
    float value = BaseColor.w;

    float noise = snoise(vec3(coord.x, coord.y, Time * 0.05), 16);
    value = 1 + noise;
    value *= texCol.a * 2;

    vec3 rgb = hsv2rgb(vec3(Hue + (abs(noise) * 0.125), 1, 1));
    vec4 color = vec4(rgb, min(texCol.a * 2, 1));

    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}