#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV1;
in ivec2 UV2;

out vec2 shapeUv;
out vec4 shapeColor;
flat out ivec2 shapeSize;
flat out ivec2 shapeParameters;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    shapeUv = UV0;
    shapeColor = Color;
    shapeSize = UV1;
    shapeParameters = UV2;
}
