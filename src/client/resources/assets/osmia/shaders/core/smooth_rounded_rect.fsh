#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec2 shapeUv;
in vec4 shapeColor;
flat in ivec2 shapeSize;
flat in ivec2 shapeParameters;

out vec4 fragColor;

float roundedBoxDistance(vec2 point, vec2 halfSize, float radius) {
    vec2 cornerDistance = abs(point) - halfSize + vec2(radius);
    return length(max(cornerDistance, vec2(0.0)))
        + min(max(cornerDistance.x, cornerDistance.y), 0.0)
        - radius;
}

void main() {
    vec2 size = max(vec2(shapeSize), vec2(1.0));
    vec2 halfSize = size * 0.5;
    vec2 point = (shapeUv - vec2(0.5)) * size;

    int sideFlag = point.x < 0.0 ? 1 : 2;
    int roundedSides = shapeParameters.y & 3;
    float requestedRadius = float(max(shapeParameters.x, 0));
    float radius = (roundedSides & sideFlag) != 0
        ? min(requestedRadius, min(halfSize.x, halfSize.y))
        : 0.0;

    float distanceToEdge = roundedBoxDistance(point, halfSize, radius);
    float antialiasWidth = max(fwidth(distanceToEdge) * 0.75, 0.0001);
    float outerCoverage = 1.0 - smoothstep(
        -antialiasWidth, antialiasWidth, distanceToEdge
    );

    int strokeWidth = shapeParameters.y >> 2;
    float coverage = outerCoverage;
    if (strokeWidth > 0) {
        float innerCoverage = 1.0 - smoothstep(
            -antialiasWidth,
            antialiasWidth,
            distanceToEdge + float(strokeWidth)
        );
        coverage = max(outerCoverage - innerCoverage, 0.0);
    }

    vec4 color = shapeColor * ColorModulator;
    color.a *= coverage;
    if (color.a <= 0.001) {
        discard;
    }
    fragColor = color;
}
