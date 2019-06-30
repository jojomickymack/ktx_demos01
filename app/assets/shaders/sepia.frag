// texture 0
uniform sampler2D u_texture;

// our screen resolution, set from Java whenever the display is resized
uniform vec2 resolution;

// "in" attributes from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

//sepia colour, adjust to taste
const vec3 SEPIA = vec3(1.2, 1.0, 0.8);

void main() {
    // sample our texture
    vec4 texColor = texture2D(u_texture, v_texCoords);

    // GRAYSCALE

    // convert to grayscale using NTSC conversion weights
    float gray = dot(texColor.rgb, vec3(0.3, 0.3, 0.3));

    // SEPIA

    // create our sepia tone from some constant value
    vec3 sepiaColor = vec3(gray) * SEPIA;

    // again we'll use mix so that the sepia effect is at 75%
    texColor.rgb = mix(texColor.rgb, sepiaColor, 0.75);

    // final colour, multiplied by vertex colour
    gl_FragColor = texColor * v_color;
}