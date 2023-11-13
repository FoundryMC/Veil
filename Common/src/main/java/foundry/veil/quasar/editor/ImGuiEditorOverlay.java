package foundry.veil.quasar.editor;

import foundry.veil.quasar.client.particle.QuasarParticleData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.anchors.AnchorPoint;
import foundry.veil.quasar.emitters.modules.emitter.EmitterModule;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmissionParticleSettings;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmissionShapeSettings;
import foundry.veil.quasar.emitters.modules.emitter.settings.shapes.AbstractEmitterShape;
import foundry.veil.quasar.emitters.modules.particle.update.forces.*;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ImGuiEditorOverlay {
    private ImGuiEditor editor = new ImGuiEditor();
    public ParticleEmitter currentlySelectedEmitterInstance = null;
    private String currentlySelectedEmitter = "None";
    public Vec3 position = new Vec3(0, 0, 0);
    private static final Path EMITTER_OUTPUT_PATH = Path.of("D:\\MC-Projects\\dndmod\\run\\quasar\\output\\emitters");
    public boolean localGizmos = false;
    public Entity currentlySelectedEntity = null;
    public ModelPart currentlySelectedEntityModelPart = null;
    public String currentlySelectedEntityModelPartName = "root";
    public ModelPart root = null;
    public List<String> currentlySelectedEntityModelParts = null;
    public Map<String, ModelPart> modelParts = null;
    public boolean renderGizmos = true;

    public ImGuiEditorOverlay() {
        ImGui.createContext();
        editor.init();
        MinecraftForge.EVENT_BUS.register(editor);
    }

    public void save() throws IOException {
        // save the particle emitter and all its components to jsons
        if(currentlySelectedEmitterInstance == null) return;
        DataResult<JsonElement> result = ParticleEmitter.CODEC.encodeStart(JsonOps.INSTANCE, currentlySelectedEmitterInstance);
        if(result.error().isPresent()) {
            System.out.println(result.error().get());
            return;
        }
        JsonElement element = result.result().get();
        Files.createDirectories(EMITTER_OUTPUT_PATH);
        Path path = EMITTER_OUTPUT_PATH.resolve(currentlySelectedEmitterInstance.registryName.getPath() + ".json");
        Files.write(path, element.toString().getBytes());
    }

    public void renderEditor() {
        ImGui.render();
        editor.renderDrawData(ImGui.getDrawData());
        editor.newFrame();

        ImGui.begin("Editor");
        ImGui.text("Emitter:");
        renderEmitterDropdown();
        if (!Objects.equals(currentlySelectedEmitter, "None")) {
            if (currentlySelectedEmitterInstance != null) {
                renderEmitterSettings();
            }
            renderEmitterSimulationSettings();
            renderForceSettings();
            renderRenderSettings();
        }
        if(ImGui.button("Save")){
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (ImGui.button("Reset")) {
            if (Minecraft.getInstance().level != null) {
                if (currentlySelectedEmitterInstance != null) {
                    ParticleSystemManager.getInstance().removeDelayedParticleSystem(currentlySelectedEmitterInstance);
                }
                ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(new ResourceLocation(currentlySelectedEmitter)).instance();
                emitter.setPosition(position);
                emitter.setLevel(Minecraft.getInstance().level);
                currentlySelectedEmitterInstance = emitter;
            }
        }
        if (ImGui.button("Reset Editor")) {
            if (Minecraft.getInstance().level != null) {
                if (currentlySelectedEmitterInstance != null) {
                    ParticleSystemManager.getInstance().removeDelayedParticleSystem(currentlySelectedEmitterInstance);
                }
                ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(new ResourceLocation(currentlySelectedEmitter)).instance();
                emitter.setPosition(position);
                emitter.setLevel(Minecraft.getInstance().level);
                currentlySelectedEmitterInstance = emitter;
            }
            currentlySelectedEmitter = "None";
        }
        ImGui.end();

        ImGui.begin("Anchor");
        Vector3f localOffset = AnchorPoint.TEST_POINT.localOffset;
        float[] anchorPos = new float[]{(float) localOffset.x(), (float) localOffset.y(), (float) localOffset.z()};
        ImGui.dragFloat3("##AnchorPos", anchorPos, 0.01f);
        AnchorPoint.TEST_POINT.localOffset = new Vector3f(anchorPos[0], anchorPos[1], anchorPos[2]);
        renderModelParts();
        ImGui.end();
    }

    private void renderModelParts(){
        if(ImGui.beginCombo("##ModelParts", currentlySelectedEntityModelPartName)){
            ImGui.pushItemWidth(-1);
            if(currentlySelectedEntityModelParts != null){
                currentlySelectedEntityModelParts.forEach(modelPart -> {
                    boolean isSelected = currentlySelectedEntityModelPartName.equals(modelPart);
                    if(ImGui.selectable(modelPart, isSelected)){
                        currentlySelectedEntityModelPartName = modelPart;
                        currentlySelectedEntityModelPart = modelParts.get(modelPart);
                    }
                    if(isSelected){
                        ImGui.setItemDefaultFocus();
                    }
                });
            }
            ImGui.popItemWidth();
            ImGui.endCombo();
        }
        if(currentlySelectedEntity instanceof LivingEntity le){
            ImFloat animationSpeed = new ImFloat(le.walkAnimation.speed());
            ImGui.sliderScalar("Animation Speed", ImGuiDataType.Float, animationSpeed, -10, 10);
            le.walkAnimation.setSpeed(animationSpeed.get());
            Vec3 eWorldPos = AnchorPoint.TEST_POINT.getWorldOffset(currentlySelectedEntity);
            float[] worldPos = new float[]{(float) eWorldPos.x, (float) eWorldPos.y, (float) eWorldPos.z};
            ImGui.dragFloat3("AnchorWorldPos", worldPos, 0.01f);
            Vec3 actualWorldPos = currentlySelectedEntity.position();
            float[] actualWorldPosArr = new float[]{(float) actualWorldPos.x, (float) actualWorldPos.y, (float) actualWorldPos.z};
            ImGui.dragFloat3("ActualWorldPos", actualWorldPosArr, 0.01f);
        }
    }

    private void renderEmitterSettings() {
        ImGui.text("Emitter Module Settings:");
        EmitterModule module = currentlySelectedEmitterInstance.getEmitterModule();
        ImInt lifetime = new ImInt(module.getMaxLifetime());
        ImGui.inputInt("Max Lifetime", lifetime);
        module.setMaxLifetime(lifetime.get());
        ImInt count = new ImInt(module.getCount());
        ImGui.inputInt("Count", count);
        module.setCount(count.get());
        ImInt rate = new ImInt(module.getRate());
        ImGui.inputInt("Rate", rate);
        module.setRate(rate.get());
        ImBoolean loop = new ImBoolean(module.getLoop());
        ImGui.checkbox("Loop", loop);
        module.setLoop(loop.get());
        ImBoolean localGizmos = new ImBoolean(this.localGizmos);
        ImGui.checkbox("Local Gizmos", localGizmos);
        this.localGizmos = localGizmos.get();
        ImBoolean renderGizmos = new ImBoolean(this.renderGizmos);
        ImGui.checkbox("Render Gizmos", renderGizmos);
        this.renderGizmos = renderGizmos.get();
    }

    private void renderEmitterDropdown() {
        if (ImGui.beginCombo("##", currentlySelectedEmitter)) {
            ImGui.pushItemWidth(-1);
            for (int i = 0; i < ParticleEmitterRegistry.getEmitters().size(); i++) {
                boolean isSelected = currentlySelectedEmitter.equals(ParticleEmitterRegistry.getEmitters().get(i).toString());
                if (ImGui.selectable(ParticleEmitterRegistry.getEmitters().get(i).toString(), isSelected)) {
                    currentlySelectedEmitter = ParticleEmitterRegistry.getEmitters().get(i).toString();
                    if (currentlySelectedEmitterInstance != null) {
                        if (currentlySelectedEmitterInstance.registryName != null && !currentlySelectedEmitterInstance.registryName.toString().equals(currentlySelectedEmitter) && Minecraft.getInstance().level != null) {
                            ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(new ResourceLocation(currentlySelectedEmitter)).instance();
                            emitter.setPosition(position);
                            emitter.setLevel(Minecraft.getInstance().level);
                            currentlySelectedEmitterInstance = emitter;
                        }
                    } else if (Minecraft.getInstance().level != null) {
                        ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(new ResourceLocation(currentlySelectedEmitter)).instance();
                        emitter.setPosition(position);
                        emitter.setLevel(Minecraft.getInstance().level);
                        currentlySelectedEmitterInstance = emitter;
                    }
                }
                if (isSelected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.popItemWidth();
            ImGui.endCombo();
        }
    }

    private void renderForceSettings() {
        if (!Objects.equals(currentlySelectedEmitter, "None") && currentlySelectedEmitterInstance != null) {
            ImGui.begin("Particle Force Settings");
            currentlySelectedEmitterInstance.getParticleData().getForces().forEach(Module::renderImGuiSettings);
            if (ImGui.button("Add Force")) {
                ImGui.openPopup("Add Force Module");
            }
            if (ImGui.beginPopup("Add Force Module")) {
                ImGui.text("Add Force Module");
                ImGui.separator();
                if (ImGui.selectable("Point Attractor")) {
                    PointAttractorForce force = new PointAttractorForce(new Vec3(0, 0, 0), 1.0f, 1.0f, 1.0f, true, false);
                    currentlySelectedEmitterInstance.getParticleData().addForce(force);
                    ImGui.closeCurrentPopup();
                }
                if (ImGui.selectable("Point Force")) {
                    PointForce force = new PointForce(new Vec3(0, 0, 0), 1.0f, 1.0f, 1.0f);
                    currentlySelectedEmitterInstance.getParticleData().addForce(force);
                    ImGui.closeCurrentPopup();
                }
                if (ImGui.selectable("Vortex Force")) {
                    VortexForce force = new VortexForce(new Vec3(0, 0, 0), new Vec3(0, 0, 0), 1.0f, 1.0f, 1.0f);
                    currentlySelectedEmitterInstance.getParticleData().addForce(force);
                    ImGui.closeCurrentPopup();
                }
                if (ImGui.selectable("Gravity Force")) {
                    GravityForce force = new GravityForce(1.0f);
                    currentlySelectedEmitterInstance.getParticleData().addForce(force);
                    ImGui.closeCurrentPopup();
                }
                if(ImGui.selectable("Drag Force")){
                    DragForce force = new DragForce(1.0f);
                    currentlySelectedEmitterInstance.getParticleData().addForce(force);
                    ImGui.closeCurrentPopup();
                }
                ImGui.endPopup();
            }
            ImGui.end();
            currentlySelectedEmitterInstance.getParticleData().getForces().removeIf(AbstractParticleForce::shouldRemove);
        }
    }

    private void renderRenderSettings() {
        if (!Objects.equals(currentlySelectedEmitter, "None") && currentlySelectedEmitterInstance != null) {
            ImGui.begin("Particle Render Settings");
            EmissionParticleSettings settings = currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionParticleSettings();
            float[] scale = new float[]{settings.getBaseParticleSize()};
            ImGui.dragFloat("Scale", scale, 0.001f);
            settings.setParticleSize(scale[0]);
            QuasarParticleData data = currentlySelectedEmitterInstance.getParticleData();
            currentlySelectedEmitterInstance.getParticleData().getRenderModules().forEach(Module::renderImGuiSettings);
            ImGui.end();
        }
    }
    boolean setEntityPos = false;
    boolean setAnchorPos = false;
    private void renderEmitterSimulationSettings() {
        // TODO: XYZ, "Simulate" button
        if (!Objects.equals(currentlySelectedEmitter, "None")) {
            ImDouble x = new ImDouble(position.x);
            ImDouble y = new ImDouble(position.y);
            ImDouble z = new ImDouble(position.z);
            ImGui.begin("Emitter Simulation Settings");
            if(ImGui.beginCombo("##Shape", EmissionShapeSettings.SHAPES.inverse().get(currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getShape()))){
                for (int i = 0; i < EmissionShapeSettings.SHAPES.size(); i++) {
                    boolean isSelected = currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getShape().equals(EmissionShapeSettings.SHAPES.values().toArray()[i]);
                    if (ImGui.selectable((String) EmissionShapeSettings.SHAPES.keySet().toArray()[i], isSelected)) {
                        currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().setShape((AbstractEmitterShape) EmissionShapeSettings.SHAPES.values().toArray()[i]);
                    }
                    if (isSelected) {
                        ImGui.setItemDefaultFocus();
                    }
                }
                ImGui.endCombo();
            }
            ImBoolean faceVelocity = new ImBoolean(currentlySelectedEmitterInstance.getParticleData().getFaceVelocity());
            ImGui.checkbox("Face Velocity", faceVelocity);
            currentlySelectedEmitterInstance.getParticleData().setFaceVelocity(faceVelocity.get());
            ImBoolean fromEdge = new ImBoolean(currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().isFromSurface());
            ImGui.checkbox("From Edge", fromEdge);
            currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().setFromSurface(fromEdge.get());
            ImInt particleLifetime = new ImInt(currentlySelectedEmitterInstance.getParticleData().getParticleSettings().getBaseParticleLifetime());
            ImGui.inputInt("Particle Lifetime", particleLifetime);
            currentlySelectedEmitterInstance.getParticleData().getParticleSettings().setBaseParticleLifetime(particleLifetime.get());
            ImGui.text("Dimensions:");
            float[] dimensions = new float[]{(float) currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getDimensions().x, (float) currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getDimensions().y, (float) currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getDimensions().z};
            ImGui.dragFloat3("##Dimensions", dimensions);
            currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().setDimensions(new Vec3(dimensions[0], dimensions[1], dimensions[2]));
            ImGui.text("Position:");
            float[] pos = new float[]{(float) position.x, (float) position.y, (float) position.z};
            ImGui.dragFloat3("##Position", pos);
            if(!setAnchorPos && !setEntityPos){
                position = new Vec3(pos[0], pos[1], pos[2]);
            }
            ImGui.text("Rotation:");
            float[] rot = new float[]{(float) currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getRotation().x(), (float) currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getRotation().y(), (float) currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().getRotation().z()};
            ImGui.dragFloat3("##Rotation", rot);
            currentlySelectedEmitterInstance.getEmitterSettingsModule().getEmissionShapeSettings().setRotation(new Vec3(rot[0], rot[1], rot[2]));
            if (ImGui.button("Set pos from ray")) {
                HitResult ray = Minecraft.getInstance().hitResult;
                if (ray != null) {
                    position = ray.getLocation();
                }
            }
            if(currentlySelectedEntity != null){
                ImBoolean setEntityPos = new ImBoolean(this.setEntityPos);
                ImGui.checkbox("Set pos from entity", setEntityPos);
                this.setEntityPos = setEntityPos.get();
                if(this.setEntityPos){
                    position = currentlySelectedEntity.position();
                }
                ImBoolean setAnchorPos = new ImBoolean(this.setAnchorPos);
                ImGui.checkbox("Set pos from anchor", setAnchorPos);
                this.setAnchorPos = setAnchorPos.get();
                if(this.setAnchorPos){
                    position = AnchorPoint.TEST_POINT.getWorldOffset(currentlySelectedEntity);
                }
            }
            if (currentlySelectedEmitterInstance != null) {
                currentlySelectedEmitterInstance.setPosition(position);
            }
            if (ImGui.button("Simulate")) {
                if (Minecraft.getInstance().level != null) {
                    currentlySelectedEmitterInstance.getEmitterModule().reset();
                    currentlySelectedEmitterInstance.isComplete = false;
                    ParticleSystemManager.getInstance().addDelayedParticleSystem(currentlySelectedEmitterInstance);
                }
            }
            if (ImGui.button("Stop")) {
                ParticleSystemManager.getInstance().removeDelayedParticleSystem(currentlySelectedEmitterInstance);
            }
            ImGui.end();
        }
    }
}
