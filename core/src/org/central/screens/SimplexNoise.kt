package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.g2d.BitmapFont
import org.central.App
import org.central.assets.Images.badlogic
import org.central.assets.Images.slime
import org.central.assets.Images.mask


class SimplexNoise(val app: App) : KtxScreen {

    val VERT = """
attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};
attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;

uniform mat4 u_projTrans;

varying vec4 vColor;
varying vec2 vTexCoord;

void main() {
    vColor = ${ShaderProgram.COLOR_ATTRIBUTE};
    vTexCoord = ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
    gl_Position = u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
}"""

    val FRAG2 = """
varying vec4 vColor;
varying vec2 vTexCoord;
uniform sampler2D u_texture;
uniform sampler2D u_texture1;
uniform sampler2D u_mask;

uniform float time;

/////////////////////////////////////////////////////////////////////////
/////////////////// SIMPLEX NOISE FROM WEBGL-NOISE //////////////////////
/////////////////////////////////////////////////////////////////////////
//            https://github.com/ashima/webgl-noise/wiki               //
/////////////////////////////////////////////////////////////////////////

vec3 mod289(vec3 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
    return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
                      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
                     -0.577350269189626,  // -1.0 + 2.0 * C.x
                      0.024390243902439); // 1.0 / 41.0
    // First corner
    vec2 i  = floor(v + dot(v, C.yy) );
    vec2 x0 = v -   i + dot(i, C.xx);

    // Other corners
    vec2 i1;
    // i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
    // i1.y = 1.0 - i1.x;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    // x0 = x0 - 0.0 + 0.0 * C.xx ;
    // x1 = x0 - i1 + 1.0 * C.xx ;
    // x2 = x0 - 1.0 + 2.0 * C.xx ;
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    // Permutations
    i = mod289(i); // Avoid truncation effects in permutation
    vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 )) + i.x + vec3(0.0, i1.x, 1.0 ));

    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    m = m * m;
    m = m * m;

    // Gradients: 41 points uniformly over a line, mapped onto a diamond.
    // The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;

    // Normalise gradients implicitly by scaling m
    // Approximation of: m *= inversesqrt( a0*a0 + h*h );
    m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

    // Compute final noise value at P
    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

/////////////////////////////////////////////////////////////////////////\n" +
////////////////////       END SIMPLEX NOISE     ////////////////////////\n" +
/////////////////////////////////////////////////////////////////////////\n" +

void main(void) {
	// sample the colour from the first texture
	vec4 texColor0 = texture2D(u_texture, vTexCoord);
	// sample the colour from the second texture
	vec4 texColor1 = texture2D(u_texture1, vTexCoord);

	// pertube texcoord by x and y
	vec2 distort = 0.2 * vec2(snoise(vTexCoord + vec2(0.0, time/3.0)), snoise(vTexCoord + vec2(time/3.0, 0.0)));

	// get the mask; we will only use the alpha channel
	float mask = texture2D(u_mask, vTexCoord + distort).a;

	// interpolate the colours based on the mask
	gl_FragColor = vColor * mix(texColor0, texColor1, mask);
}"""

    private val tex = badlogic()
    private var slime = slime()
    private var mask = mask()

    lateinit var shader: ShaderProgram
    lateinit var fps: BitmapFont
    var time = 0f

    override fun show() {
        //important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false
        // val FRAG = Gdx.files.internal("lesson4b.frag").readString()

        //print it out for clarity
        println("Vertex Shader:\n-------------\n\n$VERT")
        println("\n")
        println("Fragment Shader:\n-------------\n\n$FRAG2")

        fps = BitmapFont()

        shader = ShaderProgram(VERT, FRAG2)
        if (!shader.isCompiled) {
            System.err.println(shader.log)
            System.exit(0)
        }
        if (shader.log.isNotEmpty()) println(shader.log)

        shader.begin()
        shader.setUniformi("u_texture1", 1)
        shader.setUniformi("u_mask", 2)
        shader.end()

        // bind mask to glActiveTexture(GL_TEXTURE2)
        mask.bind(2)

        // bind dirt to glActiveTexture(GL_TEXTURE1)
        tex.bind(1)

        // now we need to reset glActiveTexture to zero!!!! since sprite batch does not do this for us
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)

        // tex0 will be bound when we call SpriteBatch.draw
        app.stg.batch.shader = shader
    }

    override fun render(delta: Float) {
        time += Gdx.graphics.deltaTime

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        app.stg.batch.begin()
        shader.setUniformf("time", time)
        app.stg.batch.draw(slime, 0f, 0f, app.width, app.height)
        app.stg.batch.end()
    }

    override fun dispose() {
        shader.dispose()
    }
}