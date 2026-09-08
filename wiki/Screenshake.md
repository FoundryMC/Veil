Veil comes with a built-in system to shake the screen. To access the manager, simply call `VeilRenderer#getScreenShakeManager`.

Currently, there are two types of screen shake implemented: local and global. The `GlobalScreenShakeType` offsets the camera regardless of distance; the `LocalScreenShakeType` tapers its strength based on distance to the center.
Both currently implemented types allow for a Molang expression to be used, allowing for more complex effects.

To implement your own screen shake type, simply extend the abstract class `ScreenShakeType`.