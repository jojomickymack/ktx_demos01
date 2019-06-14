# Ktx Demos

![demos.gif](.github/demos.gif?raw=true)

This is a 'grab-bag of demos' - basically just a place for me to dump a single screen demo or experiment as I tinker around with opengl shader 
techniques and various libgdx/ktx features.

Most of the examples here were ported directly from [mattdesl's lwjgl tutorials](https://github.com/mattdesl/lwjgl-basics).

The lightshafts example is a refactored version from an article by 
[Fabrizio Pietrucci](https://spaghettidevops.com/2017/03/22/rendering-a-godrays-effect-as-postprocess-in-libgdx-using-shaders). The water 
example is partly copied from [Rahul Srivastava's article](https://rotatingcanvas.com/fragment-shader-to-simulate-water-surface-in-libgdx).

The animated model example uses a model from 
[games from scratch](https://www.gamefromscratch.com/post/2014/01/19/3D-models-and-animation-from-Blender-to-LibGDX.aspx).

My goal here was to gather a lot of advanced graphics tests into one place and have a 'work in progress' where anything can be tossed in.

Note - I've transformed the android module into something more akin to a traditional android app which makes use of layouts and views - the goal was to be able to select a demo from a RecyclerView because of how easy it is to populate and use. The selected game is passed using an intent extra to the GameActivity, then the App class in the core module takes the gameChoice as a constructor argument. The selected demo is triggered in a 'when' statement.

The desktop launcher uses the old 'scene2d' style menu, and is meant to be for ad-hock debugging and testing (and should probably be avoided). If you want to add more demos - I'd recommend creating it in a standalone game and dropping it in here as a single screen like the others. 
