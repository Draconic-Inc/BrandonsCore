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
uniform float Decay;

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
    vec3 coord = vec3(texCoord0, 0.0);
    vec4 baseColour = BaseColor;
    float brightness = BaseColor.w;
    vec4 texCol = texture(Sampler0, texCoord0) * ColorModulator;
    bool useAlt = false;//Alt is foil

    if (texCol.a < 0.1) {
        discard;
    } else if (texCol.a > 1 - Decay) {
        if (baseColour.a == 0) {
            useAlt = true;
        } else {
            texCol *= vertexColor * ColorModulator;
            texCol.rgb = mix(overlayColor.rgb, texCol.rgb, overlayColor.a);
            texCol *= lightMapColor;
            fragColor = linear_fog(texCol, vertexDistance, FogStart, FogEnd, FogColor);
            return;
        }
    } else if(texCol.a < 0.6) {
        brightness += 1 + (snoise(vec3(coord.x, coord.y, Time * 0.01), 32) * 2);
        coord *= 4;
    }else {
        coord *= 2;
    }

    vec4 color;
    if (useAlt) {
        baseColour = vec4(1.5F, 0.259F, 0.302F, 1F);
        float rad = (distance(coord.xy, vec2(81.5/128, 49.5/128)) * 5);
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
        brightness = min(min(abs(rad - t1), abs(rad - t2)), min(abs(rad - t3), abs(rad - t4)));
        brightness = max(0, 1. - pow(brightness, .5));

        color =vec4(baseColour.rgb * brightness, 1);
    } else {
        float density = 8;
        for (int i = 1; i <= 5; i++) {
            float power = pow(2, float(i));
            brightness += (2 / power) * max(snoise(coord + vec3(Time * 0.01, Time * -0.03, Time * 0.02), power * density), -1);

            vec3 oc = vec3(coord.xyz);
            for (float l = 1; l <= 2; l++) {
                float od = 8 + (l * 3);
                oc.xy *= 1.5;
                brightness += (1 / power) * max(snoise(oc + vec3(Time * -0.01 * (l + 1), Time * 0.03 * (l + 1), Time * -0.02), power * od), -1);
            }
        }
        brightness = max(brightness, 0);
        color = vec4(pow(brightness * baseColour.r, 3 * (1.0-baseColour.r)), pow(brightness * baseColour.g, 3*(1.0-baseColour.g)), pow(brightness * baseColour.b, 3*(1.0-baseColour.b)), 1.0);
    }


    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
