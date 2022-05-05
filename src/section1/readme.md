Game.java is the entry point of the game.
JDK 16 was used in development.

<h3>Controls</h3>

- Left cursor key : Steer Left
- Right cursor key : Steer Right
- Up cursor key : Accelerate
- Down cursor key : Decelerate

Rotating the kart image geometrically while steering the kart could cause lag issues in swing.<br/> Therefore, my game uses 16 different images to represent the kart's direction/rotation angle. <br>This provides quite smooth rotation with the least rotation angle beign π/8 rad which is acceptable for a simple arcade style game.
