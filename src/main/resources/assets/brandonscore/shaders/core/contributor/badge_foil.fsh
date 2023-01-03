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
    if (texCol.a < Transition) {
        discard;
    }

    vec3 coord = vec3(texCoord0, 0.0);
    //    vec4 baseColour = BaseColor;
    float value = BaseColor.w;
    float rad = distance(coord.xy, vec2(81.5/128, 49.5/128)) * 5;

    //    vec3 rgb = hsv2rgb(vec3(rad, 1, 1));

    //    baseColour = vec4(rgb, 1F);
    //    baseColour = vec4(1.5F, 0.259F, 0.302F, 1F);


    float density = 24;
    float noise = 0;
    for (int i = 1; i <= 5; i++) {
        float power = pow(2, float(i));
        noise += (3 / power) * max(snoise((coord * 0.5) + vec3(0, 0, Time * -0.02), power * density), -1);
    }
    noise *= 0.5;
    float t1 = -1 + mod(Time, 8.0) + noise;
    float t2 = -1 + mod(Time + 2, 8.0) + noise;
    float t3 = -1 + mod(Time + 4, 8.0) + noise;
    float t4 = -1 + mod(Time + 6, 8.0) + noise;
    value = min(min(abs(rad - t1), abs(rad - t2)), min(abs(rad - t3), abs(rad - t4)));
    value = max(0, 1. - pow(value, .5));

    //Too noisy
    //    float brightness = value;//max(value, 0.25);
    //    vec3 rgb = hsv2rgb(vec3(rad + (noise * 0.5), 1, brightness));

    //Maybe...
    //    float brightness = min(value * 5, 1);
    //    vec3 rgb = hsv2rgb(vec3((value * 0.25) + (180 / 360.0), 1, brightness));//Draconium
    //    vec3 rgb = hsv2rgb(vec3((value * 0.25) + (250 / 360.0), 1, brightness));//Wyvern
    //    vec3 rgb = hsv2rgb(vec3((value * 0.25), 1, brightness));//Draconic
    //    vec3 rgb = hsv2rgb(vec3((value * 0.125/2) + (355 / 360.0), 1, brightness));// Chaotic

    //Too RGB?
//    float brightness = value;//max(value, 0.25);
//    vec3 rgb = hsv2rgb(vec3(rad + (noise * 0.125) - (Time / 4), 1, brightness));

//        float brightness = min(value * 10, 1);
//        vec3 rgb = hsv2rgb(vec3(value * 0.75, 1, brightness));

//    vec3 rgb = hsv2rgb(vec3(rad, 0, 1)) * value;

    vec3 rgb = BaseColor.rgb * value;

    vec4 color = vec4(rgb, 1);

    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}


//void main() {
//    vec4 texCol = texture(Sampler0, texCoord0) * ColorModulator;
//    if (texCol.a < 0.6) {
//        discard;
//    }
//
//    vec3 coord = vec3(texCoord0, 0.0);
//    vec4 baseColour = BaseColor;
//    float brightness = BaseColor.w;
//    float rad = distance(coord.xy, vec2(81.5/128, 49.5/128)) * 5;
//
//    vec3 rgb = hsv2rgb(vec3(rad, 1, 1));
//
//    baseColour = vec4(rgb, 1F);
//    //    baseColour = vec4(1.5F, 0.259F, 0.302F, 1F);
//
//
//    float density = 24;
//    float noise = 0;
//    for (int i = 1; i <= 5; i++) {
//        float power = pow(2, float(i));
//        noise += (3 / power) * max(snoise((coord * 0.5) + vec3(0, 0, Time * -0.02), power * density), -1);
//    }
//    noise *= 0.5;
//    float t1 = -1 + mod(Time, 8.0) + noise;
//    float t2 = -1 + mod(Time + 2, 8.0) + noise;
//    float t3 = -1 + mod(Time + 4, 8.0) + noise;
//    float t4 = -1 + mod(Time + 6, 8.0) + noise;
//    brightness = min(min(abs(rad - t1), abs(rad - t2)), min(abs(rad - t3), abs(rad - t4)));
//    brightness = max(0, 1. - pow(brightness, .5));
//
//    vec4 color = vec4(baseColour.rgb * brightness, 1);
//    color *= vertexColor * ColorModulator;
//    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
//    color *= lightMapColor;
//
//    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
//}