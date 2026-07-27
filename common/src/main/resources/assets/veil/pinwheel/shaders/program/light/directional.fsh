#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:voxel_shadow

in vec2 texCoord;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform sampler2D DepthSampler;

uniform vec3 LightColor;
uniform vec3 LightDirection;
uniform float Occluded;

out vec4 fragColor;

void main() {
    vec4 albedoColor = texture(AlbedoSampler, texCoord);
    if (albedoColor.a == 0) {
        discard;
    }

    vec3 normalVS = texture(NormalSampler, texCoord).xyz;
    vec3 lightDirectionVS = (VeilCamera.ViewMat * vec4(LightDirection, 0.0)).xyz;

    // lighting calculation
    float diffuse = clamp(smoothstep(-0.2, 0.2, -dot(normalVS, lightDirectionVS)), 0.0, 1.0);

    if (Occluded > 0.5) {
        vec3 normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
        vec3 pos = screenToWorldSpace(texCoord, texture(DepthSampler, texCoord).r).xyz;
        diffuse *= voxelshadowVisibility(pos + normalWS * 0.01, pos - LightDirection * 50);
    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * LightColor;
    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity, 1.0);
}
