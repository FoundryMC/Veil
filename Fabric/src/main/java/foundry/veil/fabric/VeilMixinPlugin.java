package foundry.veil.fabric;

import foundry.veil.Veil;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class VeilMixinPlugin implements IMixinConfigPlugin {

    private static final Set<String> COMPAT = Set.of(
            "foundry.veil.fabric.mixin.client.deferred",
            "foundry.veil.fabric.mixin.client.stage");
    private boolean sodium;

    @Override
    public void onLoad(String mixinPackage) {
        this.sodium = Veil.platform().isSodiumLoaded();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (String compat : COMPAT) {
            if (mixinClassName.startsWith(compat)) {
                return this.sodium ? !mixinClassName.startsWith(compat + ".vanilla") : !mixinClassName.startsWith(compat + ".sodium");
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    // Hack to make sure mixin doesn't have a panic attack
    public void preApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
