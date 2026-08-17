package foundry.veil.fabric.compat.immptl;

import com.mojang.blaze3d.shaders.Program;
import foundry.veil.api.compat.ImmersivePortalsCompat;
import net.minecraft.client.Minecraft;
import qouteall.imm_ptl.core.render.ShaderCodeTransformation;

public class VeilFabricImmersivePortalsCompat implements ImmersivePortalsCompat {
    private static boolean hasInitialized = false;

    @Override
    public void init() {
        if (!Minecraft.getInstance().getResourceManager().getNamespaces().isEmpty() && !hasInitialized) {
            // prevent initializing more than once
            hasInitialized = true;
            ShaderCodeTransformation.init();
        }
    }

    @Override
    public String transform(Program.Type type, String shaderId, String inputCode) {
        return ShaderCodeTransformation.transform(type, shaderId, inputCode);
    }

    @Override
    public boolean shouldAddUniform(String shaderName) {
        return ShaderCodeTransformation.shouldAddUniform(shaderName);
    }

    @Override
    public boolean renderingThroughPortal() {
        return false;
    }
}
