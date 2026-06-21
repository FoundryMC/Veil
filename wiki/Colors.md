Veil provides an expansive [`Color`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/color/Color.java) class. The Color class itself contains a
lot of utility methods, including methods to saturate, desaturate, lighten, darken, invert and mix colors. Colors can be
created from RGB(A) float values, RGB(A) int values and RGBA hex values (including strings), and can be processed back
into one of those, i.e you can go from an RGBA float value, to a Veil Color, to a hex string.

Veil also provides some default colors, along with some useful vanilla Minecraft colors - such as the 3 vanilla tooltip
color values.