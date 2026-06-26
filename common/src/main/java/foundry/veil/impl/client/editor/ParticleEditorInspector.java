package foundry.veil.impl.client.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.JsonOps;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.api.ImGuiTextureProvider;
import foundry.veil.Veil;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.client.editor.SingleWindowInspector;
import foundry.veil.api.client.render.*;
import foundry.veil.api.quasar.data.*;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.shape.EmitterShape;
import foundry.veil.api.quasar.particle.*;
import foundry.veil.api.quasar.registry.EmitterShapeRegistry;
import foundry.veil.api.quasar.registry.RenderStyleRegistry;
import imgui.ImGui;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.Path;
import java.util.*;

public class ParticleEditorInspector extends SingleWindowInspector {
    public static final Component TITLE = Component.literal("Particle Editor");

    private final List<MutableParticleEmitter> emitters = new ArrayList<>();
    private int selectedEmitter;

    private final List<String> shapeKeys;
    private final String[] shapeArray;

    private final List<String> renderStyleKeys;
    private final String[] renderStyleArray;

    private final String[] modulesArray;
    private final ImInt selectedModule = new ImInt();

    private final ImBoolean saveWindowOpen = new ImBoolean(false);
    private final ImString saveName = new ImString();
    private final ImBoolean saveSeparateSettings = new ImBoolean(false);
    private final ImBoolean saveSeparateShape = new ImBoolean(false);
    private final ImBoolean saveSeparateData = new ImBoolean(false);
    private final ImBoolean loadWindowOpen = new ImBoolean(false);

    public ParticleEditorInspector() {
        this.shapeKeys = EmitterShapeRegistry.REGISTRY.keySet().stream().map(ResourceLocation::toString).toList();
        this.shapeArray = Arrays.copyOf(shapeKeys.toArray(), shapeKeys.toArray().length, String[].class);

        this.renderStyleKeys = RenderStyleRegistry.REGISTRY.keySet().stream().map(ResourceLocation::toString).toList();
        this.renderStyleArray = Arrays.copyOf(renderStyleKeys.toArray(), renderStyleKeys.toArray().length, String[].class);

        Object[] baseArray = ParticleModuleTypeRegistry.REGISTRY.keySet().stream().map(ResourceLocation::toString).sorted().toArray();
        this.modulesArray = Arrays.copyOf(baseArray, baseArray.length, String[].class);
    }

    @Override
    protected void renderComponents() {
        int[] value = {this.selectedEmitter};

        ImGui.beginDisabled(saveWindowOpen.get() || loadWindowOpen.get());

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
                createEmitterFromData(data);
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

        ImGui.pushItemWidth(ImGui.getContentRegionAvailX() * 0.5f);
        if (ImGui.button("Save")) saveWindowOpen.set(true);
        ImGui.sameLine();
        if (ImGui.button("Load")) loadWindowOpen.set(true);
        ImGui.popItemWidth();

        ImGui.separator();

        if (ImGui.beginListBox("Properties", ImGui.getContentRegionAvail())) {
            if (!this.emitters.isEmpty() && this.emitters.get(this.selectedEmitter) != null) {
                renderParticleAttributes();
            }
            ImGui.endListBox();
        }

        ImGui.endDisabled();
    }

    @Override
    public void render() {
        ImGui.setNextWindowSizeConstraints(300, 500, Float.MAX_VALUE, Float.MAX_VALUE);

        super.render();

        if (this.saveWindowOpen.get()) {
            ImGui.setNextWindowSizeConstraints(300, 300, Float.MAX_VALUE, Float.MAX_VALUE);
            if (ImGui.begin("Save Emitter", this.saveWindowOpen, ImGuiWindowFlags.NoSavedSettings)) {
                ImGui.inputText("Filename", saveName);

                //ImGui.checkbox("Separate Particle Settings file", saveSeparateSettings);

                //ImGui.checkbox("Separate Emitter Shape file", saveSeparateShape);

                //ImGui.checkbox("Separate Particle Data file", saveSeparateData);

                if (ImGui.button("Save")) {
                    saveEmitterToFile(this.emitters.get(selectedEmitter), saveName.get());
                }
            }
            ImGui.end();
        }

        if (this.loadWindowOpen.get()) {
            ImGui.setNextWindowSizeConstraints(300, 300, Float.MAX_VALUE, Float.MAX_VALUE);
            if (ImGui.begin("Load Emitter", this.loadWindowOpen, ImGuiWindowFlags.NoSavedSettings)) {
                if (ImGui.beginListBox("Emitters")) {
                    Set<ResourceLocation> emitterLocations = QuasarParticles.registryAccess().registry(QuasarParticles.EMITTER).get().keySet();
                    for (ResourceLocation emitter : emitterLocations) {
                        if (ImGui.selectable(emitter.toString())) {
                            createEmitterFromData(QuasarParticles.registryAccess().registry(QuasarParticles.EMITTER).get().get(emitter));
                            this.loadWindowOpen.set(false);
                        }
                    }

                    ImGui.endListBox();
                }
            }
            ImGui.end();
        }
    }

    private void renderParticleAttributes() {
        MutableParticleEmitter emitter = this.emitters.get(this.selectedEmitter);

        ImGui.text("Particles: " + emitter.getParticleCount());

        float[] editPos = new float[]{(float) emitter.getPosition().x(), (float) emitter.getPosition().y(), (float) emitter.getPosition().z()};

        if (ImGui.dragFloat3("position", editPos, 0.02F)) {
            emitter.setPosition(editPos[0], editPos[1], editPos[2]);
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
        boolean shapeInspectorOpen = false;

        if (ImGui.collapsingHeader("Emitter Shapes")) {
            shapeInspectorOpen = true;
            ImGui.indent();
            //if (ImGui.beginListBox("##Shapes", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY() * 0.5f)) {
                renderShapeAttributes(emitter);
                //ImGui.endListBox();
            //}
            ImGui.unindent();
        }

        // Particle Settings (modules/emitter/particle)
        boolean settingsInspectorOpen = false;

        if (ImGui.collapsingHeader("Particle Settings")) {
            settingsInspectorOpen = true;
            ImGui.indent();

            renderParticleSettings(emitter);

            ImGui.unindent();
        }
        for (int i = 0; i < this.emitters.size(); i++) {
            this.emitters.get(i).renderEmitterShape = i == this.selectedEmitter && shapeInspectorOpen;
            this.emitters.get(i).renderDirection = i == this.selectedEmitter && settingsInspectorOpen;
        }

        // Particle Data (modules/particle_data)
        if (ImGui.collapsingHeader("Particle Data")) {
            ImGui.indent();

            renderParticleData(emitter);

            ImGui.unindent();
        }

        if (ImGui.collapsingHeader("Modules")) {
            renderModules(emitter);
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

            float[] editDimensions = new float[]{shapeSettings.dimensions().x(), shapeSettings.dimensions().y(), shapeSettings.dimensions().z()};
            if (ImGui.dragFloat3("dimensions", editDimensions, 0.01f)) {
                emitter.setShapeDimensions(i, editDimensions[0], editDimensions[1], editDimensions[2]);
            }

            float[] editRotation = new float[]{shapeSettings.rotation().x(), shapeSettings.rotation().y(), shapeSettings.rotation().z()};
            if (ImGui.dragFloat3("rotation", editRotation, 0.05f)) {
                emitter.setShapeRotation(i, editRotation[0], editRotation[1], editRotation[2]);
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

        float[] editDirection = new float[]{settings.initialDirection().x(), settings.initialDirection().y(), settings.initialDirection().z()};

        if (ImGui.dragFloat3("initial_direction", editDirection, 0.01F)) {
            emitter.setParticleDirection(editDirection[0], editDirection[1], editDirection[2]);
        }

        if (ImGui.checkbox("random_initial_rotation", settings.randomInitialRotation())) {
            emitter.toggleRandomRotation();
        }

        float[] editRotation = new float[]{settings.initialRotation().x(), settings.initialRotation().y(), settings.initialRotation().z()};
        if (ImGui.dragFloat3("initial_rotation", editRotation, 0.025F)) {
            emitter.setParticleRotation(editRotation[0], editRotation[1], editRotation[2]);
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

        if (settings.randomSize()) {
            float[] editParticleSize = new float[]{settings.particleSize(), settings.particleSize() + settings.particleSizeVariation()};

            if (ImGui.dragFloat2("particle_size", editParticleSize, 0.01F, Math.max(editParticleSize[0], 0.001f), editParticleSize[1])) {
                emitter.setParticleSize(editParticleSize[0], editParticleSize[1]);
            }
        } else {
            float[] editParticleSize = new float[]{settings.particleSize()};
            if (ImGui.dragScalar("particle_size", editParticleSize, 0.01F)) {
                emitter.setParticleSize(editParticleSize[0], 0);
            }
        }

        if (ImGui.checkbox("random_lifetime", settings.randomLifetime())) {
            emitter.toggleParticleHasRandomLifetime();
        }

        if (settings.randomLifetime()) {
            int[] editParticleLifetime = new int[]{settings.particleLifetime(), settings.particleLifetime() + (int)settings.particleLifetimeVariation()};

            if (ImGui.dragInt2("particle_lifetime", editParticleLifetime, 0.03F, Math.max(editParticleLifetime[0], 0))) {
                emitter.setParticleLifetime(editParticleLifetime[0], editParticleLifetime[1]);
            }
        } else {
            int[] editLifetimeMin = new int[]{settings.particleLifetime()};
            if (ImGui.dragInt("particle_lifetime", editLifetimeMin, 0.03F, 0)) {
                emitter.setParticleLifetime(editLifetimeMin[0], 0);
            }
        }
    }

    private void renderParticleData(MutableParticleEmitter emitter) {
        QuasarParticleData data = emitter.getParticleData();

        // Render Style
        ImInt selectedRenderStyle = new ImInt();
        for (Map.Entry<ResourceKey<RenderStyle>, RenderStyle> e : RenderStyleRegistry.REGISTRY.entrySet()) {
            if (e.getValue().getClass() == data.renderStyle().getClass()) {
                selectedRenderStyle.set(renderStyleKeys.indexOf(e.getKey().location().toString()));
            }
        }

        if (ImGui.combo("render_style", selectedRenderStyle, renderStyleArray)) {
            data.setRenderStyle(RenderStyleRegistry.REGISTRY.get(ResourceLocation.parse(renderStyleArray[selectedRenderStyle.get()])));
        }

        // Sprite data (if the render style is billboard)
        SpriteData spriteData = data.spriteData();

        if (spriteData == null) {
            if (ImGui.button("Create Sprite Data")) {
                data.setSpriteData(new SpriteData(ResourceLocation.fromNamespaceAndPath("", ""), 1, 1, 1, 1, false));
            }
        } else {
            if (ImGui.button("Delete Sprite Data")) {
                data.setSpriteData(null);
            }
            ImGuiTextureProvider texture = ImGuiMC.getTexture(Minecraft.getInstance().getTextureManager().getTexture(spriteData.sprite()));
            ImString value = new ImString(spriteData.sprite().toString(), 256);
            ImGuiMC.image(texture, 64, 64);
            ImGui.sameLine();
            if (ImGui.inputText("sprite", value)) {
                try {
                    data.setSpriteData(new SpriteData(ResourceLocation.parse(value.get()), spriteData.frameCount(), spriteData.frameTime(), spriteData.frameWidth(), spriteData.frameHeight(), spriteData.stretchToLifetime()));
                } catch (ResourceLocationException ignored) {} // in the event of a non a-z0-9/._- character
            }
            int[] editFrameCount = new int[]{spriteData.frameCount()};
            if (ImGui.dragScalar("frame_count", editFrameCount, 0.03F)) {
                data.setSpriteData(new SpriteData(spriteData.sprite(), editFrameCount[0], spriteData.frameTime(), spriteData.frameWidth(), spriteData.frameHeight(), spriteData.stretchToLifetime()));
            }

            int[] editFrameSize = new int[]{spriteData.frameWidth(), spriteData.frameHeight()};
            if (ImGui.dragInt2("size", editFrameSize, 0.03F, 0)) {
                data.setSpriteData(new SpriteData(spriteData.sprite(), spriteData.frameCount(), spriteData.frameTime(), editFrameSize[0], editFrameSize[1], spriteData.stretchToLifetime()));
            }

            float[] editFrameTime = new float[]{spriteData.frameTime()};
            if (ImGui.dragScalar("frame_time", editFrameTime)) {
                data.setSpriteData(new SpriteData(spriteData.sprite(), spriteData.frameCount(), editFrameTime[0], spriteData.frameWidth(), spriteData.frameHeight(), spriteData.stretchToLifetime()));
            }

            if (ImGui.checkbox("stretch_to_lifetime", spriteData.stretchToLifetime())) {
                data.setSpriteData(new SpriteData(spriteData.sprite(), spriteData.frameCount(), spriteData.frameTime(), spriteData.frameWidth(), spriteData.frameHeight(), !spriteData.stretchToLifetime()));
            }
        }


        // Additive
        if (ImGui.checkbox("additive", data.additive())) {
            data.setAdditive(!data.additive());
        }

        // Should collide
        if (ImGui.checkbox("collision", data.shouldCollide())) {
            data.setShouldCollide(!data.shouldCollide());
        }

        // Face velocity
        if (ImGui.checkbox("face_velocity", data.faceVelocity())) {
            data.setFaceVelocity(!data.faceVelocity());
        }

        // Velocity stretch factor
        float[] editStretchFactor = new float[] {data.velocityStretchFactor()};

        if (ImGui.dragScalar("velocity_stretch_factor", editStretchFactor, 0.005f)) {
            data.setVelocityStretchFactor(editStretchFactor[0]);
        }
    }

    private void renderModules(MutableParticleEmitter emitter) {
        ImGui.indent();
        int id = 333;
        for (ParticleModuleData module : List.copyOf(emitter.getModules())) {
            ImGui.pushID(id);
            if (ImGui.collapsingHeader(ParticleModuleTypeRegistry.REGISTRY.getKey(module.getType()).toString())) {
                ImGui.indent();
                if (module instanceof EditorAttributeProvider attributeProvider) {
                    attributeProvider.renderImGuiAttributes();
                }
                if (ImGui.button("Remove Module")) {
                    emitter.getModules().remove(module);
                    ImGui.unindent();
                    ImGui.popID();
                    continue;
                }
                ImGui.unindent();
            }
            ImGui.popID();
            id++;
        }

        ImGui.separator();

        ImGui.combo("module", selectedModule, modulesArray);
        if (ImGui.button("Add Module")) {
            ParticleModuleData newModule = ParticleModuleTypeRegistry.REGISTRY.get(ResourceLocation.parse(modulesArray[selectedModule.get()])).defaultValue().get();
            emitter.getModules().add(newModule);
        }

        ImGui.unindent();
    }

    private void saveEmitterToFile(MutableParticleEmitter emitter, String name) {
        JsonElement result = ParticleEmitterData.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, emitter.dataFromMutable()).getOrThrow();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonString = gson.toJson(result);

        try {
            String folderPath = Path.of(Minecraft.getInstance().gameDirectory.toURI()).resolve("quasar/emitters").toString();
            String filePath = folderPath + File.separator + name + ".json";
            new File(folderPath).mkdirs();
            File emitterFile = new File(filePath);
            FileWriter emitterWriter = new FileWriter(emitterFile);
            emitterWriter.write(prettyJsonString);
            emitterWriter.close();
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Saved emitter to " + name + ".json").withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, emitterFile.getAbsolutePath()))));
        } catch (IOException e) {
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Failed to save emitter! Check logs for details.").withStyle(ChatFormatting.RED));
            e.printStackTrace();
        }
    }

    private void createEmitterFromData(ParticleEmitterData data) {
        if (data == null) return;

        ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
        MutableParticleEmitter emitter = new MutableParticleEmitter(particleManager, particleManager.getLevel(), data);
        HitResult hitResult = Minecraft.getInstance().hitResult;

        Vec3 position;
        if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            position = hitResult.getLocation();
        } else {
            position = new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector()).multiply(3, 3, 3).add(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        }

        emitter.setPosition(position);

        particleManager.addParticleSystem(emitter);

        this.emitters.add(emitter);
        this.selectedEmitter = this.emitters.size() - 1;
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
        public boolean renderEmitterShape = false;
        public boolean renderDirection;

        private MutableParticleEmitter(ParticleSystemManager particleManager, ClientLevel level, ParticleEmitterData data) {
            super(particleManager, level, data);
            this.particleData = new QuasarParticleData(this.particleData.shouldCollide(), this.particleData.faceVelocity(), this.particleData.velocityStretchFactor(), this.particleData.modules(), this.particleData.spriteData(), this.particleData.additive(), this.particleData.renderStyle());
        }

        public ParticleEmitterData dataFromMutable() {
            QuasarParticleData withModules = new QuasarParticleData(
                    this.particleData.shouldCollide(), this.particleData.faceVelocity(), this.particleData.velocityStretchFactor(), this.getModules().stream().map(Holder::direct).toList(), this.particleData.spriteData(), this.particleData.additive(), this.particleData.renderStyle()
            );

            EmitterSettings emitterSettings = new EmitterSettings(getEmitterShapeSettings().stream().map(Holder::direct).toList(), Holder.direct(this.getParticleSettings()), this.isForceSpawn());

            return new ParticleEmitterData(this.getMaxLifetime(), this.isLoop(), this.getRate(), this.getCount(), this.getMaxParticles(), emitterSettings, Holder.direct(withModules));
        }

        @Override
        public void render(MatrixStack matrixStack, MultiBufferSource bufferSource, Camera camera, float partialTicks) {
            if (renderEmitterShape) {
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
            if (renderDirection) {
                VertexConsumer debugBuilder = bufferSource.getBuffer(RenderType.lineStrip());
                matrixStack.matrixPush();
                matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
                matrixStack.matrixPush();
                matrixStack.translate(getPosition());

                Matrix4f matrix4f = matrixStack.position();

                debugBuilder.addVertex(matrix4f, 0, 0, 0).setColor(1, 1f, 1f, 1).setNormal(0, 1, 0);
                Vector3f direction = getParticleSettings().initialDirection().normalize(new Vector3f()).mul(getParticleSettings().particleSpeed() * 10);
                debugBuilder.addVertex(matrix4f, direction.x, direction.y, direction.z).setColor(1f, 0.15f, 0.15f, 1f).setNormal(0, 1, 0);
                matrixStack.matrixPop();
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
            max = Math.max(min, max);
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setParticleSize(min)
                    .setParticleSizeVariation(max - min)
                    .build());
        }

        public void setParticleLifetime(int min, int max) {
            max = Math.max(min, max);
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

        public void setParticleRotation(float x, float y, float z) {
            this.setParticleSettings(new ParticleSettingsBuilder(getParticleSettings())
                    .setInitialRotation(new Vector3f(x, y, z))
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
            private Vector3fc initialRotation;
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
                this.initialRotation = from.initialRotation();
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

            public ParticleSettingsBuilder setInitialRotation(Vector3fc initialRotation) {
                this.initialRotation = initialRotation;
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
                return new ParticleSettings(particleSpeed, particleSize, particleSizeVariation, particleLifetime, particleLifetimeVariation, initialDirection, randomInitialDirection, initialRotation, randomInitialRotation, randomSpeed, randomSize, randomLifetime);
            }
        }
    }
}
