package foundry.veil.mixin.fix;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// NeoForge fails to run data generators if the openGL feature is set
// This mixin makes sure the version is at least 3.3
@Mixin(Window.class)
public class WindowMixin {

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V", ordinal = 4, remap = false), index = 1)
    public int modifyMajorVersion(int value, @Share("major") LocalIntRef major) {
        major.set(value);
        return value;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V", ordinal = 5, remap = false), index = 1)
    public int modifyMinorVersion(int value, @Share("major") LocalIntRef major) {
        if (major.get() == 3 && value < 3) {
            return 3;
        }
        return value;
    }
}
