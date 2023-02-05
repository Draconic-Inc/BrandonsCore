#version 150

#moj_import <fog.glsl>
#moj_import <brandonscore:math.glsl>

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
in vec3 vNorm;

out vec4 fragColor;

const float ils = -3; //Inner layer speed
const float ols = 6;  //Outer layer speed

void main() {
    vec3 coord = vec3(texCoord0, 0.0);
    if (Decay > 0 && (snoise(coord, 128) + 1) / 2 < Decay) {
        discard;
    }

    //Create darkening effect at edges of triangular shape. https://ss.brandon3055.com/8d065 (Example with heavy multiplier)
    float invY = 1 - coord.y; //1 at top
    float xMid = 1 - abs(coord.x - 0.5) * 2; //0 at sides
    float tval = xMid - invY; //triangle brightness value applied to base brightness

    float xAnim = (coord.x - 0.5) * 2;
    float brightness = -1.5 + min(tval * 10, 1.5) + (coord.y * coord.y * coord.y * 1.5);

    //Bottom Curve
    float curveDepth = 11.0/64.0;
    float cSin = 1 - (sin(coord.x * M_PI) * curveDepth);
    float cDist = (coord.y + 0.04) - cSin; //neg until cSin then pos, 0.04 offset helps reduce edge clipping at bottom left and right, Compensated for in renderer
    cDist = max(0, cDist + 0.1) / 0.3;
    cDist = pow(cDist, 4);
    brightness += cDist * 50;

    float density = 8;
    for (int i = 1; i <= 5; i++) {
        float power = pow(2, float(i));
        brightness += (2 / power) * max(snoise(coord + vec3(Time * 0.0 * ils, Time * 0.03 * ils, Time * 0.02), power * density), -1);

        vec3 oc = vec3(coord.xyz);
        for (float l = 1; l <= 2; l++) {
            float od = 8 + (l * 3);
            oc.xy *= 1.5;
            brightness += (1 / power) * max(snoise(oc + vec3(Time * 0.0 * (l + 1) * ols, Time * -0.02 * (l + 1) * ols, Time * -0.0), power * od), -1);
        }
    }

    brightness = max(brightness, 0) * ((BaseColor.w / 0.2) * 0.5);

    if (brightness > 3) {
        discard;
    }

    vec4 color = vec4(pow(brightness * BaseColor.r, 3 * (1.0-BaseColor.r)), pow(brightness * BaseColor.g, 3*(1.0-BaseColor.g)), pow(brightness * BaseColor.b, 3*(1.0-BaseColor.b)), 1.0);

    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
