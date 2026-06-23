package foundry.veil.impl.client.editor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.Veil;
import foundry.veil.api.client.editor.SingleWindowInspector;
import foundry.veil.api.client.render.*;
import foundry.veil.api.quasar.data.EmitterShapeSettings;
import foundry.veil.api.quasar.data.ParticleEmitterData;
import foundry.veil.api.quasar.data.ParticleSettings;
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

    private final List<String> shapeKeys;
    private final String[] shapeArray;

    public ParticleEditorInspector() {
        this.shapeKeys = EmitterShapeRegistry.REGISTRY.keySet().stream().map(ResourceLocation::toString).toList();
        this.shapeArray = Arrays.copyOf(shapeKeys.toArray(), shapeKeys.toArray().length, String[].class);
    }

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
            ParticleEmitterData data = QuasarParticles.registryAccess().registry(QuasarParticles.EMITTER).map(registry -> registry.get(Veil.veilPath("default"))).orElse(null);
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

        if (vec3Field("position", editPosX, editPosY, editPosZ, 0.02F)) {
            emitter.setPosition(editPosX[0], editPosY[0], editPosZ[0]);
        }

        // General (emitters)
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

        // Shape (modules/emitter/shape)
        if (ImGui.collapsingHeader("Emitter Shapes")) {
            ImGui.indent();
            if (ImGui.beginListBox("##Shapes", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY() * 0.5f)) {
                renderShapeAttributes(emitter);
                ImGui.endListBox();
            }
            ImGui.unindent();
        }

        // Particle Settings (modules/emitter/particle)
        if (ImGui.collapsingHeader("Particle Settings")) {
            ImGui.indent();

            renderParticleSettings(emitter);

            ImGui.unindent();
        }

        // Particle Data (modules/particle_data)
        if (ImGui.collapsingHeader("Particle Data")) {
            ImGui.indent();

            renderParticleData(emitter);

            ImGui.unindent();
        }
    }

    private void renderShapeAttributes(MutableParticleEmitter emitter) {
        for (int i = 0; i < emitter.getEmitterShapeSettings().size(); i++) {
            EmitterShapeSettings shapeSettings = emitter.getEmitterShapeSettings().get(i);

            ImInt selectedShape = new ImInt();
            for (Map.Entry<ResourceKey<EmitterShape>, EmitterShape> e : EmitterShapeRegistry.REGISTRY.entrySet()) {
                if (e.getValue().getClass() == shapeSettings.shape().getClass()) {
                    selectedShape.set(shapeKeys.indexOf(e.getKey().location().toString()));
                }
            }

            ImGui.pushID(i);
            if (ImGui.combo("shape", selectedShape, shapeArray)) {
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

            if (vec3Field("dimensions", editX, editY, editZ, 0.01f)) {
                emitter.setShapeDimensions(i, editX[0], editY[0], editZ[0]);
            }

            if (vec3Field("rotation", editRotX, editRotY, editRotZ, 0.05f)) {
                emitter.setShapeRotation(i, editRotX[0], editRotY[0], editRotZ[0]);
            }

            if (ImGui.button("Reset Transform")) {
                emitter.resetShapeTransform(i);
            }

            if (ImGui.checkbox("from_surface", shapeSettings.fromSurface())) {
                emitter.setShapeFromSurface(i, !shapeSettings.fromSurface());
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
    }

    private void renderParticleSettings(MutableParticleEmitter emitter) {
        ParticleSettings settings = emitter.getParticleSettings();

        if (ImGui.checkbox("random_initial_direction", settings.randomInitialDirection())) {
            emitter.toggleRandomDirection();
        }

        float[] editDirectionX = new float[]{settings.initialDirection().x()};
        float[] editDirectionY = new float[]{settings.initialDirection().y()};
        float[] editDirectionZ = new float[]{settings.initialDirection().z()};

        if (vec3Field("initial_direction", editDirectionX, editDirectionY, editDirectionZ, 0.01F)) {
            emitter.setParticleDirection(editDirectionX[0], editDirectionY[0], editDirectionZ[0]);
        }

        if (ImGui.checkbox("random_initial_rotation", settings.randomInitialRotation())) {
            emitter.toggleRandomRotation();
        }

        if (ImGui.checkbox("random_speed", settings.randomSpeed())) {
            emitter.toggleRandomSpeed();
        }

        float[] editSpeed = new float[]{settings.particleSpeed()};

        if (ImGui.dragScalar("particle_speed", editSpeed, 0.01F)) {
            emitter.setParticleSpeed(editSpeed[0]);
        }

        if (ImGui.checkbox("random_size", settings.randomSize())) {
            emitter.toggleRandomSize();
        }

        float[] editParticleSizeMin = new float[]{settings.particleSize()};
        if (settings.randomSize()) {
            float[] editParticleSizeMax = new float[]{settings.particleSize() + settings.particleSizeVariation()};

            if (minMaxField("particle_size", editParticleSizeMin, editParticleSizeMax, 0.01F)) {
                emitter.setParticleSize(editParticleSizeMin[0], editParticleSizeMax[0]);
            }
        } else {
            if (ImGui.dragScalar("particle_size", editParticleSizeMin, 0.01F)) {
                emitter.setParticleSize(editParticleSizeMin[0], 0);
            }
        }

        if (ImGui.checkbox("random_lifetime", settings.randomLifetime())) {
            emitter.toggleParticleHasRandomLifetime();
        }

        int[] editLifetimeMin = new int[]{settings.particleLifetime()};
        if (settings.randomLifetime()) {
            int[] editParticleLifetimeMax = new int[]{settings.particleLifetime() + (int)settings.particleLifetimeVariation()};

            if (minMaxField("particle_lifetime", editLifetimeMin, editParticleLifetimeMax, 0.015F)) {
                emitter.setParticleLifetime(editLifetimeMin[0], editParticleLifetimeMax[0]);
            }
        } else {
            if (ImGui.dragScalar("particle_lifetime", editLifetimeMin, 0.015F)) {
                emitter.setParticleLifetime(editLifetimeMin[0], 0);
            }
        }
    }

    private void renderParticleData(MutableParticleEmitter emitter) {

    }

    private static boolean vec3Field(String label, float[] x, float[] y, float[] z, float vSpeed) {
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

    private static boolean minMaxField(String label, float[] x, float[] y, float vSpeed) {
        boolean dirty = false;

        float totalWidth = ImGui.calcItemWidth();
        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("min##x" + label, x, vSpeed)) {
            dirty = true;
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("max##y" + label, y, vSpeed, x[0])) {
            dirty = true;
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text(label);

        return dirty;
    }

    private static boolean minMaxField(String label, int[] x, int[] y, float vSpeed) {
        boolean dirty = false;

        float totalWidth = ImGui.calcItemWidth();
        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("min##x" + label, x, vSpeed)) {
            dirty = true;
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("max##y" + label, y, vSpeed, x[0])) {
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

        public void setShapeFromSurface(int index, boolean fromSurface) {
            EmitterShapeSettings oldShape = getEmitterShapeSettings().get(index);
            updateShapeSettings(index, oldShape.shape(), oldShape.dimensions(), oldShape.rotation(), fromSurface);
        }

        public void resetShapeTransform(int index) {
            EmitterShapeSettings oldShape = getEmitterShapeSettings().get(index);
            updateShapeSettings(index, oldShape.shape(), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), oldShape.fromSurface());
        }

        public void setParticleSpeed(float speed) {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setParticleSpeed(speed)
                    .build());
        }

        public void toggleRandomDirection() {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setRandomInitialDirection(!getParticleSettings().randomInitialDirection())
                    .build());
        }

        public void toggleRandomRotation() {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setRandomInitialRotation(!getParticleSettings().randomInitialRotation())
                    .build());
        }

        public void toggleRandomSize() {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setRandomSize(!getParticleSettings().randomSize())
                    .build());
        }

        public void toggleRandomSpeed() {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setRandomSpeed(!getParticleSettings().randomSpeed())
                    .build());
        }

        public void toggleParticleHasRandomLifetime() {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setRandomLifetime(!getParticleSettings().randomLifetime())
                    .build());
        }

        public void setParticleSize(float min, float max) {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setParticleSize(min)
                    .setParticleSizeVariation(max - min)
                    .build());
        }

        public void setParticleLifetime(int min, int max) {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setParticleLifetime(min)
                    .setParticleLifetimeVariation(max - min)
                    .build());
        }

        public void setParticleDirection(float x, float y, float z) {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setInitialDirection(new Vector3f(x, y, z))
                    .build());
        }

        public void forceRemove() {
            forceRemoved = true;
            this.remove();
        }

        private static class ParticleSettingsBuilder {
            private float particleSpeed;
            private float particleSize;
            private float particleSizeVariation;
            private int particleLifetime;
            private float particleLifetimeVariation;
            private Vector3fc initialDirection;
            private boolean randomInitialDirection;
            private boolean randomInitialRotation;
            private boolean randomSpeed;
            private boolean randomSize;
            private boolean randomLifetime;

            public ParticleSettingsBuilder(ParticleSettings from) {
                this.particleSpeed = from.particleSpeed();
                this.particleSize = from.particleSize();
                this.particleSizeVariation = from.particleSizeVariation();
                this.particleLifetime = from.particleLifetime();
                this.particleLifetimeVariation = from.particleLifetimeVariation();
                this.initialDirection = from.initialDirection();
                this.randomInitialDirection = from.randomInitialDirection();
                this.randomInitialRotation = from.randomInitialRotation();
                this.randomSpeed = from.randomSpeed();
                this.randomSize = from.randomSize();
                this.randomLifetime = from.randomLifetime();
            }

            public ParticleSettingsBuilder setParticleSpeed(float particleSpeed) {
                this.particleSpeed = particleSpeed;
                return this;
            }

            public ParticleSettingsBuilder setParticleSize(float particleSize) {
                this.particleSize = particleSize;
                return this;
            }

            public ParticleSettingsBuilder setParticleSizeVariation(float particleSizeVariation) {
                this.particleSizeVariation = particleSizeVariation;
                return this;
            }

            public ParticleSettingsBuilder setParticleLifetime(int particleLifetime) {
                this.particleLifetime = particleLifetime;
                return this;
            }

            public ParticleSettingsBuilder setParticleLifetimeVariation(float particleLifetimeVariation) {
                this.particleLifetimeVariation = particleLifetimeVariation;
                return this;
            }

            public ParticleSettingsBuilder setInitialDirection(Vector3fc initialDirection) {
                this.initialDirection = initialDirection;
                return this;
            }

            public ParticleSettingsBuilder setRandomInitialDirection(boolean randomInitialDirection) {
                this.randomInitialDirection = randomInitialDirection;
                return this;
            }

            public ParticleSettingsBuilder setRandomInitialRotation(boolean randomInitialRotation) {
                this.randomInitialRotation = randomInitialRotation;
                return this;
            }

            public ParticleSettingsBuilder setRandomSpeed(boolean randomSpeed) {
                this.randomSpeed = randomSpeed;
                return this;
            }

            public ParticleSettingsBuilder setRandomSize(boolean randomSize) {
                this.randomSize = randomSize;
                return this;
            }

            public ParticleSettingsBuilder setRandomLifetime(boolean randomLifetime) {
                this.randomLifetime = randomLifetime;
                return this;
            }

            public ParticleSettings build() {
                return new ParticleSettings(particleSpeed, particleSize, particleSizeVariation, particleLifetime, particleLifetimeVariation, initialDirection, randomInitialDirection, randomInitialRotation, randomSpeed, randomSize, randomLifetime
                );
            }


        }
    }
}
