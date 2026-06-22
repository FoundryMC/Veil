package foundry.veil.impl.client.editor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.Veil;
import foundry.veil.api.client.editor.SingleWindowInspector;
import foundry.veil.api.client.render.*;
import foundry.veil.api.quasar.data.EmitterShapeSettings;
import foundry.veil.api.quasar.data.ParticleEmitterData;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.emitters.shape.EmitterShape;
import foundry.veil.api.quasar.particle.*;
import foundry.veil.api.quasar.registry.EmitterShapeRegistry;
import imgui.ImGui;
import imgui.flag.ImGuiDir;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.util.*;

public class ParticleEditorInspector extends SingleWindowInspector {
    public static final Component TITLE = Component.literal("Particle Editor");

    private final List<MutableParticleEmitter> emitters = new ArrayList<>();
    private int selectedEmitter;

    @Override
    protected void renderComponents() {
        ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
        Minecraft minecraft = Minecraft.getInstance();

        int[] value = {this.selectedEmitter};

        ImGui.beginDisabled(this.emitters.isEmpty());
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() * 0.6f);
        if (ImGui.sliderInt("##textures", value, 0, this.emitters.size() - 1, String.valueOf(this.selectedEmitter))) {
            this.selectedEmitter = value[0];
        }
        ImGui.endDisabled();
        ImGui.sameLine();
        ImGui.pushButtonRepeat(true);
        ImGui.beginDisabled(this.selectedEmitter <= 0);
        if (ImGui.arrowButton("##left", ImGuiDir.Left)) {
            this.selectedEmitter--;
        }
        ImGui.endDisabled();
        ImGui.beginDisabled(this.selectedEmitter >= this.emitters.size() - 1);
        ImGui.sameLine(0.0f, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.arrowButton("##right", ImGuiDir.Right)) {
            this.selectedEmitter++;
        }
        ImGui.endDisabled();
        ImGui.popButtonRepeat();

        ImGui.sameLine(0.0f, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.button("Create Emitter", ImGui.getContentRegionAvailX() * 0.5f, 0)) {
            ParticleEmitterData data = QuasarParticles.registryAccess().registry(QuasarParticles.EMITTER).map(registry -> registry.get(Veil.veilPath("burst"))).orElse(null);
            if (data != null) {
                MutableParticleEmitter emitter = new MutableParticleEmitter(particleManager, particleManager.getLevel(), data);
                HitResult hitResult = minecraft.hitResult;

                Vec3 position;
                if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                    position = hitResult.getLocation();
                } else {
                    position = new Vec3(minecraft.gameRenderer.getMainCamera().getLookVector()).multiply(3, 3, 3).add(minecraft.gameRenderer.getMainCamera().getPosition());
                }

                emitter.setPosition(position);

                particleManager.addParticleSystem(emitter);

                this.emitters.add(emitter);
                this.selectedEmitter = this.emitters.size() - 1;
            }

        }

        ImGui.beginDisabled(this.emitters.isEmpty());
        ImGui.sameLine(0.0f, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.button("Remove Emitter", ImGui.getContentRegionAvailX(), 0)) {
            MutableParticleEmitter emitter = this.emitters.get(this.selectedEmitter);
            emitter.trim(999);
            emitter.forceRemove();
            this.emitters.remove(this.selectedEmitter);
            this.selectedEmitter = Mth.clamp(this.selectedEmitter - 1, 0, this.emitters.size());

        }
        ImGui.endDisabled();

        if (ImGui.beginListBox("Properties", ImGui.getContentRegionAvail())) {
            if (!this.emitters.isEmpty() && this.emitters.get(this.selectedEmitter) != null) {
                renderParticleAttributes();
            }
            ImGui.endListBox();
        }

        for (int i = 0; i < this.emitters.size(); i++) {
            this.emitters.get(i).selected = i == this.selectedEmitter;
        }
    }

    @Override
    public void render() {
        ImGui.setNextWindowSizeConstraints(300, 500, Float.MAX_VALUE, Float.MAX_VALUE);

        super.render();
    }

    private void renderParticleAttributes() {
        MutableParticleEmitter emitter = this.emitters.get(this.selectedEmitter);

        float[] editPosX = new float[]{(float) emitter.getPosition().x()};
        float[] editPosY = new float[]{(float) emitter.getPosition().y()};
        float[] editPosZ = new float[]{(float) emitter.getPosition().z()};

        if (renderVec3Field("position", editPosX, editPosY, editPosZ, 0.02F)) {
            emitter.setPosition(editPosX[0], editPosY[0], editPosZ[0]);
        }

        int[] editMaxLifetime = new int[]{emitter.getMaxLifetime()};
        int[] editRate = new int[]{emitter.getRate()};
        int[] editCount = new int[]{emitter.getCount()};

        if (ImGui.dragScalar("max_lifetime", editMaxLifetime, 0.02F)) {
            emitter.setMaxLifetime(editMaxLifetime[0]);
        }

        if (ImGui.checkbox("loop", emitter.isLoop())) {
            emitter.setLoop(!emitter.isLoop());
        }

        float width = ImGui.getContentRegionAvailX() * 0.333f;
        ImGui.setNextItemWidth(width);
        if (ImGui.dragScalar("rate", editRate, 0.02F)) {
            emitter.setRate(editRate[0]);
        }
        ImGui.sameLine();
        ImGui.setNextItemWidth(width);
        if (ImGui.dragScalar("count", editCount, 0.02F, 0, Integer.MAX_VALUE)) {
            emitter.setCount(editCount[0]);
        }

        // Shape
        if (ImGui.collapsingHeader("Emitter Shapes")) {
            ImGui.indent();
            if (ImGui.beginListBox("##Shapes", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY() * 0.5f)) {
                for (int i = 0; i < emitter.getEmitterShapeSettings().size(); i++) {
                    EmitterShapeSettings shapeSettings = emitter.getEmitterShapeSettings().get(i);

                    List<String> shapeKeys = EmitterShapeRegistry.REGISTRY.keySet().stream().map(ResourceLocation::toString).toList();

                    ImInt selectedShape = new ImInt();
                    for (Map.Entry<ResourceKey<EmitterShape>, EmitterShape> e : EmitterShapeRegistry.REGISTRY.entrySet()) {
                        if (e.getValue().getClass() == shapeSettings.shape().getClass()) {
                            selectedShape.set(shapeKeys.indexOf(e.getKey().location().toString()));
                        }
                    }

                    ImGui.pushID(i);
                    if (ImGui.combo("shape", selectedShape, Arrays.copyOf(shapeKeys.toArray(), shapeKeys.toArray().length, String[].class))) {
                        ResourceLocation shapeKey = ResourceLocation.bySeparator(shapeKeys.get(selectedShape.get()), ':');
                        EmitterShape shape = EmitterShapeRegistry.REGISTRY.get(shapeKey);
                        emitter.setShape(i, shape);
                    }

                    float[] editX = new float[]{shapeSettings.dimensions().x()};
                    float[] editY = new float[]{shapeSettings.dimensions().y()};
                    float[] editZ = new float[]{shapeSettings.dimensions().z()};

                    float[] editRotX = new float[]{shapeSettings.rotation().x()};
                    float[] editRotY = new float[]{shapeSettings.rotation().y()};
                    float[] editRotZ = new float[]{shapeSettings.rotation().z()};

                    if (renderVec3Field("dimensions", editX, editY, editZ, 0.01f)) {
                        emitter.setShapeDimensions(i, editX[0], editY[0], editZ[0]);
                    }

                    if (renderVec3Field("rotation", editRotX, editRotY, editRotZ, 0.05f)) {
                        emitter.setShapeRotation(i, editRotX[0], editRotY[0], editRotZ[0]);
                    }

                    if (ImGui.button("Reset Transform")) {
                        emitter.resetTransform(i);
                    }

                    if (ImGui.checkbox("from_surface", shapeSettings.fromSurface())) {
                        emitter.setFromSurface(i, !shapeSettings.fromSurface());
                    }

                    if (i == emitter.getEmitterShapeSettings().size() - 1) {
                        if (ImGui.button("Add Shape")) {
                            emitter.getEmitterShapeSettings().add(new EmitterShapeSettings(EmitterShapeRegistry.REGISTRY.get(Veil.veilPath("sphere")), new Vector3f(1), new Vector3f(0), true));
                        }
                        ImGui.sameLine();
                    }
                    if (emitter.getEmitterShapeSettings().size() > 1) {
                        if (ImGui.button("Remove Shape")) {
                            emitter.getEmitterShapeSettings().remove(i--);
                        }
                    }

                    ImGui.separator();
                    ImGui.popID();
                }

                ImGui.endListBox();
            }
            ImGui.unindent();
        }
    }

    private boolean renderVec3Field(String label, float[] x, float[] y, float[] z, float vSpeed) {
        boolean dirty = false;

        float totalWidth = ImGui.calcItemWidth();
        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("##x" + label, x, vSpeed)) {
            dirty = true;
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##y" + label, y, vSpeed)) {
            dirty = true;
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##z" + label, z, vSpeed)) {
            dirty = true;
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text(label);

        return dirty;
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public Component getGroup() {
        return RESOURCE_GROUP;
    }

    private static class MutableParticleEmitter extends ParticleEmitter {
        private boolean forceRemoved = false;
        public boolean selected = false;

        public MutableParticleEmitter(ParticleSystemManager particleManager, ClientLevel level, ParticleEmitterData data) {
            super(particleManager, level, data);
        }

        @Override
        public void render(MatrixStack matrixStack, MultiBufferSource bufferSource, Camera camera, float partialTicks) {
            if (selected) {
                VertexConsumer debugBuilder = bufferSource.getBuffer(RenderType.debugLineStrip(1));
                matrixStack.matrixPush();
                matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
                for (EmitterShapeSettings shapeSettings : getEmitterShapeSettings()) {
                    matrixStack.matrixPush();
                    matrixStack.translate(getPosition());
                    shapeSettings.shape().renderShape(matrixStack.toPoseStack(), debugBuilder, shapeSettings.dimensions(), shapeSettings.rotation());
                    matrixStack.matrixPop();
                }
                matrixStack.matrixPop();
            }

            super.render(matrixStack, bufferSource, camera, partialTicks);
        }

        @Override
        protected void tick() {
            super.tick();

            if (this.isRemoved() && !forceRemoved) {
                this.reset();
                this.spawnTask = particleManager.getScheduler().scheduleAtFixedRate(this::spawn, 1, this.getRate()).toCompletableFuture();
            }
        }

        public void setRate(int rate) {
            super.setRate(rate);

            if (spawnTask != null) this.spawnTask.cancel(true);
            this.spawnTask = particleManager.getScheduler().scheduleAtFixedRate(this::spawn, 1, rate).toCompletableFuture();
            this.reset();
        }

        private void updateShapeSettings(int index, EmitterShape shape, Vector3fc dimension, Vector3fc rotation, boolean fromSurface) {
            EmitterShapeSettings shapeSettings = new EmitterShapeSettings(shape, dimension, rotation, fromSurface);
            this.getEmitterShapeSettings().set(index, shapeSettings);
        }

        public void setShape(int index, EmitterShape shape) {
            EmitterShapeSettings oldShape = getEmitterShapeSettings().get(index);
            updateShapeSettings(index, shape, oldShape.dimensions(), oldShape.rotation(), oldShape.fromSurface());
        }

        public void setShapeDimensions(int index, float x, float y, float z) {
            EmitterShapeSettings oldShape = getEmitterShapeSettings().get(index);
            updateShapeSettings(index, oldShape.shape(), new Vector3f(x, y, z), oldShape.rotation(), oldShape.fromSurface());
        }

        public void setShapeRotation(int index, float x, float y, float z) {
            EmitterShapeSettings oldShape = getEmitterShapeSettings().get(index);
            updateShapeSettings(index, oldShape.shape(), oldShape.dimensions(), new Vector3f(x, y, z), oldShape.fromSurface());
        }

        public void setFromSurface(int index, boolean fromSurface) {
            EmitterShapeSettings oldShape = getEmitterShapeSettings().get(index);
            updateShapeSettings(index, oldShape.shape(), oldShape.dimensions(), oldShape.rotation(), fromSurface);
        }

        public void resetTransform(int index) {
            EmitterShapeSettings oldShape = getEmitterShapeSettings().get(index);
            updateShapeSettings(index, oldShape.shape(), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), oldShape.fromSurface());
        }

        public void forceRemove() {
            forceRemoved = true;
            this.remove();
        }
    }
}
