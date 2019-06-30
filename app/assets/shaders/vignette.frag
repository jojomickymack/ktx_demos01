// texture 0
uniform sampler2D u_texture;

// our screen resolution, set from Java whenever the display is resized
uniform vec2 resolution;

// "in" attributes from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

// RADIUS of our vignette, where 0.5 results in a circle fitting the screen
const float RADIUS = 0.55;

// softness of our vignette, between 0.0 and 1.0
const float SOFTNESS = 0.7;

void main() {
    // sample our texture
    vec4 texColor = texture2D(u_texture, v_texCoords);

    // VIGNETTE

    // determine center position
    vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);

    // determine the vector length of the center position
    float len = length(position);

    // use smoothstep to create a smooth vignette
    float vignette = smoothstep(RADIUS, RADIUS-SOFTNESS, len);

    // apply the vignette with 50% opacity
    texColor.rgb = mix(texColor.rgb, texColor.rgb * vignette, 0.85);

    // final colour, multiplied by vertex colour
    gl_FragColor = texColor * v_color;
}