#version 150

#moj_import <brandonscore:math.glsl>

uniform float time;
uniform float charge;
uniform ivec2 ePos;
uniform ivec2 eSize;
uniform ivec2 screenSize;
uniform vec4 ColorModulator;

out vec4 fragColor;

vec4 type = vec4[](vec4(0.1, 0.5, 0.8, 1), vec4(0.55, 0.25, 0.65, 1), vec4(0.7, 0.4, 0.2, 1), vec4(0.55, 0.2, 0.1, 0.2))[3];
float yRes = screenSize.y;
bool horizontal = eSize.x > eSize.y;

vec2 offset(vec2 uv, float sgn){
    float xMin = gl_FragCoord.x - ePos.x;
    //Value ranges from -1 to 1 from left to right
    float pos = ((xMin / eSize.x) - 0.5) * 2;
    float ang = acos(pos);
    return vec2(ang, (uv.y * 2) + sgn * sin(ang));
}

vec2 offsetH(vec2 uv, float sgn){
    float ar = float(screenSize.x) / screenSize.y;
    float yMin = gl_FragCoord.y - ePos.y;
    //Value ranges from -1 to 1 from top to bottom
    float pos = ((yMin / eSize.y) - 0.5) * 2;
    float ang = acos(pos);

    return vec2((uv.x * 2 * ar) + sgn * sin(ang), ang);
}

void main() {
    float ar = float(screenSize.x) / screenSize.y;
    vec2 uv = gl_FragCoord.xy / screenSize.xy;
    uv.x *= ar;

    uv /= .5;
    float ZOOM = 0.25;

    float brightness = 0.0;
    vec2 uv2;
    float c2;
    float flair;
    float posState;
    if (horizontal) {
        uv2 = uv + (ZOOM * (offsetH(uv, .3)));

        c2 = (float((charge * float(eSize.x)) + ePos.x) / float(screenSize.x));
        c2 *= 2 * ar;
        flair = clamp(1. - abs(uv.x - c2), 0, 1);
        flair *= pow(flair, 200.) * 12.;

        posState = 1. - clamp(pow(1000., (uv.x - c2) * 1000.), 0., 1.);
    } else {
        uv2 = uv + (ZOOM * (offset(uv, .3)));

        c2 = (float((charge * float(eSize.y)) + ePos.y) / float(screenSize.y));
        c2 *= 2;
        flair = clamp(1. - abs(uv.y - c2), 0, 1);
        flair *= pow(flair, 200.) * 12.;

        posState = 1. - clamp(pow(1000., (uv.y - c2) * 1000.), 0., 1.);
    }


    vec3 rotVec = horizontal ? vec3(0., time * -0.05, 0.) : vec3(time * 0.05, 0., 0.);
    for (int i = 1; i <= 7; i++){
        float power = pow(2., float(i));
        brightness += (2.5 / power) * sin(snoise(vec3(uv2, time * 0.02 * posState) + rotVec, 5. * power) * 10.);
    }

    if ((horizontal ? uv.x : uv.y) > c2) { // Sets the active to inactive transition point
        type = vec4(0.3, 0.3, 0.3, 1.);
    }
    else {  // Creates the "ripple" effect
        brightness -= 0.;
        float pulse = clamp(sin(((horizontal ? uv2.x : uv2.y) - (time * .05)) * 110.) - .3, -1., 1.);
        pulse *= 0.8;
        brightness += pulse;
    }

    brightness += flair;
    fragColor = vec4(pow(brightness * type.r, 3.0 * (1.0-type.r)), pow(brightness * type.g, 3.0*(1.0-type.g)), pow(brightness * type.b, 3.0*(1.0-type.b)), (0.35 - 0.) * 10.0);
}
