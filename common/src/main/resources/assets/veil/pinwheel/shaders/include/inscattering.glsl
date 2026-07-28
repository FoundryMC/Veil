//Fog density
#define DENSITY 2.6
//Surface pass rate
#define PASSTHROUGH 0.5

#define STEPS 50.0

float sdSphere(vec3 p, float r)
{
    return (length(p) - r) / DENSITY + PASSTHROUGH;
}
