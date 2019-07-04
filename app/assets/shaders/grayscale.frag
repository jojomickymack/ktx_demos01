uniform sampler2D u_texture;
uniform vec2 resolution;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    // sample our texture
    vec4 texColor = texture2D(u_texture, v_texCoords);

    // GRAYSCALE
    float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    gl_FragColor = vec4(gray, gray, gray, 1.0);
}