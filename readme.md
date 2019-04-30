# Ktx Demos

This is a work in progress 'hodge-podge' with a bunch of different screens that show off some feature or another.

One of the objectives is to make it so I don't have to have a whole repo dedicated to sharing one specific screen or idea - I can just throw 
it in here and have it built with the rest of them.

The other objective is to provide inspiration and re-usable parts to others who might be experimenting with libktx. 

All of the shader demos are ripped straight off of [mattdesl's lwjgl demos](https://github.com/mattdesl/lwjgl-basics). You'll find some very 
nice explanations of how to get started with shaders there.

I felt that porting these to kotlin was a smart idea for the following reasons.

- kotlin's multiline strings and variable interpolation (where variables are inserted directly into the string like ${this}) are perfect for 
defining vertex and fragment shaders in line

- all of the demos are in a single screen, which is perfect for this

- amazing shaders can be easily found on [shadertoy](https://www.shadertoy.com) and used in a demo

Besides shaders, there's tons of cool shortcuts and syntax enhancements provided by [libktx](https://github.com/libktx) that really need to be 
displayed in some kind of prototype in order for people to get started with them. When I was trying to use ktx for the first time, I needed 
some examples - this is meant to serve that purpose for others.

In addition to that, many underutilized libgdx features, like the 3d api, can be demoed here.

Animated model example ripped off from [games from scratch 
example](https://www.gamefromscratch.com/post/2014/01/19/3D-models-and-animation-from-Blender-to-LibGDX.aspx) - thank you!
