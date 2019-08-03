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

varying vec3 v_normal;

void main() {
    gl_FragColor = vec4(v_normal, 1.0);
}
