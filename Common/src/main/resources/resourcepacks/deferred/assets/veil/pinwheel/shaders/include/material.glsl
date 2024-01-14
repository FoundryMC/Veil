#define BLOCK_SOLID 0.0
#define BLOCK_CUTOUT 1.0
#define BLOCK_CUTOUT_MIPPED 2.0
#define BLOCK_TRANSLUCENT 3.0

#define ENTITY_SOLID 4.0
#define ENTITY_CUTOUT 5.0
#define ENTITY_TRANSLUCENT 6.0
#define ENTITY_TRANSLUCENT_EMISSIVE 7.0

#define PARTICLE 8.0
#define ARMOR_CUTOUT 9.0
#define LEAD 10.0
#define BREAKING 11.0
#define CLOUD 12.0
#define WORLD_BORDER 13.0

bool isBlock(float material) {
    return material >= BLOCK_SOLID - 0.1 && material <= BLOCK_TRANSLUCENT + 0.1;
}

bool isEntity(float material) {
    return material >= ENTITY_SOLID - 0.1 && material <= ENTITY_TRANSLUCENT_EMISSIVE + 0.1;
}

bool isEmissive(float material) {
    return abs(material - ENTITY_TRANSLUCENT_EMISSIVE) < 0.1;
}
