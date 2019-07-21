#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform sampler2D u_texture0;

varying vec2 v_texCoords;

// sane values between ~[-0.003, 0.003];
uniform float offset;

void main(void) {
    vec2 uv = v_texCoords;
    uv.y = 1.0 - uv.y;

    vec4 col;
    col.r = texture2D(u_texture0, fract(vec2(uv.x + offset, -uv.y))).r;
    col.g = texture2D(u_texture0, fract(vec2(uv.x + 0.000, -uv.y))).g;
    col.b = texture2D(u_texture0, fract(vec2(uv.x - offset, -uv.y))).b;
    col.a = 1.0;

    gl_FragColor = col;
}