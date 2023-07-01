package foundry.veil.render.texture;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RenderedTexturesManager {

    private static final LoadingCache<ResourceLocation, DynamicRenderTargetTexture> TEXTURE_CACHE =
            CacheBuilder.newBuilder()
                    .removalListener(i -> {
                        //REQUEST_FOR_CLOSING.add((DynamicRenderTargetTexture) i.getValue())
                        RenderSystem.recordRenderCall(((DynamicRenderTargetTexture) i.getValue())::close);
                    })
                    .expireAfterAccess(2, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public DynamicRenderTargetTexture load(ResourceLocation key) {
                            return null;
                        }
                    });

    //clears the texture cache and forge all to be re-rendered
    public static void clearCache() {
        TEXTURE_CACHE.invalidateAll();
    }

    /**
     * Gets a texture object on which you'll be able to directly draw onto as its in essence a frame buffer
     * Remember to call isInitialized() as the returned texture might be empty
     * For practical purposes you are only interested to call something like buffer.getBuffer(RenderType.entityCutout(texture.getTextureLocation()));
     *
     * @param id                     id of this texture. must be unique
     * @param textureSize            dimension
     * @param textureDrawingFunction this is the function responsible to draw things onto this texture
     * @param updateEachFrame        if this texture should be redrawn each frame. Useful if you are drawing an entity or animated item
     * @return texture instance
     */
    public static DynamicRenderTargetTexture requestTexture(
            ResourceLocation id, int textureSize,
            Consumer<DynamicRenderTargetTexture> textureDrawingFunction,
            boolean updateEachFrame) {

        var texture = TEXTURE_CACHE.getIfPresent(id);
        if (texture == null) {
            texture =
                    new DynamicRenderTargetTexture(id, textureSize, textureDrawingFunction);
            TEXTURE_CACHE.put(id, texture);
            //add to queue which will render them next rendering cycle. Returned texture will be blank
            //REQUESTED_FOR_RENDERING.add(texture);

            RenderSystem.recordRenderCall(texture::initialize);
        }
        return texture;
    }


//    @Deprecated(forRemoval = true)
//    public static DynamicRenderTargetTexture getFlatItemStackTexture(ResourceLocation res, ItemStack stack, int size) {
//        return requestFlatItemStackTexture(res, stack, size);
//    }

//    public static DynamicRenderTargetTexture requestFlatItemStackTexture(ResourceLocation res, ItemStack stack, int size) {
//        return requestTexture(res, size, t -> drawItem(t, stack), true);
//    }

//    public static DynamicRenderTargetTexture requestFlatItemTexture(Item item, int size) {
//        return requestFlatItemTexture(item, size, null);
//    }
//
//    public static DynamicRenderTargetTexture requestFlatItemTexture(Item item, int size, @Nullable Consumer<NativeImage> postProcessing) {
//        ResourceLocation id = Moonlight.res(Utils.getID(item).toString().replace(":", "/") + "/" + size);
//        return requestFlatItemTexture(id, item, size, postProcessing, false);
//    }

//    public static DynamicRenderTargetTexture requestFlatItemTexture(
//            ResourceLocation id, Item item, int size, @Nullable Consumer<NativeImage> postProcessing) {
//        return requestFlatItemTexture(id, item, size, postProcessing, false);
//    }
//
//    /**
//     * Draws a flax GUI-like item onto this texture with the given size
//     *
//     * @param item           item you want to draw
//     * @param size           texture size
//     * @param id             texture id. Needs to be unique
//     * @param postProcessing some extra drawing functions to be applied on the native image. Can be slow as its cpu sided
//     */
//    public static DynamicRenderTargetTexture requestFlatItemTexture(
//            ResourceLocation id, Item item, int size,
//            @Nullable Consumer<NativeImage> postProcessing, boolean updateEachFrame) {
//        return requestTexture(id, size, t -> {
//            drawItem(t, item.getDefaultInstance());
//            if (postProcessing != null) {
//                t.download();
//                NativeImage img = t.getPixels();
//                postProcessing.accept(img);
//                t.upload();
//            }
//        }, updateEachFrame);
//    }
//
//
//    //Utility methods
//
//    public static void drawItem(DynamicRenderTargetTexture tex, ItemStack stack) {
//        drawAsInGUI(tex, s -> {
//            //render stuff
//            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
//            //Minecraft.getInstance().gui.render(s,1);
//            itemRenderer.renderStatic(stack, 0, 0);
//        });
//    }

    /**
     * Coordinates here are from 0 to 1
     */
    public static void drawAsInWorld(DynamicRenderTargetTexture tex, Consumer<PoseStack> drawFunction) {
        drawAsInGUI(tex, s -> {
            float scale = 1f / 16f;
            s.scale(scale, scale, scale);
            drawFunction.accept(s);
        });
    }

    /**
     * Utility method that sets up an environment akin to gui rendering with a box from 0 t0 16.
     * If you render an item at 0,0 it will be centered
     */
    public static void drawAsInGUI(DynamicRenderTargetTexture tex, Consumer<PoseStack> drawFunction) {

        Minecraft mc = Minecraft.getInstance();
        RenderTarget frameBuffer = tex.getFrameBuffer();
        frameBuffer.clear(Minecraft.ON_OSX);
        //render to this one
        frameBuffer.bindWrite(true);

        int size = 16;
        //gui setup code
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        RenderSystem.backupProjectionMatrix();

        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F,
                size, 0, size, 1000.0F, 3000); //ForgeHooksClient.getGuiFarPlane()
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);

        //model view stuff
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();

        posestack.translate(0.0D, 0.0D, 1000F - 3000); //ForgeHooksClient.getGuiFarPlane()
        //apply new model view transformation
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        //end gui setup code

        drawFunction.accept(posestack);

        //reset stuff
        posestack.popPose();
        //reset model view
        RenderSystem.applyModelViewMatrix();

        //reset projection
        RenderSystem.restoreProjectionMatrix();
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        //returns render calls to main render target
        mc.getMainRenderTarget().bindWrite(true);
    }

    public static void drawTexture(DynamicRenderTargetTexture tex, ResourceLocation texture) {
        drawAsInGUI(tex, s -> {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(-8, 8, 0).uv(0, 1).endVertex();
            bufferbuilder.vertex(8, 8, 0).uv(1, 1).endVertex();
            bufferbuilder.vertex(8, -8, 0).uv(1, 0).endVertex();
            bufferbuilder.vertex(-8, -8, 0).uv(0, 0).endVertex();
            tesselator.end();
        });
    }


    //TODO: Stitch on an atlas
    //unused

    /*
        RenderSystem.setShaderTexture(0,
                new ResourceLocation("textures/gui/container/villager2.png")
       );
        Gui.blit(posestack,0,0,1000,0,0,  256,256,16,16);
    */

//    private static Matrix4f getProjectionMatrix(double pFov, int size, int renderDistance) {
//        PoseStack posestack = new PoseStack();
//        posestack.last().pose().setIdentity();
//        float zoom = 1;
//        float zoomX = 1;
//        float zoomY = 1;
//        if (zoom != 1.0F) {
//            posestack.translate((double) zoomX, (double) (-zoomY), 0.0D);
//            posestack.scale(zoom, zoom, 1.0F);
//        }
//
//        posestack.last().pose().multiply(Matrix4f.perspective(pFov, (float) size / size, 0.05F, renderDistance * 4f));
//        return posestack.last().pose();
//    }
//
//    private static void drawItem2(DynamicRenderTargetTexture tex, BlockPos mirrorPos, Direction mirrorDir, float partialTicks) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.level == null) return;
//
//        RenderTarget frameBuffer = tex.getFrameBuffer();
//        frameBuffer.clear(Minecraft.ON_OSX);
//        //render to this one
//        frameBuffer.bindWrite(true);
//
//        //gui setup code
//        // RenderSystem.clear(256, Minecraft.ON_OSX);
//
//        // Matrix4f oldProjection = RenderSystem.getProjectionMatrix();
//        // PoseStack posestack = RenderSystem.getModelViewStack();
//        //  posestack.pushPose();
//        int size = tex.getWidth();
//
//        GameRenderer gameRenderer = mc.gameRenderer;
//        LevelRenderer levelRenderer = mc.levelRenderer;
//
//        PoseStack posestack = RenderSystem.getModelViewStack();
//        posestack.pushPose();
//        RenderSystem.applyModelViewMatrix();
//        RenderSystem.clear(16640, Minecraft.ON_OSX);
//
//        FogRenderer.setupNoFog();
//
//        RenderSystem.enableTexture();
//        RenderSystem.enableCull();
//
//        RenderSystem.viewport(0, 0, size, size);
//        gameRenderer.renderZoomed(1, 0, 0);
//        levelRenderer.doEntityOutline();
//        //RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
//        // gameRenderer.renderLevel(partialTicks,Util.getMillis(),new PoseStack());
//        /*
//        {
//            PoseStack poseStack = new PoseStack();
//            RenderSystem.viewport(0, 0, size, size);
//           // DummyCamera camera = new DummyCamera();
//            //camera.setPosition(mirrorPos);
//           // camera.setAnglesInternal(0, -180);
//Camera camera = gameRenderer.getMainCamera();
//            gameRenderer.lightTexture().updateLightTexture(partialTicks);
//            //Camera camera = this.mainCamera;
//            //this.renderDistance = (float)(this.minecraft.options.getEffectiveRenderDistance() * 16);
//            PoseStack posestack1 = new PoseStack();
//            // double d0 = this.getFov(camera, partialTicks, true);
//            float fov = 70;
//            int renderDistance = 30;
//            posestack1.last().pose().multiply(getProjectionMatrix(fov, size, renderDistance));
//            Matrix4f matrix4f = posestack1.last().pose();
//            RenderSystem.setProjectionMatrix(matrix4f);
//            //camera.setup(this.minecraft.level, (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()), !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), pPartialTicks);
//            //camera.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees(0));
//            poseStack.mulPose(Vector3f.XP.rotationDegrees(-180));
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(0 + 180.0F));
//            Matrix3f matrix3f = poseStack.last().normal().copy();
//            if (matrix3f.invert()) {
//                RenderSystem.setInverseViewRotationMatrix(matrix3f);
//            }
//            levelRenderer.prepareCullFrustum(poseStack, camera.getPosition(),
//                    getProjectionMatrix(Math.max(fov, mc.options.fov), size, renderDistance));
//            levelRenderer.renderLevel(poseStack, partialTicks, Util.getNanos(), false, camera,
//                    gameRenderer, gameRenderer.lightTexture(), matrix4f);
//        }*/
//        posestack.popPose();
//
//        RenderSystem.applyModelViewMatrix();
//
//        //reset projection
//        //  RenderSystem.setProjectionMatrix(oldProjection);
//
//        // RenderSystem.clear(256, Minecraft.ON_OSX);
//        //returns render calls to main render target
//        mc.getMainRenderTarget().bindWrite(true);
//
//    }
}