# Render Type Stages

New `RenderStateShard`s can be added to any `RenderType` using [`RenderTypeShardRegistry`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/registry/RenderTypeShardRegistry.java). This allows fully custom or
vanilla render type shards to be added to anything.

### Example

```java
import foundry.veil.api.client.registry.RenderTypeShardRegistry;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class CoolMod {

    public static void initClient() {
        // This adds arbitrary code to setup/clear for the solid render type
        RenderTypeShardRegistry.addStage(RenderType.solid(), new RenderStateShard("coolmod:debug", () -> System.out.println("Setting up solid blocks"), () -> System.out.println("Clearing up solid blocks")) {
        });

        // Regular states can be added too
        RenderTypeShardRegistry.addStage(RenderType.cutout(), RenderType.TRANSLUCENT_TARGET);

        // This adds the particle shader to all render types that don't define a shader
        RenderTypeShardRegistry.addGenericStage(renderType -> renderType.state().shaderState == RenderType.NO_SHADER, new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader));

        // Other mods can also have their render types modified if a string is used
        RenderTypeShardRegistry.addStage("coolmod:custom_render_type", RenderType.TRANSLUCENT_TARGET);
    }
}
```

# Fixed Buffers

Vanilla Minecraft uses a small set of "fixed" buffers when rendering to allow multiple things to be put into render
buffers at the same time. However, these buffers are hardcoded and must be finished manually or else they will draw at
the wrong time.

Veil adds `VeilRegisterFixedBuffersEvent` to allow render types to be added to the fixed buffer map. The stage defined
is used to automatically end the batch when a specific render stage has passed.

### Example

```java
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.platform.services.VeilEventPlatform;

public class RenderingClass {

    private static final RenderType CUSTOM_RENDER_TYPE = ...;
    private static final RenderType COOL_CUSTOM_RENDER_TYPE = ...;

    public static void init() {
        VeilEventPlatform.INSTANCE.onVeilRegisterFixedBuffers(registry -> {
            // This will make it so anything using this render type will be batched together and drawn after particles
            registry.registerFixedBuffer(VeilRenderLevelStageEvent.Stage.AFTER_PARTICLES, CUSTOM_RENDER_TYPE);

            // This will register the render type as a fixed buffer, but will not end it
            // It is up to the programmer to finish this render type when it needs to finish
            registry.registerFixedBuffer(null, COOL_CUSTOM_RENDER_TYPE);
        });
    }
}
```

# Level Stage Rendering

Veil implements the `RenderLevelStageEvent` for Fabric, but custom stage registration is not supported. This should be
used as a last-resort to draw things that must be drawn at a specific time (for example, transparency). Fixed buffers
should cover most cases.

### Example

```java
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.platform.services.VeilEventPlatform;

public class RenderingClass {

    private static final RenderType CUSTOM_RENDER_TYPE = ...;
    private static final RenderType COOL_CUSTOM_RENDER_TYPE = ...;

    public static void init() {
        // This will draw custom things after particles have drawn
        VeilEventPlatform.INSTANCE.onVeilRenderTypeStageRender((stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum) -> {
            if (stage == VeilRenderLevelStageEvent.Stage.AFTER_SKY) {
                VertexConsumer builder = bufferSource.getBuffer(CUSTOM_RENDER_TYPE);
                // Draw things
                // don't end buffer because it's a fixed buffer. It will be automatically finished after particles
            } else if (stage == VeilRenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                VertexConsumer builder = bufferSource.getBuffer(COOL_CUSTOM_RENDER_TYPE);
                // Draw things
                // End buffer because it is not a fixed buffer
                bufferSource.endBatch(COOL_CUSTOM_RENDER_TYPE);
            }
        });

        // Fixed buffers and arbitrary drawing can be combined
        // Buffers defined to end after a specific event will always finish after the event fires
        VeilEventPlatform.INSTANCE.onVeilRegisterFixedBuffers(registry -> {
            // Fixed buffers can also be used to allow arbitrary drawing without ending the buffer
            registry.registerFixedBuffer(VeilRenderLevelStageEvent.Stage.AFTER_PARTICLES, CUSTOM_RENDER_TYPE);
        });
    }
}
```

# Layered Render Types

Drawing the same mesh data with different render types is relatively expensive in Vanilla Minecraft. Veil adds a system
that draws the buffer once and re-uses the data with different render settings at the same time.

### Example

```java
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.VeilRenderType;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Spider;
import org.jetbrains.annotations.Nullable;

public class CoolSpiderEntityRenderer extends LivingEntityRenderer<? extends Spider, SpiderModel<?>> {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation("veil", "textures/entity/spider/base.png");
    private static final ResourceLocation ARMOR_TEXTURE = new ResourceLocation("veil", "textures/entity/spider/red_armor.png");
    private static final ResourceLocation SPOT_TEXTURE = new ResourceLocation("veil", "textures/entity/spider/spot0.png");

    private static final RenderType COOL_RENDER_TYPE = VeilRenderType.layered(
            RenderType.entityCutoutNoCull(BASE_TEXTURE),
            RenderType.entityCutoutNoCull(ARMOR_TEXTURE),
            RenderType.entityCutoutNoCull(SPOT_TEXTURE));

    public CoolSpiderEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SpiderModel<>(ctx.bakeLayer(ModelLayers.SPIDER)), 1.0F);
    }

    @Override
    protected @Nullable RenderType getRenderType(Spider spider, boolean visible, boolean translucent, boolean glowing) {
        ResourceLocation textureLocation = this.getTextureLocation(spider);
        if (translucent) {
            return RenderType.itemEntityTranslucentCull(textureLocation);
        } else if (visible) {
            // This will draw all parts without having to re-draw the entire model
            return COOL_RENDER_TYPE;
        } else {
            return glowing ? RenderType.outline(textureLocation) : null;
        }
    }

    // In this example, the base texture will be used for glowing and semi-visible entities
    @Override
    public ResourceLocation getTextureLocation(Spider spider) {
        return BASE_TEXTURE;
    }
}
```