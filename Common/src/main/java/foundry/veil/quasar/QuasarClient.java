package foundry.veil.quasar;

import foundry.veil.quasar.client.particle.QuasarParticleDataListener;
import foundry.veil.quasar.command.QuasarParticleCommand;
import foundry.veil.quasar.editor.Gizmo;
import foundry.veil.quasar.editor.ImGuiEditorOverlay;
import foundry.veil.quasar.editor.ImGuiEditorScreen;
import foundry.veil.quasar.editor.ImGuiGizmos;
import foundry.veil.quasar.emitters.ParticleEmitterJsonListener;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.anchors.AnchorPoint;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsJsonListener;
import foundry.veil.quasar.emitters.modules.emitter.settings.ParticleSettingsJsonListener;
import foundry.veil.quasar.emitters.modules.emitter.settings.ShapeSettingsJsonListener;
import foundry.veil.quasar.emitters.modules.particle.init.InitModuleJsonListener;
import foundry.veil.quasar.emitters.modules.particle.render.RenderModuleJsonListener;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModuleJsonListener;
import foundry.veil.quasar.fx.Line;
import foundry.veil.quasar.fx.Trail;
import foundry.veil.quasar.registry.AllParticleTypes;
import foundry.veil.quasar.util.ModelPartExtension;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Quasar.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class QuasarClient {
    public static ImGuiEditorOverlay editorScreen = null;
    public static final KeyMapping EDITOR_KEY = new KeyMapping("key.quasar.editor", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, "key.categories.misc");
    public static final List<Consumer<PoseStack>> delayedRenders = new ArrayList<>();
    public static EntityType<?> ZOMBIE = null;
    public static Gizmo xGizmo = new Gizmo(AABB.ofSize(Vec3.ZERO, 0, 0, 0), 1, 0, 0, 1, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO);
    public static Gizmo yGizmo = new Gizmo(AABB.ofSize(Vec3.ZERO, 0, 0, 0), 0, 1, 0, 1, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO);
    public static Gizmo zGizmo = new Gizmo(AABB.ofSize(Vec3.ZERO, 0, 0, 0), 0, 0, 1, 1, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO);
    public static double mouseX = 0;
    public static double mouseY = 0;
    public static Vec3[] previousPositions = new Vec3[0];
    public static Trail test = new Trail(0xFFFFFFFF, (ageScale) -> ageScale);
    public static AnchorPoint LOCAL_PLAYER_ANCHOR = new AnchorPoint(new ResourceLocation("quasar", "local_player_anchor"));

    public static Line TEST_LINE = new Line(
            new Vec3[]{
                    new Vec3(0,-55,0),
                    new Vec3(3, -55, 0),
                    new Vec3(3, -55, 3),
                    new Vec3(0, -55, 3),
            }, 0xFFFFFFFF, (ageScale) -> 0.25f);
    @SubscribeEvent
    public static void renderTranslucent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            PoseStack stack = event.getPoseStack();
            stack.pushPose();
            Vec3 pos = event.getCamera().getPosition();
            stack.translate(-pos.x, -pos.y, -pos.z);
            delayedRenders.forEach(consumer -> consumer.accept(stack));
//            test.render(stack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypeRegistry.RenderTypes.translucentNoCull(new ResourceLocation("quasar:textures/special/flamestexture.png"))), LightTexture.FULL_BRIGHT);
//            TEST_LINE.setBillboard(true);
//            TEST_LINE.setCurveMode(Line.CurveMode.BEZIER);
//            TEST_LINE.setFrequency(9);
//            TEST_LINE.render(stack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypeRegistry.RenderTypes.translucentNoCull(new ResourceLocation("quasar:textures/special/blank.png"))), LightTexture.FULL_BRIGHT);
            stack.popPose();
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            if (ZOMBIE == null) {
                ZOMBIE = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("minecraft:zombie"));
            }
            delayedRenders.clear();
            ImGuiGizmos.renderGizmos(event.getPoseStack(), event.getCamera(), editorScreen, event.getPartialTick());
        }
    }

    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(AllParticleTypes::registerFactories);
        modEventBus.addListener(QuasarClient::clientReloadListeners);
        modEventBus.addListener(QuasarClient::registerKeys);
        ParticleEmitterRegistry.bootstrap();
        TEST_LINE.setCurveMode(Line.CurveMode.BEZIER);
        TEST_LINE.setTexture(new ResourceLocation("quasar:textures/special/blank.png"));
        TEST_LINE.setLength(5000);
    }

    public static void clientReloadListeners(RegisterClientReloadListenersEvent event) {
        InitModuleJsonListener.register(event);
        UpdateModuleJsonListener.register(event);
        RenderModuleJsonListener.register(event);
        QuasarParticleDataListener.register(event);
        ParticleSettingsJsonListener.register(event);
        ShapeSettingsJsonListener.register(event);
        EmitterSettingsJsonListener.register(event);
        ParticleEmitterJsonListener.register(event);
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(EDITOR_KEY);
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(QuasarParticleCommand.CMD.register());
    }

    public static Gizmo currentlySelectedGizmo = null;
    private static Vec3 gizmoGrabOffset = Vec3.ZERO;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

    }
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if(Minecraft.getInstance().player != null){
            Player player = Minecraft.getInstance().player;
            LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
            Class<LivingEntityRenderer<?, ?>> clazz = (Class<LivingEntityRenderer<?, ?>>) renderer.getClass();
            EntityModel<LivingEntity> model = (EntityModel<LivingEntity>) clazz.cast(renderer).getModel();
            Map<String, ModelPart> parts = ImGuiEditorScreen.mapRenderers(model);
            LOCAL_PLAYER_ANCHOR.modelParts = ImGuiEditorScreen.getModelPartTree("right_arm", parts.containsKey("root") ? parts.get("root") : parts.values().stream().filter(s -> ((ModelPartExtension)(Object)s).getName() == "right_arm").findFirst().get());
            LOCAL_PLAYER_ANCHOR.updatePosition(player);
        }
        if (editorScreen == null) {
            editorScreen = new ImGuiEditorOverlay();
        }

        if (event.phase == TickEvent.Phase.END) {
            if(Minecraft.getInstance().screen instanceof ImGuiEditorScreen){
                editorScreen.renderEditor();
            }
            if(editorScreen.currentlySelectedEntity != null){
                AnchorPoint.TEST_POINT.updatePosition(editorScreen.currentlySelectedEntity);
            }
            if (editorScreen.currentlySelectedEmitterInstance != null) {
                if (currentlySelectedGizmo != null) {
                    Vec3 emitterPos = editorScreen.currentlySelectedEmitterInstance.getEmitterModule().getPosition();
                    // move emitter to player look pos on the axis of the gizmo keeping the offset from the gizmo
                    HitResult result = Minecraft.getInstance().hitResult;
                    if (result instanceof BlockHitResult bhr) {
                        Vec3 rayPos = bhr.getLocation();
                        Vec3 newPos = Vec3.ZERO;
                        if (currentlySelectedGizmo == xGizmo) {
                            newPos = new Vec3(rayPos.x - gizmoGrabOffset.x, emitterPos.y, emitterPos.z);
                        }
                        if (currentlySelectedGizmo == yGizmo) {
                            newPos = new Vec3(emitterPos.x, rayPos.y - gizmoGrabOffset.y, emitterPos.z);
                        }
                        if (currentlySelectedGizmo == zGizmo) {
                            newPos = new Vec3(emitterPos.x, emitterPos.y, rayPos.z - gizmoGrabOffset.z);
                        }
                        editorScreen.currentlySelectedEmitterInstance.getEmitterModule().setPosition(newPos);
                        editorScreen.position = newPos;
                    }

                }
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (EDITOR_KEY.consumeClick() && Minecraft.getInstance().player.hasPermissions(3)) {
            Minecraft.getInstance().setScreen(new ImGuiEditorScreen());
            HitResult result = Minecraft.getInstance().hitResult;
            if(result instanceof EntityHitResult ehr){
                editorScreen.currentlySelectedEntity = ehr.getEntity();
                editorScreen.modelParts = null;
                editorScreen.currentlySelectedEntityModelParts = null;
                editorScreen.currentlySelectedEntityModelPartName = null;
                editorScreen.root = null;
            } else {
                editorScreen.currentlySelectedEntity = null;
            }
        }
    }

    public static boolean down = false;

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !down && event.getAction() == InputConstants.PRESS) {
            down = true;
            if (editorScreen.currentlySelectedEmitterInstance != null) {
                // check if mouse is in aabb
                if (xGizmo.isMouseInAABB()) {
                    currentlySelectedGizmo = xGizmo;
                    gizmoGrabOffset = xGizmo.intersectionPoint.subtract(editorScreen.currentlySelectedEmitterInstance.getEmitterModule().getPosition()).multiply(new Vec3(1, 0, 0));
                }
                if (yGizmo.isMouseInAABB() && currentlySelectedGizmo == null) {
                    currentlySelectedGizmo = yGizmo;
                    gizmoGrabOffset = yGizmo.intersectionPoint.subtract(editorScreen.currentlySelectedEmitterInstance.getEmitterModule().getPosition()).multiply(new Vec3(0, 1, 0));
                }
                if (zGizmo.isMouseInAABB() && currentlySelectedGizmo == null) {
                    currentlySelectedGizmo = zGizmo;
                    gizmoGrabOffset = zGizmo.intersectionPoint.subtract(editorScreen.currentlySelectedEmitterInstance.getEmitterModule().getPosition()).multiply(new Vec3(0, 0, 1));
                }
            }
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && down && event.getAction() == InputConstants.RELEASE) {
            down = false;
            currentlySelectedGizmo = null;
            gizmoGrabOffset = Vec3.ZERO;
        }
    }
}
