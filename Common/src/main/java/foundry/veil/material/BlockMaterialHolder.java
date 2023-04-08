package foundry.veil.material;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.material.types.MaterialFieldType;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.util.Map;

public class BlockMaterialHolder {
    public static Map<Block, MaterialInstance> BLOCK_MATERIALS = Util.make(Maps.newHashMap(), (map) -> {

    });

    public static Material TEST_MATERIAL = new Material("rendertype_cutout", "minecraft", "veil:test", MaterialFieldType.TEXTURE, MaterialFieldType.COLOR);

    public static MaterialInstance TEST_MATERIAL_INSTANCE;

    static {
        try {
            TEST_MATERIAL_INSTANCE = new MaterialInstance(TEST_MATERIAL, ResourceLocation.tryParse("veil:textures/block/stone.png"), DefaultVertexFormat.BLOCK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MaterialInstance getMaterial(Block block) {
        return BLOCK_MATERIALS.get(block);
    }

    public static void setMaterial(Block block, MaterialInstance material) {
        BLOCK_MATERIALS.put(block, material);
    }

    public static void removeMaterial(Block block) {
        BLOCK_MATERIALS.remove(block);
    }

    public static boolean hasMaterial(Block block) {
        return BLOCK_MATERIALS.containsKey(block);
    }

    public static void clear() {
        BLOCK_MATERIALS.clear();
    }

    public static void init() {

    }
}
