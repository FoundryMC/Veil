#define BLOCK_SOLID 0.0
#define BLOCK_CUTOUT 0.1
#define BLOCK_CUTOUT_MIPPED 0.2
#define BLOCK_TRANSLUCENT 0.3

#define ENTITY_SOLID 0.4
#define ENTITY_CUTOUT 0.5
#define ENTITY_TRANSLUCENT 0.6
#define ENTITY_TRANSLUCENT_EMISSIVE 0.7

#define PARTICLE 0.8
#define ARMOR_CUTOUT 0.9
#define LEAD 1.0

bool isBlock(float material) {
    return material >= BLOCK_SOLID && material <= BLOCK_TRANSLUCENT;
}

bool isEntity(float material) {
    return material >= ENTITY_SOLID && material <= ENTITY_TRANSLUCENT_EMISSIVE;
}

bool isEmissive(float material) {
    return material == ENTITY_TRANSLUCENT_EMISSIVE;
}
