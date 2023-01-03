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

#define T_OFF 10.0
#define T_ANIM_ON 2.5
#define T_ON 5.0
#define T_ANIM_OFF 2.0

void main() {
    vec2 center = vec2(81.5/128, 49.5/128);
    vec3 origin = vec3(texCoord0 - center, 0.0);
    vec4 texCol = texture(Sampler0, texCoord0);
    float anim = mod(Time, T_OFF + T_ON + T_ANIM_ON + T_ANIM_OFF);
    float diamater = 42 / 128.0;

    if (texCol.a <= 0 || anim < T_OFF) {
        discard;
    }

    anim -= T_OFF;
    //0->1 controlling effect activation
    float turnOnAnim = anim < T_ANIM_ON ? anim / T_ANIM_ON : 1;
    anim -= T_ANIM_ON + T_ON;
    //0->1 controlling effect deactivation
    float turnOffAnim = anim < 0 ? 0 : anim / T_ANIM_OFF;

    vec3 coord = vec3(atan(origin.x, origin.y) / 6.28318 + 0.5, length(origin) * 0.4, 0);

    float len = length(origin);
    float dist = len / (diamater * 1.25 * turnOnAnim);
    if (dist > 1) {
        discard;
    }

    float value = 3 - (3 * dist);
    value *= min(1, (1.0 - (len / (diamater * 1.025))) * 100);

    vec3 coreColour = BaseColor.rgb;
    coreColour *= (1 - (texCol.a - 0.5));

    for(int i = 1; i <= 4; i++) {
        float power = pow(2.0, float(i));
        value += (1.5 / power) * snoise(coord + vec3(0.0,-Time * 0.05, Time * 0.01), power * 16.0);
    }

    value *= 1 - turnOffAnim;

    vec4 colour = vec4(pow(value * coreColour.r, 3 * (1.0 - coreColour.r)), pow(value * coreColour.g, 3 * (1.0 - coreColour.g)), pow(value * coreColour.b, 3 * (1.0 - coreColour.b)), min(value, 1));
    colour *= vertexColor * ColorModulator;
    colour.rgb = mix(overlayColor.rgb, colour.rgb, overlayColor.a);
    colour *= lightMapColor;

    fragColor = linear_fog(colour, vertexDistance, FogStart, FogEnd, FogColor);
}
