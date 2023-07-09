//package foundry.veil.mixin;
//
//import net.minecraft.client.renderer.EffectInstance;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.resources.ResourceManager;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.ModifyArg;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(EffectInstance.class)
//public class EffectInstanceMixin {
//    protected ResourceLocation trueId;
//
//    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"))
//    private void preMakeResourceName(ResourceManager $$0, String $$1, CallbackInfo ci) {
//        ResourceLocation nameId = new ResourceLocation($$1);
//        trueId = new ResourceLocation(
//                nameId.getNamespace(),
//                "shaders/program/" + nameId.getPath() + ".json"
//        );
//    }
//
//    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"))
//    private String modifyResourceName(String $$0) {
//        return trueId.toString();
//    }
//
//    @Unique
//    private static ResourceLocation idLoad = null;
//
//    @ModifyArg(method = "getOrCreate", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"))
//    private static String modifyFileId(String $$0) {
//        if (!$$0.contains(":")) idLoad = null;
//        else {
//            String name = $$0.substring("shaders/program/".length());
//            String nameWithType = name;
//            name = name.substring(0, name.length() - (".vsh".length()));
//            ResourceLocation nameId = new ResourceLocation(name);
//            idLoad = new ResourceLocation(
//                    nameId.getNamespace(),
//                    "shaders/program/" + nameId.getPath() + nameWithType.substring(name.length())
//            );
//            return idLoad.toString();
//        }
//        return $$0;
//    }
//
//    @ModifyArg(method = "getOrCreate", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/EffectProgram;compileShader(Lcom/mojang/blaze3d/shaders/Program$Type;Ljava/lang/String;Ljava/io/InputStream;Ljava/lang/String;)Lcom/mojang/blaze3d/shaders/EffectProgram;"), index = 1)
//    private static String modifyFileId2(String $$0) {
//        if (idLoad != null) return idLoad.toString();
//        return $$0;
//    }
//}
