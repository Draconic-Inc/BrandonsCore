#version 120

uniform float time;
uniform float charge;
uniform ivec2 ePos;// = vec2(500, 800);
uniform ivec2 eSize;// = vec2(80, 150);
uniform ivec2 screenSize;// = vec2(80, 150);

varying vec3 position;


vec4 type = vec4[](vec4(0.1, 0.5, 0.8, 1), vec4(0.55, 0.25, 0.65, 1), vec4(0.7, 0.4, 0.2, 1), vec4(0.55, 0.2, 0.1, 0.2))[3];
float yRes = screenSize.y;


vec2 offset(vec2 uv, float sgn){
    float ar = float(screenSize.x) / screenSize.y;
    float yMin = gl_FragCoord.y - ePos.y;
    //Value ranges from -1 to 1 from top to bottom
    float pos = ((yMin / eSize.y) - 0.5) * 2;
    float ang = acos(pos);

    return vec2((uv.x * 2 * ar) + sgn * sin(ang), ang);
}

float snoise(vec3 uv, float res){
    const vec3 s = vec3(1e0, 1e2, 1e3);
    uv *= res;
    vec3 uv0 = floor(mod(uv, res))*s;
    vec3 uv1 = floor(mod(uv+vec3(1.), res))*s;
    vec3 f = fract(uv);
    f = f*f*(3.0-2.0*f);
    vec4 v = vec4(uv0.x+uv0.y+uv0.z, uv1.x+uv0.y+uv0.z, uv0.x+uv1.y+uv0.z, uv1.x+uv1.y+uv0.z);
    vec4 r = fract(sin(v*1e-1)*1e3);
    float r0 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
    r = fract(sin((v + uv1.z - uv0.z)*1e-1)*1e3);
    float r1 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
    return mix(r0, r1, f.z)*2.-1.;
}

void main() {
    float ar = float(screenSize.x) / screenSize.y;
    vec2 uv = gl_FragCoord.xy / screenSize.xy;
    uv.x *= ar;

    uv /= .5;
    float ZOOM = 0.25;

    float brightness = 0.0;
    vec2 uv2 = uv + (ZOOM * (offset(uv, .3)));

    float c2 = (float((charge * float(eSize.x)) + ePos.x) / float(screenSize.x));
    c2 *= 2 * ar;
    float flair = clamp(1. - abs(uv.x - c2), 0, 1);
    flair *= pow(flair, 200.) * 12.;
//
    float posState = 1. - clamp(pow(1000., (uv.x - c2) * 1000.), 0., 1.);

    for (int i = 1; i <= 7; i++){
        float power = pow(2., float(i));
        brightness += (2.5 / power) * sin(snoise(vec3(uv2, time * 0.02 * posState) + vec3(0., time * -0.05, 0.), 5. * power) * 10.);
    }

    if (uv.x > c2) {
        type = vec4(0.3, 0.3, 0.3, 1.);
    }
    else {
        brightness -= 0.;
        float pulse = clamp(sin(((uv2.x) - (time * .05)) * 110.) - .3, -1., 1.);
        pulse *= 0.8;
        brightness += pulse;
    }

    brightness += flair;
    gl_FragColor = vec4(pow(brightness * type.r, 3.0 * (1.0-type.r)), pow(brightness * type.g, 3.0*(1.0-type.g)), pow(brightness * type.b, 3.0*(1.0-type.b)), (0.35 - 0.) * 10.0);


}










//#version 120
//
//uniform float time;
//uniform float charge;
//uniform ivec2 ePos;// = vec2(500, 800);
//uniform ivec2 eSize;// = vec2(80, 150);
//uniform ivec2 screenSize;// = vec2(80, 150);
//
//varying vec3 position;
//
//
//vec4 type = vec4[](vec4(0.1, 0.5, 0.8, 1), vec4(0.55, 0.25, 0.65, 1), vec4(0.7, 0.4, 0.2, 1), vec4(0.55, 0.2, 0.1, 0.2))[3];
////vec2 fCoord;
////float yRes = eSize.y;//1440;
//float xRes = screenSize.x;
//
//
//
//vec2 offset(vec2 uv, float sgn)
//{
////    float ang = acos(1. * 2. * (((gl_FragCoord.x - ePos.x) / eSize.x) - 0.5));
//    float ang = acos(1. * 2. * (((gl_FragCoord.y - ePos.y) / eSize.y) - 0.5));
//    return vec2(ang, uv.x + sgn * sin(ang));
//}
//
//float snoise(vec3 uv, float res){
//    const vec3 s = vec3(1e0, 1e2, 1e3);
//    uv *= res;
//    vec3 uv0 = floor(mod(uv, res))*s;
//    vec3 uv1 = floor(mod(uv+vec3(1.), res))*s;
//    vec3 f = fract(uv);
//    f = f*f*(3.0-2.0*f);
//    vec4 v = vec4(uv0.x+uv0.y+uv0.z, uv1.x+uv0.y+uv0.z, uv0.x+uv1.y+uv0.z, uv1.x+uv1.y+uv0.z);
//    vec4 r = fract(sin(v*1e-1)*1e3);
//    float r0 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
//    r = fract(sin((v + uv1.z - uv0.z)*1e-1)*1e3);
//    float r1 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
//    return mix(r0, r1, f.z)*2.-1.;
//}
//
//void main() {
//    float aspect = screenSize.x / screenSize.y;
//
//    vec2 uv = gl_FragCoord.xy;// - ePos;
//    uv /= xRes * .125;
//    float ZOOM = 1.5;//screenSize.y / 512;//(float(eSize.y) / eSize.x) / (float(eSize.y) / eSize.x * 2);
//
//    float brightness = 0.0;
//
//    vec2 uv2 = uv + ZOOM * (offset(uv, .2));
//    uv2.x /= 4.5;// / (float(eSize.y) / eSize.x / 5);
//
//    float c2 = (float((charge * float(eSize.x)) + ePos.x) / float(screenSize.x));
//    c2 *= 8;
//    float flair = clamp(1. - abs(uv.x - c2), 0, 1);
//    flair *= pow(flair, 50.) * 8.;
//
//    float posState = 1. - clamp(pow(1000., (uv.x - c2) * 1000.), 0., 1.);
//
//    for (int i = 1; i <= 7; i++){
//        float power = pow(2., float(i));
//        brightness += (2.5 / power) * sin(snoise(vec3(uv2, time * 0.02 * posState) + vec3(time * -0.1, 0., 0.), 5. * power) * 10.);
//    }
//
//    if (uv.x > c2) {
//        type = vec4(0.3, 0.3, 0.3, 1.);
//    }
//    else {
//        brightness -= 0.;
//        float pulse = clamp(sin(((uv2.y) - (time * .1)) * 55.) - .3, -1., 1.);
//        pulse *= 1.8;
//        brightness +=  pulse;
//    }
//
//    brightness += flair;
//    gl_FragColor = vec4(pow(brightness * type.r, 3.0 * (1.0-type.r)), pow(brightness * type.g, 3.0*(1.0-type.g)), pow(brightness * type.b, 3.0*(1.0-type.b)), (0.35 - 0.) * 10.0);
//
//
//}
