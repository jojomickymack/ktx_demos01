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

out vec4 colorOut;

void main() {
    colorOut = vec4(1.0, 0.0, 0.0, 1.0);
}