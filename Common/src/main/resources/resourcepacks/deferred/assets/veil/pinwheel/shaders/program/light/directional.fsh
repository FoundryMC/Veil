#include veil:common
#include veil:deferred_utils
#include veil:color_utilities

in vec2 texCoord;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;

uniform vec3 LightColor;
uniform vec3 LightDirection;

out vec4 fragColor;

void main() {
    vec4 albedoColor = texture(AlbedoSampler, texCoord);
    if(albedoColor.a == 0) {
        discard;
    }

    vec3 normalVS = texture(NormalSampler, texCoord).xyz;
    vec3 lightDirectionVS = worldToViewSpaceDirection(LightDirection);

    // lighting calculation
    float diffuse = -dot(normalVS, lightDirectionVS);
    diffuse = smoothstep(-0.2, 0.2, diffuse);

    float reflectivity = 0.1;
    vec3 diffuseColor = diffuse * LightColor;
    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity, albedoColor.a);
}
