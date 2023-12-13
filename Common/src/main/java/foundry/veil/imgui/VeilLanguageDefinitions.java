package foundry.veil.imgui;

import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.extension.texteditor.flag.TextEditorPaletteIndex;

import java.util.HashMap;
import java.util.Map;

/**
 * Definitions for all custom Veil languages. These can be used with {@link CodeEditor}.
 */
public final class VeilLanguageDefinitions {

    private static final TextEditorLanguageDefinition GLSL = createGlsl();

    private VeilLanguageDefinitions() {
    }

    public static TextEditorLanguageDefinition glsl() {
        return GLSL;
    }

    public static TextEditorLanguageDefinition createGlsl() {
        TextEditorLanguageDefinition definition = new TextEditorLanguageDefinition();

        String[] keywords = {
                // OpenGL
                "const", "uniform", "buffer", "shared", "attribute", "varying",
                "coherent", "volatile", "restrict", "readonly", "writeonly",
                "atomic_uint",
                "layout",
                "centroid", "flat", "smooth", "noperspective",
                "patch", "sample",
                "invariant", "precise",
                "break", "continue", "do", "for", "while", "switch", "case", "default",
                "if", "else",
                "subroutine",
                "in", "out", "inout",
                "int", "void", "bool", "true", "false", "float", "double",
                "discard", "return",
                "vec2", "vec3", "vec4", "ivec2", "ivec3", "ivec4", "bvec2", "bvec3", "bvec4",
                "uint", "uvec2", "uvec3", "uvec4",
                "dvec2", "dvec3", "dvec4",
                "mat2", "mat3", "mat4",
                "mat2x2", "mat2x3", "mat2x4",
                "mat3x2", "mat3x3", "mat3x4",
                "mat4x2", "mat4x3", "mat4x4",
                "dmat2", "dmat3", "dmat4",
                "dmat2x2", "dmat2x3", "dmat2x4",
                "dmat3x2", "dmat3x3", "dmat3x4",
                "dmat4x2", "dmat4x3", "dmat4x4",
                "lowp", "mediump", "highp", "precision",
                "sampler1D", "sampler1DShadow", "sampler1DArray", "sampler1DArrayShadow",
                "isampler1D", "isampler1DArray", "usampler1D", "usampler1DArray",
                "sampler2D", "sampler2DShadow", "sampler2DArray", "sampler2DArrayShadow",
                "isampler2D", "isampler2DArray", "usampler2D", "usampler2DArray",
                "sampler2DRect", "sampler2DRectShadow", "isampler2DRect", "usampler2DRect",
                "sampler2DMS", "isampler2DMS", "usampler2DMS",
                "sampler2DMSArray", "isampler2DMSArray", "usampler2DMSArray",
                "sampler3D", "isampler3D", "usampler3D",
                "samplerCube", "samplerCubeShadow", "isamplerCube", "usamplerCube",
                "samplerCubeArray", "samplerCubeArrayShadow",
                "isamplerCubeArray", "usamplerCubeArray",
                "samplerBuffer", "isamplerBuffer", "usamplerBuffer",
                "image1D", "iimage1D", "uimage1D",
                "image1DArray", "iimage1DArray", "uimage1DArray",
                "image2D", "iimage2D", "uimage2D",
                "image2DArray", "iimage2DArray", "uimage2DArray",
                "image2DRect", "iimage2DRect", "uimage2DRect",
                "image2DMS", "iimage2DMS", "uimage2DMS",
                "image2DMSArray", "iimage2DMSArray", "uimage2DMSArray",
                "image3D", "iimage3D", "uimage3D",
                "imageCube", "iimageCube", "uimageCube",
                "imageCubeArray", "iimageCubeArray", "uimageCubeArray",
                "imageBuffer", "iimageBuffer", "uimageBuffer",
                "struct",
                // Vulkan
                "texture1D", "texture1DArray",
                "itexture1D", "itexture1DArray", "utexture1D", "utexture1DArray",
                "texture2D", "texture2DArray",
                "itexture2D", "itexture2DArray", "utexture2D", "utexture2DArray",
                "texture2DRect", "itexture2DRect", "utexture2DRect",
                "texture2DMS", "itexture2DMS", "utexture2DMS",
                "texture2DMSArray", "itexture2DMSArray", "utexture2DMSArray",
                "texture3D", "itexture3D", "utexture3D",
                "textureCube", "itextureCube", "utextureCube",
                "textureCubeArray", "itextureCubeArray", "utextureCubeArray",
                "textureBuffer", "itextureBuffer", "utextureBuffer",
                "sampler", "samplerShadow",
                "subpassInput", "isubpassInput", "usubpassInput",
                "subpassInputMS", "isubpassInputMS", "usubpassInputMS",
                // Reserved
                "common", "partition", "active",
                "asm",
                "class", "union", "enum", "typedef", "template", "this",
                "resource",
                "goto",
                "inline", "noinline", "public", "static", "extern", "external", "interface",
                "long", "short", "half", "fixed", "unsigned", "superp",
                "input", "output",
                "hvec2", "hvec3", "hvec4", "fvec2", "fvec3", "fvec4",
                "filter",
                "sizeof", "cast",
                "namespace", "using",
                "sampler3DRect"
        };
        definition.setKeywords(keywords);

        String[] identifiers = {
                // 8.1. Angle and Trigonometry Functions

                "radians", "Converts degrees to radians, i.e., (π / 180) · degrees",
                "degrees", "Converts radians to degrees, i.e., (180 / π) · radians",
                "sin", "The standard trigonometric sine function",
                "cos", "The standard trigonometric cosine function",
                "tan", "The standard trigonometric tangent",
                "asin", "Arc sine. Returns an angle whose sine is x.\nThe range of values returned by this function is [-π / 2, π / 2].\nResults are undefined if |x| > 1.",
                "acos", "Arc cosine. Returns an angle whose cosine is x.\nThe range of values returned by this function is [0,π].\nResults are undefined if |x| > 1",
                "atan", "Arc tangent. Returns an angle whose tangent is y / x.\nThe signs of x and y are used to determine\nwhat quadrant the angle is in. The range of\nvalues returned by this function is [-π, π.\nResults are undefined if x and y are both 0",
                "sinh", "Returns the hyperbolic sine function (eˣ - 1 / eˣ) / 2",
                "cosh", "Returns the hyperbolic sine function (eˣ + 1 / eˣ) / 2",
                "tanh", "Returns the hyperbolic tangent function sinh(x) / cosh(x)",
                "asinh", "Arc hyperbolic sine; returns the inverse of sinh",
                "acosh", "Arc hyperbolic cosine; returns the non-negative\ninverse of cosh. Results are undefined if x < 1",
                "atanh", "Arc hyperbolic tangent; returns the inverse of\ntanh. Results are undefined if x >= 1",
                // 8.2. Exponential Functions
                "pow", "Returns x raised to the y power, i.e., xʸ.\nResults are undefined if x < 0. Results are undefined if x\n= 0 and y <= 0",
                "exp", "Returns the natural exponentiation of x, i.e., eˣ",
                "log", "Returns the natural logarithm of x, i.e., returns\nthe value y which satisfies the equation x = eʸ.\nResults are undefined if x <= 0.",
                "exp2", "Returns 2 raised to the x power, i.e., 2ˣ",
                "log2", "Returns the base 2 logarithm of x, i.e., returns\nthe value y which satisfies the equation x = 2ʸ.\nResults are undefined if x <= 0",
                "sqrt", "Returns sqrt(x). Results are undefined if x < 0",
                "inversesqrt", "Returns 1 / sqrt(x). Results are undefined if x <= 0",
                // 8.3. Common Functions
                "abs", "Returns x if x >= 0; otherwise it returns -x",
                "sign", "Returns 1.0 if x > 0, 0.0 if x = 0, or -1.0 if x < 0",
                "floor", "Returns a value equal to the nearest integer that\nis less than or equal to x",
                "trunc", "Returns a value equal to the nearest integer to x\nwhose absolute value is not larger than the\nabsolute value of x",
                "round", "Returns a value equal to the nearest integer to x.\nThe fraction 0.5 will round in a direction chosen\nby the implementation, presumably the\ndirection that is fastest. This includes the\npossibility that round(x) returns the same value\nas roundEven(x) for all values of x",
                "roundEven", "Returns a value equal to the nearest integer to x.\nA fractional part of 0.5 will round toward the\nnearest even integer. (Both 3.5 and 4.5 for x will\nreturn 4.0)",
                "ceil", "Returns a value equal to the nearest integer that\nis greater than or equal to x",
                "fract", "Returns x - floor(x)",
                "mod", "Modulus. Returns x - y · floor(x / y).\nNote that implementations may use a cheap\napproximation to the remainder, and the error\ncan be large due to the discontinuity in floor.\nThis can produce mathematically unexpected\nresults in some cases, such as mod(x,x)\ncomputing x rather than 0, and can also cause\nthe result to have a different sign than the\ninfinitely precise result",
                "modf", "Returns the fractional part of x and sets i to the\ninteger part (as a whole number floating-point\nvalue). Both the return value and the output\nparameter will have the same sign as x",
                "min", "Returns y if y < x; otherwise it returns x",
                "max", "Returns y if x < y; otherwise it returns x",
                "clamp", "Returns min(max(x, minVal), maxVal). Results\nare undefined if minVal > maxVal",
                "mix", "Returns the linear blend of x and y, i.e., x · (1 - a) + y · a",
                "step", "Returns 0.0 if x < edge; otherwise it returns 1.0",
                "smoothstep", "Returns 0.0 if x <= edge0 and 1.0 if x >= edge1, and\nperforms smooth Hermite interpolation\nbetween 0 and 1 when edge0 < x < edge1. This is\nuseful in cases where you would want a\nthreshold function with a smooth transition",
                "isnan", "Returns true if x holds a NaN. Returns false otherwise.\nAlways returns false if NaNs are not implemented",
                "isinf", "Returns true if x holds a positive infinity or\nnegative infinity. Returns false otherwise.",

                "floatBitsToInt", """
Returns a signed or unsigned integer value
representing the encoding of a floating-point
value. The float value’s bit-level representation
is preserved""",

                "intBitsToFloat", """
Returns a floating-point value corresponding to
a signed or unsigned integer encoding of a
floating-point value. If a NaN is passed in, it will
not signal, and the resulting value is unspecified.
If an Inf is passed in, the resulting value is the
corresponding Inf. If a subnormal number is
passed in, the result might be flushed to 0.
Otherwise, the bit-level representation
is preserved""",

                "fma", """
Computes and returns a * b + c. In uses where
the return value is eventually consumed by a
variable declared as precise:
• fma() is considered a single operation,
  whereas the expression a * b + c consumed
  by a variable declared precise is considered
  two operations.
• The precision of fma() can differ from the
  precision of the expression a * b + c.
• fma() will be computed with the same
  precision as any other fma() consumed by a
  precise variable, giving invariant results for
  the same input values of a, b, and c.
Otherwise, in the absence of precise
consumption, there are no special constraints on
the number of operations or difference in
precision between fma() and the expression a * b + c""",

                "frexp", """
Splits x into a floating-point significand in the
range [0.5,1.0], and an integral exponent of two,
such that x = significant · pow(2, exponent)

The significand is returned by the function and
the exponent is returned in the parameter exp.
For a floating-point value of zero, the significand
and exponent are both zero

If an implementation supports signed zero, an
input value of minus zero should return a
significand of minus zero. For a floating-point
value that is an infinity or is not a number, the
results are undefined

If the input x is a vector, this operation is
performed in a component-wise manner; the
value returned by the function and the value
written to exp are vectors with the same number
of components as x""",

                "ldexp", """
Builds a floating-point number from x and the
corresponding integral exponent of two in exp,
returning: significant · pow(2, exponent)

If this product is too large to be represented in
the floating-point type, the result is undefined

If exp is greater than +128 (single-precision) or
+1024 (double-precision), the value returned is
undefined. If exp is less than -126 (singleprecision) or -1022 (double-precision), the value
returned may be flushed to zero. Additionally,
splitting the value into a significand and
exponent using frexp() and then reconstructing
a floating-point value using ldexp() should yield
the original input for zero and all finite nonsubnormal values.
If the input x is a vector, this operation is
performed in a component-wise manner; the
value passed in exp and returned by the
function are vectors with the same number of
components as x""",

                // 8.4. Floating-Point Pack and Unpack Functions

                "packUnorm2x16", """
First, converts each component of the
normalized floating-point value v into 16-bit
(2x16) or 8-bit (4x8) integer values. Then, the
results are packed into the returned 32-bit
unsigned integer.

The conversion for component c of v to fixed
point is done as follows:

round(clamp(c, 0, +1) * 65535.0)

The first component of the vector will be written
to the least significant bits of the output; the last
component will be written to the most
significant bits.""",

                "packSnorm2x16", """
First, converts each component of the
normalized floating-point value v into 16-bit
(2x16) or 8-bit (4x8) integer values. Then, the
results are packed into the returned 32-bit
unsigned integer.

The conversion for component c of v to fixed
point is done as follows:

round(clamp(c, -1, +1) * 32767.0)

The first component of the vector will be written
to the least significant bits of the output; the last
component will be written to the most
significant bits.""",


                "packUnorm4x8", """
First, converts each component of the
normalized floating-point value v into 16-bit
(2x16) or 8-bit (4x8) integer values. Then, the
results are packed into the returned 32-bit
unsigned integer.

The conversion for component c of v to fixed
point is done as follows:

round(clamp(c, 0, +1) * 255.0)

The first component of the vector will be written
to the least significant bits of the output; the last
component will be written to the most
significant bits.""",

                "packSnorm4x8", """
First, converts each component of the
normalized floating-point value v into 16-bit
(2x16) or 8-bit (4x8) integer values. Then, the
results are packed into the returned 32-bit
unsigned integer.

The conversion for component c of v to fixed
point is done as follows:

round(clamp(c, -1, +1) * 127.0)

The first component of the vector will be written
to the least significant bits of the output; the last
component will be written to the most
significant bits.""",

                "unpackUnorm2x16", """
First, unpacks a single 32-bit unsigned integer p
into a pair of 16-bit unsigned integers, a pair of
16-bit signed integers, four 8-bit unsigned
integers, or four 8-bit signed integers,
respectively. Then, each component is converted
to a normalized floating-point value to generate
the returned two- or four-component vector.

The conversion for unpacked fixed-point value f
to floating-point is done as follows:

unpackUnorm2x16: f / 65535.0

The first component of the returned vector will
be extracted from the least significant bits of the
input; the last component will be extracted from
the most significant bits.""",

                "unpackSnorm2x16", """
First, unpacks a single 32-bit unsigned integer p
into a pair of 16-bit unsigned integers, a pair of
16-bit signed integers, four 8-bit unsigned
integers, or four 8-bit signed integers,
respectively. Then, each component is converted
to a normalized floating-point value to generate
the returned two- or four-component vector.

The conversion for unpacked fixed-point value f
to floating-point is done as follows:

unpackSnorm2x16: clamp(f / 32767.0, -1, +1)

The first component of the returned vector will
be extracted from the least significant bits of the
input; the last component will be extracted from
the most significant bits.""",

                "unpackUnorm4x8", """
First, unpacks a single 32-bit unsigned integer p
into a pair of 16-bit unsigned integers, a pair of
16-bit signed integers, four 8-bit unsigned
integers, or four 8-bit signed integers,
respectively. Then, each component is converted
to a normalized floating-point value to generate
the returned two- or four-component vector.

The conversion for unpacked fixed-point value f
to floating-point is done as follows:

f / 255.0

The first component of the returned vector will
be extracted from the least significant bits of the
input; the last component will be extracted from
the most significant bits.""",

                "unpackSnorm4x8", """
First, unpacks a single 32-bit unsigned integer p
into a pair of 16-bit unsigned integers, a pair of
16-bit signed integers, four 8-bit unsigned
integers, or four 8-bit signed integers,
respectively. Then, each component is converted
to a normalized floating-point value to generate
the returned two- or four-component vector.

The conversion for unpacked fixed-point value f
to floating-point is done as follows:

clamp(f / 127.0, -1, +1)

The first component of the returned vector will
be extracted from the least significant bits of the
input; the last component will be extracted from
the most significant bits.""",

                "packHalf2x16", """
Returns an unsigned integer obtained by
converting the components of a two-component
floating-point vector to the 16-bit floating-point
representation of the API, and then packing
these two 16-bit integers into a 32-bit unsigned
integer.

The first vector component specifies the 16 least-
significant bits of the result; the second
component specifies the 16 most-significant bits""",

                "unpackHalf2x16", """
Returns a two-component floating-point vector
with components obtained by unpacking a 32-bit
unsigned integer into a pair of 16-bit values,
interpreting those values as 16-bit floating-point
numbers according to the API, and converting
them to 32-bit floating-point values.

The first component of the vector is obtained
from the 16 least-significant bits of v; the second
component is obtained from the 16 mostsignificant bits of v""",

                "packDouble2x32", """
Returns a double-precision value obtained by
packing the components of v into a 64-bit value.
If an IEEE 754 Inf or NaN is created, it will not
signal, and the resulting floating-point value is
unspecified. Otherwise, the bit-level
representation of v is preserved. The first vector
component specifies the 32 least significant bits;
the second component specifies the 32 most
significant bits""",

                "unpackDouble2x32", """
Returns a two-component unsigned integer
vector representation of v. The bit-level
representation of v is preserved. The first
component of the vector contains the 32 least
significant bits of the double; the second
component consists of the 32 most significant
bits""",

                // 8.5. Geometric Functions

                "length", "Returns the length of vector x",
                "distance", "Returns the distance between p0 and p1, i.e., length(p0 - p1)",
                "dot", "Returns the dot product of x and y, i.e., x0 · y0 + x1 · y1 + ...",
                "cross", "Returns the cross product of x and y, i.e.,\n(x1 · y2 - y1 · x2, x2 · y0 - y2 · x0, x0 · y1 - y0 · x1)",
                "normalize", "Returns a vector in the same direction as x but\nwith a length of 1, i.e. x / length(x)",

                "ftransform", """
Available only when using the compatibility
profile. For core OpenGL, use invariant.
For vertex shaders only. This function will
ensure that the incoming vertex value will be
transformed in a way that produces exactly the
same result as would be produced by OpenGL’s
fixed functionality transform. It is intended to
be used to compute gl_Position, e.g.

gl_Position = ftransform()

This function should be used, for example, when
an application is rendering the same geometry
in separate passes, and one pass uses the fixed
functionality path to render and another pass
uses programmable shaders""",

                "faceforward", "If dot(Nref, I) < 0 return N, otherwise return -N",
                "reflect", "For the incident vector I and surface orientation\nN, returns the reflection direction: I - 2 · dot(N, I) · N.\nN must already be normalized in order to\nachieve the desired result",
                "refract", "For the incident vector I and surface normal N,\nand the ratio of indices of refraction eta, return\nthe refraction vector. The result is computed by\nthe refraction equation",

                // 8.6. Matrix Functions

                "matrixCompMult", """
Multiply matrix x by matrix y component-wise,
i.e., result[i][j] is the scalar product of x[i][j] and
y[i][j].

Note: to get linear algebraic matrix
multiplication, use the multiply operator (*)""",

                "outerProduct", """
Treats the first parameter c as a column vector
(matrix with one column) and the second
parameter r as a row vector (matrix with one
row) and does a linear algebraic matrix multiply
c * r, yielding a matrix whose number of rows is
the number of components in c and whose
number of columns is the number of
components in r.
""",

                "transpose", "Returns a matrix that is the transpose of m.\nThe input matrix m is not modified",
                "determinant", "Returns the determinant of m",
                "inverse", """
Returns a matrix that is the inverse of m. The
input matrix m is not modified. The values in the
returned matrix are undefined if m is singular
or poorly-conditioned (nearly singular)""",

                // 8.7. Vector Relational Functions

                "lessThan", "Returns the component-wise compare of x < y",
                "lessThanEqual", "Returns the component-wise compare of x <= y",
                "greaterThan", "Returns the component-wise compare of x > y",
                "greaterThanEqual", "Returns the component-wise compare of x >= y",
                "equal", "Returns the component-wise compare of x == y",
                "notEqual", "Returns the component-wise compare of x != y",
                "any", "Returns true if any component of x is true",
                "all", "Returns true only if all components of x are true",
                "not", "Returns the component-wise logical complement of x",

                // 8.8. Integer Functions

                "uaddCarry", """
Adds 32-bit unsigned integers x and y, returning
the sum modulo 232. The value carry is set to
zero if the sum was less than 232, or one
otherwise""",

                "usubBorrow", """
Subtracts the 32-bit unsigned integer y from x,
returning the difference if non-negative, or 232
plus the difference otherwise. The value borrow
is set to zero if x ≥ y, or one otherwise""",

                "umulExtended", """
Multiplies 32-bit unsigned or signed integers x
and y, producing a 64-bit result. The 32 least-
significant bits are returned in lsb. The 32 most-
significant bits are returned in msb""",

                "bitfieldExtract", """
Extracts bits [offset, offset + bits - 1] from value,
returning them in the least significant bits of the
result.

For unsigned data types, the most significant bits
of the result will be set to zero. For signed data
types, the most significant bits will be set to the
value of bit offset + bits - 1.

If bits is zero, the result will be zero. The result
will be undefined if offset or bits is negative, or if
the sum of offset and bits is greater than the
number of bits used to store the operand. Note
that for vector versions of bitfieldExtract(), a
single pair of offset and bits values is shared for
all components""",

                "bitfieldInsert", """
Inserts the bits least significant bits of insert into
base.

The result will have bits [offset, offset + bits - 1]
taken from bits [0, bits - 1] of insert, and all other
bits taken directly from the corresponding bits
of base. If bits is zero, the result will simply be
base. The result will be undefined if offset or bits
is negative, or if the sum of offset and bits is
greater than the number of bits used to store the
operand.
Note that for vector versions of bitfieldInsert(),
a single pair of offset and bits values is shared
for all components""",

                "bitfieldReverse", """
Reverses the bits of value. The bit numbered n of
the result will be taken from bit (bits - 1) - n of
value, where bits is the total number of bits used
to represent value""",

                "bitCount", """
Returns the number of one bits in the binary
representation of value""",

                "findLSB", """
Returns the bit number of the least significant
one bit in the binary representation of value. If
value is zero, -1 will be returned""",

                "findMSB", """
Returns the bit number of the most significant
bit in the binary representation of value.

For positive integers, the result will be the bit
number of the most significant one bit. For
negative integers, the result will be the bit
number of the most significant zero bit. For a
value of zero or negative one, -1 will be
returned""",

                // 8.9. Texture Functions

                // 8.9.1. Texture Query Functions

                "textureSize", """
Returns the dimensions of level lod (if present)
for the texture bound to sampler, as described in
section 11.1.3.4 "Texture Queries" of the OpenGL
Specification.
The components in the return value are filled in,
in order, with the width, height, and depth of the
texture.

For the array forms, the last component of the
return value is the number of layers in the
texture array, or the number of cubes in the
texture cube map array""",

                "textureQueryLod", """
Returns the mipmap array(s) that would be
accessed in the x component of the return value.

Returns the computed level-of-detail relative to
the base level in the y component of the return
value.

If called on an incomplete texture, the results
are undefined.""",

                "textureQueryLevels", """
Returns the number of mipmap levels accessible
in the texture associated with sampler, as
defined in the OpenGL Specification.

The value zero will be returned if no texture or
an incomplete texture is associated with
sampler.

Available in all shader stages""",

                "textureSamples", "Returns the number of samples of the texture or\ntextures bound to sampler",

                // 8.9.2. Texel Lookup Functions

                "texture", """
Use the texture coordinate P to do a texture
lookup in the texture currently bound to
sampler.

For shadow forms: When compare is present, it
is used as Dref and the array layer comes from
the last component of P. When compare is not
present, the last component of P is used as Dref
and the array layer comes from the second to
last component of P. (The second component of
P is unused for 1D shadow lookups)

For non-shadow forms: the array layer comes
from the last component of P""",

                "textureProj", """
Do a texture lookup with projection. The texture
coordinates consumed from P, not including the
last component of P, are divided by the last
component of P to form projected coordinates P'.
The resulting third component of P in the
shadow forms is used as D. The third
component of P is ignored when sampler has
type gsampler2D and P has type vec4. After
these values are computed, texture lookup
proceeds as in texture""",

                "textureLod", """
Do a texture lookup as in texture but with
explicit level-of-detail; lod specifies λbase] and
sets the partial derivatives as follows:
(See section 8.14 "Texture Minification" and
equations 8.4-8.6 of the OpenGL Specification)""",

                "textureOffset", """
Do a texture lookup as in texture but with offset
added to the (u,v,w) texel coordinates before
looking up each texel. The offset value must be a
constant expression. A limited range of offset
values are supported; the minimum and
maximum offset values are implementation-
dependent and given by
gl_MinProgramTexelOffset and
gl_MaxProgramTexelOffset, respectively.

Note that offset does not apply to the layer
coordinate for texture arrays. This is explained
in detail in section 8.14.2 "Coordinate Wrapping
and Texel Selection" of the OpenGL
Specification, where offset is (δu, δv
, δw).
Note that texel offsets are also not supported for
cube maps""",

                "texelFetch", """
Use integer texture coordinate P to lookup a
single texel from sampler. The array layer comes
from the last component of P for the array
forms. The level-of-detail lod (if present) is as
described in sections 11.1.3.2 "Texel Fetches"
and 8.14.1 "Scale Factor and Level of Detail" of
the OpenGL Specification""",

                "texelFetchOffset", "Fetch a single texel as in texelFetch, offset by\noffset as described in textureOffset",
                "textureProjOffset", "Do a projective texture lookup as described in\ntextureProj, offset by offset as described in textureOffset",
                "textureLodOffset", "Do an offset texture lookup with explicit level-of-\ndetail. See textureLod and textureOffset",
                "textureProjLod", "Do a projective texture lookup with explicit\nlevel-of-detail. See textureProj and textureLod",
                "textureProjLodOffset", "Do an offset projective texture lookup with\nexplicit level-of-detail. See textureProj,\ntextureLod, and textureOffset.",

                "textureGrad", """
Do a texture lookup as in texture but with
explicit gradients as shown below. The partial
derivatives of P are with respect to window x
and window y. For the cube version, the partial
derivatives of P are assumed to be in the
coordinate system used before texture
coordinates are projected onto the appropriate
cube face""",

                "textureGradOffset", "Do a texture lookup with both explicit gradient\nand offset, as described in textureGrad and textureOffset",

                "textureProjGrad", """
"Do a texture lookup both projectively, as
described in textureProj, and with explicit
gradient as described in textureGrad. The
partial derivatives dPdx and dPdy are assumed
to be already projected""",

                "textureProjGradOffset", """
Do a texture lookup projectively and with
explicit gradient as described in
textureProjGrad, as well as with offset, as
described in textureOffset""",

                // 8.9.4. Texture Gather Functions

                "textureGather", """
Returns the value

vec4(Sample_i0_j1(P, base).comp,
  Sample_i1_j1(P, base).comp,
  Sample_i1_j0(P, base).comp,
  Sample_i0_j0(P, base).comp)
  
If specified, the value of comp must be a
constant integer expression with a value of 0, 1,
2, or 3, identifying the x, y, z, or w post-swizzled
component of the four-component vector
lookup result for each texel, respectively. If
comp is not specified, it is treated as 0, selecting
the x component of each texel to generate the result""",

                "textureGatherOffset", """
Perform a texture gather operation as in
textureGather by offset as described in
textureOffset except that the offset can be
variable (non constant) and the implementation-
dependent minimum and maximum offset
values are given by
MIN_PROGRAM_TEXTURE_GATHER_OFFSET and
MAX_PROGRAM_TEXTURE_GATHER_OFFSET, respectively""",

                "textureGatherOffsets", """
Operate identically to textureGatherOffset
except that offsets is used to determine the
location of the four texels to sample. Each of the
four texels is obtained by applying the
corresponding offset in offsets as a (u, v)
coordinate offset to P, identifying the four-texel
LINEAR footprint, and then selecting the texel i0
j0 of that footprint. The specified values in offsets
must be constant integral expressions""",

                // 8.9.5. Compatibility Profile Texture Functions

                "texture1D", "Same as texture, except only available in the compatibility profile",
                "texture1DProj", "Same as texture, except only available in the compatibility profile",
                "texture1DLod", "Same as texture, except only available in the compatibility profile",
                "texture1DProjLod", "Same as texture, except only available in the compatibility profile",
                "texture2D", "Same as texture, except only available in the compatibility profile",
                "texture2DProj", "Same as texture, except only available in the compatibility profile",
                "texture2DLod", "Same as texture, except only available in the compatibility profile",
                "texture2DProjLod", "Same as texture, except only available in the compatibility profile",
                "texture3D", "Same as texture, except only available in the compatibility profile",
                "texture3DProj", "Same as texture, except only available in the compatibility profile",
                "texture3DLod", "Same as texture, except only available in the compatibility profile",
                "texture3DProjLod", "Same as texture, except only available in the compatibility profile",
                "textureCube", "Same as texture, except only available in the compatibility profile",
                "textureCubeLod", "Same as texture, except only available in the compatibility profile",
                "shadow1D", "Same as texture, except only available in the compatibility profile",
                "shadow2D", "Same as texture, except only available in the compatibility profile",
                "shadow1DProj", "Same as texture, except only available in the compatibility profile",
                "shadow2DProj", "Same as texture, except only available in the compatibility profile",
                "shadow1DLod", "Same as texture, except only available in the compatibility profile",
                "shadow2DLod", "Same as texture, except only available in the compatibility profile",
                "shadow1DProjLod", "Same as texture, except only available in the compatibility profile",
                "shadow2DProjLod", "Same as texture, except only available in the compatibility profile",

                // 8.10. Atomic Counter Functions

                "atomicCounterIncrement", """
Atomically

1. increments the counter for c, and
2. returns its value prior to the increment
   operation""",

                "atomicCounterDecrement", """
Atomically

1. decrements the counter for c, and
2. returns the value resulting from the
   decrement operation""",

                "atomicCounter", "Returns the counter value for c",

                "atomicCounterAdd", """
Atomically

1. adds the value of data to the counter for c, and
2. returns its value prior to the operation""",

                "atomicCounterSubtract", """
Atomically
1. subtracts the value of data from the counter
   for c, and
2. returns its value prior to the operation""",

                "atomicCounterMin", """
Atomically

1. sets the counter for c to the minimum of the
   value of the counter and the value of data, and
2. returns the value prior to the operation""",

                "atomicCounterMax", """
Atomically

1. sets the counter for c to the maximum of the
   value of the counter and the value of data, and
2. returns the value prior to the operation""",

                "atomicCounterAnd", """
Atomically

1. sets the counter for c to the bitwise AND of
   the value of the counter and the value of
   data, and
2. returns the value prior to the operation""",

                "atomicCounterOr", """
Atomically

1. sets the counter for c to the bitwise OR of the
   value of the counter and the value of data, and
2. returns the value prior to the operation""",

                "atomicCounterXor", """
Atomically

1. sets the counter for c to the bitwise XOR of
   the value of the counter and the value of
   data, and
2. returns the value prior to the operation""",

                "atomicCounterExchange", """
Atomically

1. sets the counter value for c to the value of
   data, and
2. returns its value prior to the operation""",

                "atomicCounterCompSwap", """
Atomically

1. compares the value of compare and the
   counter value for c
2. if the values are equal, sets the counter value
   for c to the value of data, and
3. returns its value prior to the operation""",

                // 8.11. Atomic Memory Functions

                "atomicAdd", "Computes a new value by adding the value of\ndata to the contents mem",
                "atomicMin", "Computes a new value by taking the minimum\nof the value of data and the contents of mem",
                "atomicMax", "Computes a new value by taking the maximum\nof the value of data and the contents of mem",
                "atomicAnd", "Computes a new value by performing a bit-wise\nAND of the value of data and the contents of mem",
                "atomicOr", "Computes a new value by performing a bit-wise\nOR of the value of data and the contents of mem",
                "atomicXor", "Computes a new value by performing a bit-wise\nEXCLUSIVE OR of the value of data and the\ncontents of mem",
                "atomicExchange", "Computes a new value by simply copying the\nvalue of data",
                "atomicCompSwap", """
Compares the value of compare and the contents
of mem. If the values are equal, the new value is
given by data; otherwise, it is taken from the
original contents of mem""",

                // 8.12. Image Functions

                "imageSize", """
Returns the dimensions of the image or images
bound to image. For arrayed images, the last
component of the return value will hold the size
of the array. Cube images only return the
dimensions of one face, and the number of
cubes in the cube map array, if arrayed.
Note: The qualification readonly writeonly
accepts a variable qualified with readonly,
writeonly, both, or neither. It means the formal
argument will be used for neither reading nor
writing to the underlying memory""",

                "imageSamples", "Returns the number of samples of the image or\nimages bound to image",

                "imageLoad", """
Loads the texel at the coordinate P from the
image unit image (in IMAGE_PARAMS). For
multisample loads, the sample number is given
by sample. When image, P, and sample identify a
valid texel, the bits used to represent the
selected texel in memory are converted to a
vec4, ivec4, or uvec4 in the manner described
in section 8.26 "Texture Image Loads and Stores"
of the OpenGL Specification and returned""",

                "imageStore", """
Stores data into the texel at the coordinate P
from the image specified by image. For
multisample stores, the sample number is given
by sample. When image, P, and sample identify a
valid texel, the bits used to represent data are
converted to the format of the image unit in the
manner described in section 8.26 "Texture
Image Loads and Stores" of the OpenGL
Specification and stored to the specified texel""",

                "imageAtomicAdd", "Computes a new value by adding the value of\ndata to the contents of the selected texel",
                "imageAtomicMin", "Computes a new value by taking the minimum\nof the value of data and the contents of the\nselected texel",
                "imageAtomicMax", "Computes a new value by taking the maximum\nof the value data and the contents of the selected texel",
                "imageAtomicAnd", "Computes a new value by performing a bit-wise\nAND of the value of data and the contents of the\nselected texel",
                "imageAtomicOr", "Computes a new value by performing a bit-wise\nOR of the value of data and the contents of the\nselected texel",
                "imageAtomicXor", "Computes a new value by performing a bit-wise\nEXCLUSIVE OR of the value of data and the\ncontents of the selected texel",
                "imageAtomicExchange", "Computes a new value by simply copying the\nvalue of data",
                "imageAtomicCompSwap", """
Compares the value of compare and the contents
of the selected texel. If the values are equal, the
new value is given by data; otherwise, it is taken
from the original value loaded from the texel""",

                // 8.13. Geometry Shader Functions

                "EmitStreamVertex", """
Emits the current values of output variables to
the current output primitive on stream stream.
The argument to stream must be a constant
integral expression. On return from this call, the
values of all output variables are undefined.
Can only be used if multiple output streams are
supported""",

                "EndStreamPrimitive", """
Completes the current output primitive on
stream stream and starts a new one. The
argument to stream must be a constant integral
expression. No vertex is emitted.
Can only be used if multiple output streams
are supported""",

                "EmitVertex", """
Emits the current values of output variables to
the current output primitive. When multiple
output streams are supported, this is equivalent
to calling EmitStreamVertex(0).
On return from this call, the values of output
variables are undefined""",

                "EndPrimitive", """
Completes the current output primitive and
starts a new one. When multiple output streams
are supported, this is equivalent to calling
EndStreamPrimitive(0).
No vertex is emitted""",

                // 8.14. Fragment Processing Functions

                "dFdx", """
Returns either dFdxFine(p) or dFdxCoarse(p),
based on implementation choice, presumably
whichever is the faster, or by whichever is
selected in the API through quality-versus-speed
hints""",

                "dFdy", """
Returns either dFdyFine(p) or dFdyCoarse(p),
based on implementation choice, presumably
whichever is the faster, or by whichever is
selected in the API through quality-versus-speed
hints""",

                "dFdxFine", """
Returns the partial derivative of p with respect
to the window x coordinate. Will use local
differencing based on the value of p for the
current fragment and its immediate neighbor(s)""",

                "dFdyFine", """
Returns the partial derivative of p with respect
to the window y coordinate. Will use local
differencing based on the value of p for the
current fragment and its immediate neighbor(s)""",

                "dFdxCoarse", """
Returns the partial derivative of p with respect
to the window x coordinate. Will use local
differencing based on the value of p for the
current fragment’s neighbors, and will possibly,
but not necessarily, include the value of p for the
current fragment. That is, over a given area, the
implementation can x compute derivatives in
fewer unique locations than would be allowed
for dFdxFine(p)""",

                "dFdyCoarse", """
Returns the partial derivative of p with respect
to the window y coordinate. Will use local
differencing based on the value of p for the
current fragment’s neighbors, and will possibly,
but not necessarily, include the value of p for the
current fragment. That is, over a given area, the
implementation can compute y derivatives in
fewer unique locations than would be allowed
for dFdyFine(p)""",

                "fwidth", "Returns abs(dFdx(p)) + abs(dFdy(p))",
                "fwidthFine", "Returns abs(dFdxFine(p)) + abs(dFdyFine(p))",
                "fwidthCoarse", "Returns abs(dFdxCoarse(p)) + abs(dFdyCoarse(p))",

                // 8.14.2. Interpolation Functions

                "interpolateAtCentroid", """
Returns the value of the input interpolant
sampled at a location inside both the pixel and
the primitive being processed. The value
obtained would be the same value assigned to
the input variable if declared with the centroid
qualifier""",

                "interpolateAtSample", """
Returns the value of the input interpolant
variable at the location of sample number
sample. If multisample buffers are not available,
the input variable will be evaluated at the center
of the pixel. If sample sample does not exist, the
position used to interpolate the input variable is
undefined""",

                "interpolateAtOffset", """
Returns the value of the input interpolant
variable sampled at an offset from the center of
the pixel specified by offset. The two floating-
point components of offset, give the offset in
pixels in the x and y directions, respectively.
An offset of (0, 0) identifies the center of the
pixel. The range and granularity of offsets
supported by this function is implementationdependent""",

                // 8.15. Noise Functions

                "noise1", """
Returns a 1D noise value based on the input
value x

Deprecated starting with version
4.4 of GLSL""",

                "noise2", """
Returns a 2D noise value based on the input
value x

Deprecated starting with version
4.4 of GLSL""",

                "noise3", """
Returns a 3D noise value based on the input
value x

Deprecated starting with version
4.4 of GLSL""",

                "noise4", """
Returns a 4D noise value based on the input
value x

Deprecated starting with version
4.4 of GLSL""",

                // 8.16. Shader Invocation Control Functions

                "barrier", """
For any given static instance of barrier(), all
tessellation control shader invocations for a
single input patch must enter it before any will
be allowed to continue beyond it, or all compute
shader invocations for a single workgroup must
enter it before any will continue beyond it""",

                // 8.17. Shader Memory Control Functions

                "memoryBarrier", "Control the ordering of memory transactions\nissued by a single shader invocation",
                "memoryBarrierAtomicCounter", "Control the ordering of accesses to atomic-\ncounter variables issued by a single shader invocation",
                "memoryBarrierBuffer", "Control the ordering of memory transactions to\nbuffer variables issued within a single shader invocation",

                "memoryBarrierShared", """
Control the ordering of memory transactions to
shared variables issued within a single shader
invocation, as viewed by other invocations in
the same workgroup.

Only available in compute shaders""",

                "memoryBarrierImage", "Control the ordering of memory transactions to\nimages issued within a single shader invocation",

                "groupMemoryBarrier", """
Control the ordering of all memory transactions
issued within a single shader invocation, as
viewed by other invocations in the same
workgroup.

Only available in compute shaders""",

                // 8.18. Subpass-Input Functions

                "subpassLoad", """
Read from a subpass input, from the implicit
location (x, y, layer) of the current fragment
coordinate""",

                // 8.19. Shader Invocation Group Functions

                "anyInvocation", "Returns true if and only if value is true for at\nleast one active invocation in the group",
                "allInvocations", "Returns true if and only if value is true for all\nactive invocations in the group",
                "allInvocationsEqual", "Returns true if value is the same for all active\ninvocations in the group",
        };

        Map<String, String> identifiersMap = new HashMap<>(identifiers.length / 2);
        for (int i = 0; i < identifiers.length; i += 2) {
            identifiersMap.put(identifiers[i], identifiers[i + 1]);
        }
        definition.setIdentifiers(identifiersMap);

        Map<String, Integer> tokenRegexStrings = new HashMap<>();
        tokenRegexStrings.put("[ \\t]*#[ \\t]*[a-zA-Z_]+", TextEditorPaletteIndex.Preprocessor);
        tokenRegexStrings.put("[ \\t]*#[ \\t]*version.+", TextEditorPaletteIndex.Preprocessor);
        tokenRegexStrings.put("L?\\\"(\\\\.|[^\\\"])*\\\"", TextEditorPaletteIndex.String);
        tokenRegexStrings.put("\\'\\\\?[^\\']\\'", TextEditorPaletteIndex.CharLiteral);
        tokenRegexStrings.put("0[xX][0-9a-fA-F]+[uU]?[lL]?[lL]?", TextEditorPaletteIndex.Number);
        tokenRegexStrings.put("0[0-7]+[Uu]?[lL]?[lL]?", TextEditorPaletteIndex.Number);
        tokenRegexStrings.put("[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)([eE][+-]?[0-9]+)?[fF]?", TextEditorPaletteIndex.Number);
        tokenRegexStrings.put("[+-]?[0-9]+[Uu]?[lL]?[lL]?", TextEditorPaletteIndex.Number);
        tokenRegexStrings.put("[a-zA-Z_][a-zA-Z0-9_]*", TextEditorPaletteIndex.Identifier);
        tokenRegexStrings.put("[\\[\\]\\{\\}\\!\\%\\^\\&\\*\\(\\)\\-\\+\\=\\~\\|\\<\\>\\?\\/\\;\\,\\.]", TextEditorPaletteIndex.Punctuation);
        definition.setTokenRegexStrings(tokenRegexStrings);

        definition.setCommentStart("/*");
        definition.setCommentEnd("*/");
        definition.setSingleLineComment("//");

        definition.setAutoIdentation(true);

        definition.setName("GLSL");
        return definition;
    }
}
